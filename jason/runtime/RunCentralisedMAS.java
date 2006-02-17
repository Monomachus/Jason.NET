//----------------------------------------------------------------------------
// Copyright (C) 2003  Rafael H. Bordini and Jomi F. Hubner
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
//   Revision 1.5  2006/02/17 13:13:16  jomifred
//   change a lot of method/classes names and improve some comments
//
//   Revision 1.4  2006/02/16 13:33:22  jomifred
//   no message
//
//   Revision 1.3  2006/01/14 18:23:40  jomifred
//   centralised infra does not use xml script file anymore
//
//   Revision 1.2  2006/01/04 03:00:46  jomifred
//   using java log API instead of apache log
//
//   Revision 1.1  2005/12/08 20:14:28  jomifred
//   changes for JasonIDE plugin
//
//   Revision 1.19  2005/11/16 18:35:25  jomifred
//   fixed the print(int) on console bug
//
//   Revision 1.17  2005/10/30 18:39:48  jomifred
//   change in the AgArch customisation  support (the same customisation is used both to Cent and Saci infrastructures0
//
//   Revision 1.16  2005/10/19 21:41:51  jomifred
//   fixed the bug  continue/stop when running the MAS
//
//   Revision 1.15  2005/09/20 16:59:14  jomifred
//   do not use MASConsole when the logger in Console (and so, do not need an X11)
//
//   Revision 1.14  2005/08/12 21:08:23  jomifred
//   add cvs keywords
//
//----------------------------------------------------------------------------


package jason.runtime;

import jason.JasonException;
import jason.architecture.AgArch;
import jason.architecture.CentralisedAgArch;
import jason.control.CentralisedExecutionControl;
import jason.control.ExecutionControlGUI;
import jason.environment.CentralisedEnvironment;
import jason.mas2j.AgentParameters;
import jason.mas2j.MAS2JProject;
import jason.mas2j.parser.ParseException;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.swing.JButton;

/**
 * Runs a SACI script without SACI.
 * It is used to run the MAS without saci.
 */
public class RunCentralisedMAS {
    
    CentralisedEnvironment env = null;
    CentralisedExecutionControl control = null;
    List ags = new ArrayList();
    
    private static Logger logger = Logger.getLogger(RunCentralisedMAS.class.getName());
    private static RunCentralisedMAS runner = null;
    
