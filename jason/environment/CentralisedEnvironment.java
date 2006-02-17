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
//   Revision 1.12  2006/02/17 13:13:16  jomifred
//   change a lot of method/classes names and improve some comments
//
//   Revision 1.11  2006/01/04 03:00:46  jomifred
//   using java log API instead of apache log
//
//   Revision 1.10  2005/10/30 18:39:48  jomifred
//   change in the AgArch customisation  support (the same customisation is used both to Cent and Saci infrastructures0
//
//   Revision 1.9  2005/10/30 16:07:33  jomifred
//   add comments
//
//   Revision 1.8  2005/08/18 20:37:55  jomifred
//   no message
//
//   Revision 1.7  2005/08/12 22:26:08  jomifred
//   add cvs keywords
//
//
//----------------------------------------------------------------------------


package jason.environment;

import jason.JasonException;
import jason.architecture.AgArch;

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
    
	/**
	 * @see jason.environment.EnvironmentInfraTier#informAgsEnvironmentChanged()
	 */
    public void informAgsEnvironmentChanged() {
        Iterator i = agents.values().iterator();
        while (i.hasNext()) {
        	AgArch agArch = (AgArch)i.next();
        	agArch.getTS().newMessageHasArrived();
        }
    }

	/**
	 * @see jason.environment.EnvironmentInfraTier#informAgsEnvironmentChanged(java.util.Collection)
	 */
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
     * returns the agents map, key is the agent name (String) and value 
     * is the AgArch agent object.
     */
    public Map getAgents() {
    	return agents;
    }
        
    public AgArch getAgent(String name) {
        return (AgArch)agents.get(name);
    }

}
