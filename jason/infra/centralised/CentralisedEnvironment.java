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


package jason.infra.centralised;

import jason.JasonException;
import jason.architecture.AgArch;
import jason.environment.Environment;
import jason.environment.EnvironmentInfraTier;
import jason.runtime.RuntimeServicesInfraTier;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * This class implements the centralised version of the environment infrastructure tier.
 * It also manages the current agents inside the MAS, their mailboxes, etc.
 */
public class CentralisedEnvironment implements EnvironmentInfraTier {

	// TODO: try ConcurrentHashMap when moving to jdk 1.5
    private Map mboxes;
    private Map agents;
    
    /** the user customisation class for the environment */
	private Environment fUserEnv;
    
    static Logger logger = Logger.getLogger(CentralisedEnvironment.class.getName());
	
    public CentralisedEnvironment(String userEnvClassName) throws JasonException {
        mboxes      = Collections.synchronizedMap(new HashMap());
        agents      = Collections.synchronizedMap(new HashMap());
        
        try { 
			fUserEnv = (Environment) getClass().getClassLoader().loadClass(userEnvClassName).newInstance();
			fUserEnv.setEnvironmentInfraTier(this);
			fUserEnv.init(null);
        } catch (Exception e) {
            logger.log(Level.SEVERE,"Error in Centralised MAS environment creation",e);
            throw new JasonException("The user environment class instantiation '"+userEnvClassName+"' has failed!"+e.getMessage());
        }
    }
	
	/** called before the end of MAS execution, it just calls the user environment class stop method. */
	public void stop() {
		fUserEnv.stop();
	}

    public Environment getUserEnvironment() {
    	return fUserEnv;
    }
    
    public void informAgsEnvironmentChanged() {
        Iterator i = agents.values().iterator();
        while (i.hasNext()) {
        	AgArch agArch = (AgArch)i.next();
        	agArch.getTS().newMessageHasArrived();
        }
    }

    public void informAgsEnvironmentChanged(Collection agentsToNotify) {
        if (agentsToNotify == null) {
        	informAgsEnvironmentChanged();
        } else {
            Iterator i = agentsToNotify.iterator();
            while (i.hasNext()) {
                String agName = i.next().toString();
                AgArch agArch = getAgent(agName);
                if (agArch != null) {
                    agArch.getTS().newMessageHasArrived();
                } else {
                    logger.log(Level.SEVERE,"Error sending message notification: agent "+agName+" does not exist!");
                }
            }
        }
    }

    
    public void addAgent(AgArch agent) {
        if (mboxes.get(agent.getAgName()) != null) {
        	logger.warning("Warning: adding an agent that already exists: "+ agent.getAgName());
        }
        mboxes.put(agent.getAgName(), new LinkedList());
        agents.put(agent.getAgName(), agent);
    }

    public void delAgent(AgArch agent) {
        mboxes.remove(agent.getAgName());
        agents.remove(agent.getAgName());
    }
    
    public List getAgMbox(String name) {
        return (List) mboxes.get(name);
    }
    
    /** 
     * Returns the agents map, key is the agent name (String) and value 
     * is the AgArch agent object.
     */
    public Map getAgents() {
    	return agents;
    }
        
    public AgArch getAgent(String name) {
        return (AgArch)agents.get(name);
    }

    public RuntimeServicesInfraTier getRuntimeServices() {
    	return new CentralisedRuntimeServices();
    }
}
