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

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;
import jason.runtime.RuntimeServicesInfraTier;

/**
  <p>Internal action: <b><code>.stopMAS</code></b>.
  
  <p>Description: stops all agents of the systems.
  
  <p>Example:<ul> 

  <li> <code>.stopMAS</code>.</li>

  </ul>

  @see jason.stdlib.create_agent
  @see jason.stdlib.kill_agent
  @see jason.runtime.RuntimeServicesInfraTier
 */
public class stopMAS extends DefaultInternalAction {

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {

        try {
            RuntimeServicesInfraTier rs = ts.getUserAgArch().getArchInfraTier().getRuntimeServices();
            rs.stopMAS();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
