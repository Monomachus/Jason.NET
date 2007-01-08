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

import jason.asSemantics.Circumstance;
import jason.asSemantics.CircumstanceListener;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.Event;
import jason.asSemantics.Intention;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.StringTerm;
import jason.asSyntax.Term;
import jason.asSyntax.Trigger;

import java.util.logging.Level;

/**
  <p>Internal action: <b><code>.wait(<i>E</i>,<i>T</i>)</code></b>.
  
  <p>Description: suspend the intention for the time specified by <i>T</i> (in
  milliseconds) or until some event <i>E</i> happens. The events are
  strings in AgentSpeak syntax, e.g. <code>+bel(33)</code>,
  <code>+!go(X,Y)</code>.

  <p>Parameters:<ul>
  <li><i>+ event</i> (string): the waited event.<br/>
  <li>+ timeout (number).<br/>
  </ul>
  
  
  <p>Examples:<ul>
  <li> <code>.wait(1000)</code>: suspend the intention for 1 second.

  <li> <code>.wait("+b(1)")</code>: suspend the intention until the belief
  <code>b(1)</code> is added in the belief base.

  <li> <code>.wait("+!g", 2000)</code>: suspend the intention until the goal
  <code>g</code> is triggered or 2 seconds have passed, whatever happens
  first.

  </ul>

  @see jason.stdlib.at

 */
public class wait extends DefaultInternalAction {

    @Override
    public Object execute(final TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        long timeout = -1;
        Trigger te = null;
        try {
            if (args[0].isNumeric()) {
                // time in milliseconds
                NumberTerm time = (NumberTerm)args[0];
                time.apply(un);
                timeout = (long) time.solve();
                // Thread.sleep((long) time.solve());
            } else if (args[0].isString()) {
                // wait for event
                StringTerm st = (StringTerm) args[0];
                st.apply(un);
                te = Trigger.parseTrigger(st.getString());

                if (args.length == 2) {
                    NumberTerm tot = (NumberTerm) args[1];
                    tot.apply(un);
                    timeout = (long) tot.solve();
                }

            }
        } catch (Exception e) {
            ts.getLogger().log(Level.SEVERE, "Error at .wait.", e);
            return false;
        }
        WaitEvent wet = new WaitEvent(te, un, ts, timeout);
        wet.start();
        return true;
    }    

    @Override
    public boolean suspendIntention() {
        return true;
    }  

    class WaitEvent extends Thread implements CircumstanceListener {
        Trigger          te;
        String           sTE; // a string version of TE
        Unifier          un;
        Intention        si;
        TransitionSystem ts;
        Circumstance     c;
        boolean          ok      = false;
        boolean          drop    = false;
        long             timeout = -1;
        
        WaitEvent(Trigger te, Unifier un, TransitionSystem ts, long to) {
            this.te = te;
            this.un = un;
            this.ts = ts;
            c = ts.getC();
            si = c.getSelectedIntention();
            this.timeout = to;

            // register listener
            c.addEventListener(this);
            
            if (te != null) {
                sTE = te.toString();
            } else {
                sTE = "time"+(timeout);
            }
            sTE = si.getId()+"/"+sTE;
            c.getPendingIntentions().put(sTE, si);
        }

        public void run() {
            try {
                waitEvent();

                // unregister (for not to receive intentionAdded again)
                c.removeEventListener(this);

                // add SI again in C.I if it was not removed and this 
                // wait was not dropped
                if (c.getPendingIntentions().remove(sTE) == si && !c.getIntentions().contains(si) && !drop) {
                    si.peek().removeCurrentStep();
                    c.addIntention(si);
                }

            } catch (Exception e) {
                ts.getLogger().log(Level.SEVERE, "Error at .wait thread", e);
            }
        }

        synchronized public void waitEvent() {
            long init = System.currentTimeMillis();
            long pass = 0;
            while (!ok || drop) {
                try {
                    if (timeout == -1) {
                        wait();
                    } else {
                        long to = timeout - pass;
                        if (to <= 0)
                            to = 100;
                        wait(to);
                        pass = System.currentTimeMillis() - init;
                        if (pass >= timeout) {
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        synchronized public void eventAdded(Event e) {
            if (te != null && !drop && un.unifies(te, e.getTrigger())) {
                ok = true;
                notifyAll();
            }
        }

        synchronized public void intentionDropped(Intention i) {
            if (i.equals(si)) {
                ok = false;
                drop = true;
                notifyAll();
            }
        }

        public void intentionAdded(Intention i) {  
        }
        
        public String toString() {
            return sTE;
        }

    }
}
