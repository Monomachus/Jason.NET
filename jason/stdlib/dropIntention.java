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

import java.util.Iterator;

import jason.JasonException;
import jason.asSemantics.ActionExec;
import jason.asSemantics.Circumstance;
import jason.asSemantics.CircumstanceListener;
import jason.asSemantics.Event;
import jason.asSemantics.Intention;
import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Literal;
import jason.asSyntax.Term;
import jason.asSyntax.Trigger;

/**
 * This changes the agent's circumstance. It removes an intention from I, E, PI or PA.
 * (no event is generated)
 */
public class dropIntention implements InternalAction {
    
    public boolean execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        try {
            Literal l = Literal.parseLiteral(args[0].toString());
            un.apply(l);
            dropInt(ts.getC(),l,un);
            return true;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new JasonException("The internal action 'dropIntention' has not received one argument.");
        } catch (Exception e) {
            throw new JasonException("Error in internal action 'dropIntention': " + e);
        }
    }
    
    public void dropInt(Circumstance C, Literal l, Unifier un) {
        Trigger g = new Trigger(Trigger.TEAdd, Trigger.TEAchvG, l);

        Iterator<Intention> j = C.getIntentions().iterator(); 
        while (j.hasNext()) {
            Intention i = j.next();
            if (i.hasTrigger(g, un)) {
                j.remove();
            }
        }

        // intention may be suspended in E
        for (Iterator<Event>ie = C.getEvents().iterator(); ie.hasNext();) {
            Intention i = ie.next().getIntention();
            if (i != null && i.hasTrigger(g, un)) {
                ie.remove();
            }
        }
        
        // intention may be suspended in PA! (in the new semantics)
        if (C.hasPendingAction()) {
            Iterator<ActionExec> ipa = C.getPendingActions().values().iterator();
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
        if (C.hasPendingIntention()) {
            Iterator<Intention> ipi = C.getPendingIntentions().values().iterator();
            while (ipi.hasNext()) {
                Intention i = ipi.next(); 
                if (i.hasTrigger(g, un)) {
                    ipi.remove();
                }
                
                // check in wait internal action
                for (CircumstanceListener el : C.getListeners()) {
                    el.intentionDropped(i);
                }
            }
        }
    }
}
