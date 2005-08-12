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
//   Revision 1.5  2005/08/12 20:41:35  jomifred
//   add cvs keywords
//
//----------------------------------------------------------------------------

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
        	System.out.println("Creating controller from "+args[0]);//+" Jason Home is "+args[0]);
        	fUserControl = (ExecutionControl)Class.forName(args[0]).newInstance();
        	fUserControl.setJasonExecutionControl(this);
        	//fUserControl.setJasonDir(args[0]);
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
                	boolean breakpoint = false;
                	if (m.get("breakpoint") != null) {
                		breakpoint = m.get("breakpoint").equals("true");
                	}
            		fUserControl.receiveFinishedCycle(sender, breakpoint);
                    return true; // no other message handler gives this message
                }
            });
            
        } catch (Exception e) {
            System.err.println("Error adding message handler for agent:"+e);
            e.printStackTrace();
        }
    }
    
    public void stopAg() {
		super.stopAg();
		fUserControl.stop();
	}

	public void run() {
    	try {
    		Thread.sleep(1000); // gives a time to agents enter in wait
    	} catch (Exception e) {}
    	informAllAgsToPerformCycle();
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
	 * @see jason.control.ExecutionControlInterface#informAllAgsToPerformCycle()
	 */
	public void informAllAgsToPerformCycle() {
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
