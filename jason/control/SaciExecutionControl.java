package jason.control;

import jason.JasonException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;

import saci.MBoxSAg;
import saci.Message;
import saci.MessageHandler;

/**
 * Concrete execution control implementation based on saci distributed architecture.
 */
public class SaciExecutionControl extends saci.Agent implements ExecutionControlInterface {

	private ExecutionControl fUserControl;

    public void initAg(String[] args) throws JasonException {
        // create the user controller
        try {
        	System.out.println("Creating controller from "+args[1]+" asHome is "+args[0]);
        	fUserControl = (ExecutionControl)Class.forName(args[1]).newInstance();
        	fUserControl.setJasonExecutionControl(this);
        	fUserControl.setJasonDir(args[0]);
        	fUserControl.init();
        } catch (Exception e) {
            System.err.println("Error "+e);
            e.printStackTrace();
            throw new JasonException("The user execution control class instantiation '"+args[1]+"' has failed!"+e.getMessage());
        }
        
        try {
        	// message handler for "informCycleFinished"
            mbox.addMessageHandler("cycleFinished", "tell", null, "AS-ExecControl", new MessageHandler() {
                public boolean processMessage(saci.Message m) {
                	String sender = (String)m.get("sender");
            		fUserControl.receiveFinishedCycle(sender);
                    return true; // no other message handler gives this message
                }
            });
            
        } catch (Exception e) {
            System.err.println("Error adding message handler for agent:"+e);
            e.printStackTrace();
        }
    }
    
    public void run() {
    	try {
    		Thread.sleep(1000); // gives a time to agents enter in wait
    	} catch (Exception e) {}
    	informAllAgToPerformCycle();
    }
	
	public ExecutionControl getUserControl() {
		return fUserControl;
	}
    

	/**
	 * @see jason.control.ExecutionControlInterface#informAgToPerformCycle(java.lang.String)
	 */
	public void informAgToPerformCycle(String agName) {
	    Message m = new Message("(tell)");
	    m.put("ontology", "AS-ExecControl");
	    m.put("receiver", agName);
	    m.put("content", "performCycle");
	    try {
	    	mbox.sendMsg(m);
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
	}

	/**
	 * @see jason.control.ExecutionControlInterface#informAllAgToPerformCycle()
	 */
	public void informAllAgToPerformCycle() {
	    Message m = new Message("(tell)");
	    m.put("ontology", "AS-ExecControl");
	    m.put("content", "performCycle");
	    try {
	    	mbox.broadcast(m);
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
	}

	/**
	 * @see jason.control.ExecutionControlInterface#getAgentsName()
	 */
	public Collection getAgentsName() {
		try {
			Map ags = ((MBoxSAg)mbox).getFacilitator().getAllWP();
			
			List l = new ArrayList(ags.size());
			Iterator ia = ags.keySet().iterator();
            while (ia.hasNext()) {
            	l.add(ia.next());
            }
            return l;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

		
	
	/* 
	 * @see jason.control.ExecutionControlInterface#getAgentsQty()
	 */
	public int getAgentsQty() {
		try {
			return ((MBoxSAg)mbox).getFacilitator().getAgQty() - 3; // do not include controller, environment, and facilitator 
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	
	/**
	 *  @see jason.control.ExecutionControlInterface#getAgState(java.lang.String)
	 */
	public Document getAgState(String agName) {
	    Message m = new Message("(ask)");
	    m.put("ontology", "AS-ExecControl");
	    m.put("receiver", agName);
	    m.put("content", "agState");
	    try {
	    	Message r = mbox.ask(m);

	    	//System.out.println("** ans = "+r.get("content"));
	        //System.out.println(r.get("content").getClass().getName()+" = "+m.get("content"));
	    	
	    	return  (Document)r.get("content");
	    } catch (Exception e) {
	    	System.err.println("Error receiving agent state "+e);
	    	e.printStackTrace();
	    }
		return null;
	}
}
