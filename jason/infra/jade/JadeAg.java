package jason.infra.jade;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.AMSService;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jason.asSemantics.Message;
import jason.asSyntax.Term;

import java.io.IOException;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Implementation of a basic jade agent for jason agents
 * 
 * @author Jomi
 */
public abstract class JadeAg extends Agent {

	// KQML performatives not available in FIPA-ACL
	public static final int UNTELL    = 1001;
	public static final int ASKALL    = 1002;
	public static final int UNACHIEVE = 1003;
	public static final int TELLHOW   = 1004;
	public static final int UNTELLHOW = 1005;
	public static final int ASKHOW    = 1006;
	
	private static final long serialVersionUID = 1L;

    private static Logger logger = Logger.getLogger(JadeAg.class.getName());

    protected static int rwid = 0; // reply-with counter

    protected boolean running = true;
    protected boolean inAsk = false;

    @Override
    public void doDelete() {
        running = false;
        super.doDelete();
    }
    

    public boolean isRunning() {
        return running;
    }
    
    public void sendMsg(Message m) throws Exception {
        ACLMessage acl = jasonToACL(m);
        acl.addReceiver(new AID(m.getReceiver(), AID.ISLOCALNAME));
        send(acl);
    }

	public void broadcast(Message m) throws Exception {
		ACLMessage acl = jasonToACL(m);
        addAllAgsAsReceivers(acl);
        send(acl);
	}
    
    // send a message and wait answer
    protected ACLMessage ask(ACLMessage m) {
        rwid++;
        m.setReplyWith("id"+rwid);
        try {
            inAsk = true;
            send(m);
            MessageTemplate t = MessageTemplate.MatchInReplyTo(m.getReplyWith());
            ACLMessage r = blockingReceive(t, 2000);
            if (r != null) return r;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error waiting message.", e);            
        } finally {
            inAsk = false;
        }
        return null;
    }

    public void addAllAgsAsReceivers(ACLMessage m) throws Exception {
        // get all agents' name
        SearchConstraints c = new SearchConstraints();
        c.setMaxResults( new Long(-1) );
        AMSAgentDescription[] all = AMSService.search( this, new AMSAgentDescription(), c);
        for (AMSAgentDescription ad: all) {
            AID agentID = ad.getName();
            if (!agentID.equals(getAID()) && 
                    !agentID.getName().startsWith("ams@") && 
                    !agentID.getName().startsWith("df@") &&
                    !agentID.getName().startsWith(RunJadeMAS.environmentName) &&
                    !agentID.getName().startsWith(RunJadeMAS.controllerName)
               ) {
                m.addReceiver(agentID);                
            }
        }        
    }

	protected ACLMessage jasonToACL(Message m) throws IOException {
		ACLMessage acl = new ACLMessage(kqmlToACL(m.getIlForce()));
		// send content as string if it is a Term/String (it is better for interoperability)
		if (m.getPropCont() instanceof Term || m.getPropCont() instanceof String) {
			acl.setContent(m.getPropCont().toString());			
		} else {
		    acl.setContentObject((Serializable)m.getPropCont());
		}
		acl.setReplyWith(m.getMsgId());
		acl.setLanguage("AgentSpeak");
        if (m.getInReplyTo() != null) {
        	acl.setInReplyTo(m.getInReplyTo());
        }
        return acl;
	}
	
	private int kqmlToACL(String p) {
		if (p.equals("tell")) {
			return ACLMessage.INFORM;
		} else if (p.equals("askOne")) {
			return ACLMessage.QUERY_REF;
		} else if (p.equals("achieve")) {
			return ACLMessage.REQUEST;
		} else if (p.equals("untell")) {
			return UNTELL;
		} else if (p.equals("unachieve")) {
			return UNACHIEVE;
		} else if (p.equals("askAll")) {
			return ASKALL;
		} else if (p.equals("askHow")) {
			return ASKHOW;
		} else if (p.equals("tellHow")) {
			return TELLHOW;
		} else if (p.equals("untellHow")) {
			return UNTELLHOW;
		}
		
		return ACLMessage.UNKNOWN;			
	}
	
	protected String aclToKqml(int p) {
		switch(p) {
		case ACLMessage.INFORM:	return "tell"; 
		case ACLMessage.QUERY_REF: return "askOne";
		case ACLMessage.REQUEST: return "achieve";
		case UNTELL: return "untell";
		case UNACHIEVE: return "unachieve";
		case ASKALL: return "askAll";
		case ASKHOW: return "askHow";
		case TELLHOW: return "tellHow";
		case UNTELLHOW: return "untellHow";
		}
		return "unknown";		
	}
	
}
