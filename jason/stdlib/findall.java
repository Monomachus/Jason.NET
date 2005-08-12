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
// CVS information:
//   $Date$
//   $Revision$
//   $Log$
//   Revision 1.5  2005/08/12 22:20:10  jomifred
//   add cvs keywords
//
//
//----------------------------------------------------------------------------

package jason.stdlib;

import jason.JasonException;
import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.ListTerm;
import jason.asSyntax.Literal;
import jason.asSyntax.Term;

import java.util.List;

public class findall implements InternalAction {

	/** .findall(Var, a(Var), List) */
	public boolean execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
		try {
			Term var = (Term) args[0].clone();
			if (!var.isVar()) {
				throw new JasonException("The first parameter ('"+args[0]+"') of the internal action 'findAll' is not a Variable!");
			}
			Literal bel = Literal.parseLiteral(args[1].toString());
			if (bel == null) {
				throw new JasonException("The second parameter ('"+args[1]+"') of the internal action 'findAll' is not a literal!");
			}
			un.apply(bel);

			// find all bel in belief base and build a list with them
			ListTerm all = new ListTerm();
			List relB = ts.getAg().getBS().getRelevant(bel);
			if (relB != null) {
				for (int i = 0; i < relB.size(); i++) {
					Literal b = (Literal) relB.get(i);
					Unifier newUn = (un == null) ? new Unifier() : (Unifier) un.clone();
					// recall that order is important because of annotations!
					//System.out.println("b="+b+"="+bel);
					if (newUn.unifies(bel, b)) {
						// get the val value and add it in the list
						Term vl = newUn.get(var.toString());
						if (vl != null) {
							all.add((Term) vl);
						}
					}
				}
			}
			Term list = args[2];
			//System.out.println("all="+all);
			return un.unifies(list, all);
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new JasonException("The internal action 'findall' has not received three arguments");
		} catch (Exception e) {
			throw new JasonException("Error in internal action 'findall': " + e);
		//} finally {
		//	System.out.println("u="+un);
		}
	}
}
