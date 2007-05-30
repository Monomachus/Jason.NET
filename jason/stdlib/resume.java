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
import jason.asSemantics.Circumstance;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.Intention;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Literal;
import jason.asSyntax.Term;
import jason.asSyntax.Trigger;

import java.util.Iterator;

/**
  <p>Internal action:
  <b><code>.fail_goal(<i>G</i>)</code></b>.
  
  <p>Description: aborts goals <i>G</i> in the agent circumstance as if a plan
  for such goal had failed. Assuming that one of the plans requiring <i>G</i> was 
  <code>G0 &lt;- !G; ...</code>, an event <code>-!G0</code> is generated. In
  case <i>G</i> was triggered by <code>!!G</code> (and therefore it was
  not a subgoal, as happens, for instance, when an "achieve" performative is used),
  the generated event is <code>-!G</code>.  A literal <i>G</i>
  is a goal if there is a trigerring event <code>+!G</code> in any plan within
  any intention; also note that intentions can be suspended hence appearing
  in sets E, PA, or PI of the agent's circumstance as well.

  <p>Example:<ul> 

  <li> <code>.fail_goal(go(1,3))</code>: aborts any attempt to achieve
  goals such as <code>!go(1,3)</code> as if a plan for it had failed. Assuming that
  it is a subgoal in the plan <code>get_gold(X,Y) &lt;- go(X,Y); pick.</code>, the
  generated event is <code>-!get_gold(1,3)</code>.

  </ul>

  (Note: this internal action was introduced in a DALT 2006 paper, where it was called .dropGoal(G,false).)

  @see jason.stdlib.current_intention
  @see jason.stdlib.desire
  @see jason.stdlib.drop_all_desires
  @see jason.stdlib.drop_desire
  @see jason.stdlib.drop_all_events
  @see jason.stdlib.drop_all_intentions
  @see jason.stdlib.drop_intention
  @see jason.stdlib.intend
  @see jason.stdlib.succeed_goal
  @see jason.stdlib.fail_goal
  
 */
public class resume extends DefaultInternalAction {
    
    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        try {
            Trigger      g = new Trigger(Trigger.TEAdd, Trigger.TEAchvG, (Literal)args[0]);
            Circumstance C = ts.getC();
            
            Iterator<String> ik = C.getPendingIntentions().keySet().iterator();
            while (ik.hasNext()) {
            	String k = ik.next();
            	if (k.startsWith(suspend.SUSPENDED_INT)) {
            		Intention i = C.getPendingIntentions().get(k);
	            	if (i.hasTrigger(g, un)) {
	                	C.addIntention(i);
	                	i.setSuspended(false);
	            		ik.remove();
	            	}
            	}
            }
            return true;

        } catch (ArrayIndexOutOfBoundsException e) {
            throw new JasonException("The internal action 'resume' has not received one argument.");
        } catch (Exception e) {
            e.printStackTrace();
            throw new JasonException("Error in internal action 'resume': " + e, e);
        }
    }        
}
