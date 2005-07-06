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

import jason.asSemantics.Message;

import java.util.List;



/**
 * This interface should be used for defining the overall agent
 * architecture; the AS interpreter is only the reasoner (a kind of mind) within such
 * architecture (a kind of body).
 * 
 **/

public interface AgentArchitecture {

    // Default functions for the overall agent architecture
    // The user can always override them
    public List perceive();
    public void checkMail();
    public void act();

    public String getAgName();
    public void   sendMsg(Message m) throws Exception;
    public void   broadcast(Message m) throws Exception;
    
    // methods for execution control

	/** inform the (centralised/saci) controller that this agent's cycle was finished (used in sync mode) */ 
	public void informCycleFinished();
    
}
