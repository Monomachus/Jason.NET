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

import jason.JasonException;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Literal;
import jason.asSyntax.Pred;
import jason.asSyntax.Term;

public class addAnnot {
    
    public static boolean execute(TransitionSystem ts, Unifier un, String[] args) throws Exception {
        try {
            Term bel  = Literal.parse(args[0]);
            if (bel == null) {
                throw new JasonException("The first parameter of internal action 'addAnnot' is not a term!");            	
            }
            if (bel.isVar()) {
            	un.apply(bel);
            }
        	if (args[0].startsWith("[")) {
        		// TODO: add annot on all list members that are predicate!!!!
        		return true;
        	} else {
        		try {
        			// in case it is a predicate, add annot
        			Pred p = (Pred)bel;
                    p.addAnnot(Term.parse(args[1]));
        		} catch (Exception e) {
        			// no problem, the content is not a pred (is a number, string, ....) received in a message, for instance
        		}
        	}
            Literal result = Literal.parseLiteral(args[2]);
            if (result == null || !result.isVar()) {
                throw new JasonException("The third parameter of internal action 'addAnnot' is not a variable!");            	
            }
            un.unifies(bel, result);
            //System.out.println("result = "+result+"/"+un);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new JasonException("The internal action 'myName' has not received one argument");
        } 
        return true;
    }
    
}
