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
import jason.asSyntax.ListTerm;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.NumberTermImpl;
import jason.asSyntax.StringTerm;
import jason.asSyntax.Term;

/**

  <p>Internal action: <b><code>.length</code></b>.

  <p>Description: get the length of strings or lists. 

  <p>Parameters:<ul>
  <li>+   arg[0] (string of list): the terms to be sized.<br/>
  <li>+/- arg[1] (number): the length. 
  </ul>

  <p>Examples:<ul>
  <li> <code>.length("abc",X)</code>: <code>X</code> unifies with 3.
  <li> <code>.length([a,b],X)</code>: <code>X</code> unifies with 2.
  <li> <code>.length("a",2)</code>: fails.
  </ul>

  @see jason.stdlib.concat
  @see jason.stdlib.member
  @see jason.stdlib.sort

 */
public class length extends DefaultInternalAction {

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        Term l1 = args[0];
        Term l2 = args[1];
        un.apply(l1);
        un.apply(l2);
        NumberTerm size = null;
        if (l1.isList()) {
            ListTerm lt = (ListTerm) l1;
            size = new NumberTermImpl(lt.size());
        } else if (l1.isString()) {
            StringTerm st = (StringTerm) l1;
            size = new NumberTermImpl(st.getString().length());
        }
        if (size != null) {
            return un.unifies(l2, size);
        }
        return false;
    }
}
