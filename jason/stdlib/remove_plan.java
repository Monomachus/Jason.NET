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
import jason.asSyntax.Pred;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;

/**
  @see jason.stdlib.add_plan
  @see jason.stdlib.plan_label
  @see jason.stdlib.relevant_plans
 */
public class remove_plan extends DefaultInternalAction {

	/**
	 * args[0] = list of plans (a ListTerm) where each element is a plan label 
	 *                 that represent a plan to be removed
	 *           or only one plan's label
	 * args[1] = source (the name of the agent, for instance),
	 *           if not informed, source is "self"
	 *           
	 * Example: .remove_lan(l1);
	 *          .remove_plan(X); // X is unified with a plan's label 
	 *          .remove_plan([l1,l2,l3]);
	 *          .remove_plan(l1,ag1); // remove the plan l1 sent (tellHow) by agent ag1 
	 */
    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        try {
            Term label = (Term)args[0].clone();
        	un.apply(label);

            Structure source = new Structure("self");
        	if (args.length > 1) {
        		source = (Structure)args[1].clone();
        		un.apply(source);
        	}
        	
        	if (label.isList()) { // if arg[0] is a list
        		boolean r = true;
        		ListTerm lt = (ListTerm)args[0];
                for (Term t: lt) {
        			r = r && ts.getAg().getPL().removePlan((Pred)t, source);
        		}
        		return r;
        	} else { // args[0] is a plan's label
        		return ts.getAg().getPL().removePlan((Pred)label, source);
        	}
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new JasonException("The internal action 'remove_plan' has not received the plan's label as argument.");
        } 
    }
}
