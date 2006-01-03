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
//   Revision 1.2  2006/01/03 00:17:05  jomifred
//   change in =.. (using two lists, list of terms and list of annots)
//
//   Revision 1.1  2005/12/31 16:29:11  jomifred
//   add operator =..
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

/** 
 * This IA implements the =.. operator
 * 
 * Literal =.. [functor, list of terms, list of annots]
 * 
 * Example: X =.. [~p, [t1, t2], [a1,a2]]
 *          X is ~p(t1,t2)[a1,a2]
 *          
 *          ~p(t1,t2)[a1,a2] =.. X
 *          X is [~p, [t1, t2], [a1,a2]]
 * 
 * @author jomi
 */
public class literalBuilder implements InternalAction {
	/**
	 * arg[0] is the Literal
	 * arg[1] is the List
	 */
    public boolean execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
    	try {
    		Literal p = (Literal)args[0];
    		ListTerm l = (ListTerm)args[1];
    		
    		// both are not vars, using normal unification
    		if (!args[0].isVar() && !args[1].isVar()) {
    			return un.unifies((Term)p.getAsListOfTerms(), (Term)l);
    		}
    		
    		// first is var, second is list, var is assigned to l tranformed in literal
    		if (args[0].isVar() && args[1].isList()) {
    			return un.unifies(p, Literal.newFromListOfTerms(l));
    		}
    		
    		// first is literal, second is var, var is assigned to l tranformed in list
    		if (args[0].isLiteral() && args[1].isVar()) {
    			return un.unifies((Term)p.getAsListOfTerms(), (Term)l);
    		}
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new JasonException("The internal action 'literalBuilder' has not received two arguments (a Literal and a List)");
        } catch (ClassCastException e) {
            throw new JasonException("The internal action 'literalBuilder' arguments are not Literal and List.");
    	}
    	return false;
    }
}
