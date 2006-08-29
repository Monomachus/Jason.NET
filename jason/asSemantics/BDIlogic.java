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

package jason.asSemantics;

import jason.asSyntax.Literal;
import jason.asSyntax.Trigger;

public final class BDIlogic {

    /**
     * Checks if <i>l</i> is a desire: <i>l</i> is a desire either if there is
     * an event with +!l as triggering event or it is an Intention.
     */
    public static final boolean Des(TransitionSystem ts, Literal l, Unifier un) {
        Trigger teFromL = new Trigger(Trigger.TEAdd, Trigger.TEAchvG, l);
        // logger.log(Level.SEVERE,"Des: "+l+un+ts.C.E+ts.C.I);

        // need to check the slected event in this cycle!!! (already removed
        // from E)
        if (ts.C.SE != null) {
            Trigger t = ts.C.SE.trigger;
            if (ts.C.SE.intention != Intention.EmptyInt) {
                t = (Trigger) t.clone();
                ts.C.SE.intention.peek().unif.apply(t.getLiteral());
            }
            // logger.log(Level.SEVERE,"Des: "+t+" unif "+teFromL);
            if (un.unifies(t, teFromL)) {
                return true;
            }
        }

        for (Event ei : ts.C.getEvents()) {
            Trigger t = (Trigger) ei.trigger;
            if (ei.intention != Intention.EmptyInt) {
                t = (Trigger) t.clone();
                ei.intention.peek().unif.apply(t.getLiteral());
            }
            // logger.log(Level.SEVERE,"Des: "+t+" unif "+teFromL);
            if (un.unifies(t, teFromL)) {
                return true;
            }
        }

        return Int(ts, l, un); // Int subset Des (see formal definitions)
    }

    /**
     * Checks if <i>l</i> is an intention: <i>l</i> is an intention if there
     * is a trigerring event +!l in any plan within an intention; just note that
     * intentions can be suspended and appear in E or PA as well.
     */
    public static final boolean Int(TransitionSystem ts, Literal l, Unifier un) {
        Trigger g = new Trigger(Trigger.TEAdd, Trigger.TEAchvG, l);
        // logger.log(Level.SEVERE,"Entering Int: "+ts.C.I);

        // need to check the intention in the slected event in this cycle!!!
        // (already removed from E)
        if (ts.C.SE != null) {
            // logger.log(Level.SEVERE,"Int: "+g+" unif "+ts.C.SE);
            if (ts.C.SE.intention != null)
                if (ts.C.SE.intention.hasTrigger(g, un))
                    return true;
        }

        // need to check the slected intention in this cycle!!!
        if (ts.C.SI != null) {
            // logger.log(Level.SEVERE,"Int: "+g+" unif "+ts.C.SI);
            if (ts.C.SI.hasTrigger(g, un))
                return true;
        }

        // intention may be suspended in E
        for (Event evt : ts.C.getEvents()) {
            if (evt.intention != null && evt.intention.hasTrigger(g, un))
                return true;
        }

        // intention may be suspended in PA! (in the new semantics)
        if (ts.C.hasPendingAction()) {
            for (ActionExec ac: ts.C.getPendingActions().values()) {
                Intention intention = ac.getIntention();
                if (intention.hasTrigger(g, un))
                    return true;
            }
        }
        // intention may be suspended in PI! (in the new semantics)
        if (ts.C.hasPendingIntention()) {
            for (Intention intention: ts.C.getPendingIntentions().values()) {
                if (intention.hasTrigger(g, un))
                    return true;
            }
        }

        for (Intention i : ts.C.getIntentions()) {
            if (i.hasTrigger(g, un))
                return true;
        }

        return false;
    }
    
}
