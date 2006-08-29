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
import jason.asSyntax.Term;
import jason.mas2j.ClassParameters;
import jason.runtime.RuntimeServicesInfraTier;
import jason.runtime.Settings;

import java.util.Iterator;
import java.util.List;
import java.util.Queue;
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

    /** The user implementation of the architecture */
    protected AgArch                    fUserAgArh;

    private String                      agName       = "";

    private boolean                     running      = true;

    protected Logger                    logger;

    /**
     * Creates the user agent architecture, default architecture is
     * jason.architecture.AgArch. The arch will create the agent that creates
     * the TS.
     */
    public void initAg(String agArchClass, String agClass, ClassParameters bbPars, String asSrc, Settings stts) throws JasonException {
        logger = Logger.getLogger(CentralisedAgArch.class.getName() + "." + getAgName());
        try {
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

    /** Stops the agent */
    public void stopAg() {
        fUserAgArh.stopAg();
        running = false;
        fUserAgArh.getTS().receiveSyncSignal(); // in case the agent is wainting
        // .....
        fUserAgArh.getTS().newMessageHasArrived(); // in case the agent is
        // wainting .....
        synchronized (syncStopRun) {
            infraEnv.delAgent(fUserAgArh);
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
    public void sendMsg(jason.asSemantics.Message m) throws Exception {
        // suspend intention if it is an ask
        if (m.isAsk()) {
            fUserAgArh.getTS().getC().getPendingIntentions().put(m.getMsgId(), fUserAgArh.getTS().getC().getSelectedIntention());
        }

        // actually send the message
        m.setSender(getName());
        Queue<Message> mbox = infraEnv.getAgMbox(m.getReceiver());
        if (mbox == null) {
            throw new JasonException("Receiver '" + m.getReceiver() + "' does not exists! Could not send " + m);
        }
        //synchronized (mbox) {
            mbox.offer(new Message(m));
        //}
        infraEnv.getAgent(m.getReceiver()).getTS().newMessageHasArrived();
    }

    public void broadcast(jason.asSemantics.Message m) throws Exception {
        Iterator i = infraEnv.getAgents().values().iterator();
        while (i.hasNext()) {
            AgArch ag = (AgArch) i.next();
            if (!ag.getAgName().equals(this.getAgName())) {
                m.setReceiver(ag.getAgName());
                sendMsg(m);
            }
        }
    }

    // Deafult procedure for checking messages
    public void checkMail() {
        Queue<Message> mbox = infraEnv.getAgMbox(getName());
        Queue<Message> tsmb = fUserAgArh.getTS().getC().getMailBox();
        while (!mbox.isEmpty()) {
            Message im = mbox.poll();
            tsmb.offer(im);
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("received message: " + im);
            }
        }
    }

    // Default acting on the environment
    // it gets action from ts.C.A;
    public void act(ActionExec action, List<ActionExec> feedback) {
        Term acTerm = action.getActionTerm();
        logger.info("doing: " + acTerm);

        if (infraEnv.getUserEnvironment().executeAction(getName(), acTerm)) {
            action.setResult(true);
        } else {
            action.setResult(false);
        }
        feedback.add(action);
    }

    public boolean canSleep() {
        return infraEnv.getAgMbox(getName()).isEmpty();
    }

    public void informCycleFinished(boolean breakpoint, int cycle) {
        infraControl.receiveFinishedCycle(getName(), breakpoint, cycle);
    }

    public RuntimeServicesInfraTier getRuntimeServices() {
        return new CentralisedRuntimeServices();
    }
}
