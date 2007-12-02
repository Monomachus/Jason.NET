package jason.infra.jade;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jason.architecture.AgArch;
import jason.architecture.AgArchInfraTier;
import jason.asSemantics.ActionExec;
import jason.asSemantics.TransitionSystem;
import jason.asSyntax.ListTermImpl;
import jason.asSyntax.Literal;
import jason.asSyntax.StringTermImpl;
import jason.asSyntax.Term;
import jason.infra.centralised.RunCentralisedMAS;
import jason.mas2j.AgentParameters;
import jason.mas2j.ClassParameters;
import jason.runtime.RuntimeServicesInfraTier;
import jason.runtime.Settings;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Document;


/**
 * Implementation of the Jade Architecture to run Jason agents
 * 
 * @author Jomi
 */
public class JadeAgArch extends JadeAg implements AgArchInfraTier {

    /** name of the "jason agent" service in DF */
    public  static String dfName = "j_agent";
    
	private static final long serialVersionUID = 1L;
   
    /** the user customisation of the architecture */
    protected AgArch userAgArch;

    // map of pending actions
    private Map<String,ActionExec> myPA = new HashMap<String,ActionExec>();

    private boolean enterInSleepMode = false;

    AID controllerAID  = new AID(RunJadeMAS.controllerName, AID.ISLOCALNAME);
    AID environmentAID = null;

    Behaviour tsBehaviour;
    
    // 
	// Jade Methods
	// ------------
	//

    @SuppressWarnings("serial")
    @Override
    protected void setup() {
        RunCentralisedMAS.setupLogger();
        logger = Logger.getLogger(JadeAgArch.class.getName() + "." + getAgName());
        logger.info("starting "+getLocalName());
        try {
    
            // default values for args
            String asSource        = null;
            String archClassName   = null;
            String agClassName     = null;
            ClassParameters bbPars = null;
            Settings stts          = null;

            Object[] args = getArguments();
            if (args == null) {
                logger.info("No AgentSpeak source informed!");
                return;
            }
            if (args[0] instanceof AgentParameters) {
                AgentParameters ap = (AgentParameters)args[0];
                asSource      = ap.asSource.getAbsolutePath();
                archClassName = ap.archClass.className;
                agClassName   = ap.agClass.className;
                bbPars        = ap.bbClass;
                stts          = ap.getAsSetts((Boolean)args[1], (Boolean)args[2]); // TODO: get this parameters

            } else {
                // read arguments
                // [0] is the file with AS source for the agent
                // arch <arch class>
                // ag <agent class>
                // bb < belief base class >
                // option < options >

                asSource = args[0].toString();
        
                // default values for args
                archClassName  = AgArch.class.getName();
                agClassName    = jason.asSemantics.Agent.class.getName();
                bbPars         = new ClassParameters(jason.bb.DefaultBeliefBase.class.getName());
                stts           = new Settings();
        
        
                int i=1;
                while (i < args.length) {
                    
                    if (args[i].toString().equals("arch")) {
                        i++;
                        archClassName = args[i].toString();
                    } else if (args[i].toString().equals("ag")) {
                        i++;
                        agClassName = args[i].toString();
                    }

                    // TODO: read custom BB and settings from arguments 

                    i++;
                }       
            }

            userAgArch = (AgArch) Class.forName(archClassName).newInstance();
            userAgArch.setArchInfraTier(this);
            userAgArch.initAg(agClassName, bbPars, asSource, stts);
            logger.setLevel(userAgArch.getTS().getSettings().logLevel());
    
            // DF register
            DFAgentDescription dfa = new DFAgentDescription();
            dfa.setName(getAID());
            ServiceDescription vc = new ServiceDescription();
            vc.setType("jason");
            vc.setName(dfName);
            dfa.addServices(vc);
            try {
                DFService.register(this,dfa);
            } catch (FIPAException e) {
                logger.log(Level.SEVERE, "Error registering agent in DF", e);
            }
            
            tsBehaviour = new CyclicBehaviour() {
                TransitionSystem ts = userAgArch.getTS();
                public void action() {
                    if (ts.getSettings().isSync()) {
                        if (processExecutionControlOntologyMsg()) {
                            // execute a cycle in sync mode
                            ts.reasoningCycle();
                            boolean isBreakPoint = false;
                            try {
                                isBreakPoint = ts.getC().getSelectedOption().getPlan().hasBreakpoint();
                                if (logger.isLoggable(Level.FINE)) logger.fine("Informing controller that I finished a reasoning cycle "+userAgArch.getCycleNumber()+". Breakpoint is " + isBreakPoint);
                            } catch (NullPointerException e) {
                                // no problem, there is no sel opt, no plan ....
                            }
                            informCycleFinished(isBreakPoint, userAgArch.getCycleNumber());

                        } else {
                            block(1000);
                        }
                    } else {
                        if (enterInSleepMode) {
                            block(1000);
                            enterInSleepMode = false;
                        } else {
                            ts.reasoningCycle();
                        }
                    }
                }
            };
            addBehaviour(tsBehaviour);

            logger.fine("Created from source "+asSource);
        } catch (Exception e) {
            logger.log(Level.SEVERE,"Error creating JADE architecture.",e);
        }
    }

