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


package jason.environment;

import jason.asSyntax.Literal;
import jason.asSyntax.Pred;
import jason.asSyntax.Term;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An abstract adapter class for Environment.
 * 
 * It is overridden by the user application to define the environment "behaviour".
 * 
 * Execution sequence: 	setJasonEnvironment, init, (getPercept|executeAction)*, stop.
 * 
 * <p>An example of interaction:
 * <img src="../../../uml/environmentInteraction.gif" />
 *
 * <p>The related classes:
 * <img src="../../../uml/jason.environment.gif" />
 *  
 */
public class Environment { 

	private List percepts = Collections.synchronizedList(new ArrayList());
	private Map  agPercepts = Collections.synchronizedMap(new HashMap());
	
	// TODO: remove in v 0.8
	private List oldPercepts = Collections.synchronizedList(new ArrayList());
    private List oldNegPercepts = Collections.synchronizedList(new ArrayList());

	private EnvironmentInterface jasonEnvironment = null;

	// set of agents that already received the last version of perception
	private Set uptodateAgs = Collections.synchronizedSet(new HashSet());
	
	
	/** called before the start of MAS execution, the user environment could override it */
	public void init() {
	}
	
	/** called before the end of MAS execution, the user environment could override it */
	public void stop() {
	}
	
	
	/**
	 * sets the jason part of the environment (saci or centralised)
	 */
	protected void setJasonEnvironment(EnvironmentInterface je) {
		jasonEnvironment = je;
	}
	protected EnvironmentInterface getJasonEnvironment() {
		return jasonEnvironment;
	}

    
	/**
	 * @see jason.environment.EnvironmentInterface#informAgsEnvironmentChanged(java.util.Collection)
	 */
    public void informAgsEnvironmentChanged(Collection agents) {
        if (jasonEnvironment != null) {
            jasonEnvironment.informAgsEnvironmentChanged(agents);
        }
    }

	/**
	 * @see jason.environment.EnvironmentInterface#informAgsEnvironmentChanged()
	 */
    public void informAgsEnvironmentChanged() {
        if (jasonEnvironment != null) {
            jasonEnvironment.informAgsEnvironmentChanged();
        }
    }

	/** 
	 * @deprecated use add/rem Percept to change the perceptions, this method will be
	 * removed in the future a version
	 */
	public List getPercepts() {
		return oldPercepts;
	}
	/** 
	 * @deprecated use add/rem Percept to change the perceptions, this method will be
	 * removed in the future a version
	 */
	public List getNegativePercepts() {
		return oldNegPercepts;
	}

	/**
	 * Returns perceptions for an agent.
	 * A full copy of both common and agent perceptions lists is returned.
	 * 
	 * TODO: turn it final in the v 0.8
	 */
    public List getPercepts(String agName) {
		
		// check whether this agent needs the current version of perception
		if (uptodateAgs.contains(agName) && !oldNegPercepts.isEmpty() && !oldPercepts.isEmpty()) {
			return null;
		}
		// add agName in the set of uptodate agents
		uptodateAgs.add(agName);
		
		int size = percepts.size();
		List agl = (List)agPercepts.get(agName);
		if (agl != null) {
			size += agl.size();
		}
		List p = new ArrayList(size);
		
        synchronized (percepts) {
            // make a local copy of the environment percepts
			// Note: a deep copy will be done by BB.add
			p.addAll(percepts);
        }
		if (agl != null) {
	        synchronized (agl) {
				p.addAll(agl);
	        }
		}
		
		// add old style perceptions (where they are Terms)
		// TODO: remove it in v 0.8
		if (!oldPercepts.isEmpty()) {
			synchronized (oldPercepts) {
				Iterator i = oldPercepts.iterator();
				while (i.hasNext()) {
					p.add( new Literal(Literal.LPos, new Pred((Term)i.next())));
				}
			}
		}
		if (!oldNegPercepts.isEmpty()) {
			synchronized (oldNegPercepts) {
				Iterator i = oldNegPercepts.iterator();
				while (i.hasNext()) {
					p.add( new Literal(Literal.LNeg, new Pred((Term)i.next())));
				}
			}
		}
		
        return p;
    }

	/** add a perception for all agents */
	public void addPercept(Literal per) {
		if (per != null) {
			if (! percepts.contains(per)) {
				percepts.add(per);
				uptodateAgs.clear();
			}
		}
	}
	/** remove a perception in the commom perception list */
	public boolean removePercept(Literal per) {
		if (per != null) {
			uptodateAgs.clear();
			return percepts.remove(per);
		} 
		return false;
	}
	
	
	/** clear list of global percepts */
	public void clearPercepts() {
		uptodateAgs.clear();
		percepts.clear();
	}
	
	public boolean containsPercept(Literal per) {
		if (per != null) {
			return percepts.contains(per);
		} 
		return false;
	}
	
	
	
	/** add a perception for a specific agent */
	public void addPercept(String agName, Literal per) {
		if (per != null && agName != null) {
			List agl = (List)agPercepts.get(agName);
			if (agl == null) {
				agl = Collections.synchronizedList(new ArrayList());
				uptodateAgs.remove(agName);
				agl.add(per);
				agPercepts.put( agName, agl);
			} else {
				if (! agl.contains(per)) {
					uptodateAgs.remove(agName);
					agl.add(per);
				}
			}
		}
	}
	
	/** remove a perception for one agent */
	public boolean removePercept(String agName, Literal per) {
		if (per != null && agName != null) {
			List agl = (List)agPercepts.get(agName);
			if (agl != null) {
				uptodateAgs.remove(agName);
				return agl.remove(per);
			}
		}
		return false;
	}

	public boolean containsPercept(String agName, Literal per) {
		if (per != null && agName != null) {
			List agl = (List)agPercepts.get(agName);
			if (agl != null) {
				return agl.contains(per);
			}
		}
		return false;
	}

	/** clear list of percepts of a specific agent */
	public void clearPercepts(String agName) {
		if (agName != null) {
			List agl = (List)agPercepts.get(agName);
			if (agl != null) {
				uptodateAgs.remove(agName);
				agl.clear();
				//agPercepts.put( agName, agl);
			}
		}
	}
	
	
	
    /**
     * called by the agent architecture to execute an action on the environment.
     */
    public boolean executeAction(String agName, Term act) {
        return true;
    }
}
