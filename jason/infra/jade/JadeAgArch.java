package jason.infra.jade;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jason.architecture.AgArch;
import jason.architecture.AgArchInfraTier;
import jason.asSemantics.ActionExec;
import jason.asSyntax.Literal;
import jason.asSyntax.Term;
import jason.infra.centralised.RunCentralisedMAS;
import jason.mas2j.AgentParameters;
import jason.mas2j.ClassParameters;
import jason.runtime.RuntimeServicesInfraTier;
import jason.runtime.Settings;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Implementation of the Jade Architecture to run Jason agents
 * 
 * @author Jomi
 */
public class JadeAgArch extends JadeAg implements AgArchInfraTier {

	// KQML performatives not available in FIPA-ACL
	public static final int UNTELL    = 1001;
	public static final int ASKALL    = 1002;
	public static final int UNACHIEVE = 1003;
	public static final int TELLHOW   = 1004;
	public static final int UNTELLHOW = 1005;
	public static final int ASKHOW    = 1006;
	
	private static final long serialVersionUID = 1L;

	private Logger logger;
   
    /** the user customisation of the architecture */
    protected AgArch userAgArh;

    // map of pending actions
    private Map<String,ActionExec> myPA = new HashMap<String,ActionExec>();



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

                    // TODO: BB custom
                    //mas2j parser = new mas2j(new StringReader(args[2].replace('$','\"')));
                    //ClassParameters bbPars = parser.classDef();
                    
                    // TODO: get and register user directives
                    
                    // TODO: settings
                    //stts.setOptions("[" + args[5] + "]");
                    
                    i++;
                }       
            }

            userAgArh = (AgArch) Class.forName(archClassName).newInstance();
            userAgArh.setArchInfraTier(this);
            userAgArh.initAg(agClassName, bbPars, asSource, stts);
            logger.setLevel(userAgArh.getTS().getSettings().logLevel());
            logger.fine("Created from source "+asSource);
    
            // main reasoning cycle behaviour
            addBehaviour(new CyclicBehaviour() {
                public void action() {
                    if (isRunning()) {
                        userAgArh.getTS().reasoningCycle();
                    }
                }
            });
    
            // wakeup the agent when new messages arrives
            addBehaviour(new CyclicBehaviour() {
                public void action() {
                    ACLMessage m = receive();
                    if (m == null) {
                        block();
                    } else {
                        putBack(m);
                        userAgArh.getTS().newMessageHasArrived();
                    }
                }
            });
        } catch (Exception e) {
            logger.log(Level.SEVERE,"Error creating JADE architecture.",e);
        }
    }

	@Override
	protected void takeDown() {
        super.takeDown();
        new Thread() {
            public void run() {
                userAgArh.stopAg();
            }
        }.start();
        userAgArh.getTS().receiveSyncSignal();    // in case the agent is waiting sync
        userAgArh.getTS().newMessageHasArrived(); // in case the agent is waiting messages
        logger.fine("finished running.\n");
	}
	
	
	// Jason Methods
	// -------------
	//
	

	public void stopAg() {
		doDelete();
	}

	public String getAgName() {
		return getLocalName();
	}

	public boolean canSleep() {
		return getCurQueueSize() == 0;
	}

	public void checkMail() {
        ACLMessage m = null;
        do {
            try {
                m = receive();
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error receiving message.", e);
            }
            if (m != null) {
                if (logger.isLoggable(Level.FINE)) logger.fine("Received message: " + m);

                String ilForce   = aclToKqml(m.getPerformative());
                String sender    = m.getSender().getLocalName();
                String replyWith = m.getReplyWith();
                if (replyWith == null || replyWith.length() == 0) replyWith = "noid";
                String irt       = m.getInReplyTo();

                Object propCont = null;
				try {
					propCont = m.getContentObject();
				} catch (UnreadableException e) {
                    propCont = m.getContent();
					//logger.log(Level.SEVERE,"Error reading message content.",e);
				}
                if (propCont != null) {
                	/*
                    String sPropCont = propCont.toString();
                    if (sPropCont.startsWith("\"") && sPropCont.endsWith("\"")) { // deal with a term enclosed by "
                        sPropCont = sPropCont.substring(1, sPropCont.length() - 1);
                        if (DefaultTerm.parse(sPropCont) != null) {
                            // it was a term with "
                            propCont = sPropCont.trim();
                        }
                    }
                    */

                    jason.asSemantics.Message im = new jason.asSemantics.Message(ilForce, sender, getLocalName(), propCont, replyWith);
                    if (irt != null) {
                        im.setInReplyTo(irt);
                    }
                    userAgArh.getTS().getC().getMailBox().add(im);
                }
            }
        } while (m != null);
	}

    AID environment    = new AID("environment", AID.ISLOCALNAME);
    MessageTemplate at = MessageTemplate.MatchOntology("AS-actions");
    
	@SuppressWarnings("unchecked")
    public List<Literal> perceive() {
        if (!isRunning()) return null;

        List percepts = null;

        ACLMessage askMsg = new ACLMessage(ACLMessage.QUERY_REF);
        askMsg.addReceiver(environment);
        askMsg.setContent("getPercepts");
        try {
            ACLMessage r = ask(askMsg);
            if (r != null && r.getContentObject() instanceof List) {
                percepts = (List)r.getContentObject();
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error in perceive.", e);            
        }

        // check if there are feedbacks on requested action executions
        try {
            ACLMessage m;
            do {
                m = receive(at);
                if (m != null) {
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
                            userAgArh.getTS().getC().getFeedbackActions().add(a);
                        } else {
                            logger.log(Level.SEVERE, "Error: received feedback for an Action that is not pending.");
                        }
                    }
                }
            } while (m != null);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error receiving message.", e);
        }
        
        return percepts;
	}

	public void act(ActionExec action, List<ActionExec> feedback) {
        if (!isRunning()) return;

        try {
            Term acTerm = action.getActionTerm();
            logger.info("doing: " + acTerm);

            rwid++;
            String rw  = "id"+rwid;
            ACLMessage m = new ACLMessage(ACLMessage.REQUEST);
            m.addReceiver(environment);
            m.setOntology("AS-actions");
            m.setContentObject(acTerm);
            m.setReplyWith(rw);
            send(m);
            myPA.put(rw, action); 
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error sending action " + action, e);
        }
	}

	public RuntimeServicesInfraTier getRuntimeServices() {
		return new JadeRuntimeServices();
	}

	public void informCycleFinished(boolean arg0, int arg1) {
		// TODO
	}
}
