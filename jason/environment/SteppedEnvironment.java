package jason.environment;

import jason.asSyntax.Literal;
import jason.asSyntax.Structure;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * General environment class that "synchronise" all agents actions. 
 * It waits one action for each agent and, when all actions is received, 
 * executes them.
 * 
 * @author Jomi
 *
 */
public class SteppedEnvironment extends Environment {

	private Logger logger = Logger.getLogger(SteppedEnvironment.class.getName());

    /** Policy used when a second action is requested and the agent still has another action pending execution */
	public enum OverActionsPolicy {
    	/** Queue the second action request for future execution */
    	queue, 
    	
    	/** Fail the second action */
    	failSecond,
    	
    	/** Ignore the second action, it is considered as successfully executed */
    	ignoreSecond
	};
    	
	private int step = 0;   // step counter
    private int nbAgs = -1; // number of agents acting on the environment
    private Map<String,ActRequest> requests = new HashMap<String,ActRequest>(); // actions to be executed
    private Queue<ActRequest> overRequests = new LinkedList<ActRequest>(); // second action tentative in the step  
    private TimeOutThread timeoutThread = null;
    private long stepTimeout = 0;

    private OverActionsPolicy overActPol = OverActionsPolicy.ignoreSecond;
    
    public SteppedEnvironment() {
    	super(2);
	}
    
    @Override
    public void init(String[] args) {
    	super.init(args);
    	   	
    	if (args.length > 0) {
    		try {
    			stepTimeout = Integer.parseInt(args[0]);
    		} catch (Exception e) {
    			logger.warning("The argument "+args[0]+" is not a valid number for step timeout");
    		}
    	}
		
		stepStarted(step);
    }
    
    @Override
    public void stop() {
    	super.stop();
    	if (timeoutThread != null) timeoutThread.interrupt();
    }
    
    
    /** 
     * 	Updates the number of agents using the environment, this default
     *  implementation, considers all agents in the MAS as actors in the
     *  environment.
     */
    protected void updateNumberOfAgents() {
    	nbAgs = getEnvironmentInfraTier().getRuntimeServices().getAgentsName().size();
    }

    /** returns the current step counter */
    public int getStep() {
    	return step;
    }
    
    /** 
     * Sets the policy used for the second ask for an action while another action is not finished yet.
     * If set as queue, the second action is added in a queue for future execution 
     * If set as failSecond, the second action fails.
     */
    public void setOverActionsPolicy(OverActionsPolicy p) {
    	overActPol = p;
    }
    
    @Override
    public void scheduleAction(String agName, Structure action, Object infraData) {
		ActRequest newRequest = new ActRequest(agName, action, requiredSteps(agName, action), infraData);

		boolean startNew = false;
		
		synchronized (requests) { // lock access to requests
	    	if (nbAgs < 0) {
	    		// initialise dynamic information
	    		// (must be in sync part, so that more agents do not start the timeout thread)
	    		updateNumberOfAgents();
	        	if (stepTimeout > 0 && timeoutThread == null) {
	        		timeoutThread = new TimeOutThread(stepTimeout);
	        		timeoutThread.start();
	        	}
	    	}

	    	// if the agent already has an action scheduled, fail the first
    		ActRequest inSchedule = requests.get(agName);
    		if (inSchedule != null) {
    			if (overActPol == OverActionsPolicy.queue) {
    				overRequests.offer(newRequest);
    			} else if (overActPol == OverActionsPolicy.failSecond) {
					getEnvironmentInfraTier().actionExecuted(agName, action, false, infraData);
    			} else if (overActPol == OverActionsPolicy.ignoreSecond) {
    				getEnvironmentInfraTier().actionExecuted(agName, action, true, infraData);
    			}	
    		} else {
				// store the action request 		
				requests.put(agName, newRequest);
		
		    	// wait all agents to sent their actions
				if (requests.size() == nbAgs) {
					startNew = true;
		    	}
    		}
    	}
		
		if (startNew) {
			// starts the execution of the next step by another thread, so to not look the agent thread
			executor.execute(new Runnable() {
				public void run() {
					if (timeoutThread != null) timeoutThread.allAgFinished();
					startNewStep();
				}
			});			
		}
    }
    
