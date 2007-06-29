package jason.infra.jade;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
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
    //protected boolean inAsk = false;
    
    protected Object syncReceive = new Object();

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
    
    private String waintingRW = null;
    private ACLMessage answer = null;
    
    // send a message and wait answer
    protected ACLMessage ask(ACLMessage m) {
        try {
            rwid++;
            waintingRW = "id"+rwid;
            m.setReplyWith(waintingRW);
            answer = null;
            send(m);
            ACLMessage r = waitAns();
            if (r != null) 
                return r;
            else 
                logger.warning("ask timeout for "+m.getContent());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error waiting message.", e);            
        }
        return null;
    }
    
    synchronized ACLMessage waitAns() {
        try {
            if (answer == null) {
                wait(3000);
            }
            return answer;
        } catch (InterruptedException e) {
            return null;
        }        
    }
    
    synchronized boolean isAskAnswer(ACLMessage m) {
        if (m.getInReplyTo() != null && m.getInReplyTo().equals(waintingRW)) {
            answer = m;
            notifyAll();
            return true;
        } else {
            return false;
        }
    }

    public void addAllAgsAsReceivers(ACLMessage m) throws Exception {
        // get all agents' name
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("jason");
        sd.setName(JadeAgArch.dfName);
        template.addServices(sd);
        DFAgentDescription[] ans;
        synchronized (syncReceive) {
            ans = DFService.search(this, template);
        }
        for (int i=0; i<ans.length; i++) {
            if (!ans[i].getName().equals(getAID())) {
                m.addReceiver(ans[i].getName());
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
