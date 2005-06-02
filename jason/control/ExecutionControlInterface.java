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


package jason.control;

import java.util.Collection;

import org.w3c.dom.Document;


/** The execution control defines how the agents will run: asynchronous or synchronous.
 *  
 *  <p>This interface is implemented by Jason Execution Control to ecapsulate 
 *  the MAS architecture (distributed/centralised),
 *  since the way to access the agent's methods is different in the these architectures.
 *  
 *  It is composed by methods that the <b>user</b> controller may call.
 */
public interface ExecutionControlInterface {

    /**
     * informs an agent to continue to its next reasoning cycle.
     */
    public void informAgToPerformCycle(String agName);

    /**
     * informs all agents to continue to its next reasoning cycle.
     */
    public void informAllAgToPerformCycle();

    /** gets a string list with all the agents names */
    public Collection getAgentsName();
    
    public int getAgentsQty();

    /**
     * get the agent state (beliefs, intentions, plans, ...)
     * as an XML document
     */
	public Document getAgState(String agName);
}
