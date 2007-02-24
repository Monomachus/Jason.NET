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


package jason.environment;

import jason.asSyntax.Literal;
import jason.asSyntax.Structure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * It is a base class for Environment, it is overridden by the user
 * application to define the environment "behaviour".
 * 
 * <p>Execution sequence: 	
 *     <ul><li>setEnvironmentInfraTier, 
 *         <li>init, 
 *         <li>(getPercept|executeAction)*, 
 *         <li>stop.
 *     </ul>
 * 
 */
public class Environment { 

	private List<Literal> percepts = Collections.synchronizedList(new ArrayList<Literal>());
	private Map<String,List<Literal>>  agPercepts = new ConcurrentHashMap<String, List<Literal>>();
	
    /** the infrastructure tier for environment (Centralised, Saci, ...) */
	private EnvironmentInfraTier environmentInfraTier = null;

	// set of agents that already received the last version of perception
	private Set<String> uptodateAgs = Collections.synchronizedSet(new HashSet<String>());
	
	//static private Logger logger = Logger.getLogger(Environment.class.getName());

	/** 
     * Called before the MAS execution with the args informed in
     * .mas2j project, the user environment could override it.
     */
	public void init(String[] args) {
	}
	
	/** 
     * Called just before the end of MAS execution, the user
     * environment could override it.
     */
	public void stop() {
	}
	
	
	/**
	 * Sets the infrastructure tier of the environment (saci, centralised, ...)
	 */
	public void setEnvironmentInfraTier(EnvironmentInfraTier je) {
		environmentInfraTier = je;
	}
	public EnvironmentInfraTier getEnvironmentInfraTier() {
		return environmentInfraTier;
	}

    
    public void informAgsEnvironmentChanged(Collection<String> agents) {
        if (environmentInfraTier != null) {
            environmentInfraTier.informAgsEnvironmentChanged(agents);
        }
    }

    public void informAgsEnvironmentChanged() {
        if (environmentInfraTier != null) {
            environmentInfraTier.informAgsEnvironmentChanged();
        }
    }

	/**
	 * Returns perceptions for an agent.  A full copy of both common
	 * and agent perceptions lists is returned.
	 */
    public List<Literal> getPercepts(String agName) {
		
		// check whether this agent needs the current version of perception
		if (uptodateAgs.contains(agName)) {
			return null;
		}
		// add agName in the set of uptodate agents
		uptodateAgs.add(agName);
		
		int size = percepts.size();
		List<Literal> agl = agPercepts.get(agName);
		if (agl != null) {
			size += agl.size();
		}
		List<Literal> p = new ArrayList<Literal>(size);
		
        if (! percepts.isEmpty()) { // has global perception?
            synchronized (percepts) {
                // make a local copy of the environment percepts
    			// Note: a deep copy will be done by BB.add
    			p.addAll(percepts);
            }
        }
		if (agl != null) { // add agent personal perception
	        synchronized (agl) {
				p.addAll(agl);
	        }
		}
		
        return p;
    }

	/** Adds a perception for all agents */
	public void addPercept(Literal per) {
		if (per != null) {
			if (! percepts.contains(per)) {
				percepts.add(per);
				uptodateAgs.clear();
			}
		}
	}

	/** Removes a perception in the commom perception list */
	public boolean removePercept(Literal per) {
		if (per != null) {
			uptodateAgs.clear();
			return percepts.remove(per);
		} 
		return false;
	}
	
	
	/** Clears the list of global perceptions */
	public void clearPercepts() {
        if (!percepts.isEmpty()) {
            uptodateAgs.clear();
            percepts.clear();
        }
	}
	
	public boolean containsPercept(Literal per) {
		if (per != null) {
			return percepts.contains(per);
		} 
		return false;
	}
	
	
	
	/** Adds a perception for a specific agent */
	public void addPercept(String agName, Literal per) {
		if (per != null && agName != null) {
			List<Literal> agl = agPercepts.get(agName);
			if (agl == null) {
				agl = Collections.synchronizedList(new ArrayList<Literal>());
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
	
	/** Removes a perception for one agent */
	public boolean removePercept(String agName, Literal per) {
		if (per != null && agName != null) {
			List<Literal> agl = agPercepts.get(agName);
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

	/** Clears the list of perceptions of a specific agent */
	public void clearPercepts(String agName) {
		if (agName != null) {
			List<Literal> agl = agPercepts.get(agName);
			if (agl != null) {
				uptodateAgs.remove(agName);
				agl.clear();
				//agPercepts.put( agName, agl);
			}
		}
	}
	
    /**
     * Called by the agent architecture to execute an action on the
     * environment.
     */
    public boolean executeAction(String agName, Structure act) {
        return true;
    }
}
