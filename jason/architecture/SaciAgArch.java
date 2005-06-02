//----------------------------------------------------------------------------
// Copyright (C) 2003  Rafael H. Bordini, Jomi F. Hubner, et al.
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
// To contact the authors:
// http://www.csc.liv.ac.uk/~bordini
// http://www.inf.furb.br/~jomi
//----------------------------------------------------------------------------


package jason.architecture;


import jason.D;
import jason.JasonException;
import jason.asSemantics.ActionExec;
import jason.asSemantics.Agent;
import jason.asSemantics.TransitionSystem;
import jason.asSyntax.ListTerm;
import jason.asSyntax.Literal;
import jason.asSyntax.Term;

import java.util.Iterator;
import java.util.List;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;

import org.w3c.dom.Document;

import saci.Config;
import saci.MBoxChangedListener;
import saci.MBoxSAg;
import saci.Message;
import saci.MessageHandler;

/**
 * SACI Infra Structure for a Mulit-Agent Society of AgentSpeak Agents
 */

public class SaciAgArch extends saci.Agent implements AgentArchitecture {
    
    // needed in perceive() and brf(), must be called "percepts"
    // as the user may want to alter it for agent-specific perception
    protected List percepts;
    //protected List negPercepts;
    
    // to get the percepts via SACI (the normal mbox is used for inter-agent com.)
    private MBoxSAg mboxPercept = null;

	private TransitionSystem fTS = null;

    
    // this is used by SACI to initialize the agent
    public void initAg(String[] args) throws JasonException {
        
        // set the agent class
        try {
            // create the jasonId console
            if (! jIDE.MASConsole.hasConsole()) {
                jIDE.MASConsole.get("Saci - "+getSociety(), null).setAsDefaultOut();
            }

            String className = null;
            if (args.length < 1) { // error
            	running = false;
                throw new JasonException("The Agent class name were not informed for the SaciAgArch creation!");
            } else {
                className = args[0].trim();
            }
            Agent ag = (Agent)Class.forName(className).newInstance();
            fTS = ag.initAg(args, this);
        } catch (Exception e) {
            throw new JasonException("as2j: error creating the agent class! - "+e.getMessage());
        }
        
        // enter in the Environment society
        try {
            Config c = new Config();
            c.set("society.name", getMBox().getSociety()+"-env");
            mboxPercept = new MBoxSAg( getMBox().getName(), c);
            mboxPercept.init();
            mboxPercept.setMboxChangedListener(new MBoxChangedListener() {
                public void mboxChanged() {
                    if (fTS != null) {
                        fTS.newMessageHasArrived();
                    }
                }
            });
            
            mboxPercept.addMessageHandler("performCycle", "tell", null, "AS-ExecControl", new MessageHandler() {
                public boolean processMessage(saci.Message m) {
                    fTS.receiveSyncSignal();
                    return true; // no other message handler gives this message
                }
            });

            final Transformer stateSerializer = TransformerFactory.newInstance().newTransformer();
            mboxPercept.addMessageHandler("agState", "ask", null, "AS-ExecControl", new MessageHandler() {
                    public boolean processMessage(saci.Message m) {
                        saci.Message r = new saci.Message("(tell)");
                        r.put("receiver", m.get("sender"));
                        r.put("in-reply-to", m.get("reply-with"));
                        r.put("ontology", m.get("ontology"));
                        
                        try {
                            Document agStateDoc = fTS.getAg().getAgState();
                            
                            // serialize
                            //StringWriter so = new StringWriter();
                            //stateSerializer.setOutputProperty(OutputKeys.ENCODING,"ISO-8859-1");
                            //stateSerializer.transform(new DOMSource(agStateDoc), new StreamResult(so));
                            //r.put("content", so.toString());
                            r.putWithoutSerialization("content", agStateDoc);

                            mboxPercept.sendMsg(r);
                            //System.out.println("sending "+r.get("content").getClass().getName());
                        } catch (Exception e) {
                        	System.err.println("Error sending message "+r);
                        	e.printStackTrace();
                        }
                        return true; // no other message handler gives this message
                    }
                });
                
            getMBox().setMboxChangedListener(new MBoxChangedListener() {
                public void mboxChanged() {
                    if (fTS != null) {
                        fTS.newMessageHasArrived();
                    }
                }
            });

            
        } catch (Exception e) {
            System.err.println("Error entering the environment's society.");
            e.printStackTrace();
        }
    }
    
    public void stopAg() {
        super.stopAg();
        mboxPercept.disconnect();
    }
    
