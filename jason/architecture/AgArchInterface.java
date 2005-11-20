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
//   Revision 1.3  2005/11/20 16:53:17  jomifred
//   the canSleep method in TS asks the agent arch if it can sleep.
//
//   Revision 1.2  2005/10/30 18:37:27  jomifred
//   change in the AgArch customisation  support (the same customisation is used both to Cent and Saci infrastructures0
//
//   Revision 1.1  2005/08/15 17:40:55  jomifred
//   AgentArchitecture renamed to AgArchInterface
//
//   Revision 1.8  2005/08/13 13:55:35  jomifred
//   java doc updated
//
//   Revision 1.7  2005/08/12 22:19:26  jomifred
//   add cvs keywords
//
//
//----------------------------------------------------------------------------


package jason.architecture;

import jason.asSemantics.Message;

import java.util.List;



/**
 * This interface should be used to define the overall agent
 * architecture; the AS interpreter is only the reasoner (a kind of mind) within such
 * architecture (a kind of body).
 * 
 * <p>The agent reasoning cycle (implemented in TransitionSystem class)
 * calls these methods to get perception, action, and communication.
 **/

public interface AgArchInterface {

    // Default functions for the overall agent architecture
    // The user can always override them

    /** gets the agent's perception as a list of Literals */
    public List perceive();

    /** reads the agent's mailbox and adds messages into the agent's circumstance */
    public void checkMail();

    /** executes the action in agent's circumstance (C.A) */
    public void act();

    /** returns true whether the agent can sleep according to the arch */
    public boolean canSleep();
    
    /** gets the agent's name */
    public String getAgName();

    /** sends a Jason message in a specific infrastructure */
    public void   sendMsg(Message m) throws Exception;

    /** broadcasts a Jason message in a specific infrastructure */
    public void   broadcast(Message m) throws Exception;
    
    /** checks whether the agent is running */
    public boolean isRunning();
    
    // methods for execution control

	/** 
	 *  Inform the (centralised/saci) controller that this agent's cycle 
	 *  has finished its reasoning cycle (used in sync mode).
	 *  
	 *  <p><i>breakpoint</i> is true in case the agent selected one plan 
	 *  with the "breakpoint"  annotation.  
	 */ 
	public void informCycleFinished(boolean breakpoint);
}
