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
//----------------------------------------------------------------------------

package jason.infra.centralised;

import jason.JasonException;
import jason.architecture.AgArch;
import jason.architecture.AgArchInfraTier;
import jason.asSemantics.ActionExec;
import jason.asSemantics.Message;
import jason.asSyntax.Literal;
import jason.asSyntax.Structure;
import jason.mas2j.ClassParameters;
import jason.runtime.RuntimeServicesInfraTier;
import jason.runtime.Settings;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class provides an agent architecture when using Centralised
 * infrastructure to run the MAS inside Jason.
 * 
 * <p>
 * Execution sequence:
 * <ul>
 * <li>initAg,
 * <li>setEnvInfraTier,
 * <li>setControlInfraTier,
 * <li>run (perceive, checkMail, act),
 * <li>stopAg.
 * </ul>
 */
public class CentralisedAgArch extends Thread implements AgArchInfraTier {

	protected CentralisedEnvironment    infraEnv     = null;
    private CentralisedExecutionControl infraControl = null;
    private RunCentralisedMAS		    masRunner    = null;

    /** The user implementation of the architecture */
    protected AgArch        fUserAgArh;

    private String          agName  = "";
    private boolean         running = true;
    private Queue<Message>  mbox    = new ConcurrentLinkedQueue<Message>();	
    protected Logger        logger;
    
    private static List<MsgListener> msgListeners = null;
    public static void addMsgListener(MsgListener l) {
        if (msgListeners == null) {
        	msgListeners = new ArrayList<MsgListener>();
        }
        msgListeners.add(l);
    }
    public static void removeMsgListener(MsgListener l) {
        msgListeners.remove(l);
    }

    /**
     * Creates the user agent architecture, default architecture is
     * jason.architecture.AgArch. The arch will create the agent that creates
     * the TS.
     */
    public void initAg(String agArchClass, String agClass, ClassParameters bbPars, String asSrc, Settings stts, RunCentralisedMAS masRunner) throws JasonException {
        logger = Logger.getLogger(CentralisedAgArch.class.getName() + "." + getAgName());
        try {
        	this.masRunner = masRunner; 
            fUserAgArh = (AgArch) Class.forName(agArchClass).newInstance();
            fUserAgArh.setArchInfraTier(this);
            fUserAgArh.initAg(agClass, bbPars, asSrc, stts);
            logger.setLevel(fUserAgArh.getTS().getSettings().logLevel());
        } catch (Exception e) {
            running = false;
            throw new JasonException("as2j: error creating the agent class! - " + e);
        }
    }

    public Logger getLogger() {
        return logger;
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

    public void setEnvInfraTier(CentralisedEnvironment env) {
        infraEnv = env;
    }

    public CentralisedEnvironment getEnvInfraTier() {
        return infraEnv;
    }

    public void setControlInfraTier(CentralisedExecutionControl pControl) {
        infraControl = pControl;
    }

    public CentralisedExecutionControl getControlInfraTier() {
        return infraControl;
    }

    private Object syncStopRun = new Object();

    public void stopAg() {
        fUserAgArh.stopAg();
        running = false;
        fUserAgArh.getTS().receiveSyncSignal(); // in case the agent is wainting
        // .....
        fUserAgArh.getTS().newMessageHasArrived(); // in case the agent is
        // wainting .....
        synchronized (syncStopRun) {
            //infraEnv.delAgent(fUserAgArh);
        	masRunner.delAg(agName);
        }
    }

    public boolean isRunning() {
        return running;
    }

    public void run() {
        synchronized (syncStopRun) {
            while (running) {
                fUserAgArh.getTS().reasoningCycle();
            }
        }
    }

    // Default perception assumes Complete and Accurate sensing.
    public List<Literal> perceive() {
        List<Literal> percepts = infraEnv.getUserEnvironment().getPercepts(getName());
        if (logger.isLoggable(Level.FINE) && percepts != null) {
            logger.fine("percepts: " + percepts);
        }
        return percepts;
    }

    // this is used by the .send internal action in stdlib
    public void sendMsg(Message m) throws Exception {
        // actually send the message
        m.setSender(getName());
        CentralisedAgArch rec = masRunner.getAg(m.getReceiver());
        
        if (rec == null) {
            throw new JasonException("Receiver '" + m.getReceiver() + "' does not exists! Could not send " + m);
        }
        rec.receiveMsg(new Message(m)); // send a cloned message
	
        // notify listeners
        if (msgListeners != null) {
        	for (MsgListener l: msgListeners) {
        		l.msgSent(m);
        	}
        }
    }
    
    public void receiveMsg(Message m) {
        mbox.offer(m);
        fUserAgArh.getTS().newMessageHasArrived();    	
    }

    public void broadcast(jason.asSemantics.Message m) throws Exception {
    	for (String agName: masRunner.getAgs().keySet()) {
            if (!agName.equals(this.getAgName())) {
                m.setReceiver(agName);
                sendMsg(m);
            }
        }
    }

    // Deafult procedure for checking messages, move message from local mbox to C.mbox
    public void checkMail() {
        Queue<Message> tsmb = fUserAgArh.getTS().getC().getMailBox();
        while (!mbox.isEmpty()) {
            Message im = mbox.poll();
            tsmb.offer(im);
            if (logger.isLoggable(Level.FINE)) logger.fine("received message: " + im);
        }
    }

    // Default acting on the environment
    // it gets action from ts.C.A;
    public void act(ActionExec action, List<ActionExec> feedback) {
        Structure acTerm = action.getActionTerm();
        logger.info("doing: " + acTerm);

        if (infraEnv.getUserEnvironment().executeAction(getName(), acTerm)) {
            action.setResult(true);
        } else {
            action.setResult(false);
        }
        feedback.add(action);
    }

    public boolean canSleep() {
        return mbox.isEmpty();
    }

    public void informCycleFinished(boolean breakpoint, int cycle) {
        infraControl.receiveFinishedCycle(getName(), breakpoint, cycle);
    }

    public RuntimeServicesInfraTier getRuntimeServices() {
        return new CentralisedRuntimeServices(masRunner);
    }
}