    public final static String logPropFile = "logging.properties";
    
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("You should inform the MAS project file.");
            System.exit(1);
        }
        boolean debug = false;

        setupLogger();
        
        if (args.length > 1) {
        	if (args[1].equals("-debug")) {
        		debug = true;
        		logger.getLogger("").setLevel(Level.FINE);
        	}
        }
        
        runner = new RunCentralisedMAS();

        
    	MAS2JProject project = null;
		try {
			jason.mas2j.parser.mas2j parser = new jason.mas2j.parser.mas2j(new FileReader(args[0]));
	    	project = parser.mas();

			runner.createAg(project, debug);
			runner.startAgs();
			runner.startSyncMode();
			
		} catch (FileNotFoundException e1) {
			logger.log(Level.SEVERE, "File "+args[0]+" not found!");
			System.exit(2);
		} catch (ParseException e) {
			logger.log(Level.SEVERE, "Error parsing file "+args[0]+"!",e);
			System.exit(3);
		} catch (Exception e) {
			logger.log(Level.SEVERE,"Error!?: ",e);
			System.exit(4);
		}

        if (MASConsoleGUI.hasConsole()) {
			// add Button
	        JButton btStop = new JButton("Stop MAS");
	        btStop.addActionListener(new ActionListener() {
	            public void actionPerformed(ActionEvent evt) {
	            	MASConsoleGUI.get().setPause(false);
	            	runner.finish();
	            }
	        });
	        MASConsoleGUI.get().addButton(btStop);

	        // add Button
	        final JButton btPause = new JButton("Pause MAS");
	        btPause.addActionListener(new ActionListener() {
	            public void actionPerformed(ActionEvent evt) {
	            	if (MASConsoleGUI.get().isPause()) {
	            		btPause.setText("Pause MAS");
	            		MASConsoleGUI.get().setPause(false);
	            	} else {
	            		btPause.setText("Continue");
	            		MASConsoleGUI.get().setPause(true);
	            	}
	            	
	            }
	        });
	        MASConsoleGUI.get().addButton(btPause);
	        MASConsoleGUI.get().setAsDefaultOut();
        }
    
        runner.waitEnd();
    }
    
    
    public static void setupLogger() {
        // see for a local log configuration
        if (new File(logPropFile).exists()) {
        	try {
        		LogManager.getLogManager().readConfiguration(new FileInputStream(logPropFile));
        	} catch (Exception e) {
        		System.err.println("Error setting up logger:"+e);
        	}
        } else {
        	try {
        		LogManager.getLogManager().readConfiguration(RunCentralisedMAS.class.getResource("/"+logPropFile).openStream());
        	} catch (Exception e) {
        		System.err.println("Error setting up logger:"+e);
        	}
        	/*
            // remove current handlers
            Handler[] hs = Logger.getLogger("").getHandlers();
            for (int i = 0; i < hs.length; i++) {
            	Logger.getLogger("").removeHandler(hs[i]);
            }
            
            Handler h = new MASConsoleLogHandler(); 
            h.setFormatter(new MASConsoleLogFormatter());
            Logger.getLogger("").addHandler(h);
            Logger.getLogger("").setLevel(Level.INFO);
            */
        }
    }
    
    public static RunCentralisedMAS getRunner() {
    	return runner;
    }
    
    public CentralisedExecutionControl getControllerInfraTier() {
    	return control;
    }
    
    public CentralisedEnvironment getEnvironmentInfraTier() {
    	return env;
    }


    void createAg(MAS2JProject project, boolean debug) throws JasonException {
        
    	if (MASConsoleGUI.hasConsole()) {
    		MASConsoleGUI.get().setTitle("MAS Console - " + project.getSocName());
	    }
		
    	// create environment
        String envClass = project.getEnvClass();
        if (envClass == null) {
            envClass = jason.environment.Environment.class.getName();;
        }
        logger.info("Creating environment "+envClass);
        env = new CentralisedEnvironment(envClass);
    	
        // create the agents
    	Iterator ia = project.getAgents().iterator();
    	while (ia.hasNext()) {
    		AgentParameters ap = (AgentParameters)ia.next();
    		try {
                String agName = ap.name;

                String tmpAgClass = ap.agClass;
                if (tmpAgClass == null) {
                	tmpAgClass = jason.asSemantics.Agent.class.getName();
                }
                String tmpAgArchClass = ap.archClass;
                if (tmpAgArchClass == null) {
                	tmpAgArchClass = AgArch.class.getName();
                }
                String tmpAsSrc = project.getDirectory() + ap.asSource;

                for (int cAg=0; cAg < ap.qty; cAg++) {
                    String numberedAg = agName;
                    if (ap.qty > 1) {
                        numberedAg += (cAg+1);
                    }
                    logger.info("Creating agent "+numberedAg+" ("+(cAg+1)+"/"+ap.qty+")");
                    CentralisedAgArch agArch = new CentralisedAgArch();
                    agArch.setAgName(numberedAg);
                    agArch.setEnvInfraTier(env);
                    agArch.initAg(tmpAgArchClass, tmpAgClass, tmpAsSrc, ap.getAsSetts(debug, project.getControlClass() != null));
                    env.addAgent(agArch.getUserAgArch());
                    ags.add(agArch);
                }
    		} catch (Exception e) {
    			logger.log(Level.SEVERE,"Error creating agent "+ap.name,e);
    		}
    	}
    	
    	// create controller
        String controlClass = project.getControlClass();
		if (debug && controlClass == null) {
			controlClass = ExecutionControlGUI.class.getName();
		}
		if (controlClass != null) {
			logger.info("Creating controller "+controlClass);
	    	control = new CentralisedExecutionControl(env, controlClass);
		}
	}
    

	void startAgs() {
        // run the agents
        Iterator i = ags.iterator();
        while (i.hasNext()) {
            CentralisedAgArch ag = (CentralisedAgArch)i.next();
            ag.setControlInfraTier(control);
            ag.start();
        }
	}
	
	void stopAgs() {
        // run the agents
        Iterator i = ags.iterator();
        while (i.hasNext()) {
            CentralisedAgArch ag = (CentralisedAgArch)i.next();
            ag.stopAg();
        }
	}

	void startSyncMode() {
        if (control != null) {
            // start the execution, if it is controlled
    		try {
				Thread.sleep(500); // gives a time to agents enter in wait					
				control.informAllAgsToPerformCycle();
    		} catch (Exception e) {
    			e.printStackTrace();
            }	
        }		
	}
	
	void waitEnd() {
		try {
			int c = System.in.read();
			while (c != 1) { // RunProject prints "1" out to signal finishing
				c = System.in.read();
			}
			finish();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void finish() {
		try {
			stopAgs();
			
			if (control != null) {
				control.stop();
				control = null;
			}
			if (env != null) {
				env.stop();
				env = null;
			}
		
			if (MASConsoleGUI.hasConsole()) {
				MASConsoleGUI.get().close();
			}
			
			runner = null;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.exit(0);
	}
}