    private void startNewStep() {
    	synchronized (requests) {

			//logger.info("#"+requests.size());
    		//logger.info("#"+overRequests.size());
			
            try {

	    		// execute all scheduled actions
				for (ActRequest a: requests.values()) {
					a.remainSteps--;
					if (a.remainSteps == 0) {
						// calls the user implementation of the action
						a.success = executeAction(a.agName, a.action);
					}
				}
				
				// notify the agents about the result of the execution
				Iterator<ActRequest> i = requests.values().iterator();
				while (i.hasNext()) {
					ActRequest a = i.next();
					if (a.remainSteps == 0) {
						getEnvironmentInfraTier().actionExecuted(a.agName, a.action, a.success, a.infraData);
						i.remove();
					}
				}
            	
				// clear all requests
				//requests.clear();
				
				// add actions waiting in over requests into the requests
				Iterator<ActRequest> io = overRequests.iterator();
				while (io.hasNext()) {
					ActRequest a = io.next();
					if (requests.get(a.agName) == null) {
						requests.put(a.agName, a);
						io.remove();
					}
				}
				
				// the over requests could complete the requests
		    	// so test end of step again
				if (requests.size() == nbAgs) {
					startNewStep();
		    	}
            } catch (Exception ie) {
                if (!(ie instanceof InterruptedException)) {
                    logger.log(Level.WARNING, "act error!",ie);
                }
            }
			
    	}
    	step++;
    	stepStarted(step);    	
    }
    
    /** to be overridden by the user class */
    protected void stepStarted(int step) {
    }

    /** to be overridden by the user class */
    protected void stepFinished(int step, long time, boolean timeout) {    	
    }
    
    protected int requiredSteps(String agName, Structure action) {
    	return 1;
    }
    
    /** stops perception while executing the step's actions */
    @Override
    public List<Literal> getPercepts(String agName) {
    	synchronized (requests) {
    		return super.getPercepts(agName);
    	}
    }
    
    class ActRequest {
    	String agName;
    	Structure action;
    	Object infraData;
    	boolean success; 
    	int remainSteps; // the number os steps this action have to wait to be executed
    	public ActRequest(String ag, Structure act, int rs, Object data) {
    		agName = ag;
    		action = act;
    		infraData = data;
    		remainSteps = rs;
		}
    	public boolean equals(Object obj) {
    		return agName.equals(obj);
    	}
    	public int hashCode() {
    		return agName.hashCode();
    	}
    	public String toString() {
    		return "["+agName+","+action+"]";
    	}
    }
    
    class TimeOutThread extends Thread {
		Lock lock = new ReentrantLock();
		Condition agActCond = lock.newCondition();
    	long timeout = 0;

    	public TimeOutThread(long to) {
    		super("EnvironmentTimeOutThread");
    		timeout = to;
		}
    	
    	public void allAgFinished() {
    		lock.lock();
    		agActCond.signal();
    		lock.unlock();
    	}
    	
		public void run() {
			try {
				while (true) {
		    		lock.lock();
		    		long lastStepStart = System.currentTimeMillis();
		    		boolean byTimeOut = !agActCond.await(timeout, TimeUnit.MILLISECONDS);
					long now  = System.currentTimeMillis();
		    		long time = (now-lastStepStart);
		    		stepFinished(step, time, byTimeOut);
		    		lock.unlock();
					
		    		if (byTimeOut) {
						startNewStep();
					}
				}
			} catch (InterruptedException e) {				
			} catch (Exception e) {
				logger.log(Level.SEVERE, "Error in timeout thread!",e);
			}
		}
    }    
}