    @Override
    public void doDelete() {
        try {
            running = false;
            if (userAgArch != null) {
                userAgArch.stopAg();
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE,"Error in doDelete.",e);
        } finally {
            super.doDelete();
        }
    }
    
	@Override
	protected void takeDown() {
        logger.info("Finished!");
	}
	
	
	// Jason Methods
	// -------------
	//	

	public void stopAg() {
		doDelete();
	}
    
    public void sleep() {
        enterInSleepMode = true;
        //tsBehaviour.block(1000);
    }
    
    public void wake() {
        tsBehaviour.restart();
    }

	public String getAgName() {
		return getLocalName();
	}

	public boolean canSleep() {
		return getCurQueueSize() == 0 && isRunning();
	}

	public void checkMail() {
        ACLMessage m = null;
        do {
            try {
                m = receive();
                if (m != null) {
                    if (logger.isLoggable(Level.FINE)) logger.fine("Received message: " + m);
                    
                    if (isActionFeedback(m)) {
                        // ignore this message
                        continue;
                    }
                    
                    String ilForce   = aclToKqml(m.getPerformative());
                    String sender    = m.getSender().getLocalName();
                    String replyWith = m.getReplyWith();
                    if (replyWith == null || replyWith.length() == 0) replyWith = "noid";
                    String irt       = m.getInReplyTo();
                
                    Object propCont = null;
    				try {
    					propCont = m.getContentObject();
    				} catch (UnreadableException e) {
                        if (m.getLanguage() == null || !m.getLanguage().equals("AgentSpeak")) {
                            // not AS messages are treated as string 
                            propCont = new StringTermImpl(m.getContent());
                            
                            // also remembers conversation ID
                            if (!replyWith.equals("noid") && m.getConversationId() != null)
                                conversationIds.put(replyWith, m.getConversationId());
                        } else {
                            propCont = m.getContent();
                        }
    				}
                    if (propCont != null) {
                        jason.asSemantics.Message im = new jason.asSemantics.Message(ilForce, sender, getLocalName(), propCont, replyWith);
                        if (irt != null) {
                            im.setInReplyTo(irt);
                        }
                        userAgArch.getTS().getC().getMailBox().add(im);
                    }
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error receiving message.", e);
            }
        } while (m != null);
	}

    boolean isActionFeedback(ACLMessage m) {
        // check if there are feedbacks on requested action executions
        if (m.getOntology() != null && m.getOntology().equals(JadeEnvironment.actionOntology)) {
            String irt = m.getInReplyTo();
            if (irt != null) {
                ActionExec a = myPA.remove(irt);
                // was it a pending action?
                if (a != null) {
                    if (m.getContent().equals("ok")) {
                        a.setResult(true);
                    } else {
                        a.setResult(false);
                    }
                    userAgArch.getTS().getC().addFeedbackAction(a);
                } else {
                    logger.log(Level.SEVERE, "Error: received feedback for an Action that is not pending. The message is "+m);
                }
            }
            return true;
        }
        return false;
    }
    
    private MessageTemplate ts = MessageTemplate.and(
            MessageTemplate.MatchContent("agState"),
            MessageTemplate.MatchOntology(JadeExecutionControl.controllerOntology));
    private MessageTemplate tc = MessageTemplate.and(
            MessageTemplate.MatchContent("performCycle"),
            MessageTemplate.MatchOntology(JadeExecutionControl.controllerOntology));

    boolean processExecutionControlOntologyMsg() {
        ACLMessage m = receive(ts);
        if (m != null) {
            // send the agent state
            ACLMessage r = m.createReply();
            r.setPerformative(ACLMessage.INFORM);
            try {
                Document agStateDoc = userAgArch.getTS().getAg().getAgState();
                r.setContentObject((Serializable)agStateDoc);
                send(r);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error sending message " + r, e);
            }            
        }
        
        m = receive(tc);
        if (m != null) {
            int cycle = Integer.parseInt(m.getUserDefinedParameter("cycle"));
            logger.fine("new cycle: "+cycle);
            userAgArch.setCycleNumber(cycle);
            return true;
        }
        return false;
    }
    
    boolean isPerceptionOntology(ACLMessage m) {
        return m.getOntology() != null && m.getOntology().equals(JadeEnvironment.perceptionOntology);
    }

	@SuppressWarnings("unchecked")
    public List<Literal> perceive() {
        if (!isRunning()) return null;
        if (getEnvironmentAg() == null) return null;
        
        List percepts = null;
        try {
            ACLMessage askMsg = new ACLMessage(ACLMessage.QUERY_REF);
            askMsg.addReceiver(environmentAID);
            askMsg.setOntology(JadeEnvironment.perceptionOntology);
            askMsg.setContent("getPercepts");
            ACLMessage r = ask(askMsg);
            if (r != null && r.getContent().startsWith("[")) {
                percepts = ListTermImpl.parseList(r.getContent());
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error in perceive.", e);
        }
        
        return percepts;
	}

    private AID getEnvironmentAg() {
        // get the name of the environment
        if (environmentAID == null) {
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType("jason");
            sd.setName(RunJadeMAS.environmentName);
            template.addServices(sd);
            try {
                DFAgentDescription[] ans = DFService.search(this, template);
                if (ans.length > 0) {
                    environmentAID =  ans[0].getName();
                    return environmentAID;
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE,"Error getting environment from DF.",e);
            }
        }
        return environmentAID;
    }

	public void act(ActionExec action, List<ActionExec> feedback) {
        if (!isRunning()) return;
        if (getEnvironmentAg() == null) return;
        
        try {
            Term acTerm = action.getActionTerm();
            logger.info("doing: " + acTerm);

            rwid++;
            String rw  = "id"+rwid;
            ACLMessage m = new ACLMessage(ACLMessage.REQUEST);
            m.addReceiver(environmentAID);
            m.setOntology(JadeEnvironment.actionOntology);
            m.setContent(acTerm.toString());
            m.setReplyWith(rw);
            myPA.put(rw, action); 
            send(m);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error sending action " + action, e);
        }
	}

	public RuntimeServicesInfraTier getRuntimeServices() {
	    return new JadeRuntimeServices(getContainerController(), this);
	}

    /** 
     *  Informs the infrastructure tier controller that the agent 
     *  has finished its reasoning cycle (used in sync mode).
     *  
     *  <p><i>breakpoint</i> is true in case the agent selected one plan 
     *  with the "breakpoint" annotation.  
     */ 
	public void informCycleFinished(boolean breakpoint, int cycle) {
        try {
            ACLMessage m = new ACLMessage(ACLMessage.INFORM);
            m.addReceiver(controllerAID);
            m.setOntology(JadeExecutionControl.controllerOntology);
            m.setContent(breakpoint+","+cycle);
            send(m);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error sending cycle finished.", e);
        }
	}
}
