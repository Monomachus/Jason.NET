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
//   Revision 1.3  2006/03/02 13:33:40  jomifred
//   changes in MASLauncher interface
//
//   Revision 1.2  2006/03/01 17:25:44  jomifred
//   fix bug in using masconsole
//
//   Revision 1.1  2006/02/18 15:24:30  jomifred
//   changes in many files to detach jason kernel from any infrastructure implementation
//
//   Revision 1.5  2006/02/17 13:13:16  jomifred
//   change a lot of method/classes names and improve some comments
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


package jason.infra.centralised;

import jason.JasonException;
import jason.architecture.AgArch;
import jason.control.ExecutionControlGUI;
import jason.mas2j.AgentParameters;
import jason.mas2j.MAS2JProject;
import jason.mas2j.parser.ParseException;
import jason.runtime.MASConsoleGUI;
import jason.runtime.Settings;

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

import javax.swing.ImageIcon;
import javax.swing.JButton;

/**
 * Runs MASProject using centralised infrastructure.
 */
public class RunCentralisedMAS {
    
    CentralisedEnvironment env = null;
    CentralisedExecutionControl control = null;
    static boolean debug = false;
    public JButton btDebug;
    
    List ags = new ArrayList();
    
    private static Logger logger = Logger.getLogger(RunCentralisedMAS.class.getName());
    private static RunCentralisedMAS runner = null;
    
    public final static String logPropFile = "logging.properties";
    
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("You should inform the MAS project file.");
            System.exit(1);
        }

        setupLogger();
        
        if (args.length > 1) {
        	if (args[1].equals("-debug")) {
        		debug = true;
        		logger.getLogger("").setLevel(Level.FINE);
        	}
        }
        
        runner = new RunCentralisedMAS();

        int errorCode = 0;
        
    	MAS2JProject project = null;
		try {
			jason.mas2j.parser.mas2j parser = new jason.mas2j.parser.mas2j(new FileReader(args[0]));
	    	project = parser.mas();

			runner.createAg(project, debug);
			runner.startAgs();
			runner.startSyncMode();
			
	        if (MASConsoleGUI.hasConsole()) {
				// add Button
		        JButton btStop = new JButton("Stop", new ImageIcon(RunCentralisedMAS.class.getResource("/images/suspend.gif")));
		        btStop.addActionListener(new ActionListener() {
		            public void actionPerformed(ActionEvent evt) {
		            	MASConsoleGUI.get().setPause(false);
		            	runner.finish();
		            }
		        });
		        MASConsoleGUI.get().addButton(btStop);

				// add Button
		        runner.btDebug = new JButton("Debug", new ImageIcon(RunCentralisedMAS.class.getResource("/images/debug.gif")));
		        runner.btDebug.addActionListener(new ActionListener() {
		            public void actionPerformed(ActionEvent evt) {
		            	runner.changeToDebugMode();
		            	runner.btDebug.setEnabled(false);
		            	if (runner.control != null) {
		            		try {
		            			ExecutionControlGUI ecg = (ExecutionControlGUI)runner.control.getUserControl();
		            			ecg.setRunMode(false);
		            		} catch (Exception e) {}
		            	}
		            }
		        });
		        if (debug) {
	            	runner.btDebug.setEnabled(false);		        	
		        }
		        MASConsoleGUI.get().addButton(runner.btDebug);

		        
		        // add Button pause
		        final JButton btPause = new JButton("Pause", new ImageIcon(RunCentralisedMAS.class.getResource("/images/resume_co.gif")));
		        btPause.addActionListener(new ActionListener() {
		            public void actionPerformed(ActionEvent evt) {
		            	if (MASConsoleGUI.get().isPause()) {
		            		btPause.setText("Pause");
		            		MASConsoleGUI.get().setPause(false);
		            	} else {
		            		btPause.setText("Continue");
		            		MASConsoleGUI.get().setPause(true);
		            	}
		            	
		            }
		        });
		        MASConsoleGUI.get().addButton(btPause);

		        // add Button start
		        final JButton btStartAg = new JButton("Start new agent", new ImageIcon(RunCentralisedMAS.class.getResource("/images/newAgent.gif")));
		        btStartAg.addActionListener(new ActionListener() {
		            public void actionPerformed(ActionEvent evt) {
		            	new StartNewAgentGUI(MASConsoleGUI.get().getFrame(), "Start a new agent to run in current MAS");
		            }
		        });
		        MASConsoleGUI.get().addButton(btStartAg);

		        // add Button kill
		        final JButton btKillAg = new JButton("Kill agent", new ImageIcon(RunCentralisedMAS.class.getResource("/images/killAgent.gif")));
		        btKillAg.addActionListener(new ActionListener() {
		            public void actionPerformed(ActionEvent evt) {
		            	new KillAgentGUI(MASConsoleGUI.get().getFrame(), "Kill an agent of the current MAS");
		            }
		        });
		        MASConsoleGUI.get().addButton(btKillAg);

		        MASConsoleGUI.get().setAsDefaultOut();
	        }
	    
	        runner.waitEnd();
	        errorCode = 0;
	        
		} catch (FileNotFoundException e1) {
			logger.log(Level.SEVERE, "File "+args[0]+" not found!");
			errorCode = 2;
		} catch (ParseException e) {
			logger.log(Level.SEVERE, "Error parsing file "+args[0]+"!",e);
			errorCode = 3;
		} catch (Exception e) {
			logger.log(Level.SEVERE,"Error!?: ",e);
			errorCode = 4;
		}
        System.out.close();
        System.err.close();

        if (!MASConsoleGUI.hasConsole() && errorCode != 0) {
        	System.exit(errorCode);
        }

    }
    
    
    public static boolean isDebug() {
    	return debug;
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

	/** change the current running MAS to debug mode */
	void changeToDebugMode() {
		try {
			if (control == null) {
				control = new CentralisedExecutionControl(env, ExecutionControlGUI.class.getName());
		        Iterator i = ags.iterator();
		        while (i.hasNext()) {
		            CentralisedAgArch ag = (CentralisedAgArch)i.next();
		            ag.setControlInfraTier(control);
		            Settings stts = ag.getUserAgArch().getTS().getSettings();
		            stts.setVerbose(2);
			    	stts.setSync(true);
			    	ag.getLogger().setLevel(Level.FINE);
			    	ag.getUserAgArch().getTS().getLogger().setLevel(Level.FINE);
			    	ag.getUserAgArch().getTS().getAg().getLogger().setLevel(Level.FINE);
		        }
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Error entering in debug mode",e);
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
