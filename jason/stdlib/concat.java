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
import jason.asSyntax.StringTerm;
import jason.asSyntax.StringTermImpl;
import jason.asSyntax.Term;

/**
  <p>Internal action: <b><code>.concat</code></b>.

  <p>Description: concatenates strings or lists. 

  <p>Parameters:<ul>
  <li>+ arg[0] ... + arg[n-1] (any term): the terms to be concatenated.<br/>
  <li>+- arg[n]: the result of the concatenation. 
  </ul>
  Parameters that are not string are concatenated using the toString method of
  their class.

  <p>Examples:<ul>
  <li> <code>.concat("a","b",X)</code>: <code>X</code> unifies with "ab".
  <li> <code>.concat("a","b","a")</code>: false.
  <li> <code>.concat("a b",1,a,X)</code>: <code>X</code> unifies with "a b1a".
  <li> <code>.concat("a", "b", "c", "d", X)</code>: <code>X</code> unifies with "abcd".
  <li> <code>.concat([a,b,c],[d,e],[f,g],X)</code>: <code>X</code> unifies with <code>[a,b,c,d,e,f,g]</code>.
  </ul>

  @see jason.stdlib.length
  @see jason.stdlib.member
  @see jason.stdlib.sort
  @see jason.stdlib.substring
  @see jason.stdlib.nth

*/
public class concat extends DefaultInternalAction {

	@Override
	public Object execute(TransitionSystem ts, Unifier un, Term[] args)	throws Exception {
        for (int i=0; i < args.length; i++) {
            args[i].apply(un);
        }
		
	    if (args[0].isList()) {
            // list concat
			if (!args[args.length-1].isVar() && !args[args.length-1].isList()) {
				throw new JasonException("Last argument of concat '"+args[args.length-1]+"'is not a list nor a variable.");
			}
            ListTerm result = (ListTerm)args[0];
            for (int i=1; i<args.length-1; i++) {
    			if (!args[i].isList()) {
    				throw new JasonException("arg["+i+"] is not a list in concat.");
    			}
                result.concat((ListTerm)args[i]);
            }
			return un.unifies(result, args[args.length-1]);

        
	    } else {
            // string concat
            if (!args[args.length-1].isVar() && !args[args.length-1].isString()) {
                throw new JasonException("Last argument of concat '"+args[args.length-1]+"' is not a string nor a variable.");
            }
            String vl = args[0].toString();
            if (args[0].isString()) {
                vl = ((StringTerm)args[0]).getString();
            }
            StringBuilder sr = new StringBuilder(vl);
            for (int i=1; i<args.length-1; i++) {
                vl = args[i].toString();
                if (args[i].isString()) {
                    vl = ((StringTerm)args[i]).getString();
                }
                sr.append(vl);
            }
            return un.unifies(new StringTermImpl(sr.toString()), args[args.length-1]);
		}
	}
}
