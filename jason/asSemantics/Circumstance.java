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
import jason.asSyntax.Trigger.TEOperator;
import jason.asSyntax.Trigger.TEType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Circumstance implements Serializable {

	private static final long serialVersionUID = 1L;

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

    private Map<Integer, ActionExec>   PA; // Pending actions, waiting action execution (key is the intention id)
    private List<ActionExec>           FA; // Feedback actions, those that are already executed
    
    private Map<String, Intention>     PI; // pending intentions, intentions suspended by any other reason                                                        

    private List<CircumstanceListener> listeners = new CopyOnWriteArrayList<CircumstanceListener>();

    public Circumstance() {
    	create();
        reset();
    }

    /** creates new collections for E, I, MB, PA, PI, and FA */
    public void create() {
        // use LinkedList since we use a lot of remove(0) in selectEvent
        E = new ConcurrentLinkedQueue<Event>();
        I = new ConcurrentLinkedQueue<Intention>();
        MB = new LinkedList<Message>();
        PA = new ConcurrentHashMap<Integer, ActionExec>();
        PI = new ConcurrentHashMap<String, Intention>();
        FA = new ArrayList<ActionExec>();    	
    }
    
    /** set null for A, RP, AP, SE, SO, and SI */
    public void reset() {
        A  = null;
        RP = null;
        AP = null;
        SE = null;
        SO = null;
        SI = null;
    }

    public void addAchvGoal(Literal l, Intention i) {
        addEvent(new Event(new Trigger(TEOperator.add, TEType.achieve, l), i));
    }

    public void addExternalEv(Trigger trig) {
        addEvent(new Event(trig, Intention.EmptyInt));
    }

    /** Events */

    public void addEvent(Event ev) {
        E.add(ev);

        // notify listeners
        for (CircumstanceListener el : listeners) {
            el.eventAdded(ev);
        }
    }

    public boolean removeEvent(Event ev) {
        return E.remove(ev);
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
            if (e.getIntention() != null && e.getIntention().isAtomic()) {
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

    public Collection<CircumstanceListener> getListeners() {
        return listeners;
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
        boolean removed = I.remove(i);

        // notify listeners
        for (CircumstanceListener el : listeners) {
            el.intentionAdded(i);
        }
        return removed;
    }

    
    public void clearIntentions() {
        setAtomicIntention(null);
        
        for (CircumstanceListener el : listeners) {
            for (Intention i: I) {
                el.intentionAdded(i);
            }            
        }
        
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
    
    public Map<String, Intention> getPendingIntentions() {
        return PI;
    }

    public boolean hasPendingIntention() {
        return PI != null && PI.size() > 0;
    }
    
    public void clearPendingIntentions() {
    	PI.clear();
    }
    
    public boolean dropPendingIntention(Intention i) {
        Iterator<Intention> it = PI.values().iterator();
        while (it.hasNext()) {
            if (it.next().equals(i)) {
                it.remove();
                
                // check in wait internal action
                for (CircumstanceListener el : listeners) {
                    el.intentionDropped(i);
                }
                return true;
            }
        }
        return false;
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

    public boolean hasFeedbackAction() {
    	return !FA.isEmpty();
    }
    
    public List<ActionExec> getFeedbackActions() {
        return FA;
    }
    
    public void addFeedbackAction(ActionExec act) {
    	if (act.getIntention() != null) {
	    	synchronized (FA) {
				FA.add(act);
			}
    	}
    }

    public Map<Integer, ActionExec> getPendingActions() {
        return PA;
    }
    
    public void clearPendingActions() {
    	PA.clear();
    }

    public boolean hasPendingAction() {
        return PA != null && PA.size() > 0;
    }

    public boolean dropPendingAction(Intention i) {
        Iterator<ActionExec> it = PA.values().iterator();
        while (it.hasNext()) {
            if (it.next().getIntention().equals(i)) {
                it.remove();
                
                // check in wait internal action
                for (CircumstanceListener el : listeners) {
                    el.intentionDropped(i);
                }
                return true;
            }
        }
        return false;
    }

    public List<Option> getRelevantPlans() {
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

    /** clone E, I, MB, PA, PI, FA, and AI */
    public Object clone() {
    	Circumstance c = new Circumstance();
    	for (Event e: this.E) {
    		c.E.add((Event)e.clone());
    	}
    	for (Intention i: this.I) {
    		c.I.add((Intention)i.clone());
    	}
    	for (Message m: this.MB) {
    		c.MB.add((Message)m.clone());
    	}
    	for (int k: this.PA.keySet()) {
    		c.PA.put(k, (ActionExec)PA.get(k).clone());
    	}
    	for (String k: this.PI.keySet()) {
    		c.PI.put(k, (Intention)PI.get(k).clone());
    	}
    	for (ActionExec ae: FA) {
    		c.FA.add((ActionExec)ae.clone());
    	}
    	return c;
    }


    /** get the agent circumstance as XML */
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
        Element plans = (Element) document.createElement("options");
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
        Intention ci = getSelectedIntention();
        if (ci != null && !ci.isFinished()) {
            selIntEle = ci.getAsDOM(document);
            selIntEle.setAttribute("selected", "true");
            ints.appendChild(selIntEle);
        }
        for (Intention in : getIntentions()) {
            if (getSelectedIntention() != in && !in.isFinished()) {
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
        if (hasPendingAction()) {
            for (int key : getPendingActions().keySet()) {
                ActionExec ac = getPendingActions().get(key);
                Intention aci = ac.getIntention();
                if (getSelectedIntention() != null && getSelectedIntention().equals(aci)) {
                    selIntEle.setAttribute("pending", ac.getActionTerm().toString());                    
                } else if (aci != null) {
                    e = aci.getAsDOM(document);
                    e.setAttribute("pending", ac.getActionTerm().toString());
                    ints.appendChild(e);
                }
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
            synchronized (getFeedbackActions()) {
                if (getFeedbackActions().contains(getAction())) {
                    e.setAttribute("feedback", "true");
                }
            }
            acts.appendChild(e);
        }

        // pending actions
        if (hasPendingAction()) {
            for (int key : getPendingActions().keySet()) {// .iterator();
                ActionExec ac = getPendingActions().get(key);
                if (!alreadyIn.contains(ac)) {
                    e = ac.getAsDOM(document);
                    e.setAttribute("pending", key+"");
                    acts.appendChild(e);
                    alreadyIn.add(ac);
                }
            }
        }

        // FA
        if (hasFeedbackAction()) {
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
    	StringBuilder s = new StringBuilder("Circumstance:\n");
    	s.append("  E ="+E +"\n");
    	s.append("  I ="+I +"\n");
    	s.append("  A ="+A +"\n");
    	s.append("  MB="+MB+"\n");
    	s.append("  RP="+RP+"\n");
    	s.append("  AP="+AP+"\n");
    	s.append("  SE="+SE+"\n");
    	s.append("  SO="+SO+"\n");
    	s.append("  SI="+SI+"\n");
    	s.append("  AI="+AI+"\n");
    	s.append("  PA="+PA+"\n");
    	s.append("  PI="+PI+"\n");
    	s.append("  FA="+FA+".");
        return s.toString();
    }

}
