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

package jason.control;

import jason.runtime.RuntimeServicesInfraTier;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
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
	
	private Set<String> finished = new HashSet<String>(); // the agents that have finished its reasoning cycle
	private int cycleNumber = 0;
    private boolean runningCycle = true;

	private Lock lock = new ReentrantLock();
    private Condition agFinishedCond = lock.newCondition();

	static Logger logger = Logger.getLogger(ExecutionControl.class.getName());
	RuntimeServicesInfraTier runtime;

	public ExecutionControl() {

		// create a thread to wait ag Finished signals
		new Thread("ExecControlWaitAgFinish") {
			public void run() {
                lock.lock();
                try {
					while (true) {
						try {
                            agFinishedCond.await(5, TimeUnit.SECONDS); // waits signal
                            if (runtime != null && runningCycle) { 
                                runningCycle = false;
                                allAgsFinished();
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				} finally {
				    lock.unlock();
                }
			}
		}.start();
	}

    protected void startNewCycle() {
        runningCycle = true;
        finished.clear();
        cycleNumber++;        
    }
    
	
	/** 
	 * Called when the agent <i>agName</i> has finished its reasoning cycle.
	 * <i>breakpoint</i> is true in case the agent selected one plan with "breakpoint" 
	 * annotation. 
	  */
	public void receiveFinishedCycle(String agName, boolean breakpoint, int cycle) {
        if (cycle == this.cycleNumber && runningCycle) { // the agent finished the current cycle
            lock.lock();
            try {                
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("Agent "+agName+" has finished the cycle "+cycle+", # of finished agents is "+(finished.size()+1)+"/"+runtime.getAgentsQty());
                    if (breakpoint) logger.fine("Agent "+agName+" reached a breakpoint");               
                }

                finished.add(agName);
                if (finished.size() >= runtime.getAgentsQty()) {
                    agFinishedCond.signal();
                }
    		} finally {
    		    lock.unlock();
            }
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
	 * This method is called when setExecutionControlInfraTier was already called
	 */
	public void init(String[] args) {
	}
	
	/**
	 * This method is called when MAS execution is being finished
	 */
	public void stop() {
	}
	
	/** Called when all agents have finished the current cycle */
	protected void allAgsFinished() {
        startNewCycle();
		infraControl.informAllAgsToPerformCycle(cycleNumber);
		logger.fine("starting cycle "+cycleNumber);
	}

	public int getCycleNumber() {
		return cycleNumber;
	}
    
    public void setRunningCycle(boolean rc) {
        runningCycle = rc;
    }
    
}
