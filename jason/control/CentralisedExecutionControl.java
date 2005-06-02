package jason.control;

import jason.JasonException;
import jason.architecture.CentralisedAgArch;
import jason.environment.CentralisedEnvironment;

import java.util.Collection;
import java.util.Iterator;

import org.w3c.dom.Document;

/**
 * Concrete execution control implementation based on centralised architecture.
 */
public class CentralisedExecutionControl implements ExecutionControlInterface {

	private CentralisedEnvironment    fEnv;
	private ExecutionControl          fUserControl;
	
	public CentralisedExecutionControl(CentralisedEnvironment env, String userControlClass, String jasonDir) throws JasonException {
		fEnv = env;
        try {
        	fUserControl = (ExecutionControl)Class.forName(userControlClass).newInstance();
        	fUserControl.setJasonExecutionControl(this);
        	fUserControl.setJasonDir(jasonDir);
        	fUserControl.init();
        } catch (Exception e) {
            System.err.println("Error "+e);
            e.printStackTrace();
            throw new JasonException("The user execution control class instantiation '"+userControlClass+"' has failed!"+e.getMessage());
        }
	}
	
	public ExecutionControl getUserControl() {
		return fUserControl;
	}
	
	public CentralisedEnvironment getJasonEnvironment() {
		return fEnv;
	}
	
	/** 
	 * @see jason.control.ExecutionControlInterface#receiveFinishedCycle(java.lang.String)
	 */
	public void receiveFinishedCycle(String agName) {
		// pass to user controller
		fUserControl.receiveFinishedCycle(agName);
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
