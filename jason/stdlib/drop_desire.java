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
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.Event;
import jason.asSemantics.Intention;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Literal;
import jason.asSyntax.Term;
import jason.asSyntax.Trigger;


/**
  This changes the agent's circumstance. Currently what it does is simply
  to change all +!l to -!l in events which would give true for Des(l) in
  the whole set of events. IMPORTANT: unlike Des() this only alters
  literals explicitly desired (rather than intended), that is, it does NOT
  consider intentions. You should use both dropDes() AND dropInt() to
  remove all desires and intentions of l.

  @see jason.stdlib.current_intention
  @see jason.stdlib.desire
  @see jason.stdlib.drop_all_desires
  @see jason.stdlib.drop_all_intentions
  @see jason.stdlib.drop_intention
  @see jason.stdlib.drop_goal
  @see jason.stdlib.intend


 */
public class drop_desire extends DefaultInternalAction {
    
    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        try {
            Literal l = (Literal)args[0]; //Literal.parseLiteral(args[0].toString());
            un.apply(l);
            
            Event e = new Event(new Trigger(Trigger.TEAdd, Trigger.TEAchvG, l),Intention.EmptyInt);
            for (Event ei : ts.getC().getEvents()) {
                Trigger t = (Trigger) ei.getTrigger();
                if (ei.getIntention() != Intention.EmptyInt) {
                    t = (Trigger) t.clone();
                    ei.getIntention().peek().getUnif().apply(t.getLiteral());
                }
                if (un.unifies(t, e.getTrigger())) {
                    t.setTrigType(Trigger.TEDel); // Just changing "+!g" to "-!g"
                }
            }
            return true;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new JasonException("The internal action 'drop_desire' has not received one argument.");
        } catch (Exception e) {
            throw new JasonException("Error in internal action 'drop_desire': " + e);
        }
    }
}
