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
//   Revision 1.3  2006/02/28 15:11:29  jomifred
//   improve javadoc
//
//   Revision 1.2  2006/02/27 18:46:26  jomifred
//   creation of the RuntimeServices interface
//
//   Revision 1.1  2006/02/18 15:24:30  jomifred
//   changes in many files to detach jason kernel from any infrastructure implementation
//
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

package jason.infra.centralised;

import jason.JasonException;
import jason.architecture.AgArch;
import jason.control.ExecutionControl;
import jason.control.ExecutionControlInfraTier;
import jason.runtime.RuntimeServicesInfraTier;

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
	
	public void stop() {
		userController.stop();
	}
	
	public ExecutionControl getUserControl() {
		return userController;
	}
	
	public CentralisedEnvironment getEnvInfraTier() {
		return infraEnv;
	}
	
	public void receiveFinishedCycle(String agName, boolean breakpoint) {
		// pass to user controller
		userController.receiveFinishedCycle(agName, breakpoint);
	}

	public void informAgToPerformCycle(String agName) {
		// call the agent method to "go on"
		infraEnv.getAgent(agName).getTS().receiveSyncSignal();
	}

	public void informAllAgsToPerformCycle() {
		synchronized(infraEnv.getAgents()) { 
			Iterator i = infraEnv.getAgents().values().iterator();
			while (i.hasNext()) {
				AgArch ag = (AgArch)i.next();
				ag.getTS().receiveSyncSignal();
			}
		}
	}
	
		
	public Document getAgState(String agName) {
		return infraEnv.getAgent(agName).getTS().getAg().getAgState();
	}

	public RuntimeServicesInfraTier getRuntimeServices() {
		return new CentralisedRuntimeServices();
	}
}
