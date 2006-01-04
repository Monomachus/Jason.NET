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
//   Revision 1.2  2006/01/04 03:00:46  jomifred
//   using java log API instead of apache log
//
//   Revision 1.1  2005/12/08 20:14:28  jomifred
//   changes for JasonIDE plugin
//
//   Revision 1.19  2005/11/16 18:35:25  jomifred
//   fixed the print(int) on console bug
//
//   Revision 1.18  2005/11/07 12:41:31  jomifred
//   no message
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

import jason.architecture.CentralisedAgArch;
import jason.control.CentralisedExecutionControl;
import jason.environment.CentralisedEnvironment;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Runs a SACI script without SACI.
 * It is used to run the MAS without saci.
 */
public class RunCentralisedMAS {
    
    CentralisedEnvironment env = null;
    CentralisedExecutionControl control = null;
    List ags = new ArrayList();
    boolean insideJIDE = false;
    
    private static Logger logger = Logger.getLogger(RunCentralisedMAS.class.getName());
    private static RunCentralisedMAS runner = null;
    
    public final static String logPropFile = "logging.properties";
    
    //static MASConsoleGUI console;
    
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("You should inform the MAS XML script only.");
            System.exit(1);
        }

        runner = new RunCentralisedMAS();

        setupLogger();
        
        Document docDOM = parse(args[0]);
        if (docDOM != null) {
			try {
				runner.createAg(docDOM);
				runner.startAgs();
				runner.startSyncMode();
			} catch (Exception e) {
				logger.log(Level.SEVERE,"Error!?: ",e);
			}

	        if (args.length == 2) { // from inside jIDE
	        	if (args[1].equals("insideJIDE")) {
	        		runner.insideJIDE = true;
	        	}
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
	        
	        if (! runner.insideJIDE) {
	        	runner.waitEnd();
	        }
        }
    }
    
    
    public static void setupLogger() {
        // see for a local log4j configuration
        if (new File(logPropFile).exists()) {
        	// PropertyConfigurator.configure(logPropFile);
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
            // PropertyConfigurator.configure(RunCentralisedMAS.class.getResource("/"+logPropFile));
        	//Logger.getRootLogger().addAppender(new ConsoleAppender(new PatternLayout("[%c{1}] %m%n")));
        	//Logger.getRootLogger().setLevel(Level.INFO);
        }
    }
    
    public static Document parse(String file) {
        try {
            if (file.startsWith("\"")) {
        			file = file.substring(1, file.length()-1);
            }
        	
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true); // se o parser considera o name space
			
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse( new File( file ) );
        } catch (Exception e) {
            logger.log(Level.SEVERE,"Error parsing the script file",e);
            return null;
        }
    }

    public static RunCentralisedMAS getRunner() {
    	return runner;
    }
    
	void createAg(Document docDOM) {
        
		// get soc nome
		try {
			Element app = (Element)docDOM.getElementsByTagName("application").item(0);
	        if (MASConsoleGUI.hasConsole()) {
	        	MASConsoleGUI.get().setTitle("MAS Console - " + app.getAttribute("id"));
	        }
		} catch (Exception e) {
			logger.log(Level.SEVERE,"Error getting the society name",e);
		}
		
        // create the agentes
        NodeList listAg = docDOM.getElementsByTagName("startAgent");
        for (int i=0; i<listAg.getLength(); i++) {
            Element sAg = (Element)listAg.item(i);

            String agName = sAg.getAttribute("name");
            try {
                if (agName.equals("environment")) {
                    logger.info("Creating environment "+sAg.getAttribute("class"));
                    env = new CentralisedEnvironment(sAg.getAttribute("class"));
                } else if (agName.equals("controller")) {
                	logger.info("Creating controller "+sAg.getAttribute("class"));
                	control = new CentralisedExecutionControl(env, sAg.getAttribute("class"));
                } else {
                    // it is an agent
                    int qty = 1;
                    try {
                        qty = Integer.parseInt(sAg.getAttribute("qty"));
                    } catch (Exception e) {}
                    String className = sAg.getAttribute("class");
                    String[] agArgs = getArrayFromString(sAg.getAttribute("args"));
                    for (int cAg=0; cAg<qty; cAg++) {
                        String numberedAg = agName;
                        if (qty > 1) {
                            numberedAg += (cAg+1);
                        }
                        logger.info("Creating agent "+numberedAg+" ("+(cAg+1)+"/"+qty+") from "+className);
                        CentralisedAgArch agArch = (CentralisedAgArch)Class.forName(className).newInstance();
                        agArch.setAgName(numberedAg);
                        agArch.setEnv(env);
                        agArch.initAg(agArgs);
                        env.addAgent(agArch.getUserAgArch());
                        ags.add(agArch);
                    }
                }
                
            } catch (Exception e) {
            	logger.log(Level.SEVERE,"Error creating agent "+agName,e);
            }
        } // for
	}

	void startAgs() {
        // run the agents
        Iterator i = ags.iterator();
        while (i.hasNext()) {
            CentralisedAgArch ag = (CentralisedAgArch)i.next();
            ag.setControl(control);
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
			while (c != 1) {
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
		if (!insideJIDE) {
			System.exit(0);
		}
	}
	
	
    public static String[] getArrayFromString(String s) {
        if (s == null) {
            return new String[0];
        }

        List v = new ArrayList();
        
        s = s.trim();
        String token = "";
        char lookingFor = ' ';
        for (int i = 0; i < s.length(); i++) {
        		if (s.charAt(i) == lookingFor) {
        			if (token.length() > 0 && lookingFor == ' ') {
        				token = token.trim();
        				if (token.startsWith("\"") && token.endsWith("\"")) {
        					token = token.substring(1,token.length()-1);
        				}
        				v.add(token);
            			token = "";
        			}
        			if (lookingFor == '\'') {
            			token += "\"";
        				lookingFor = ' ';
        			}
        		} else if (s.charAt(i) == '\'') {
        			lookingFor = '\'';
        			token += "\"";
        		} else {
        			token += s.charAt(i);
        		}
        }
        if (token.length() > 0) {
			token = token.trim();
			if (token.startsWith("\"") && token.endsWith("\"")) {
				token = token.substring(1,token.length()-1);
			}
			v.add(token);
        }
        
        String[] a = new String[v.size()];

        for (int i = 0; i < v.size(); i++) {
            a[i] = (String) v.get(i);
        }

        return a;
    }

}
