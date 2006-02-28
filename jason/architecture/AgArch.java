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
//   Revision 1.5  2006/02/28 15:11:28  jomifred
//   improve javadoc
//
//   Revision 1.4  2006/02/17 13:13:15  jomifred
//   change a lot of method/classes names and improve some comments
//
//   Revision 1.3  2006/01/14 18:22:45  jomifred
//   centralised infra does not use xml script file anymore
//
//   Revision 1.2  2005/11/20 16:53:17  jomifred
//   the canSleep method in TS asks the agent arch if it can sleep.
//
//   Revision 1.1  2005/10/30 18:37:27  jomifred
//   change in the AgArch customisation  support (the same customisation is used both to Cent and Saci infrastructures0
//
//
//----------------------------------------------------------------------------

package jason.architecture;

import jason.JasonException;
import jason.asSemantics.Agent;
import jason.asSemantics.Message;
import jason.asSemantics.TransitionSystem;
import jason.runtime.Settings;

import java.util.List;

/** 
 * Base agent architecture class that defines the overall agent
 * architecture; the AS interpreter is only the reasoner (a kind of mind) within this
 * architecture (a kind of body).
 * 
 * <p>The agent reasoning cycle (implemented in TransitionSystem class)
 * calls these methods to get perception, action, and communication.
 * 
 * <p>This class just calls the AgArchInfraTier methods
 * implemented by the infrastructure tier (Centralised, Saci, ...). 
 * However, the user can customise
 * this methods overridding some of them in his/her arch. class.
 */
public class AgArch {

	protected TransitionSystem fTS = null;
	
	/** The class that implements the architecture tier for the MAS infrastructure */
	AgArchInfraTier archTier;

    /** Creates the agent class defined by <i>agClass</i>, default is jason.semantics.Agent. */
    public void initAg(String agClass, String asSrc, Settings stts) throws JasonException {
        // set the agent
        try {
            Agent ag = (Agent)Class.forName(agClass).newInstance();
            fTS = ag.initAg(this, asSrc, stts);
        } catch (Exception e) {
            throw new JasonException("as2j: error creating the agent class! - "+e);
        }
    }

    /** Stops the agent, the user should override this method to do something before the agent is killed. The default implementation does nothing. */
    public void stopAg() {
    }

    public void setArchInfraTier(AgArchInfraTier ai) {
		archTier = ai;
	}
    public AgArchInfraTier getArchInfraTier() {
    	return archTier;
    }
	
    public TransitionSystem getTS() {
    	return fTS;
    }


	
    /** Gets the agent's perception as a list of Literals */
	public List perceive() {
		return archTier.perceive();
	}

    /** Reads the agent's mailbox and adds messages into the agent's circumstance */
	public void checkMail() {
		archTier.checkMail();
	}

    /** Executes the action in agent's circumstance (C.A) */
	public void act() {
		archTier.act();
	}

	/** Returns true if the agent can enter in sleep mode. */
	public boolean canSleep() {
		return archTier.canSleep();
    }

    /** Gets the agent's name */
	public String getAgName() {
		return archTier.getAgName();
	}

    /** Sends a Jason message */
	public void sendMsg(Message m) throws Exception {
		archTier.sendMsg(m);
	}

    /** Broadcasts a Jason message */
	public void broadcast(Message m) throws Exception {
		archTier.broadcast(m);
	}

    /** Checks whether the agent is running */
	public boolean isRunning() {
		return archTier.isRunning();
	}
}
