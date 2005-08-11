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


package jason.asSemantics;


import jason.asSyntax.Literal;
import jason.asSyntax.Trigger;

import java.util.Iterator;

public final class BDIlogic {

    public static final boolean Bel(TransitionSystem ts, Literal l) {
        return ts.ag.believes(l,null)!=null;
    }

    /** 
     * Checks if <i>l</i> is a desire: <i>l</i> is a desire either 
     * if there is an event with +!l as triggering event or it is an Intention. 
     */
    public static final boolean Des(TransitionSystem ts, Literal l) {
    	Trigger teFromL = new Trigger(Trigger.TEAdd,Trigger.TEAchvG,l);
        for(Iterator i=ts.C.E.iterator(); i.hasNext(); ) {
            Event ei = (Event)i.next();
            Trigger t = (Trigger)ei.trigger;
            if (ei.intention!=Intention.EmptyInt) {
                t = (Trigger)t.clone();
                ((IntendedMeans)ei.intention.peek()).unif.apply(t);
            }
            if(new Unifier().unifies(t,teFromL)) {
                return true;
            }
        }
        return Int(ts,l); // Int subset Des (see formal definitions)
    }

    /** 
     * Checks if <i>l</i> is an intention: <i>l</i> is an intention
     * if there is a trigerring event +!l in any plan within an intention;
     * just note that intentions can be suspended and appear in E or PA as well.
     */
    public static final boolean Int(TransitionSystem ts, Literal l) {
        Trigger g = new Trigger(Trigger.TEAdd,Trigger.TEAchvG,l);
        for(Iterator i=ts.C.I.iterator(); i.hasNext(); ) {
            if (((Intention)i.next()).hasTrigger(g))
                return true;
        }

        // intention may be suspended in E
        for(Iterator i=ts.C.E.iterator(); i.hasNext(); ) {
            if (((Event)i.next()).intention.hasTrigger(g))
                return true;
        }
        
        // intention may be suspended in PA! (in the new semantics)
        if (ts.C.PA!=null) {
            for(Iterator i=ts.C.PA.values().iterator(); i.hasNext(); ) {
                if (((ActionExec)i.next()).getIntention().hasTrigger(g))
                    return true;
            }
        }
        return false;
    }

    // Changing the Agent's Circumstance!!!

    /**
     * This changes the agent's circumstance. Currently what it does
     * is simply to change all +!l to -!l in events which would give
     * true for Des(l) in the whole set of events.
     * IMPORTANT: unlike Des() this only alters literals explicitly
     * desired (rather than intended), that is, it does NOT consider
     * intentions. You should use both dropDes() AND dropInt() to
     * remove all desires and intentions of l.
     */
    public static final void dropDes(TransitionSystem ts, Literal l) {
        Event e = new Event(new Trigger(Trigger.TEAdd,Trigger.TEAchvG,l),Intention.EmptyInt);
        for(Iterator i=ts.C.E.iterator(); i.hasNext(); ) {
            Event ei = (Event)i.next();
            Trigger t = (Trigger)ei.trigger;
            if (ei.intention!=Intention.EmptyInt) {
                t = (Trigger)t.clone();
                ((IntendedMeans)ei.intention.peek()).unif.apply(t);
            }
            if(new Unifier().unifies(t,e.trigger)) {
                t.setTrigType(Trigger.TEDel); // Just changing "+!g" to "-!g" !!!
            }
        }
    }

    /**
     * This changes the agent's circumstance. It removes an intention
     * from I, E or PA and use that intention in a new event that is
     * added to E with triggering event -!l. This is EXPERIMENTAL, in
     * particular for intentions suspended in PA, this is bound to
     * create problems at the moment.
     */
    public static final void dropInt(TransitionSystem ts, Literal l) {
        Trigger g = new Trigger(Trigger.TEAdd,Trigger.TEAchvG,l);
        for(Iterator j=ts.C.I.iterator(); j.hasNext(); ) {
        	Intention i = (Intention) j.next();
            if (i.hasTrigger(g)) {
                Trigger ng = (Trigger) g.clone();
                ng.setTrigType(Trigger.TEDel);
                ts.C.E.add(new Event(ng,i));
                j.remove();
            }
        }
        // intention may be suspended in E
        for(Iterator j=ts.C.E.iterator(); j.hasNext(); ) {
        	Intention i = ((Event)j.next()).intention;
            if (i.hasTrigger(g)) {
                Trigger ng = (Trigger) g.clone();
                ng.setTrigType(Trigger.TEDel);
                ts.C.E.add(new Event(ng,i));
                j.remove();
            }
        }
        // intention may be suspended in PA! (in the new semantics)
        if (ts.C.PA!=null) {
            for(Iterator j=ts.C.PA.values().iterator(); j.hasNext(); ) {
            	Intention i = ((ActionExec)j.next()).getIntention();
		// TODO CAREFUL: The semantics for this isn't well defined yet.
		// The goal deletion on top of the intention will not get to know
		// the result of the action, as it is removed from the PA set!
       	// If left in PA, the action won't be the the top of
       	// the stack (that might cause problems?)
                if (i.hasTrigger(g)) {
                    Trigger ng = (Trigger) g.clone();
                    ng.setTrigType(Trigger.TEDel);
                    ts.C.E.add(new Event(ng,i));
                    j.remove();
                }
            }
        }
    }

    /**
     * This changes the agent's circumstance by simply emptying
     * the whole set of events (E). IMPORTANT: note that this
     * is different from droping one desires, in which case a
     * goal deletion event is generated.
     */
    public static final void dropAllDes(TransitionSystem ts) {
        ts.C.E.clear();
    }

    /**
     * This changes the agent's circumstance by simply emptying
     * the whole set of intentions (I). IMPORTANT: note that this
     * is different from droping one intention, in which case a
     * goal deletion event is generated.
     */
    public static final void dropAllInt(TransitionSystem ts) {
        ts.C.I.clear();
    }

}
