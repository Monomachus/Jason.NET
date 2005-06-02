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

import jason.JasonException;
import jason.architecture.CentralisedAgArch;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This class implements the centralised version of the environment and
 * the agents mailbox.
 */

public class CentralisedEnvironment implements EnvironmentInterface {

	// TODO: try ConcurrentHashMap when moving to jdk 1.5
    private Map mboxes;
    private Map agents;
    
	private Environment fUserEnv;
    
    public CentralisedEnvironment(String userEnvClassName) throws JasonException {
        mboxes      = Collections.synchronizedMap(new HashMap());
        agents      = Collections.synchronizedMap(new HashMap());
        
        try {
            fUserEnv = (Environment)Class.forName(userEnvClassName).newInstance();
            fUserEnv.setJasonEnvironment(this);
        } catch (Exception e) {
            System.err.println("Error "+e);
            e.printStackTrace();
            throw new JasonException("The user environment class instantiation '"+userEnvClassName+"' has failed!"+e.getMessage());
        }
    }

    public Environment getUserEnvironment() {
    	return fUserEnv;
    }
    
    /** make a copy of the percepts with synchronized access to the percepts list */
	/*
    public List getSafePerceptsCopy(String agName) {
    	List userPercepts = fUserEnv.getPercepts(agName);
        synchronized (userPercepts) {
            // make a local copy of the environment percepts
        	List l = new ArrayList(userPercepts.size());
        	Iterator i = userPercepts.iterator();
        	while (i.hasNext()) {
        		l.add( ((Term)i.next()).clone());
        	}
			return l;
        }
    	
    }
    */


    /** make a copy of the neg percepts with synchronized access to the percepts list */
    /*
     public List getSafeNegPerceptsCopy(String agName) {
    	List userNegPercepts = fUserEnv.getNegativePercepts(agName);
        synchronized (userNegPercepts) {
            // make a local copy of the environment percepts
            return new ArrayList(userNegPercepts);
        }
    }
    */

	/**
	 * @see jason.environment.EnvironmentInterface#informAgsEnvironmentChanged()
	 */
    public void informAgsEnvironmentChanged() {
        Iterator i = agents.values().iterator();
        while (i.hasNext()) {
        	CentralisedAgArch agArch = (CentralisedAgArch)i.next();
        	agArch.getTS().newMessageHasArrived();
        }
    }

	/**
	 * @see jason.environment.EnvironmentInterface#informAgsEnvironmentChanged(java.util.Collection)
	 */
    public void informAgsEnvironmentChanged(Collection agentsToNotify) {
        if (agentsToNotify == null) {
        	informAgsEnvironmentChanged();
        } else {
            Iterator i = agentsToNotify.iterator();
            while (i.hasNext()) {
                String agName = i.next().toString();
                CentralisedAgArch agArch = getAgent(agName);
                if (agArch != null) {
                    agArch.getTS().newMessageHasArrived();
                } else {
                    System.err.println("Error sending notify events to agent "+agName+", it does not exist!");
                }
            }
        }
    }

    
    public void addAgent(CentralisedAgArch agent) {
        if (mboxes.get(agent.getName()) != null) {
        	System.err.println("Warning: add an agent that already exists: "+ agent.getName());
        }
        mboxes.put(agent.getName(), new LinkedList());
        agents.put(agent.getName(), agent);
    }

    public void delAgent(CentralisedAgArch agent) {
        mboxes.remove(agent.getName());
        agents.remove(agent.getName());
    }
    
    public List getAgMbox(String name) {
        return (List) mboxes.get(name);
    }
    
    /** 
     * returns the agents map, key is the agent name (String) and value 
     * is the CentralisedAgArch agent object.
     */
    public Map getAgents() {
    	return agents;
    }
        
    public CentralisedAgArch getAgent(String name) {
        return (CentralisedAgArch)agents.get(name);
    }

}
