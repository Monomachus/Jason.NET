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
import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.ListTerm;
import jason.asSyntax.StringTerm;
import jason.asSyntax.Term;

import java.util.Iterator;

public class addPlan implements InternalAction {
    
	/**
	 * args[0] = plan or list of plans
	 * args[1] = source
	 */
    public boolean execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        try {
			Term source = args[1];
        	if (args[0].isList()) { // if arg[0] is a list
				ListTerm lt = (ListTerm)args[0];
        		Iterator i = lt.iterator();
        		while (i.hasNext()) {
					lt = (ListTerm)i.next();
            		ts.getAg().addPlan( (StringTerm)lt.getTerm(), source);
        		}
        	} else { // args[0] is a plan
        		ts.getAg().addPlan((StringTerm)args[0], source);
        	}
            return true;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new JasonException("The internal action 'addPlan' has not received two arguments (plan's string and source)");
        } catch (Exception e) {
            throw new JasonException("Error in internal action 'addPlan': "+e);
        }
    }
    
}
