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
//   Revision 1.11  2006/02/28 15:11:29  jomifred
//   improve javadoc
//
//   Revision 1.10  2006/02/27 18:46:26  jomifred
//   creation of the RuntimeServices interface
//
//   Revision 1.9  2006/02/17 13:13:16  jomifred
//   change a lot of method/classes names and improve some comments
//
//   Revision 1.8  2006/01/04 03:00:46  jomifred
//   using java log API instead of apache log
//
//   Revision 1.7  2005/08/12 20:52:18  jomifred
//   change in the informAgs method name
//
//
//----------------------------------------------------------------------------

package jason.control;

import jason.runtime.RuntimeServicesInfraTier;

import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Base class for the user implementation of execution control.
 * 
 * <p>This default implementation synchronise the agents execution, i.e.,
 * each agent will perform its next reasoning cycle only when all agents have 
 * finished its reasoning cycle.
 * 
 * <p>Execution sequence:
 *    <ul><li>setExecutionControlInfraTier, 
 *        <li>init, 
 *        <li>(receivedFinishedCycle)*, 
 *        <li>stop.
 *    </ul>
 */
public class ExecutionControl {

	protected ExecutionControlInfraTier infraControl = null;
	
	private int nbFinished = 0; // the number of agents that have finished its reasoning cycle
	private int cycleNumber = 0;

	private Object syncAgFinished = new Object();

	static Logger logger = Logger.getLogger(ExecutionControl.class.getName());
	RuntimeServicesInfraTier runtime;

	public ExecutionControl() {

		// create a thread to wait ag Finished signals
		new Thread("ExecControlWaitAgFinish") {
			public void run() {
				int tries = 0;
				synchronized(syncAgFinished) {
					while (true) {
						try {
							syncAgFinished.wait(1000); // waits notify
							if (runtime != null) { 
								if (nbFinished >= runtime.getAgentsQty() || tries > 6) {
									nbFinished = 0;
									allAgsFinished();
									//setAllAgFinished();
									cycleNumber++;
									tries = 0;
								} else {
									tries++;
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		}.start();
	}
	
	
	/** 
	 * Called when the agent <i>agName</i> has finished its reasoning cycle.
	 * <i>breakpoint</i> is true in case the agent selected one plan with "breakpoint" 
	 * annotation. 
	  */
	public void receiveFinishedCycle(String agName, boolean breakpoint) {
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("Agent "+agName+" has finished a cycle, # of finished agents is "+(nbFinished+1)+"/"+runtime.getAgentsQty());
			if (breakpoint) {
				logger.fine("Agent "+agName+" reached a breakpoint");				
			}
		}
		synchronized(syncAgFinished) {
			nbFinished++;
			syncAgFinished.notifyAll();
		}
	}

	public void setExecutionControlInfraTier(ExecutionControlInfraTier jasonControl) {
		infraControl = jasonControl;
		runtime = infraControl.getRuntimeServices();
	}
	
	public ExecutionControlInfraTier getExecutionControlInfraTier() {
		return infraControl;
	}

	/**
	 * This method is called when setJasonExecutionControl and setJasonDir was already called
	 */
	public void init() {
		
	}
	
	/**
	 * This method is called when MAS execution is being finished
	 */
	public void stop() {
		
	}
	
	/** Called when all agents have finished the current cycle */
	protected void allAgsFinished() {
		infraControl.informAllAgsToPerformCycle();
		logger.fine("starting cycle "+cycleNumber);
	}

	public int getCycleNumber() {
		return cycleNumber;
	}
}
