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
  <p>Internal action: <b><code>.add_annot</code></b>.
  
  <p>Description: adds an annotation into a literal.
  
  <p>Parameters:<ul>
  
  <li>+ arg[0] (literal or list): the literal where the annotation
  will be added. If this parameter is a list, all literals of the list
  will have the annotation added.<br/>
  
  <li>+ arg[1] (structure): the annotation.<br/>

  <li>+/- arg[2] (literal, or list): this argument unifies with the
  result of the addition.<br/>

  </ul>
  
  <p>Examples:<ul> 

  <li> <code>.add_annot(a,source(jomi),B)</code>: <code>B</code>
  unifies with <code>a[source(jomi)]</code>.</li>

  <li> <code>.add_annot(a,source(jomi),b[jomi])</code>: fail because
  the result of the addition does not unifies the third argument.</li>

  <li> <code>.add_annot([a1,a2], source(jomi), B)</code>: <code>B</code>
  unifies with <code>[a1[source(jomi)], a2[source(jomi)]]</code>.</li>

  </ul>

 */
public class add_annot extends DefaultInternalAction {

    @Override
	public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
		try {
			Term result = addAnnotToList(un, args[0], args[1]);
			return un.unifies(result,args[2]);
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new JasonException("The internal action 'add_annot' requires three arguments.");
		} //finally {
			//System.out.println("annot result = "+un);			
		//}
	}

	public Term addAnnotToList(Unifier unif, Term l, Term annot) {
		if (l.isVar()) {
			unif.apply(l);
		}
		if (l.isList()) {
			ListTerm result = new ListTermImpl();
			ListTerm lt = (ListTerm)l;
			Iterator i = lt.iterator();
			while (i.hasNext()) {
				Term t = addAnnotToList( unif, (Term)i.next(), annot);
				if (t != null) {
					result.add(t);
				}
			}
			return result;
		} else {
			try {
				// if it can be parsed as a literal, OK to add annot
				Literal result = Literal.parseLiteral(l.toString());
				result.addAnnot(annot);
				return result;
			} catch (Exception e) {
				// no problem, the content is not a pred (is a number,
				// string, ....) received in a message, for instance
			}
		}
		return null;
	}	
}
