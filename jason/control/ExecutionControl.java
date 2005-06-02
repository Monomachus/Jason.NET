package jason.control;




/**
 * Base class for the user implementation of execution control.
 * 
 * This default implementation synchronize the agents execution, i.e.,
 * each agent will perform its next reasoning cycle only when all agents have finished its 
 * reasoning cycle.
 */
public class ExecutionControl {

	protected ExecutionControlInterface fJasonControl = null;
	
	private int nbFinished = 0; // the number of agents that have finished its reasoning cycle
	private int cycleNumber = 0;

	private Object syncAgFinished = new Object();
	//private Object syncAllAgFinished = new Object();

	protected String jasonDir = "..";


	public ExecutionControl() {

		// create a thread to wait ag Finished signals
		new Thread("ExecControlWaitAgFinish") {
			public void run() {
				synchronized(syncAgFinished) {
					while (true) {
						try {
							syncAgFinished.wait(); // waits notify
							if (fJasonControl != null) { 
								if (nbFinished == fJasonControl.getAgentsQty()) {
									allAgsFinished();
									//setAllAgFinished();
									nbFinished = 0;
									cycleNumber++;
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		}.start();
		
		// create a thread to control AllAgFinished
		// this thread is necessary since the user allAgsFinished method could take
		// many time to run and the allAgFinished control could lose the notification
		// that an agent has finished
		/*
		new Thread("ExecControlWaitAllAgFinish") {
			public void run() {
				synchronized(syncAllAgFinished) {
					while (true) {
						try {
							syncAllAgFinished.wait();
							allAgsFinished();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		}.start(); 
		*/
	}
	
	
	/** 
	 * Called by the agent when it finishes its reasoning cycle
	 */
	public void receiveFinishedCycle(String agName) {
		//System.out.println("agent "+agName+" has finished a cycle, total nb of ags is "+fJasonControl.getAgentsQty());
		synchronized(syncAgFinished) {
			nbFinished++;
			syncAgFinished.notifyAll();
		}
	}

	public void setJasonExecutionControl(ExecutionControlInterface jasonControl) {
		fJasonControl = jasonControl;
	}
	
	public ExecutionControlInterface getJasonExecutionControl() {
		return fJasonControl;
	}

	public void setJasonDir(String dir) {
		if (dir.startsWith("'")) {
			dir = dir.substring(1, dir.length()-1);
		}
		jasonDir = dir;
	}

	/**
	 * This method is called when setJasonExecutionControl and setJasonDir was already called
	 */
	public void init() {
		
	}
	
	/*
	private void waitAgFinish() {
		try {
			synchronized(syncAgFinished) {
				syncAgFinished.wait(1000); // waits notify
			
				int nbAgs = fJasonControl.getAgentsQty();
				if (nbFinished == nbAgs) {
						setAllAgFinished();
						nbFinished = 0;
						cycleNumber++;
				}
			}
		} catch (Exception e) {
			if (fJasonControl != null) {
				e.printStackTrace();
			}
		}
	}
	*/

	/*
	private void setAllAgFinished() {
		synchronized(syncAllAgFinished) {
			syncAllAgFinished.notifyAll();
		}
	}
	*/
	
	/*
	private void waitAllAgFinised() {
		try {
			synchronized(syncAllAgFinished) {
				syncAllAgFinished.wait();
				allAgsFinished();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	*/
	
	/** called when all agents have finished the current cycle */
	protected void allAgsFinished() {
		fJasonControl.informAllAgToPerformCycle();
		System.out.println("starting cycle "+cycleNumber);
	}

	public int getCycleNumber() {
		return cycleNumber;
	}
}
