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
import jason.asSyntax.ListTerm;
import jason.asSyntax.ListTermImpl;
import jason.asSyntax.Literal;
import jason.asSyntax.Term;

import java.util.Iterator;

/**

  <p>Internal action: <b><code>.findall(Var,Literal,List)</code></b>.
  
  <p>Description: builds a <i>List</i> of all instantiations of
  <i>Var</i> which make <i>Literal</i> (which has at least an instance
  of <i>Var</i>) a logical consequence of the agent's belief base
  (BB). Unlike Prolog, the second argument can not be a conjunction.

  <p>Parameters:<ul>
  
  <li>+ arg[0] (variable or structure): the variable or structure that
  "populate" the list.<br/>
  
  <li>+ arg[1] (litetal): the literal in belief base.<br/>

  <li>+/- arg[2] (list): the resulting list.<br/>
  
  </ul>
  
  <p>Examples supposing BB as composed by
  {a(30),a(20),b(1,2),b(3,4),b(5,6)} :

  <ul>

  <li> <code>.findall(X,a(X),L)</code>: <code>L</code> unifies with
  <code>[30,20]</code>.</li>

  <li> <code>.findall(c(Y,X),b(X,Y),L)</code>: <code>L</code> unifies
  with <code>c(2,1),c(4,3),c(6,5)</code>.</li>

  </ul>


  @see jason.stdlib.count
*/
public class findall extends DefaultInternalAction {

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        try {
            Term var = args[0];
            Literal bel = (Literal)args[1];
            un.apply(bel);
            // find all bel in belief base and build a list with them
            ListTerm all = new ListTermImpl();
            Iterator<Unifier> iu = bel.logicalConsequence(ts.getAg(), un);
            while (iu.hasNext()) {
                Unifier nu = iu.next();
                Term vl = (Term) var.clone();
                nu.apply(vl);
                all.add(vl);
            }
            Term list = args[2];
            return un.unifies(list, all);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new JasonException("The internal action 'findall' has not received three arguments");
        } catch (Exception e) {
            throw new JasonException("Error in internal action 'findall': " + e);
        }
    }
}
