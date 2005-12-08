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
// http://www.dur.ac.uk/r.bordini
// http://www.inf.furb.br/~jomi
//
// CVS information:
//   $Date$
//   $Revision$
//   $Log$
//   Revision 1.20  2005/12/08 20:06:59  jomifred
//   changes for JasonIDE plugin
//
//   Revision 1.19  2005/12/05 16:04:47  jomifred
//   Message content can be object
//
//   Revision 1.18  2005/11/20 16:53:17  jomifred
//   the canSleep method in TS asks the agent arch if it can sleep.
//
//   Revision 1.17  2005/11/16 18:35:25  jomifred
//   fixed the print(int) on console bug
//
//   Revision 1.16  2005/11/07 12:42:23  jomifred
//   receive message is shown only in debug mode
//
//   Revision 1.15  2005/10/30 18:37:27  jomifred
//   change in the AgArch customisation  support (the same customisation is used both to Cent and Saci infrastructures0
//
//   Revision 1.14  2005/10/07 18:53:35  jomifred
//   fix a bug in console (it tried to use X11 event with Console logger)
//
//   Revision 1.13  2005/08/16 21:03:42  jomifred
//   add some comments on TODOs
//
//   Revision 1.12  2005/08/15 17:41:36  jomifred
//   AgentArchitecture renamed to AgArchInterface
//
//   Revision 1.11  2005/08/13 13:55:35  jomifred
//   java doc updated
//
//   Revision 1.10  2005/08/12 22:19:26  jomifred
//   add cvs keywords
//
//
//----------------------------------------------------------------------------


package jason.architecture;


import jason.JasonException;
import jason.asSemantics.ActionExec;
import jason.asSemantics.TransitionSystem;
import jason.asSyntax.ListTerm;
import jason.asSyntax.Term;
import jason.runtime.MASConsoleGUI;
import jason.runtime.RunCentralisedMAS;

import java.io.File;
import java.util.List;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.w3c.dom.Document;

import saci.Config;
import saci.MBoxChangedListener;
import saci.MBoxSAg;
import saci.Message;
import saci.MessageHandler;

/**
 * This class provides an agent architecture when using SACI Infra Structure
 * to run the MAS.
 * 
 * <p>Execution sequence: initAg, run (perceive, checkMail, act), stopAg.
 */

public class SaciAgArch extends saci.Agent implements AgArchInterface {
    
    // to get the percepts via SACI (the normal mbox is used for inter-agent com.)
    private MBoxSAg mboxPercept = null;

	/** the user implementation of the architecture */
	protected AgArch fUserAgArh;

	private Logger logger;
    
