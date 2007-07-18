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
import jason.asSyntax.Literal;
import jason.asSyntax.Term;

/**
  <p>Internal action: <b><code>.abolish</code></b>.
  
  <p>Description: removes all beliefs that match the argument. As for the
  "-" operator, an event will be generated for each deletion.
  
  <p>Parameters:<ul>
  <li>+ pattern (literal): the "pattern" for what should be removed.<br/>
  </ul>
  
  <p>Examples:<ul>
  <li> <code>.abolish(b(_))</code>: remove all <code>b/1</code> beliefs, regardless of the argument value.</li>
  <li> <code>.abolish(c(_,t))</code>: remove all <code>c/2</code> beliefs where the second argument is <code>2</code>.</li>
  <li> <code>.abolish(c(_,_)[source(ag1)])</code>: remove all <code>c/2</code> beliefs that have <code>ag1</code> as source.</li>
  </ul>

 */
public class abolish extends DefaultInternalAction {

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        try {
            ts.getAg().abolish((Literal)args[0], un);
            return true;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new JasonException("The internal action 'abolish' has not received the required argument.");
        } catch (Exception e) {
            throw new JasonException("Error in internal action 'abolish': " + e, e);
        }
    }
}
