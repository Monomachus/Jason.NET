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

	// TODO: polices for more action per step tentative: fail_first, fail_second, queue
	
	private int step = 0;   // step counter
    private int nbAgs = -1; // number of agents acting on the environment
    private Map<String,ActRequest> requests = new HashMap<String,ActRequest>(); // actions to be executed
    private Queue<ActRequest> overRequests = new LinkedList<ActRequest>(); // second action tentative in the step  
    private TimeOutThread timeoutThread = null;
    private long stepTimeout = 0;
    
    public SteppedEnvironment() {
    	super(1);
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
    
    @Override
    public void scheduleAction(String agName, Structure action, Object infraData) {
    	synchronized (requests) {
        	if (nbAgs < 0) {
        		// init something
        		updateNumberOfAgents();
            	if (stepTimeout > 0 && timeoutThread == null) {
            		timeoutThread = new TimeOutThread(stepTimeout);
            		timeoutThread.start();
            	}
        	}
        	
    		ActRequest newRequest = new ActRequest(agName, action, infraData);
    		
    		// if the agent already has an action scheduled, fail the first
    		ActRequest inSchedule = requests.get(agName);
    		if (inSchedule != null) {
    			overRequests.offer(newRequest);
    			// logger.info("add over " + newRequest + " in step "+step);
    			// for fail_second:
    			//getEnvironmentInfraTier().actionExecuted(inSchedule.agName, inSchedule.action, false, inSchedule.infraData);
    		} else {
				// store the action request 		
				requests.put(agName, newRequest);
		
		    	// wait all agents to sent their actions
				if (requests.size() == nbAgs) {
					if (timeoutThread != null) timeoutThread.allAgFinished();
					startNewStep();
		    	}
    		}
    	}
    }
    
    private void startNewStep() {
    	step++;
    	stepStarted(step);
    	
    	synchronized (requests) {

			//logger.info("#"+requests.size());
			
    		// execute all scheduled actions
			for (ActRequest a: requests.values()) {
                try {
                    boolean success = executeAction(a.agName, a.action);
                    getEnvironmentInfraTier().actionExecuted(a.agName, a.action, success, a.infraData);
                } catch (Exception ie) {
                    if (!(ie instanceof InterruptedException)) {
                        logger.log(Level.WARNING, "act error!",ie);
                    }
                }
                //super.scheduleAction(a.agName, a.action, a.infraData);				
			}
			requests.clear();
			
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
			
    	}
    }
    
    /** to be overridden by the user class */
    protected void stepStarted(int step) {
    }

    /** to be overridden by the user class */
    protected void stepFinished(int step, long time, boolean timeout) {    	
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
    	public ActRequest(String ag, Structure act, Object data) {
    		agName = ag;
    		action = act;
    		infraData = data;
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
    
    // TODO: add step spend time
    
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