    // this is used by SACI to initialize the agent
    public void initAg(String[] args) throws JasonException {

        
    	// create a logger
    	logger = Logger.getLogger(SaciAgArch.class.getName()+"."+getAgName());
        if (new File(RunCentralisedMAS.logPropFile).exists()) {
        	PropertyConfigurator.configure(RunCentralisedMAS.logPropFile);
        } else {
        	PropertyConfigurator.configure(SaciAgArch.class.getResource("/"+RunCentralisedMAS.logPropFile));
        }

    	// create the jasonId console
        if (MASConsoleGUI.hasConsole()) { // the logger created the MASConsole
        	MASConsoleGUI.get().setTitle("MAS Console - "+getSociety());
        	MASConsoleGUI.get().setAsDefaultOut();
        }

        // set the agent class
        try {
            String archClassName = null;
            if (args.length < 1) { // error
                throw new JasonException("The Agent Architecture class name was not informed for the SaciAgArch creation!");
            } else {
            	archClassName = args[0].trim();
            }
            fUserAgArh = (AgArch)Class.forName(archClassName).newInstance();
            fUserAgArh.setInfraArch(this);
            fUserAgArh.initAg(args);
    		logger.setLevel(fUserAgArh.getTS().getSettings().log4JLevel());
        } catch (Exception e) {
        	running = false;
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
                    if (fUserAgArh.getTS() != null) {
                    	fUserAgArh.getTS().newMessageHasArrived();
                    }
                }
            });
            
            mboxPercept.addMessageHandler("performCycle", "tell", null, "AS-ExecControl", new MessageHandler() {
                public boolean processMessage(saci.Message m) {
                	fUserAgArh.getTS().receiveSyncSignal();
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
                            Document agStateDoc = fUserAgArh.getTS().getAg().getAgState();
                            
                            // serialize
                            //StringWriter so = new StringWriter();
                            //stateSerializer.setOutputProperty(OutputKeys.ENCODING,"ISO-8859-1");
                            //stateSerializer.transform(new DOMSource(agStateDoc), new StreamResult(so));
                            //r.put("content", so.toString());
                            r.putWithoutSerialization("content", agStateDoc);

                            mboxPercept.sendMsg(r);
                        } catch (Exception e) {
                        	logger.error("Error sending message "+r,e);
                        }
                        return true; // no other message handler gives this message
                    }
                });
                
            getMBox().setMboxChangedListener(new MBoxChangedListener() {
                public void mboxChanged() {
                    if (fUserAgArh.getTS() != null) {
                    	fUserAgArh.getTS().newMessageHasArrived();
                    }
                }
            });

            
        } catch (Exception e) {
        	logger.error("Error entering the environment's society.",e);
        }
    }
    
    public String getAgName() {
    	return super.getName();
    }
    
    public void stopAg() {
        super.stopAg();
        mboxPercept.disconnect();
        if (MASConsoleGUI.hasConsole()) { // the logger created the MASConsole
        	MASConsoleGUI.get().close();
        }
    }
    
    public void run() {
        while (running) {
        	fUserAgArh.getTS().reasoningCycle();
        }
        logger.debug("finished running.\n");
    }
    
    // Default functions for the overall agent architecture (based on SACI)
    // they facilitate things a lot in case the programmer doesn't need
    // anything special
    
    // Default perception assumes Complete and Accurate sensing.
    // In the case of the SACI Architecture, the results of requests
    // for action execution is also recieved here.
    public List perceive() {
        if (! running) {
            return null;
        }
		
		List percepts = null;
        
        saci.Message askMsg = new saci.Message("(ask-all :receiver environment :ontology AS-Perception :content getPercepts)");
        
        // asks current environment state (positive percepts)
        saci.Message m = null;
        try {
            m = mboxPercept.ask(askMsg);
        } catch (Exception e) {
        	logger.error("Error receiving perceptions.",e);
        }
        if (m != null) {
			String content = (String) m.get("content");
			if (content != null) {
				percepts = ListTerm.parseList(content).getAsList();
				if (logger.isDebugEnabled()) {
					logger.debug("received percepts: "+percepts);
				}
			} else {
				percepts = null; // used to indicate that are nothing new in the environment, no BRF needed
			}
        }
        
	
        // check if there are feedbacks on requested action executions
        try {
            do {
                m = mboxPercept.receive();
                if (m != null) {
                    if (m.get("ontology") != null) {
                        if ( ((String)m.get("ontology")).equals("AS-Action")) {
                            String irt = (String)m.get("in-reply-to");
                            if (irt != null) {
                                ActionExec a = (ActionExec)fUserAgArh.getTS().getC().getPendingActions().remove(irt);
                                // was it a pending action?
                                if (a != null) {
                                    if (((String)m.get("content")).equals("ok")) {
                                        a.setResult(true);
                                    }
                                    else {
                                        a.setResult(false);
                                    }
                                    fUserAgArh.getTS().getC().getFeedbackActions().add(a);
                                }
                                else {
                                	logger.error("Error: received feedback for an Action that is not pending.");
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
        	logger.error("Error receiving message.",e);
        }
		return percepts;
    }
    
    // this is used by the .send internal action in stdlib
    /** the saci implementation of the sendMsg interface */
    public void sendMsg(jason.asSemantics.Message m) throws Exception {
    	// suspend intention if it is an ask
    	if (m.isAsk()) {
    		fUserAgArh.getTS().getC().getPendingActions().put(m.getMsgId(), fUserAgArh.getTS().getC().getSelectedIntention());    		
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
            logger.warn("I have no mail box!");
            return;
        }
        
        saci.Message m  = null;
        do {
            try {
                m  = getMBox().receive();
            } catch (Exception e) {
            	logger.error("Error receiving message.",e);
            }
            if (m != null) {
            	if (logger.isDebugEnabled()) {
            		logger.debug("Received message: " + m + ". Content class is "+m.get("content").getClass().getName());
            	}
                String ilForce   = (String)m.get("performative");
                String sender    = (String)m.get("sender");
                String receiver  = (String)m.get("receiver");
                String replyWith = (String)m.get("reply-with");
                String irt       = (String)m.get("in-reply-to");
                
                Object propCont  = m.get("content");
                if (propCont != null) {
                    propCont = m.get("content");
                    String sPropCont = propCont.toString();
                    if (sPropCont.startsWith("\"")) { // deal with a term closed by "
                    	sPropCont = sPropCont.substring(1,sPropCont.length()-1);
                    	if (Term.parse(sPropCont) != null) {
                    		// it was a term with "
                    		propCont = sPropCont.trim();
                    	}
                    }
                    
                    jason.asSemantics.Message im = new jason.asSemantics.Message(ilForce, sender, receiver, propCont, replyWith);
                    if (irt != null) {
                    	im.setInReplyTo(irt);
                    }
                    fUserAgArh.getTS().getC().getMB().add(im);

                    /*
                    if (Term.parse(sPropCont) != null) { // the contents are well formed
                        
                    } else { 
                    	// the content is a Java Object (architectures deals with this kind of content)
                       //logger.warn("Warning! Message received cannot be handled: "+m);
                    	
                    }
                    */
                }
            }
        } while (m != null);
    }
    
    // Default acting on the environment
    // it gets action from CA; it must be NULL after that!
    public void act() {
        if (! running) {
            return;
        }
        TransitionSystem ts = fUserAgArh.getTS();
        if (ts.getC().getAction() == null)
            return;
        try {
        	Term acTerm = ts.getC().getAction().getActionTerm();
        	logger.info("doing: "+acTerm);

            String rw = mboxPercept.getRW();
            saci.Message m = new saci.Message("(ask :receiver environment :ontology AS-Action :content execute)");
            m.put("action", acTerm.toString());
            m.put("reply-with", rw);
            m.put("verbose", new Integer(ts.getSettings().verbose()).toString());
            
            mboxPercept.sendMsg(m);
            
            ts.getC().getPendingActions().put(rw, ts.getC().getAction());
        } catch (Exception e) {
        	logger.error("Error sending action "+ ts.getC().getAction(),e);
        }
    }

    
    public boolean canSleep() {
    	try {
    		return getMBox().getMessages(null, 1, 0, false).size() == 0;
    	} catch (Exception e) {
    		return true;
    	}
    }

    private static Message cycleFinished = new Message("(tell :receiver controller :ontology AS-ExecControl :content cycleFinished)");
    
	/** inform the remote controller that this agent's cycle was finished (used in sync mode) */ 
	public void informCycleFinished(boolean breakpoint) {
		// send a message to the executionControl agent
	    Message m = (Message)cycleFinished.clone();
	    if (breakpoint) {
	    	m.put("breakpoint","true");
	    }
		mboxPercept.sendMsg(m);
	}

}
