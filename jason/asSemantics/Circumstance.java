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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Circumstance implements Serializable {

	private static final long serialVersionUID = 1L;

	static Logger                      logger    = Logger.getLogger(Circumstance.class.getName());

    private Queue<Event>               E;
    private Queue<Intention>           I;
    protected ActionExec               A;
    protected Queue<Message>           MB;
    protected List<Option>             RP;
    protected List<Option>             AP;
    protected Event                    SE;
    protected Option                   SO;
    protected Intention                SI;
    private   Intention                AI; // Atomic Intention

    // protected Intention AI; 
    private Map<String, ActionExec>    PA;
    // pending intentions
    private Map<String, Intention>     PI;                                                        

    protected List<ActionExec>         FA;

    private List<CircumstanceListener> listeners = new CopyOnWriteArrayList<CircumstanceListener>();

    public Circumstance() {
        // use LinkedList since we use a lot of remove(0) in selectEvent
        E = new LinkedList<Event>();
        I = new ConcurrentLinkedQueue<Intention>();
        MB = new LinkedList<Message>();
        PA = new HashMap<String, ActionExec>();
        PI = new HashMap<String, Intention>();
        FA = new ArrayList<ActionExec>();
        reset();
    }

    public void reset() {
        A = null;
        RP = null;
        AP = null;
        SE = null;
        SO = null;
        SI = null;
    }

    public void addAchvGoal(Literal l, Intention i) {
        addEvent(new Event(new Trigger(Trigger.TEAdd, Trigger.TEAchvG, l), i));
    }

    public void addExternalEv(Trigger trig) {
        addEvent(new Event(trig, Intention.EmptyInt));
    }

    /** Events */

    public void addEvent(Event ev) {
        E.offer(ev);

        // notify listeners
        for (CircumstanceListener el : listeners) {
            el.eventAdded(ev);
        }
    }

    public void clearEvents() {
        E.clear();
    }

    public Queue<Event> getEvents() {
        return E;
    }

    public boolean hasEvent() {
        return !E.isEmpty();
    }

    /** remove and returns the event with atomic intention, null if none */
    public Event removeAtomicEvent() {
        Iterator<Event> i = E.iterator();
        while (i.hasNext()) {
            Event e = i.next();
            if (e.intention != null && e.intention.isAtomic()) {
                i.remove();
                return e;
            }
        }
        return null;
    }

    /** Listeners */

    public void addEventListener(CircumstanceListener el) {
        listeners.add(el);
    }

    public void removeEventListener(CircumstanceListener el) {
        listeners.remove(el);
    }

    public boolean hasListener() {
        return !listeners.isEmpty();
    }

    /** Messages */

    public Queue<Message> getMailBox() {
        return MB;
    }

    /** Intentions */

    public Queue<Intention> getIntentions() {
        return I;
    }

    public boolean hasIntention() {
        return I != null && !I.isEmpty();
    }

    public void addIntention(Intention intention) {
        I.offer(intention);
        if (intention.isAtomic()) {
            setAtomicIntention(intention);
        }

        // notify listeners
        for (CircumstanceListener el : listeners) {
            el.intentionAdded(intention);
        }
    }

    public boolean removeIntention(Intention i) {
        if (i == AI) {
            setAtomicIntention(null);
        }
        return I.remove(i);
    }

    public void clearIntentions() {
        setAtomicIntention(null);
        I.clear();
    }
    
    public void setAtomicIntention(Intention i) {
        AI = i;
    }

    public Intention removeAtomicIntention() {
        if (AI != null) {
            Intention tmp = AI;
            removeIntention(AI);
            return tmp;
        }
        return null;
    }

    public boolean hasAtomicIntention() {
        return AI != null;
    }
    

    /**
     * This changes the agent's circumstance. Currently what it does is simply
     * to change all +!l to -!l in events which would give true for Des(l) in
     * the whole set of events. IMPORTANT: unlike Des() this only alters
     * literals explicitly desired (rather than intended), that is, it does NOT
     * consider intentions. You should use both dropDes() AND dropInt() to
     * remove all desires and intentions of l.
     */
    public void dropDes(Literal l, Unifier un) {
        Event e = new Event(new Trigger(Trigger.TEAdd, Trigger.TEAchvG, l),Intention.EmptyInt);
        for (Event ei : getEvents()) {
            Trigger t = (Trigger) ei.trigger;
            if (ei.intention != Intention.EmptyInt) {
                t = (Trigger) t.clone();
                ei.intention.peek().unif.apply(t.getLiteral());
            }
            if (un.unifies(t, e.trigger)) {
                t.setTrigType(Trigger.TEDel); // Just changing "+!g" to "-!g"
            }
        }
    }


    /**
     * This changes the agent's circumstance. It removes an intention from I, E, PI or PA.
     */
    public void dropInt(Literal l, Unifier un) {
        Trigger g = new Trigger(Trigger.TEAdd, Trigger.TEAchvG, l);

        Iterator<Intention> j = I.iterator(); 
        while (j.hasNext()) {
            Intention i = j.next();
            if (i.hasTrigger(g, un)) {
                j.remove();
            }
        }

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
    }

    /**
     * This changes the agent's circumstance by simply emptying the whole set of
     * events (E). IMPORTANT: note that this is different from droping one
     * desires, in which case a goal deletion event is generated.
     */
    public void dropAllDes() {
        clearEvents();
    }

    /**
     * This changes the agent's circumstance by simply emptying the whole set of
     * intentions (I). IMPORTANT: note that this is different from droping one
     * intention, in which case a goal deletion event is generated.
     */
    public void dropAllInt() {
        clearIntentions();
    }


    public Map<String, Intention> getPendingIntentions() {
        return PI;
    }

    public boolean hasPendingIntention() {
        return PI != null && PI.size() > 0;
    }

    public ActionExec getAction() {
        return A;
    }

    public void setAction(ActionExec a) {
        this.A = a;
    }

    public List<Option> getApplicablePlans() {
        return AP;
    }

    public List<ActionExec> getFeedbackActions() {
        return FA;
    }

    public Map<String, ActionExec> getPendingActions() {
        return PA;
    }

    public boolean hasPendingAction() {
        return PA != null && PA.size() > 0;
    }

    public List getRelevantPlans() {
        return RP;
    }

    public Event getSelectedEvent() {
        return SE;
    }

    public Intention getSelectedIntention() {
        return SI;
    }

    public Option getSelectedOption() {
        return SO;
    }

    /** get the agent circunstance as XML */
    @SuppressWarnings("unchecked")
    public Element getAsDOM(Document document) {
        Element c = (Element) document.createElement("circumstance");
        Element e;
        Iterator i;

        // MB
        if (getMailBox() != null && !getMailBox().isEmpty()) {
            Element ms = (Element) document.createElement("mailbox");
            i = getMailBox().iterator();
            while (i.hasNext()) {
                e = (Element) document.createElement("message");
                e.appendChild(document.createTextNode(i.next().toString()));
                ms.appendChild(e);
            }
            c.appendChild(ms);
        }

        // events
        Element events = (Element) document.createElement("events");
        boolean add = false;
        if (E != null && !E.isEmpty()) {
            i = E.iterator();
            while (i.hasNext()) {
                add = true;
                Event evt = (Event) i.next();
                e = evt.getAsDOM(document);
                events.appendChild(e);
            }
        }
        if (getSelectedEvent() != null) {
            add = true;
            e = getSelectedEvent().getAsDOM(document);
            e.setAttribute("selected", "true");
            events.appendChild(e);
        }
        if (add) {
            c.appendChild(events);
        }

        // relPlans
        Element plans = (Element) document.createElement("plans");
        List<Object> alreadyIn = new ArrayList<Object>();

        // option
        if (getSelectedOption() != null) {
            alreadyIn.add(getSelectedOption());
            e = getSelectedOption().getAsDOM(document);
            e.setAttribute("relevant", "true");
            e.setAttribute("applicable", "true");
            e.setAttribute("selected", "true");
            plans.appendChild(e);
        }

        // appPlans
        if (getApplicablePlans() != null && !getApplicablePlans().isEmpty()) {
            for (Option o : getApplicablePlans()) {
                if (!alreadyIn.contains(o)) {
                    alreadyIn.add(o);
                    e = o.getAsDOM(document);
                    e.setAttribute("relevant", "true");
                    e.setAttribute("applicable", "true");
                    plans.appendChild(e);
                }
            }
        }

        if (getRelevantPlans() != null && !getRelevantPlans().isEmpty()) {
            i = getRelevantPlans().iterator();
            while (i.hasNext()) {
                Option o = (Option) i.next();
                if (!alreadyIn.contains(o)) {
                    alreadyIn.add(o);
                    e = o.getAsDOM(document);
                    e.setAttribute("relevant", "true");
                    plans.appendChild(e);
                }
            }
        }

        if (!alreadyIn.isEmpty()) {
            c.appendChild(plans);
        }

        // intentions
        Element ints = (Element) document.createElement("intentions");
        Element selIntEle = null;
        if (getSelectedIntention() != null) {
            selIntEle = getSelectedIntention().getAsDOM(document);
            selIntEle.setAttribute("selected", "true");
            ints.appendChild(selIntEle);
        }
        for (Intention in : getIntentions()) {
            if (getSelectedIntention() != in) {
                ints.appendChild(in.getAsDOM(document));
            }
        }
        // pending intentions
        for (String wip : getPendingIntentions().keySet()) {
            Intention ip = getPendingIntentions().get(wip);
            if (getSelectedIntention() != ip) {
                e = ip.getAsDOM(document);
                e.setAttribute("pending", wip);
                ints.appendChild(e);
            }
        }

        Element acts = (Element) document.createElement("actions");
        alreadyIn = new ArrayList();

        // action
        if (getAction() != null) {
            alreadyIn.add(getAction());
            e = getAction().getAsDOM(document);
            e.setAttribute("selected", "true");
            if (getPendingActions().values().contains(getAction())) {
                e.setAttribute("pending", "true");
            }
            if (getFeedbackActions().contains(getAction())) {
                e.setAttribute("feedback", "true");
            }
            acts.appendChild(e);
        }

        // pending actions
        if (hasPendingAction()) {
            for (String key : getPendingActions().keySet()) {// .iterator();
                ActionExec ac = getPendingActions().get(key);
                if (!alreadyIn.contains(ac)) {
                    e = ac.getAsDOM(document);
                    e.setAttribute("pending", key.toString());
                    acts.appendChild(e);
                    alreadyIn.add(ac);
                }
            }
        }

        // FA
        if (getFeedbackActions() != null && !getFeedbackActions().isEmpty()) {
            i = getFeedbackActions().iterator();
            while (i.hasNext()) {
                ActionExec o = (ActionExec) i.next();
                if (!alreadyIn.contains(o)) {
                    alreadyIn.add(o);
                    e = o.getAsDOM(document);
                    e.setAttribute("feedback", "true");
                    acts.appendChild(e);
                }
            }
        }

        if (ints.getChildNodes().getLength() > 0) {
            c.appendChild(ints);
        }

        if (acts.getChildNodes().getLength() > 0) {
            c.appendChild(acts);
        }

        return c;
    }

    public String toString() {
        return "<" + E + "," + I + "," + A + "," + MB + "," + RP + "," + AP + "," + SE + "," + SO + "," + SI + "," + PA + "," + FA + ">";
    }

}
