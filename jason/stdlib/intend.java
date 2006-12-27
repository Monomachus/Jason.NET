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
  @see jason.stdlib.current_intention
  @see jason.stdlib.desire
  @see jason.stdlib.drop_all_desires
  @see jason.stdlib.drop_desire
  @see jason.stdlib.drop_all_intentions
  @see jason.stdlib.drop_intention
  @see jason.stdlib.drop_goal
 */
public class intend extends DefaultInternalAction {
    
    @Override
	public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        try {
    	    Literal l = (Literal)args[0];
    	    un.apply(l);
    	    return intends(ts.getC(),l,un);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new JasonException("The internal action 'intend' has not received one argument.");
        } catch (Exception e) {
            throw new JasonException("Error in internal action 'intend': " + e);
        }
    }
    
    /**
     * Checks if <i>l</i> is an intention: <i>l</i> is an intention if there
     * is a trigerring event +!l in any plan within an intention; just note that
     * intentions can be suspended and appear in E or PA as well.
     */
    public boolean intends(Circumstance C, Literal l, Unifier un) {
        Trigger g = new Trigger(Trigger.TEAdd, Trigger.TEAchvG, l);

        // need to check the intention in the slected event in this cycle!!!
        // (already removed from E)
        if (C.getSelectedEvent() != null) {
            // logger.log(Level.SEVERE,"Int: "+g+" unif "+ts.C.SE);
            if (C.getSelectedEvent().getIntention() != null)
                if (C.getSelectedEvent().getIntention().hasTrigger(g, un))
                    return true;
        }

        // need to check the slected intention in this cycle!!!
        if (C.getSelectedIntention() != null) {
            // logger.log(Level.SEVERE,"Int: "+g+" unif "+ts.C.SI);
            if (C.getSelectedIntention().hasTrigger(g, un))
                return true;
        }

        // intention may be suspended in E
        for (Event evt : C.getEvents()) {
            if (evt.getIntention() != null && evt.getIntention().hasTrigger(g, un))
                return true;
        }

        // intention may be suspended in PA! (in the new semantics)
        if (C.hasPendingAction()) {
            for (ActionExec ac: C.getPendingActions().values()) {
                if (ac.getIntention().hasTrigger(g, un))
                    return true;
            }
        }

        // intention may be suspended in PI! (in the new semantics)
        if (C.hasPendingIntention()) {
            for (Intention intention: C.getPendingIntentions().values()) {
                if (intention.hasTrigger(g, un))
                    return true;
            }
        }

        for (Intention i : C.getIntentions()) {
            if (i.hasTrigger(g, un))
                return true;
        }

        return false;
    }
    
}
