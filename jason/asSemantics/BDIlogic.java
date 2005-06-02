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
// http://www.csc.liv.ac.uk/~bordini
// http://www.inf.furb.br/~jomi
//----------------------------------------------------------------------------


package jason.asSemantics;


import jason.D;
import jason.asSyntax.Literal;
import jason.asSyntax.Trigger;

import java.util.Iterator;

public final class BDIlogic {

	// TODO: maybe these methods could be moved to TS class (or Agent class), since they use TS...
	
    public static final boolean Bel(TransitionSystem ts, Literal l) {
        return ts.ag.believes(l,null)!=null;
    }

    /** 
     * Verifies if <i>l</i> is a desire. l is a desire either 
     * if there is an event with l as trigger event or
     * it is an Intention. 
     */
    public static final boolean Des(TransitionSystem ts, Literal l) {
    	Trigger teFromL = new Trigger(D.TEAdd,D.TEAchvG,l);
        for(Iterator i=ts.C.E.iterator(); i.hasNext(); ) {
            Event ei = (Event)i.next();
            Trigger t = (Trigger)ei.trigger;
            if (ei.intention!=D.EmptyInt) {
                t = (Trigger)t.clone();
                ((IntendedMeans)ei.intention.peek()).unif.apply(t);
            }
            if(new Unifier().unifies(t,teFromL)) {
                return true;
            }
        }
        return Int(ts,l); // Int subset Des (see formal definitions)
    }

    public static final boolean Int(TransitionSystem ts, Literal l) {
        Trigger g = new Trigger(D.TEAdd,D.TEAchvG,l);
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

    public static final void dropDes(TransitionSystem ts, Literal l) {
        Event e = new Event(new Trigger(D.TEAdd,D.TEAchvG,l),D.EmptyInt);
        for(Iterator i=ts.C.E.iterator(); i.hasNext(); ) {
            Event ei = (Event)i.next();
            Trigger t = (Trigger)ei.trigger;
            if (ei.intention!=D.EmptyInt) {
                t = (Trigger)t.clone();
                ((IntendedMeans)ei.intention.peek()).unif.apply(t);
            }
            if(new Unifier().unifies(t,e.trigger)) {
                t.setTrigType(D.TEDel); // Just changing "+!g" to "-!g" !!!
            }
        }
    }

    public static final void dropInt(TransitionSystem ts, Literal l) {
        Trigger g = new Trigger(D.TEAdd,D.TEAchvG,l);
        for(Iterator j=ts.C.I.iterator(); j.hasNext(); ) {
        	Intention i = (Intention) j.next();
            if (i.hasTrigger(g)) {
                Trigger ng = (Trigger) g.clone();
                ng.setTrigType(D.TEDel);
                ts.C.E.add(new Event(ng,i));
                j.remove();
            }
        }
        // intention may be suspended in E
        for(Iterator j=ts.C.E.iterator(); j.hasNext(); ) {
        	Intention i = ((Event)j.next()).intention;
            if (i.hasTrigger(g)) {
                Trigger ng = (Trigger) g.clone();
                ng.setTrigType(D.TEDel);
                ts.C.E.add(new Event(ng,i));
                j.remove();
            }
        }
        // intention may be suspended in PA! (in the new semantics)
        if (ts.C.PA!=null) {
            for(Iterator j=ts.C.PA.values().iterator(); j.hasNext(); ) {
            	Intention i = ((ActionExec)j.next()).getIntention();
		// CAREFUL: The semantics for this isn't well defined yet.
		// The goal deletion on top of the intention will not get to know
		// the result of the action, as it is removed from the PA set!
                if (i.hasTrigger(g)) {
                    Trigger ng = (Trigger) g.clone();
                    ng.setTrigType(D.TEDel);
                    ts.C.E.add(new Event(ng,i));
                    j.remove();
                }
            }
        }
    }

    public static final void dropAllDes(TransitionSystem ts) {
        ts.C.E.clear();
    }

    public static final void dropAllInt(TransitionSystem ts) {
        ts.C.I.clear();
    }

}
