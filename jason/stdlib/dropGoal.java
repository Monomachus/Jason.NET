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
import jason.asSemantics.Event;
import jason.asSemantics.Intention;
import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Literal;
import jason.asSyntax.Term;
import jason.asSyntax.Trigger;

/**
 * Drop a goal (see DALT 2006 paper)
 */
public class dropGoal implements InternalAction {
    
    public boolean execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        try {
            Literal l = (Literal)args[0].clone();
            un.apply(l);
            Term success = (Term)args[1].clone();
            un.apply(success);
            drop(ts, l, success.equals(Literal.LTrue), un);
            return true;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new JasonException("The internal action 'dropGoal' has not received two arguments.");
        } catch (Exception e) {
            throw new JasonException("Error in internal action 'dropGoal': " + e);
        }
    }
    
    public void drop(TransitionSystem ts, Literal l, boolean success, Unifier un) throws JasonException{
        Trigger g = new Trigger(Trigger.TEAdd, Trigger.TEAchvG, l);

        for (Intention i: ts.getC().getIntentions()) {
            if (i.dropGoal(g, un)) {
                if (success) {
                    // continue the intention
                    i.peek().removeCurrentStep();
                    ts.applyClrInt(i);
                } else {
                    // generate fail
                    Event failEvent = ts.findEventForFailure(i, i.peek().getTrigger());
                    if (failEvent != null) {
                        ts.getC().addEvent(failEvent);
                        ts.getLogger().warning(".dropGoal is generating goal deletion event " + failEvent.getTrigger());
                    } else {
                        ts.getLogger().warning(".dropGoal is removing intention\n" + i);
                        ts.getC().removeIntention(i);
                    }
                }
            }
        }

        // may be the current intention
        
        /*
        // intention may be suspended in E
        for (Iterator<Event>ie = E.iterator(); ie.hasNext();) {
            Intention i = ie.next().intention;
            if (i != null && i.hasTrigger(g, un)) {
                ie.remove();
            }
        }
        
        // intention may be suspended in PA! (in the new semantics)
        if (hasPendingAction()) {
            Iterator<ActionExec> ipa = getPendingActions().values().iterator();
            while (ipa.hasNext()) {
                Intention i = ipa.next().getIntention();
                // CAREFUL: The semantics for this isn't well defined yet.
                // The goal deletion on top of the intention will not get to
                // know the result of the action, as it is removed from the PA set!
                // If left in PA, the action won't be the the top of
                // the stack (that might cause problems?)
                if (i.hasTrigger(g, un)) {
                    ipa.remove();
                }
            }
        }
        
        // intention may be suspended in PI! (in the new semantics)
        if (hasPendingIntention()) {
            Iterator<Intention> ipi = getPendingIntentions().values().iterator();
            while (ipi.hasNext()) {
                Intention i = ipi.next(); 
                if (i.hasTrigger(g, un)) {
                    ipi.remove();
                }
                
                // check in wait internal action
                for (CircumstanceListener el : listeners) {
                    el.intentionDropped(i);
                }
            }
        }
        */
    }        
}
