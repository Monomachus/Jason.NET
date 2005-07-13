package jason.control;

import jason.JasonException;
import jason.architecture.CentralisedAgArch;
import jason.environment.CentralisedEnvironment;

import java.util.Collection;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

/**
 * Concrete execution control implementation based on centralised architecture.
 */
public class CentralisedExecutionControl implements ExecutionControlInterface {

	private CentralisedEnvironment    fEnv;
	private ExecutionControl          fUserControl;
	
	static Logger logger = Logger.getLogger(CentralisedExecutionControl.class);
	
	public CentralisedExecutionControl(CentralisedEnvironment env, String userControlClass) throws JasonException {
		fEnv = env;
        try {
        	fUserControl = (ExecutionControl)Class.forName(userControlClass).newInstance();
        	fUserControl.setJasonExecutionControl(this);
        	//fUserControl.setJasonDir(jasonDir);
        	fUserControl.init();
        } catch (Exception e) {
            logger.error("Error ",e);
            throw new JasonException("The user execution control class instantiation '"+userControlClass+"' has failed!"+e.getMessage());
        }
	}
	
	/**
	 * This method is called when MAS execution is being finished
	 */
	public void stop() {
		fUserControl.stop();
	}


	
	public ExecutionControl getUserControl() {
		return fUserControl;
	}
	
	public CentralisedEnvironment getJasonEnvironment() {
		return fEnv;
	}
	
	/** 
	 * Called (by the ag arch) when the agent <i>agName</i> has finished its reasoning cycle.
	 * <i>breakpoint</i> is true in case the agent selected one plan with "breakpoint" 
	 * annotation.
     */
	public void receiveFinishedCycle(String agName, boolean breakpoint) {
		// pass to user controller
		fUserControl.receiveFinishedCycle(agName, breakpoint);
	}

	/**
	 * @see jason.control.ExecutionControlInterface#informAgToPerformCycle(java.lang.String)
	 */
	public void informAgToPerformCycle(String agName) {
		// call the agent method to "go on"
		fEnv.getAgent(agName).getTS().receiveSyncSignal();
	}

	/**
	 * @see jason.control.ExecutionControlInterface#informAllAgToPerformCycle()
	 */
	public void informAllAgToPerformCycle() {
		synchronized(fEnv.getAgents()) { 
			Iterator i = fEnv.getAgents().values().iterator();
			while (i.hasNext()) {
				CentralisedAgArch ag = (CentralisedAgArch)i.next();
				ag.getTS().receiveSyncSignal();
			}
		}
	}
	
	
	/**
	 * @see jason.control.ExecutionControlInterface#getAgentsName()
	 */
	public Collection getAgentsName() {
		return fEnv.getAgents().keySet();
	}
	
    public int getAgentsQty() {
    	return fEnv.getAgents().size();
    }
	
	/**
	 *  @see jason.control.ExecutionControlInterface#getAgState(java.lang.String)
	 */
	public Document getAgState(String agName) {
		return fEnv.getAgent(agName).getTS().getAg().getAgState();
	}
}
