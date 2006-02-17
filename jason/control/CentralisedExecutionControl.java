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
//   Revision 1.7  2006/02/17 13:13:16  jomifred
//   change a lot of method/classes names and improve some comments
//
//   Revision 1.6  2006/01/04 03:00:46  jomifred
//   using java log API instead of apache log
//
//   Revision 1.5  2005/10/30 18:39:48  jomifred
//   change in the AgArch customisation  support (the same customisation is used both to Cent and Saci infrastructures0
//
//   Revision 1.4  2005/08/12 20:52:18  jomifred
//   change in the informAgs method name
//
//
//----------------------------------------------------------------------------

package jason.control;

import jason.JasonException;
import jason.architecture.AgArch;
import jason.environment.CentralisedEnvironment;

import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Document;

/**
 * Concrete implementation of the controller for
 * centralised infrastructure tier.
 */
public class CentralisedExecutionControl implements ExecutionControlInfraTier {

	private CentralisedEnvironment    infraEnv;
	private ExecutionControl          userController;
	
	static Logger logger = Logger.getLogger(CentralisedExecutionControl.class.getName());
	
	public CentralisedExecutionControl(CentralisedEnvironment envInfraTier, String userControlClass) throws JasonException {
		infraEnv = envInfraTier;
        try {
        	userController = (ExecutionControl)Class.forName(userControlClass).newInstance();
        	userController.setExecutionControlInfraTier(this);
        	//fUserControl.setJasonDir(jasonDir);
        	userController.init();
        } catch (Exception e) {
            logger.log(Level.SEVERE,"Error ",e);
            throw new JasonException("The user execution control class instantiation '"+userControlClass+"' has failed!"+e.getMessage());
        }
	}
	
	/**
	 * This method is called when MAS execution is being finished
	 */
	public void stop() {
		userController.stop();
	}
	
	public ExecutionControl getUserControl() {
		return userController;
	}
	
	public CentralisedEnvironment getEnvInfraTier() {
		return infraEnv;
	}
	
	/** 
	 * Called (by the ag arch) when the agent <i>agName</i> has finished its reasoning cycle.
	 * <i>breakpoint</i> is true in case the agent selected one plan with "breakpoint" 
	 * annotation.
     */
	public void receiveFinishedCycle(String agName, boolean breakpoint) {
		// pass to user controller
		userController.receiveFinishedCycle(agName, breakpoint);
	}

	/**
	 * @see jason.control.ExecutionControlInfraTier#informAgToPerformCycle(java.lang.String)
	 */
	public void informAgToPerformCycle(String agName) {
		// call the agent method to "go on"
		infraEnv.getAgent(agName).getTS().receiveSyncSignal();
	}

	/**
	 * @see jason.control.ExecutionControlInfraTier#informAllAgsToPerformCycle()
	 */
	public void informAllAgsToPerformCycle() {
		synchronized(infraEnv.getAgents()) { 
			Iterator i = infraEnv.getAgents().values().iterator();
			while (i.hasNext()) {
				AgArch ag = (AgArch)i.next();
				ag.getTS().receiveSyncSignal();
			}
		}
	}
	
	
	/**
	 * @see jason.control.ExecutionControlInfraTier#getAgentsName()
	 */
	public Collection getAgentsName() {
		return infraEnv.getAgents().keySet();
	}
	
    public int getAgentsQty() {
    	return infraEnv.getAgents().size();
    }
	
	/**
	 *  @see jason.control.ExecutionControlInfraTier#getAgState(java.lang.String)
	 */
	public Document getAgState(String agName) {
		return infraEnv.getAgent(agName).getTS().getAg().getAgState();
	}
}
