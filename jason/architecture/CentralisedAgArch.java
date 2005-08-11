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

import jason.JasonException;
import jason.asSemantics.ActionExec;
import jason.asSemantics.Agent;
import jason.asSemantics.Message;
import jason.asSemantics.TransitionSystem;
import jason.asSyntax.Term;
import jason.control.CentralisedExecutionControl;
import jason.environment.CentralisedEnvironment;

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * Centralised architecture for a Mulit-Agent Society of AgentSpeak Agents.
 * 
 * Execution sequence: initAg, setEnv, setControl, run (perceive, checkMail, act), stopAg.
 */
public class CentralisedAgArch extends Thread implements AgentArchitecture {
    
	protected CentralisedEnvironment fEnv = null;
	
	private CentralisedExecutionControl fControl = null;

	protected TransitionSystem fTS = null;

	private String agName = "";
    private boolean running = true;
	
	protected Logger logger;
    
    public void initAg(String[] args) throws JasonException {
    	logger = Logger.getLogger(CentralisedAgArch.class.getName()+"."+getAgName());
        // set the agent
        try {
            String className = null;
            if (args.length < 1) { // error
            	running = false;
                throw new JasonException("The Agent class name were not informed for the CentralisedAgArch creation!");
            } else {
                className = args[0].trim();
            }
            Agent ag = (Agent)Class.forName(className).newInstance();
            fTS = ag.initAg(args, this);
    		logger.setLevel(fTS.getSettings().log4JLevel());
        } catch (Exception e) {
        	logger.error("as2j: error creating the agent class! - ",e);
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
    
    public void setTS(TransitionSystem ts) {
        this.fTS = ts;
    }
    
    public TransitionSystem getTS() {
    	return fTS;
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
    
    /** stop the agent */
    public void stopAg() {
    	running = false;
    	fTS.receiveSyncSignal(); // in case the agent is wainting .....
    	synchronized(syncStopRun) {
    		fEnv.delAgent(this);
    	}
    }

    public boolean isRunning() {
    	return running;
    }
    
    public void run() {
    	synchronized(syncStopRun) {
	        while (running) {
	            fTS.reasoningCycle();
	        }
    	}
    }
    
    // Default perception assumes Complete and Accurate sensing.
    public List perceive() {
    	List percepts = fEnv.getUserEnvironment().getPercepts(getName());
    	if (logger.isDebugEnabled()) { // to salve CPU time building the string
    		logger.debug("percepts: "+percepts);
    	}
        return percepts;
    }
    
    // this is used by the .send internal action in stdlib
    public void sendMsg(jason.asSemantics.Message m) throws Exception {
    	// suspend intention if it is an ask
    	if (m.isAsk()) {
            fTS.getC().getPendingActions().put(m.getMsgId(), fTS.getC().getSelectedIntention());    		
    	}

    	// actually send the message
        m.setSender(getName());
        List mbox = fEnv.getAgMbox(m.getReceiver());
		if (mbox == null) {
            throw new JasonException("the receiver '"+m.getReceiver()+"' does not exists! Could not send "+m);
		}
        synchronized (mbox) {
            mbox.add(new Message(m));
        }
        fEnv.getAgent(m.getReceiver()).fTS.newMessageHasArrived();
    }

    public void broadcast(jason.asSemantics.Message m) throws Exception {
    	Iterator i = fEnv.getAgents().values().iterator();
    	while (i.hasNext()) {
    		CentralisedAgArch ag = (CentralisedAgArch)i.next();
    		if (! ag.getName().equals(this.getName())) {
    	        m.setReceiver(ag.getName());
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
                fTS.getC().getMB().add(im);
                i.remove();
                logger.info("received message: " + im);
            }
        }
    }

    // Default acting on the environment
    // it gets action from ts.C.A; 
    public void act() {
    	ActionExec acExec = fTS.getC().getAction(); 
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
        fTS.getC().getFeedbackActions().add(acExec);
    }
    
    // methods for execution control

	/** inform the controller that this agent's cycle was finished (used in sync mode) */ 
	public void informCycleFinished(boolean breakpoint) {
		fControl.receiveFinishedCycle(getName(), breakpoint);
	}
}
