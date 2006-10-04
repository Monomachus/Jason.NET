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
import jason.asSyntax.Term;
import jason.asSyntax.TermImpl;

import java.util.Iterator;

public class removePlan extends DefaultInternalAction {

	/**
	 * args[0] = list of plans (a ListTerm) where each element is a plan label 
	 *                 that represent a plan to be removed
	 *           or only one plan's label
	 * args[1] = source (the name of the agent, for instance),
	 *           if not informed, source is "self"
	 *           
	 * Example: .removePlan(l1);
	 *          .removePlan(X); // X is unified with a plan's label 
	 *          .removePlan([l1,l2,l3]);
	 *          .removePlan(l1,ag1); // remove the plan l1 sent (tellHow) by agent ag1 
	 */
    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        try {
        	Term label = (Term)args[0].clone();
        	un.apply(label);

        	Term source = new TermImpl("self");
        	if (args.length > 1) {
        		source = (Term)args[1].clone();
        		un.apply(source);
        	}
        	
        	if (label.isList()) { // if arg[0] is a list
        		boolean r = true;
        		ListTerm lt = (ListTerm)args[0];
        		Iterator i = lt.iterator();
        		while (i.hasNext()) {
        			label = (Term)i.next();
        			r = r && ts.getAg().getPL().removePlan(label, source);
        		}
        		return r;
        	} else { // args[0] is a plan's label
        		return ts.getAg().getPL().removePlan(label, source);
        	}
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new JasonException("The internal action 'removePlan' has not received the plan's label as argument.");
        } 
    }
}
