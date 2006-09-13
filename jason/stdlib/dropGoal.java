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
import jason.asSemantics.ActionExec;
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
        Circumstance C = ts.getC();
        
        for (Intention i: C.getIntentions()) {
            if (dropIntention(i, g, success, ts, un)) {
                C.removeIntention(i);
            }
        }

        // dropping the current intention?
        if (dropIntention(C.getSelectedIntention(), g, success, ts, un)) {
        }
            
        // dropping G in Events
        for (Event e: C.getEvents()) {
            Intention i = e.getIntention();
            if (i != null && dropIntention(i, g, success, ts, un)) {
                C.removeEvent(e);
            }
        }
        
        // dropping from Pending Actions
        for (ActionExec a: C.getPendingActions().values()) {
            Intention i = a.getIntention();
            if (i != null && dropIntention(i, g, success, ts, un)) {
                C.removePendingAction(i);
            }
        }
        
        // dropping from Pending Intentions
        for (Intention i: C.getPendingIntentions().values()) {
            if (i != null && dropIntention(i, g, success, ts, un)) {
                C.removePendingIntention(i);
            }
        }
    }
    
    private boolean dropIntention(Intention i, Trigger g, boolean success, TransitionSystem ts, Unifier un) throws JasonException {
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
                    return true;
                }
            }
        }
        return false;        
    }
}
