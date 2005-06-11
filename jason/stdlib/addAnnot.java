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
// http://www.csc.liv.ac.uk/~bordini
// http://www.inf.furb.br/~jomi
//----------------------------------------------------------------------------


// TESTING!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

package jason.stdlib;

import jason.JasonException;
import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Literal;
import jason.asSyntax.Term;

public class addAnnot implements InternalAction {

	/**
	 * Example: .addAnnot(a,source(jomi),B)
	 *          B will be a[source(jomi)]
	 * 
	 * args[0] is the literal that will be annotted
	 * args[1] is the annot
	 * args[2] is the result
	 */
	public boolean execute(TransitionSystem ts, Unifier un, Term[] args)
			throws Exception {
		try {
			Term result = args[2]; // Literal.parseLiteral(args[2]);
			// do not need to be a var!

			Term l = (Term)args[0].clone();
			if (l.isVar()) {
				un.apply(l);
			}

			if (args[0].isList()) {
				// TODO: add annot on all list members that are predicate!!!!
				throw new JasonException("Not implemented!");
			} else {
				try {
					// in case it could be a literal (tested by parsing), add annot
					Literal bel = Literal.parseLiteral(l.toString());
					bel.addAnnot((Term)args[1].clone());
					return un.unifies(bel, result);
				} catch (Exception e) {
					// no problem, the content is not a pred (is a number,
					// string, ....) received in a message, for instance
				}
			}
			return un.unifies(l, result);
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new JasonException("The internal action 'addAnnot' has not received three arguments.");
		} finally {
			//System.out.println("annot result = "+un);			
		}
	}

}