    public void run() {
        while (running) {
            fTS.reasoningCycle();
        }
        if (fTS.getSettings().verbose()>=1) {
            System.out.println("Agent "+fTS.getAgArch().getName()+" finished running.\n");
        }
    }
    
    // Default functions for the overall agent architecture (based on SACI)
    // they facilitate things a lot in case the programmer doesn't need
    // anything special
    
    // Default perception assumes Complete and Accurate sensing.
    // In the case of the SACI Architecture, the results of requests
    // for action execution is also recieved here.
    public void perceive() {
        if (! running) {
            return;
        }
        
        saci.Message askMsg = new saci.Message("(ask-all :receiver environment :ontology AS-Perception :content getPercepts)");
        
        // asks current environment state (positive percepts)
        saci.Message m = null;
        try {
            m = mboxPercept.ask(askMsg);
        } catch (Exception e) {
            System.err.println("Error receiving perceptions.");
            e.printStackTrace();
        }
        if (m != null) {
			String content = (String) m.get("content");
			if (content != null) {
				percepts = ListTerm.parseList(content).getAsList();
				if (fTS.getSettings().verbose()>=5) {
					System.out.println("Agent " + mboxPercept.getName() + " received percepts: "+percepts);
				}
			} else {
				percepts = null; // used to indicate that are nothing new in the environment, no BRF needed
			}
        }
        
		/*
        askMsg = new saci.Message("(ask-all :receiver environment :ontology AS-Perception :content getNegativePercepts)");
        
        // asks current environment state (negative percepts)
        m = null;
        try {
            m = mboxPercept.ask(askMsg);
        } catch (Exception e) {
            System.err.println("Error receiving perceptions.");
            e.printStackTrace();
        }
        if (m != null) {
            negPercepts = new ParseList((String) m.get("content")).getAsList();
            if (fTS.getSettings().verbose()>=5)
                System.out.println("Agent " + mboxPercept.getName() + " received negative percepts: "+negPercepts);
        }
        */
		
        // check if there are feedbacks on requested action executions
        try {
            do {
                m = mboxPercept.receive();
                if (m != null) {
                    if (m.get("ontology") != null) {
                        if ( ((String)m.get("ontology")).equals("AS-Action")) {
                            String irt = (String)m.get("in-reply-to");
                            if (irt != null) {
                                ActionExec a = (ActionExec)fTS.getC().getPendingActions().remove(irt);
                                // was it a pending action?
                                if (a != null) {
                                    if (((String)m.get("content")).equals("ok")) {
                                        a.setResult(true);
                                    }
                                    else {
                                        a.setResult(false);
                                    }
                                    fTS.getC().getFeedbackActions().add(a);
                                }
                                else {
                                    System.err.println("*** Warning: received feedback for an Action that is not pending.");
                                }
                            }
                            else {
                                throw new JasonException("Cannot identify executed action.");
                            }
                        }
                    }
                }
            } while (m != null);
        } catch (Exception e) {
            System.err.println("Error receiving message.");
            e.printStackTrace();
        }
    }
    
    // this is used by the .send internal action in stdlib
    /** the saci implementation of the sendMsg interface */
    public void sendMsg(jason.asSemantics.Message m) throws Exception {
    	// suspend intention if it is an ask
    	if (m.isAsk()) {
    		//System.out.println("adding PA "+m.getMsgId()+":"+fTS.getC().getSelectedIntention());
            fTS.getC().getPendingActions().put(m.getMsgId(), fTS.getC().getSelectedIntention());    		
    	}
        saci.Message msaci = new saci.Message("("+m.getIlForce()+")");
        msaci.put("receiver", m.getReceiver());
        msaci.put("content", m.getPropCont());
        msaci.put("reply-with", m.getMsgId());
        if (m.getInReplyTo() != null) {
        	msaci.put("in-reply-to", m.getInReplyTo());
        }
        getMBox().sendMsg(msaci);
    }

