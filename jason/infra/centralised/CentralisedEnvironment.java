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
import jason.asSemantics.Message;
import jason.environment.Environment;
import jason.environment.EnvironmentInfraTier;
import jason.mas2j.ClassParameters;
import jason.runtime.RuntimeServicesInfraTier;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
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

    private Map<String,List<Message>> mboxes;
    private Map<String,AgArch> agents;
    
    /** the user customisation class for the environment */
	private Environment fUserEnv;
    
    static Logger logger = Logger.getLogger(CentralisedEnvironment.class.getName());
	
    public CentralisedEnvironment(ClassParameters userEnv) throws JasonException {
        mboxes      = Collections.synchronizedMap(new HashMap<String,List<Message>>());
        agents      = Collections.synchronizedMap(new HashMap<String,AgArch>());
        
        try { 
			fUserEnv = (Environment) getClass().getClassLoader().loadClass(userEnv.className).newInstance();
			fUserEnv.setEnvironmentInfraTier(this);
			fUserEnv.init(userEnv.getParametersArray());
        } catch (Exception e) {
            logger.log(Level.SEVERE,"Error in Centralised MAS environment creation",e);
            throw new JasonException("The user environment class instantiation '"+userEnv+"' has failed!"+e.getMessage());
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
        for (AgArch agArch: agents.values()) {
            agArch.getTS().newMessageHasArrived();
        }
    }

    public void informAgsEnvironmentChanged(Collection<String> agentsToNotify) {
        if (agentsToNotify == null) {
            informAgsEnvironmentChanged();
        } else {
            for (String agName: agentsToNotify) {
                AgArch agArch = getAgent(agName);
                if (agArch != null) {
                    agArch.getTS().newMessageHasArrived();
                } else {
                    logger.log(Level.SEVERE, "Error sending message notification: agent " + agName + " does not exist!");
                }
            }
        }
    }

    
    public void addAgent(AgArch agent) {
        if (mboxes.get(agent.getAgName()) != null) {
            logger.warning("Warning: adding an agent that already exists: " + agent.getAgName());
        }
        mboxes.put(agent.getAgName(), new LinkedList<Message>());
        agents.put(agent.getAgName(), agent);
    }

    public void delAgent(AgArch agent) {
        mboxes.remove(agent.getAgName());
        agents.remove(agent.getAgName());
    }
    
    public List<Message> getAgMbox(String name) {
        return mboxes.get(name);
    }
    
    /** 
     * Returns the agents map, key is the agent name (String) and value 
     * is the AgArch agent object.
     */
    public Map<String,AgArch> getAgents() {
    	    return agents;
    }
        
    public AgArch getAgent(String name) {
        return agents.get(name);
    }

    public RuntimeServicesInfraTier getRuntimeServices() {
    	    return new CentralisedRuntimeServices();
    }
}
