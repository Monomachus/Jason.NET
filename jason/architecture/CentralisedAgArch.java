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
//   Revision 1.20  2006/01/14 18:22:45  jomifred
//   centralised infra does not use xml script file anymore
//
//   Revision 1.19  2006/01/04 02:54:41  jomifred
//   using java log API instead of apache log
//
//   Revision 1.18  2005/12/05 16:04:47  jomifred
//   Message content can be object
//
//   Revision 1.17  2005/11/20 16:53:17  jomifred
//   the canSleep method in TS asks the agent arch if it can sleep.
//
//   Revision 1.16  2005/11/07 12:42:23  jomifred
//   receive message is shown only in debug mode
//
//   Revision 1.15  2005/10/30 18:37:27  jomifred
//   change in the AgArch customisation  support (the same customisation is used both to Cent and Saci infrastructures0
//
//   Revision 1.14  2005/08/15 17:41:36  jomifred
//   AgentArchitecture renamed to AgArchInterface
//
//   Revision 1.13  2005/08/13 13:55:35  jomifred
//   java doc updated
//
//   Revision 1.12  2005/08/12 22:19:26  jomifred
//   add cvs keywords
//
//
//----------------------------------------------------------------------------


package jason.architecture;

import jason.JasonException;
import jason.asSemantics.ActionExec;
import jason.asSemantics.Message;
import jason.asSyntax.Term;
import jason.control.CentralisedExecutionControl;
import jason.environment.CentralisedEnvironment;
import jason.runtime.Settings;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * This class provides an agent architecture when using Centralised
 * infrastructure to run the MAS inside Jason.
 * 
 * <p>Execution sequence: initAg, setEnv, setControl, run (perceive, checkMail, act), stopAg.
 */
public class CentralisedAgArch extends Thread implements AgArchInterface {
    
	protected CentralisedEnvironment fEnv = null;
	
	private CentralisedExecutionControl fControl = null;

	//protected TransitionSystem fTS = null;
	
	/** the user implementation of the architecture */
	protected AgArch fUserAgArh;
	
	private String agName = "";
    private boolean running = true;
	
	protected Logger logger;
    
	/** creates the user agent architecture, default architecture is jason.architecture.AgArch. 
	 *  The arch will create the agent that creates the TS. */
    public void initAg(String agArchClass, String agClass, String asSrc, Settings stts) throws JasonException {
    	logger = Logger.getLogger(CentralisedAgArch.class.getName()+"."+getAgName());
        try {
            fUserAgArh = (AgArch)Class.forName(agArchClass).newInstance();
            fUserAgArh.setInfraArch(this);
            fUserAgArh.initAg(agClass, asSrc, stts);
    		logger.setLevel(fUserAgArh.getTS().getSettings().logLevel());
        } catch (Exception e) {
        	running = false;
        	throw new JasonException("as2j: error creating the agent class! - "+e);
        }
    }
    
    public void setAgName(String name) {
    	super.setName(name);
    	agName = name;
    }
    
    public String getAgName() {
    	return agName;
    }
    
    public AgArch getUserAgArch() {
    	return fUserAgArh;
    }

	public void setEnv(CentralisedEnvironment env) {
		fEnv = env;
	}
	public CentralisedEnvironment getEnv() {
		return fEnv;
	}
	
	public void setControl(CentralisedExecutionControl pControl) {
		fControl = pControl;
	}
	public CentralisedExecutionControl getControl() {
		return fControl;
	}
    
    
    private Object syncStopRun = new Object();
    
    /** stops the agent */
    public void stopAg() {
    	running = false;
    	fUserAgArh.getTS().receiveSyncSignal(); // in case the agent is wainting .....
    	fUserAgArh.getTS().newMessageHasArrived(); // in case the agent is wainting .....
    	synchronized(syncStopRun) {
    		fEnv.delAgent(fUserAgArh);
    	}
    	fUserAgArh.stopAg();
    }

    public boolean isRunning() {
    	return running;
    }
    
    public void run() {
    	synchronized(syncStopRun) {
	        while (running) {
	            fUserAgArh.getTS().reasoningCycle();
	        }
    	}
    }
    
    // Default perception assumes Complete and Accurate sensing.
    public List perceive() {
    	List percepts = fEnv.getUserEnvironment().getPercepts(getName());
    	if (logger.isLoggable(Level.FINE)) { // to salve CPU time building the string
    		logger.fine("percepts: "+percepts);
    	}
        return percepts;
    }
    
    // this is used by the .send internal action in stdlib
    public void sendMsg(jason.asSemantics.Message m) throws Exception {
    	// suspend intention if it is an ask
    	if (m.isAsk()) {
    		fUserAgArh.getTS().getC().getPendingActions().put(m.getMsgId(), fUserAgArh.getTS().getC().getSelectedIntention());    		
    	}

    	// actually send the message
        m.setSender(getName());
        List mbox = fEnv.getAgMbox(m.getReceiver());
		if (mbox == null) {
            throw new JasonException("Receiver '"+m.getReceiver()+"' does not exists! Could not send "+m);
		}
        synchronized (mbox) {
            mbox.add(new Message(m));
        }
        fEnv.getAgent(m.getReceiver()).getTS().newMessageHasArrived();
    }

    public void broadcast(jason.asSemantics.Message m) throws Exception {
    	Iterator i = fEnv.getAgents().values().iterator();
    	while (i.hasNext()) {
    		AgArch ag = (AgArch)i.next();
    		if (! ag.getAgName().equals(this.getAgName())) {
    	        m.setReceiver(ag.getAgName());
    	        sendMsg(m);
    		}
    	}
    }
    
    
    // Deafult procedure for checking messages
    public void checkMail() {
        List mbox = (List)fEnv.getAgMbox(getName());
        synchronized (mbox) {
            Iterator i = mbox.iterator();
            while (i.hasNext()) {
                Message im = (Message)i.next();
                fUserAgArh.getTS().getC().getMB().add(im);
                i.remove();
                if (logger.isLoggable(Level.FINE)) {
                	logger.fine("received message: " + im);
                }
            }
        }
    }

    // Default acting on the environment
    // it gets action from ts.C.A; 
    public void act() {
    	ActionExec acExec = fUserAgArh.getTS().getC().getAction(); 
        if (acExec == null) {
            return;
        }
        Term acTerm = acExec.getActionTerm();
        logger.info("doing: "+acTerm);

        if (fEnv.getUserEnvironment().executeAction(getName(), acTerm)) {
            acExec.setResult(true);
        } else {
            acExec.setResult(false);
        }
        fUserAgArh.getTS().getC().getFeedbackActions().add(acExec);
    }
    
    public boolean canSleep() {
        List mbox = (List)fEnv.getAgMbox(getName());
        return mbox.size() == 0;
    }
    
    
    // methods for execution control

	/** inform the controller that this agent's cycle was finished (used in sync mode) */ 
	public void informCycleFinished(boolean breakpoint) {
		fControl.receiveFinishedCycle(getName(), breakpoint);
	}
}
