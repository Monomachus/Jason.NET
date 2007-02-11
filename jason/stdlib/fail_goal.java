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
//----------------------------------------------------------------------------


package jason.stdlib;

import jason.JasonException;
import jason.asSemantics.Event;
import jason.asSemantics.Intention;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Literal;
import jason.asSyntax.Term;
import jason.asSyntax.Trigger;

/**
  <p>Internal action:
  <b><code>.fail_goal(<i>G</i>,<i>R</i>)</code></b>.
  
  <p>Description: removes the goal <i>G</i> of the agent. <i>G</i> is a goal
  if there is a trigerring event <code>+!G</code> in any plan within an
  intention; just note that intentions can be suspended hence appearing
  in E, PA, or PI as well.

  <p>The intention is updated as if the plan for that goal
  had failed, and an event <code>-!G</code> is generated.

  <p>Example:<ul> 

  <li> <code>.fail_goal(go(1,3))</code>: stops any attempt to achieve
  <code>!go(1,3)</code> as if it had failed.

  </ul>

  (Note: this internal action was introduced in a DALT 2006 paper)

  @see jason.stdlib.current_intention
  @see jason.stdlib.desire
  @see jason.stdlib.drop_all_desires
  @see jason.stdlib.drop_desire
  @see jason.stdlib.drop_all_intentions
  @see jason.stdlib.drop_intention
  @see jason.stdlib.intend
  @see jason.stdlib.succeed_goal
  
 */
public class fail_goal extends succeed_goal {
    
    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        try {
        	args[0].apply(un);

            drop(ts, (Literal)args[0], un);
            return true;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new JasonException("The internal action 'fail_goal' has not received one argument.");
        } catch (Exception e) {
            e.printStackTrace();
            throw new JasonException("Error in internal action 'fail_goal': " + e);
        }
    }
    
    @Override
    int dropIntention(Intention i, Trigger g, TransitionSystem ts, Unifier un) throws JasonException {
        if (i != null && i.dropGoal(g, un)) {
            // generate failure event
            Event failEvent = null;
            if (!i.isFinished()) {
                failEvent = ts.findEventForFailure(i, i.peek().getTrigger());
            }
            if (failEvent != null) {
                ts.getC().addEvent(failEvent);
                ts.getLogger().warning("'.fail_goal' is generating a goal deletion event: " + failEvent.getTrigger());
                return 2;
            } else {
                ts.getLogger().warning("'.fail_goal' is removing an intention:\n" + i);
                return 3;
            }
        }
        return 0;        
    }
}