    public void broadcast(jason.asSemantics.Message m) throws Exception {
        saci.Message msaci = new saci.Message("("+m.getIlForce()+")");
        msaci.put("content", m.getPropCont());
        getMBox().broadcast(msaci);
    }
    
    
    // Deafult procedure for checking messages
    public void checkMail() {
        if (! running) {
            return;
        }
        if (getMBox() == null) {
            System.err.println("*** Warning! Agent "+fTS.getAgArch().getName()+" has no mail box.");
            return;
        }
        
        saci.Message m  = null;
        do {
            try {
                m  = getMBox().receive();
            } catch (Exception e) {
                System.err.println("Error receiving message.");
                e.printStackTrace();
            }
            if (m != null) {
                String ilForce   = (String)m.get("performative");
                String sender    = (String)m.get("sender");
                String receiver  = (String)m.get("receiver");
                String replyWith = (String)m.get("reply-with");
                String irt       = (String)m.get("in-reply-to");
                
                String propCont  = null;
                if (m.get("content") != null) {
                    propCont = m.get("content").toString();
                    if (Term.parse(propCont) != null) { // the contents are well formed
                        jason.asSemantics.Message im = new jason.asSemantics.Message(ilForce, sender, receiver, propCont, replyWith);
                        if (irt != null) {
                        	im.setInReplyTo(irt);
                        }
                        fTS.getC().getMB().add(im);
                        if (fTS.getSettings().verbose()>=1)
                            System.out.println("Agent " + fTS.getAgArch().getName() + " received message: " + im);
                    } else {
                        System.err.println("*** Warning! Message received cannot be handled:"+m);
                    }
                }
            }
        } while (m != null);
    }
    
    /** same code than CentalisedAgArch */
    public void brf() {
        if (! running || percepts == null) {
            return;
        }


        // deleting percepts in the BB that is not percepted anymore
        List perceptsInBB = fTS.getAg().getBS().getPercepts();
        for (int i=0; i<perceptsInBB.size(); i++) { 
            Literal l = (Literal)perceptsInBB.get(i);
            // could not use percepts.contains(l), since equalsAsTerm must be used
            boolean wasPercepted = false;
            for (int j=0; j<percepts.size(); j++) {
            	Term t = (Term)percepts.get(j); // it probably is a Pred
            	if (l.equalsAsTerm(t)) { // if percept t already is in BB
            		wasPercepted = true;
            	}
            }
            if (!wasPercepted) {
                if (fTS.getAg().delBel(l,D.TPercept,fTS.getC())) {
                	i--;
                }
            }
        }
  	
        // addBel only adds a belief when appropriate
        // checking all percpets for new beliefs
        Iterator i = percepts.iterator();
        while (i.hasNext()) {
			Literal l = (Literal)i.next();
			try {
				fTS.getAg().addBel( l, D.TPercept, fTS.getC(), D.EmptyInt);
			} catch (Exception e) {
				System.err.println("Error adding percetion "+l+"\n");
				e.printStackTrace();
			}
        }
    }

    /*
    // Default BRF (works both for Closed World Assumption and Open World)
    public void brf() {
        if (! running) {
            return;
        }
        brfUpdate(percepts,fTS.getAg().getBS().getBels(),D.LPos);
        brfUpdate(negPercepts,fTS.getAg().getBS().getNegBels(),D.LNeg);
    }
    
    private void brfUpdate(List percepts, List beliefs, boolean type) {
        Literal l;
        // deleting first is more efficient
        // delBel deletes as appropriate, delete all beliefs with a "percept" annotation
        // which does not appear in the percepts anymore
        // CAREFUL with clone!!! (if removing clone(), careful with p.clearAnnot
        for(Iterator i=((List) ((ArrayList)beliefs).clone()).iterator(); i.hasNext(); ) {
            l = (Literal)i.next();
            if (l.hasAnnot(D.TPercept) && !percepts.contains(l)) {
                fTS.getAg().delBel(l,D.TPercept,fTS.getC());
            }
        }
        // addBel only adds a belief when appropriate
        // checking all percpets for new beliefs
        for(Iterator i=percepts.iterator(); i.hasNext(); ) {
            fTS.getAg().addBel(new Literal(type, new Pred((Term)i.next())),D.TPercept,fTS.getC());
        }
    }
    */
    
    // Default acting on the environment
    // it gets action from CA; it must be NULL after that!
    public void act() {
        if (! running) {
            return;
        }
        if (fTS.getC().getAction() == null)
            return;
        try {
            String rw = mboxPercept.getRW();
            saci.Message m = new saci.Message("(ask :receiver environment :ontology AS-Action :content execute)");
            m.put("action", fTS.getC().getAction().getActionTerm().toString());
            m.put("reply-with", rw);
            m.put("verbose", new Integer(fTS.getSettings().verbose()).toString());
            
            mboxPercept.sendMsg(m);
            
            fTS.getC().getPendingActions().put(rw, fTS.getC().getAction());
        } catch (Exception e) {
            System.err.println("Error sending action "+ fTS.getC().getAction());
            e.printStackTrace();
        }
    }

    private static Message cycleFinished = new Message("(tell :receiver controller :ontology AS-ExecControl :content cycleFinished)");
    
	/** inform the remote controller that this agent's cycle was finished (used in sync mode) */ 
	public void informCycleFinished() {
		// send a message to the executionControl agent
		mboxPercept.sendMsg(cycleFinished);
	}

}
