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


package jason.stdlib;

import java.util.Iterator;

import jason.JasonException;
import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Literal;
import jason.asSyntax.Term;
import jason.asSyntax.ListTerm;

public class addAnnot implements InternalAction {

	/**
	 * Example: .addAnnot(a,source(jomi),B)
	 *          B will be a[source(jomi)]
	 * 
	 * args[0] is the literal to be annotted
	 * args[1] is the annotation itself
	 * args[2] is the result -- does't have to be a var!
	 */

	// TODO Jomi: implementei considerando se for lista tambem
	// Da uma olhada se ta OK e se sim pode apagar este todo
	
	private Unifier unif;
	
	public boolean execute(TransitionSystem ts, Unifier un, Term[] args)
			throws Exception {
		try {
			unif = un;
			Term l = (Term)args[0].clone();
			addAnnotToList(l,args[1]);
			return un.unifies(l,args[2]);
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new JasonException("The internal action 'addAnnot' requires three arguments.");
		} //finally {
			//System.out.println("annot result = "+un);			
		//}
	}

	public void addAnnotToList(Term l, Term annot) {
		if (l.isVar()) {
			unif.apply(l);
		}
		if (l.isList()) {
			ListTerm lt = (ListTerm)l;
			Iterator i = lt.iterator();
			while (i.hasNext()) {
				addAnnotToList(((ListTerm) i.next()).getTerm(),annot);
			}
		} else {
			try {
				// if it can be parsed as a literal, OK to add annot
				Literal tmp = Literal.parseLiteral(l.toString());
				tmp = (Literal)l;
				tmp.addAnnot((Term) annot.clone());
			} catch (Exception e) {
				// no problem, the content is not a pred (is a number,
				// string, ....) received in a message, for instance
			}
		}
	}

	
}
