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

package jason.stdlib;

import jason.JasonException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;


/**
  <p>Internal action: <b><code>.kill_agent</code></b>.
  
  <p>Description: kills the agent whose name is given as parameter. This is a
     provisional internal action to be used while more adequate mechanisms for
     creating and killing agents are being developed. In particular, note that
     an agent can kill any other agent, without any consideration on
     permissions, etc.! It is the programmers' responsibility to use this
     action.

  
  <p>Parameters:<ul>
  
  <li>+ name (atom): the name of the agent to be killed.<br/>

  </ul>
  
  <p>Example:<ul> 

  <li> <code>.kill_agent(bob)</code>: kills the agent named bob.</li>

  </ul>

  @see jason.stdlib.create_agent
  @see jason.stdlib.stopMAS
  @see jason.runtime.RuntimeServicesInfraTier

*/
public class kill_agent extends DefaultInternalAction {

    @Override
	public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
		
		try {
            Term name = args[0];
            return ts.getUserAgArch().getArchInfraTier().getRuntimeServices().killAgent(name.toString());
		} catch (IndexOutOfBoundsException e) {
			throw new JasonException("The internal action 'kill_agent' received a wrong number of arguments.");
		} catch (Exception e) {
            throw new JasonException("Error in internal action 'kill_agent': " + e, e);
		}
	}
}
