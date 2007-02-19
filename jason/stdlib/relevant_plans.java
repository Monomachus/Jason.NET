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
import jason.asSemantics.Option;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.ListTerm;
import jason.asSyntax.ListTermImpl;
import jason.asSyntax.Plan;
import jason.asSyntax.StringTerm;
import jason.asSyntax.StringTermImpl;
import jason.asSyntax.Term;
import jason.asSyntax.Trigger;

import java.util.List;

/**
  <p>Internal action: <b><code>.relevant_plans</code></b>.
  
  <p>Description: gets all relevant plans for some triggering event. This
  internal action is used, for example, to answer "askHow" messages.

  <p>Parameters:<ul>
  
  <li>+ trigger (string): the string representing the triggering event.</li>
  
  <li>- plans (list): the list of strings with the code of the relevant
  plans.</li>
  
  </ul>
  
  <p>Example:<ul> 

  <li> <code>.relevant_plans("+!go(X,Y)",L)</code>: unifies L with a list of
  all plans in the agent's plan library that are relevant for the triggering
  event <code>+!go(X,Y)</code>.</li>

  </ul>

  @see jason.stdlib.add_plan
  @see jason.stdlib.plan_label
  @see jason.stdlib.remove_plan


 */
public class relevant_plans extends DefaultInternalAction {

    @Override
	public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
		try {
			StringTerm sTe = (StringTerm) args[0];
			Trigger te = Trigger.parseTrigger(sTe.getString());
			if (te == null) {
				throw new JasonException("The first argument of the 'relevant_plans' internal action is not a triggering event.");
			}
			ListTerm lt = new ListTermImpl();
            List<Option> rp = ts.relevantPlans(te);
            if (rp != null) {
                for (Option opt: rp) {
                    // remove sources (this IA is used for communication)
                    Plan np = (Plan)opt.getPlan().clone();
                    if (np.getLabel() != null) {
                        np.getLabel().delSources();
                    }
                    StringTerm stplan = new StringTermImpl(np.toASString().replaceAll("\\\"", "\\\\\""));
                    lt.add(stplan);
                }
			}

			// second arg is a var
			Term listVar = args[1];

			return un.unifies(lt, listVar);
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new JasonException("The internal action 'relevant_plans' has not received two arguments (TE and VAR).");
		} catch (Exception e) {
			throw new JasonException("Error in internal action 'relevant_plans': " + e, e);
		}
	}
}
