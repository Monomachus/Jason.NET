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
//   Revision 1.16  2006/02/28 15:11:29  jomifred
//   improve javadoc
//
//   Revision 1.15  2006/02/18 15:23:32  jomifred
//   changes in many files to detach jason kernel from any infrastructure implementation
//
//   Revision 1.14  2006/02/17 13:13:16  jomifred
//   change a lot of method/classes names and improve some comments
//
//   Revision 1.12  2005/10/30 16:07:33  jomifred
//   add comments
//
//   Revision 1.11  2005/08/12 22:26:08  jomifred
//   add cvs keywords
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
 * It is a base class for Environment, it is overridden by the user application 
 * to define the environment "behaviour".
 * 
 * <p>Execution sequence: 	
 *     <ul><li>setEnvironmentInfraTier, 
 *         <li>init, 
 *         <li>(getPercept|executeAction)*, 
 *         <li>stop.
 *     </ul>
 * 
 * <p>An example of interaction:
 * <img src="../../../uml/environmentInteraction.gif" />
 *
 * <p>The related classes:
 * <img src="../../../uml/jason.environment.gif" />
 *  
 */
public class Environment { 

	private List<Literal> percepts = Collections.synchronizedList(new ArrayList<Literal>());
	private Map<String,List<Literal>>  agPercepts = Collections.synchronizedMap(new HashMap<String,List<Literal>>());
	
        /** the infrastructure tier for environment (Centralised, Saci, ...) */
	private EnvironmentInfraTier environmentInfraTier = null;

	// set of agents that already received the last version of perception
	private Set<String> uptodateAgs = Collections.synchronizedSet(new HashSet<String>());
	
	
	/** Called before the start of MAS execution, the user environment could override it */
	public void init(String[] args) {
		// TODO: implement env. args in .mas2j
	}
	
	/** Called before the end of MAS execution, the user environment could override it */
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

    
    public void informAgsEnvironmentChanged(Collection agents) {
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
	 * Returns perceptions for an agent.
	 * A full copy of both common and agent perceptions lists is returned.
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

	/** Add a perception for all agents */
	public void addPercept(Literal per) {
		if (per != null) {
			if (! percepts.contains(per)) {
				percepts.add(per);
				uptodateAgs.clear();
			}
		}
	}
	/** Remove a perception in the commom perception list */
	public boolean removePercept(Literal per) {
		if (per != null) {
			uptodateAgs.clear();
			return percepts.remove(per);
		} 
		return false;
	}
	
	
	/** Clear list of global percepts */
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
	
	
	
	/** Add a perception for a specific agent */
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
	
	/** Remove a perception for one agent */
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
			List<Literal> agl = agPercepts.get(agName);
			if (agl != null) {
				return agl.contains(per);
			}
		}
		return false;
	}

	/** Clear list of percepts of a specific agent */
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
     * Called by the agent architecture to execute an action on the environment.
     */
    public boolean executeAction(String agName, Term act) {
        return true;
    }
}
