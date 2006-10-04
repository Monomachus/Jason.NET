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

public class addAnnot extends DefaultInternalAction {

	/**
	 * Example: .addAnnot(a,source(jomi),B)
	 *          B will be a[source(jomi)]
	 * or       .addAnnot([a1,a2], source(jomi), B)
	 *          B will be [a1[source(jomi)], a2[source(jomi)]]
	 *          
	 * args[0] is the literal to be annotted
	 * args[1] is the annotation itself
	 * args[2] is the result -- does't have to be a var!
	 */
	
    @Override
	public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
		try {
			Term result = addAnnotToList(un, (Term)args[0].clone(), args[1]);
			return un.unifies(result,args[2]);
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new JasonException("The internal action 'addAnnot' requires three arguments.");
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
				result.addAnnot( (Term)annot.clone());
				return result;
			} catch (Exception e) {
				// no problem, the content is not a pred (is a number,
				// string, ....) received in a message, for instance
			}
		}
		return null;
	}

	
}
