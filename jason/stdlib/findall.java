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
import jason.asSyntax.ParseList;
import jason.asSyntax.Term;

import java.util.List;

public class findall {
    
	/** .findall(Var, a(Var), ListVar) */
    public static boolean execute(TransitionSystem ts, Unifier un, String[] args) throws Exception {
        try {
        	Term var = Term.parse(args[0]);
            if (var == null || !var.isVar()) {
                throw new JasonException("The first parameter of the internal action 'findAll' is not a Variable!");            	
            }
        	Literal bel = Literal.parseLiteral(args[1]);
            if (bel == null) {
                throw new JasonException("The second parameter of the internal action 'findAll' is not a literal!");            	
            }
            un.apply(bel);
            
        	Term list = Term.parse(args[2]);
            if (list == null || !list.isVar()) {
                throw new JasonException("The third parameter of the internal action 'findAll' is not a Variable!");            	
            }
        	
        	// find all bel in belief base and build a list with them
            StringBuffer all = new StringBuffer();
    		List relB = ts.getAg().getBS().getRelevant(bel);
    		if (relB != null) {
    			for (int i=0; i < relB.size(); i++) {
    				Literal b = (Literal) relB.get(i);
    				Unifier newUn = (un == null) ? new Unifier() : (Unifier)un.clone();
    				// recall that order is important because of annotations!
    				if (newUn.unifies(bel,b)) {
    					// get the val value and add it in the list
    					Term vl = newUn.get(var.toString());
    					if (vl != null) {
    						if (all.length() > 0) {
    							all.append(",");
    						}
    						all.append(vl.toString());
    					}
    				}
    			}
    		}
            
    		un.unifies(list, new ParseList("["+all.toString()+"]").getList());
            return true;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new JasonException("The internal action 'findall' has not received three arguments");
        } catch (Exception e) {
            throw new JasonException("Error in internal action 'findall': "+e);
        }
    }
    
}
