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
import jason.asSemantics.ActionExec;
import jason.asSemantics.Circumstance;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.Event;
import jason.asSemantics.Intention;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Literal;
import jason.asSyntax.Term;
import jason.asSyntax.Trigger;

/**
  This changes the agent's circumstance. It removes an intention from
  I, E, PI or PA.  (no event is generated)

  @see jason.stdlib.current_intention
  @see jason.stdlib.desire
  @see jason.stdlib.drop_all_desires
  @see jason.stdlib.drop_desire
  @see jason.stdlib.drop_all_intentions
  @see jason.stdlib.drop_goal
  @see jason.stdlib.intend

 */
public class drop_intention extends DefaultInternalAction {
    
    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        try {
            Literal l = Literal.parseLiteral(args[0].toString());
            un.apply(l);
            dropInt(ts.getC(),l,un);
            return true;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new JasonException("The internal action 'drop_intention' has not received one argument.");
        } catch (Exception e) {
            throw new JasonException("Error in internal action 'drop_intention': " + e);
        }
    }
    
    public void dropInt(Circumstance C, Literal l, Unifier un) {
        Trigger g = new Trigger(Trigger.TEAdd, Trigger.TEAchvG, l);

        for (Intention i: C.getIntentions()) {
            if (i.hasTrigger(g, un)) {
                C.removeIntention(i);
            }
        }
        
        // intention may be suspended in E
        for (Event e: C.getEvents()) {
            Intention i = e.getIntention();
            if (i != null && i.hasTrigger(g, un)) {
                C.removeEvent(e);
            }
        }
        
        // intention may be suspended in PA! (in the new semantics)
        for (ActionExec a: C.getPendingActions().values()) {
            Intention i = a.getIntention();
            if (i.hasTrigger(g, un)) {
                C.dropPendingAction(i);
            }
        }

        // intention may be suspended in PI! (in the new semantics)
        for (Intention i: C.getPendingIntentions().values()) {
            if (i != null && i.hasTrigger(g, un)) {
                C.dropPendingIntention(i);
            }
        }
        
    }
}
