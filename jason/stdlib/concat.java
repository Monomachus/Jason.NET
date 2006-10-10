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

<p>Description: concatenate strings or lists. 

<p>Parameters:<ul>
<li>+ arg[0] ... + arg[n-1] (any term): the terms to be concatenated.<br/>
<li>+- arg[n]: the result of the concatenation. 
</ul>
Parameters that are not string are concatenated using the toString of it.

<p>Examples:<ul>
<li> <code>.concat("a","b",X)</code>: X unifies with "ab".
<li> <code>.concat("a","b","a")</code>: fail.
<li> <code>.concat("a b",1,a,X)</code>: X unifies with "a b1a".
<li> <code>.concat("a","b","c", "d", X)</code>: X unifies with "abcd".
<li> <code>. concat([a,b,c],[d,e],[f,g],X)</code>: X unifies with [a,b,c,d,e,f,g].
</ul>
*/
public class concat extends DefaultInternalAction {

	@Override
	public Object execute(TransitionSystem ts, Unifier un, Term[] args)	throws Exception {
        Term[] clones = new Term[args.length-1];
        for (int i=0; i < clones.length; i++) {
            clones[i] = (Term)args[i].clone();
            un.apply(clones[i]);
        }
		Term result = (Term)args[clones.length].clone();
		un.apply(result);
		
		if (clones[0].isList()) {
            for (int i=1; i<clones.length; i++) {
    			if (!clones[i].isList()) {
    				throw new JasonException("arg["+i+"] is not a list in concat.");
    			} else {
    			    ((ListTerm)clones[0]).concat((ListTerm)clones[i]);
                }
            }
			if (!result.isVar() && !result.isList()) {
				throw new JasonException("last argument of concat is not a list or variable.");
			}
			return un.unifies(clones[0], result);
		} else {
            String vl = clones[0].toString();
            if (clones[0].isString()) {
                vl = ((StringTerm)clones[0]).getString();
            }
            StringBuffer sr = new StringBuffer(vl);
            for (int i=1; i<clones.length; i++) {
                vl = clones[i].toString();
                if (clones[i].isString()) {
                    vl = ((StringTerm)clones[i]).getString();
                }
                sr.append(vl);
            }
            if (!result.isVar() && !result.isString()) {
                throw new JasonException("last argument of concat is not a string or variable.");
            }
            return un.unifies(new StringTermImpl(sr.toString()), result);
		}
	}
}
