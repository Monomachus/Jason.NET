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
// CVS information:
//   $Date$
//   $Revision$
//   $Log$
//   Revision 1.12  2006/01/04 02:54:41  jomifred
//   using java log API instead of apache log
//
//   Revision 1.11  2006/01/02 13:49:00  jomifred
//   add plan unique id, fix some bugs
//
//   Revision 1.10  2005/12/30 20:40:16  jomifred
//   new features: unnamed var, var with annots, TE as var
//
//   Revision 1.9  2005/08/16 21:03:42  jomifred
//   add some comments on TODOs
//
//   Revision 1.8  2005/08/12 22:18:37  jomifred
//   add cvs keywords
//
//
//----------------------------------------------------------------------------


package jason.asSemantics;


import jason.JasonException;
import jason.asSyntax.Literal;
import jason.asSyntax.Trigger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Circumstance implements Serializable {

	static Logger logger = Logger.getLogger(Circumstance.class.getName());
	
    private List   E;
    private List   I;

	protected ActionExec A;

    protected List   MB;
    protected List   RP;
    protected List   AP;

	protected Event SE;

	protected Option    SO;

    protected Intention SI;
    //protected Intention AI; // atomic intention
    protected Map       PA;
    protected List      FA;

    private List listeners = new ArrayList();
    
    public Circumstance() {
        E  = new LinkedList();
        I  = new LinkedList();
        MB = new LinkedList();
        PA = new HashMap();
        FA = new LinkedList();
        reset();
    }
    
    public void reset() {
        A  = null;
        RP = null;
        AP = null;
        SE = null;
        SO = null;
        SI = null;    	
    }

    public void addAchvGoal(Literal l, Intention i) {
        addEvent(new Event(new Trigger(Trigger.TEAdd,Trigger.TEAchvG, l), i));
    }
    public void addTestGoal(Literal l, Intention i) {
    	addEvent(new Event(new Trigger(Trigger.TEAdd,Trigger.TETestG, l), i));
    }

    public void delAchvGoal(Literal l, Intention i) {
    	addEvent(new Event(new Trigger(Trigger.TEDel,Trigger.TEAchvG,l), i));
    }
    public void delTestGoal(Literal l, Intention i) {
    	addEvent(new Event(new Trigger(Trigger.TEDel,Trigger.TETestG,l), i));
    }

    public void delGoal(byte g, Literal l, Intention i) throws JasonException {
        if (g==Trigger.TEAchvG)
            delAchvGoal(l, i);
        else if (g==Trigger.TETestG)
            delTestGoal(l, i);
        else
            throw new JasonException("Unknown type of goal.");
    }

    public void addExternalEv(Trigger trig) {
    	addEvent(new Event(trig, Intention.EmptyInt));
    }

    public void addEvent(Event ev) {
        E.add(ev);
        
        // notify listeners
        synchronized (listeners) {
	        Iterator i = listeners.iterator();
	        while (i.hasNext()) {
	        	CircumstanceListener el = (CircumstanceListener)i.next();
	        	el.eventAdded(ev);
	        }
		}
    }
    
    public void clearEvents() {
    	E.clear();
    }

    public List getEvents() {
		return E;
	}
    
    public boolean hasEvent() {
        return ! E.isEmpty();
    }
    
    public void addEventListener(CircumstanceListener el) {
        synchronized (listeners) {
        	listeners.add(el);
        }
    }
    
    public void removeEventListener(CircumstanceListener el) {
        synchronized (listeners) {
        	listeners.remove(el);
        }
    }

    public List getMB() {
		return MB;
	}


	public List getIntentions() {
		return I;
	}
    
    public boolean hasIntention() {
        return ! I.isEmpty();
    }
    
	public void addIntention(Intention intention) {
		I.add(intention);

		// notify listeners
        synchronized (listeners) {
	        Iterator i = listeners.iterator();
	        while (i.hasNext()) {
	        	CircumstanceListener el = (CircumstanceListener)i.next();
	        	el.intentionAdded(intention);
	        }
        }
	}
    
    public boolean removeIntention(Intention i) {
        return I.remove(i);
    }
    
    public void clearIntentions() {
        I.clear();
    }
    
    public ActionExec getAction() {
		return A;
	}
	public void setA(ActionExec a) {
		this.A = a;
	}
	public List getApplicablePlans() {
		return AP;
	}
	public List getFeedbackActions() {
		return FA;
	}
	public Map getPendingActions() {
		return PA;
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
	public Element getAsDOM(Document document) {
		Element c = (Element) document.createElement("circumstance");
		Element e;
		Iterator i;
		
		// MB
		if (getMB() != null && !getMB().isEmpty()) {
			Element ms = (Element) document.createElement("mailbox");
			i = getMB().iterator();
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
				Event evt = (Event)i.next();
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
		List alreadyIn = new ArrayList();
		
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
			i = getApplicablePlans().iterator();
			while (i.hasNext()) {
				Option o = (Option)i.next();
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
				Option o = (Option)i.next();
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
		if (getIntentions() != null && !getIntentions().isEmpty()) {
			i = getIntentions().iterator();
			while (i.hasNext()) {
				Intention in = (Intention) i.next();
				if (getSelectedIntention() != in) {
					e = in.getAsDOM(document);
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
				e.setAttribute("pending","true");
			}
			if (getFeedbackActions().contains(getAction())) {
				e.setAttribute("feedback","true");
			}
			acts.appendChild(e);			
		}
		
		// pending actions
		if (getPendingActions() != null && !getPendingActions().isEmpty()) {
			i = getPendingActions().values().iterator();
			while (i.hasNext()) {
				Object o = i.next();
				if (!alreadyIn.contains(o)) {
					try { // try ActionExec
						e = ((ActionExec)o).getAsDOM(document);
						e.setAttribute("pending","true");
						acts.appendChild(e);
						alreadyIn.add(o);
					} catch (Exception ex1) {
						try { // try Intention
							e = ((Intention)o).getAsDOM(document);
							if (! o.equals(getSelectedIntention())) {
								e.setAttribute("pending","true");
								// add in intentions
								ints.appendChild(e);
							} else {
								selIntEle.setAttribute("pending","true");
							}
						} catch (Exception ex2) {
							logger.log(Level.SEVERE,"Trying to add an unknown pending action "+o.getClass().getName()+" - "+ex2,ex2);
						}
					}
				}
			}
		}

		// FA
		if (getFeedbackActions() != null && !getFeedbackActions().isEmpty()) {
			i = getFeedbackActions().iterator();
			while (i.hasNext()) {
				ActionExec o = (ActionExec)i.next();
				if (!alreadyIn.contains(o)) {
					alreadyIn.add(o);
					e = o.getAsDOM(document);
					e.setAttribute("feedback","true");
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
        return "<"+E+","+I+","+A+","+MB+","+RP+","+AP+","+SE+","+SO+","+
            SI+","+PA+","+FA+">";
    }

}
