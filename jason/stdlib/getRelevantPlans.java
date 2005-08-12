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
//   Revision 1.6  2005/08/12 22:20:10  jomifred
//   add cvs keywords
//
//
//----------------------------------------------------------------------------

package jason.stdlib;

import jason.JasonException;
import jason.asSemantics.InternalAction;
import jason.asSemantics.Option;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.ListTerm;
import jason.asSyntax.Plan;
import jason.asSyntax.StringTerm;
import jason.asSyntax.Term;
import jason.asSyntax.Trigger;

import java.util.Iterator;

public class getRelevantPlans implements InternalAction {

	/**
	 * args[0] = trigger event (as a StringTerm)
	 * args[1] = variable or list (Term) that will be unified with a ListTerm
	 *           that contains all plans (as StringTerms)
	 *           (the splans'sources in this list are empty)
	 */
	public boolean execute(TransitionSystem ts, Unifier un, Term[] args)
			throws Exception {
		try {
			StringTerm sTe = (StringTerm) args[0];
			Trigger te = Trigger.parseTrigger(sTe.getFunctor());
			if (te == null) {
				throw new JasonException("The first argument is not a TE (getRelevantPlans internal action)");
			}
			ListTerm lt = new ListTerm();
			Iterator i = ts.relevantPlans(te).iterator();
			while (i.hasNext()) {
				Option opt = (Option) i.next();
				// remove sources (this IA is used for communication)
				Plan np = (Plan)opt.getPlan().clone();
				if (np.getLabel() != null) {
					np.getLabel().delSources();
				}
				StringTerm stplan = new StringTerm(np.toASString().replaceAll("\\\"", "\\\\\""));
				lt.add(stplan);
			}

			// second arg is a var
			Term listVar = args[1];
			//if (listVar == null || !listVar.isVar()) {
			//	throw new JasonException("The second argument '" + args[1]
			//			+ "' is not a VAR (getRelevantPlans internal action)");
			//}

			// un.unifies(new Term(sPlanList.toString()), listVar);
			return un.unifies(lt, listVar);
			// System.out.println("*** un = "+un);
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new JasonException("The internal action 'getRelevantPlans' has not received two arguments (TE and a VAR)");
		} catch (Exception e) {
			throw new JasonException("Error in internal action 'getRelevantPlans': " + e);
		}
	}

}
