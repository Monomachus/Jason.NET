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
//   Revision 1.13  2005/12/19 00:14:53  jomifred
//   no message
//
//   Revision 1.12  2005/10/30 16:07:33  jomifred
//   add comments
//
//   Revision 1.11  2005/08/12 22:26:08  jomifred
//   add cvs keywords
//
//
//----------------------------------------------------------------------------


package jason.environment;

import jason.asSyntax.Literal;
import jason.asSyntax.Term;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An abstract adapter class for Environment.
 * 
 * It is overridden by the user application to define the environment "behaviour".
 * 
 * Execution sequence: 	setEnvironmentInfraTier, init, (getPercept|executeAction)*, stop.
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
	
    /** the infrastructure tier for environment (Centralised, Saci, ...) */
	private EnvironmentInterface environmentInfraTier = null;

	// set of agents that already received the last version of perception
	private Set uptodateAgs = Collections.synchronizedSet(new HashSet());
	
	
	/** called before the start of MAS execution, the user environment could override it */
	public void init(String[] args) {
		// TODO: implement env. args in .mas2j
	}
	
	/** called before the end of MAS execution, the user environment could override it */
	public void stop() {
	}
	
	
	/**
	 * sets the infrastructure tier of the environment (saci, centralised, ...)
	 */
	protected void setEnvironmentInfraTier(EnvironmentInterface je) {
		environmentInfraTier = je;
	}
	protected EnvironmentInterface getEnvironmentInfraTier() {
		return environmentInfraTier;
	}

    
	/**
	 * @see jason.environment.EnvironmentInterface#informAgsEnvironmentChanged(java.util.Collection)
	 */
    public void informAgsEnvironmentChanged(Collection agents) {
        if (environmentInfraTier != null) {
            environmentInfraTier.informAgsEnvironmentChanged(agents);
        }
    }

	/**
	 * @see jason.environment.EnvironmentInterface#informAgsEnvironmentChanged()
	 */
    public void informAgsEnvironmentChanged() {
        if (environmentInfraTier != null) {
            environmentInfraTier.informAgsEnvironmentChanged();
        }
    }

	/**
	 * Returns perceptions for an agent.
	 * A full copy of both common and agent perceptions lists is returned.
	 */
    public List getPercepts(String agName) {
		
		// check whether this agent needs the current version of perception
		if (uptodateAgs.contains(agName)) {
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
