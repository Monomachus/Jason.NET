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
import jason.asSyntax.Plan;
import jason.asSyntax.StringTermImpl;
import jason.asSyntax.Term;

/**

  @see jason.stdlib.addPlan
  @see jason.stdlib.relevantPlans
  @see jason.stdlib.removePlan

 */
public class planLabel extends DefaultInternalAction {

    /**
     * args[0] = -plan as string,
     * args[1] = +label as term
     */
    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        try {
            Term label = (Term)args[1].clone();
            un.apply(label);
            Plan p = ts.getAg().getPL().get(label.toString());
            return un.unifies(new StringTermImpl(p.toASString()), args[0]);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new JasonException("The internal action 'planLabel' has not received two arguments!");
        } catch (Exception e) {
            throw new JasonException("Error in internal action 'planLabel': " + e);
        }
    }
}
