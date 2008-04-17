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
import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.ListTerm;
import jason.asSyntax.Term;

import java.util.Collections;

/**

  <p>Internal action: <b><code>.sort</code></b>.

  <p>Description: sorts a list of terms. The "natural" order for each type of
  terms is used. Between different types of terms, the following order is
  used:<br>

  numbers &lt; atoms &lt; structures &lt; lists 

  <p>Parameters:<ul>
  <li>+   unordered list (list): the list the be sorted.<br/>
  <li>+/- ordered list (list): the sorted list. 
  </ul>

  <p>Examples:<ul>
  
  <li> <code>.sort([c,a,b],X)</code>: <code>X</code> unifies with
  <code>[a,b,c]</code>.

  <li>
  <code>.sort([b,c,10,g,casa,f(10),[3,4],5,[3,10],f(4)],X)</code>:
  <code>X</code> unifies with
  <code>[5,10,b,c,casa,g,f(4),f(10),[3,4],[3,10]]</code>.

  <li>
  <code>.sort([3,2,5],[2,3,5])</code>: true.

  <li>
  <code>.sort([3,2,5],[a,b,c])</code>: false.

  </ul>

  @see jason.stdlib.concat
  @see jason.stdlib.delete
  @see jason.stdlib.length
  @see jason.stdlib.member
  @see jason.stdlib.nth
  @see jason.stdlib.max
  @see jason.stdlib.min
  @see jason.stdlib.reverse

  @see jason.stdlib.difference
  @see jason.stdlib.intersection
  @see jason.stdlib.union

*/
public class sort extends DefaultInternalAction {
	
	private static InternalAction singleton = null;
	public static InternalAction create() {
		if (singleton == null) 
			singleton = new sort();
		return singleton;
	}
    
    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        try {
            ListTerm l1 = (ListTerm) args[0].clone();
            if (l1.isVar()) {
            	throw new JasonException("The first argument of .sort should not be a free variable.");
            }
            Collections.sort(l1);
            return un.unifies(l1, args[1]);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new JasonException("The internal action 'sort' has not received two arguments.");
        } catch (JasonException e) {
        	throw e;
        } catch (Exception e) {
            throw new JasonException("Error in internal action 'sort': " + e, e);
        }    
    }
}
