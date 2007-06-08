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


package jason.infra.centralised;

import jason.JasonException;
import jason.asSemantics.ActionExec;
import jason.asSemantics.TransitionSystem;
import jason.asSyntax.Structure;
import jason.environment.Environment;
import jason.environment.EnvironmentInfraTier;
import jason.mas2j.ClassParameters;
import jason.runtime.RuntimeServicesInfraTier;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * This class implements the centralised version of the environment infrastructure tier.
 */
public class CentralisedEnvironment implements EnvironmentInfraTier {

    /** the user customisation class for the environment */
	private Environment fUserEnv;

	private RunCentralisedMAS masRunner = null;
    
	ExecutorService executor; // the thread pool used to execute actions
	
    static Logger logger = Logger.getLogger(CentralisedEnvironment.class.getName());
	
    public CentralisedEnvironment(ClassParameters userEnv, RunCentralisedMAS masRunner) throws JasonException {
        this.masRunner = masRunner;
        try { 
			fUserEnv = (Environment) getClass().getClassLoader().loadClass(userEnv.className).newInstance();
			fUserEnv.setEnvironmentInfraTier(this);
			fUserEnv.init(userEnv.getParametersArray());
        } catch (Exception e) {
            logger.log(Level.SEVERE,"Error in Centralised MAS environment creation",e);
            throw new JasonException("The user environment class instantiation '"+userEnv+"' has failed!"+e.getMessage());
        }

        // creates and executor with 1 core thread
		// where no more than 3 tasks will wait for a thread
		// The max number of thread is 1000 (so the 1001 task will be rejected) 
		// Threads idle for 10 sec. will be removed from the pool
        //executor= new ThreadPoolExecutor(1,1000,10,TimeUnit.SECONDS,new ArrayBlockingQueue<Runnable>(3));
        
        // creates a thread pool with 4 threads
        executor = Executors.newFixedThreadPool(4);
    }
	
	/** called before the end of MAS execution, it just calls the user environment class stop method. */
	public void stop() {
		fUserEnv.stop();
		executor.shutdownNow();
	}

    public Environment getUserEnvironment() {
        return fUserEnv;
    }

    public void act(final String agName, final ActionExec action, final List<ActionExec> feedback, final TransitionSystem ts) {
    	if (executor.isTerminated()) return;
    	executor.execute(new Runnable() {
    		public void run() {
    			try {
	    			Structure acTerm = action.getActionTerm();
	    	        if (fUserEnv.executeAction(agName, acTerm)) {
	    	            action.setResult(true);
	    	        } else {
	    	            action.setResult(false);
	    	        }
	    	        feedback.add(action);
	    	        ts.newMessageHasArrived();
    			} catch (Exception ie) {
    				if (!(ie instanceof InterruptedException)) {
    					logger.log(Level.WARNING, "act error!",ie);
    				}
    			}
    		}
    	});
    }
    
    public void informAgsEnvironmentChanged() {
        for (CentralisedAgArch ag: masRunner.getAgs().values()) {
            ag.getUserAgArch().getTS().newMessageHasArrived();
        }
    }

    public void informAgsEnvironmentChanged(Collection<String> agentsToNotify) {
        if (agentsToNotify == null) {
            informAgsEnvironmentChanged();
        } else {
            for (String agName: agentsToNotify) {
            	CentralisedAgArch ag = masRunner.getAg(agName);
                if (ag != null) {
                    ag.getUserAgArch().getTS().newMessageHasArrived();
                } else {
                    logger.log(Level.SEVERE, "Error sending message notification: agent " + agName + " does not exist!");
                }
            }
        }
    }

    public RuntimeServicesInfraTier getRuntimeServices() {
        return new CentralisedRuntimeServices(masRunner);
    }
}
