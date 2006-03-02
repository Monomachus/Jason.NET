package jason.infra.saci;

import jason.architecture.AgArch;
import jason.control.ExecutionControlGUI;
import jason.jeditplugin.Config;
import jason.jeditplugin.MASLauncher;
import jason.mas2j.AgentParameters;
import jason.mas2j.MAS2JProject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import saci.launcher.Launcher;

public class SaciMASLauncher extends MASLauncher {
	StartSaci saciThread;
	Launcher l;

	boolean iHaveStartedSaci = false;
	
	private static Logger logger = Logger.getLogger(SaciMASLauncher.class.getName());

	
	public SaciMASLauncher(MAS2JProject project) {
		super(project);
		stopOnProcessExit = false;
	}

	public void stopMAS() {
		super.stopMAS();
		new Thread() {
			public void run() {
				try {
					new SaciRuntimeServices(project.getSocName()).stopMAS();
					if (iHaveStartedSaci) {
						saciThread.stopSaci();
					}					
				} catch (Exception e) {
					logger.log(Level.SEVERE, "Error stoping saci MAS", e);
				}
			}
		}.start();
	}

	public void run() {
		saciThread = new StartSaci(project);
		l = saciThread.getLauncher();
		if (l == null) { // no saci running, start one
			saciThread.start();
			if (!saciThread.waitSaciOk()) {
				return;
			}
			iHaveStartedSaci = true;
		}
		l = saciThread.getLauncher();

		super.run();
	}

	/** return the operating system command that runs the MAS */
	public String getStartCommand() {
		return getRunScriptCommand(project.getSocName());
	}


	/** write the scripts necessary to run the project */	
	public void writeScripts(boolean debug) {

		String saciJar = Config.get().getSaciJar();
		if (!Config.checkJar(saciJar)) {
			System.err.println("The path to the saci.jar file ("+saciJar+") was not correctly set. Go to menu Plugin->Options->Jason to configure the path.");
		}
		
		writeSaciXMLScript(debug);
		
		try {
			String classPath = getFullClassPath();
			String javaHome = Config.get().getJavaHome();
			
			PrintWriter out;
			
			// -- windows scripts
			if (System.getProperty("os.name").indexOf("indows") > 0) {
				out = new PrintWriter(new FileWriter(project.getDirectory() + project.getSocName() + ".bat"));
				out.println("@echo off\n");
				out.println("rem this file was generated by Jason\n");
				if (javaHome != null) {
					out.println("set PATH=\"" + javaHome + "bin\";%PATH%\n");
				}
				out.println("java -classpath " + classPath + " "
							+ "saci.tools.runApplicationScript"
							+ " \"" + project.getSocName() + ".xml\"");
				out.close();

				// write start saci script
				out = new PrintWriter(new FileWriter(project.getDirectory() + "saci-" + project.getSocName() + ".bat"));
				out.println("@echo off");
				out.println("rem this file was generated by Jason\n");
				if (javaHome != null) {
						out.println("set PATH=\"" + javaHome + "bin\";%PATH%\n");
				}
				out.println("set CLASSPATH=" + classPath + "\n");
				// out.println("cd \""+saciHome+"\"");
				// out.println("saci &");
				out.println("java -Djava.security.policy=\"jar:file:"
							+ Config.get().getSaciJar() + "!/policy\" saci.tools.SaciMenu");
				out.close();
				
				
			} else {
				// ---- unix scripts
				// the script to run the MAS
				out = new PrintWriter(new FileWriter(project.getDirectory() + project.getSocName() + ".sh"));
				out.println("#!/bin/sh\n");
				out.println("# this file was generated by Jason\n");
				if (javaHome != null) {
					out.println("export PATH=\"" + javaHome + "bin\":$PATH\n");
				}
				out.println("java -classpath " + classPath + " "
							+ saci.tools.runApplicationScript.class.getName()
							+ " \"" + project.getSocName() + ".xml\"");
				out.close();

				// write start saci script
				out = new PrintWriter(new FileWriter(project.getDirectory() + "saci-"	+ project.getSocName() + ".sh"));
				out.println("#!/bin/sh");
				out.println("# this file was generated by Jason\n");
				if (javaHome != null) {
					out.println("export PATH=\"" + javaHome + "bin\":$PATH\n");
				}
					// out.println("CURDIR=`pwd`");
					// out.println("cd "+destDir);
					// out.println("APPDIR=`pwd`");
					// out.println("export
					// CLASSPATH=$APPDIR:$CURDIR:"+classPath);
				out.println("export CLASSPATH=" + classPath + "\n");
					// out.println("cd \""+saciHome+"\"");
					// out.println("./saci &");
				out.println("java -Djava.security.policy=\"jar:file:"
							+ Config.get().getSaciJar() + "!/policy\" saci.tools.SaciMenu");
				out.close();
			}
		} catch (Exception e) {
			System.err.println("mas2j: could not write " + project.getSocName() + ".sh");
			e.printStackTrace();
		}
	}

	
	public void writeSaciXMLScript(boolean debug) {
		try {
			String file = project.getDirectory() + project.getSocName() + ".xml";
			writeSaciXMLScript(new PrintStream(new FileOutputStream(file)), debug);
		} catch (Exception e) {
			System.err.println("Error writing XML script!"+e);
		}
	}
	
	public void writeSaciXMLScript(PrintStream out, boolean debug) {
		out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		out.println("<!-- this file was generated by Jason -->");
		out.println("<?xml-stylesheet href=\"http://www.inf.furb.br/~jomi/jason/saci/applications.xsl\" type=\"text/xsl\" ?>");
		out.println("<saci>");
		out.println("<application id=\"" + project.getSocName() + "\">");

		out.println("<script id=\"run\">\n");

		out.println("\t<killSocietyAgents society.name=\"" + project.getSocName() + "\" />");
		out.println("\t<killFacilitator society.name=\"" + project.getSocName() + "\" />");
		out.println("\t<startSociety society.name=\"" + project.getSocName() + "\" />\n");

		out.println("\t<killSocietyAgents society.name=\"" + project.getSocName() + "-env\" />");
		out.println("\t<killFacilitator society.name=\"" + project.getSocName() + "-env\" />");
		out.println("\t<startSociety society.name=\"" + project.getSocName() + "-env\" />\n");

		// environment
		out.println("\t<startAgent "); 
        out.println("\t\tname=\"environment\" "); 
        out.println("\t\tsociety.name=\""+project.getSocName()+"-env\" "); 
        String tmpEnvClass;
        if (project.getEnvClass() == null) {
            tmpEnvClass = jason.environment.Environment.class.getName();
        } else {
            tmpEnvClass = project.getEnvClass();
        }        
        out.println("\t\targs=\""+tmpEnvClass+"\" ");
        tmpEnvClass = jason.infra.saci.SaciEnvironment.class.getName();
        out.println("\t\tclass=\""+tmpEnvClass+"\" ");
        if (project.getEnvHost() != null) {
        	out.println("\t\thost="+project.getEnvHost()); 
        }
        out.println("\t/>"); 		
		
		// agents
		Iterator iag = project.getAgents().iterator();
		while (iag.hasNext()) {
			AgentParameters agp = (AgentParameters)iag.next();
			out.println(getAgSaciXMLScript(agp, debug, project.getControlClass() != null));
		}
		
		// controller
        String fControlClass = project.getControlClass();
		if (debug && fControlClass == null) {
			fControlClass = ExecutionControlGUI.class.getName();
		}
		if (fControlClass != null) {
			out.println("\t<startAgent "); 
			out.println("\t\tname=\"controller\" "); 
			out.println("\t\tsociety.name=\""+project.getSocName()+"-env\" "); 

           	out.println("\t\targs=\""+fControlClass+"\"");
           	fControlClass = jason.infra.saci.SaciExecutionControl.class.getName();
            out.println("\t\tclass=\""+fControlClass+"\" ");
            if (project.getControlHost() != null) {
            	out.println("\t\thost="+project.getControlHost());
            }
            out.println("\t/>");
		}
		out.println("\n</script>");
		out.println("</application>");
		out.println("</saci>");
		out.close();
	}

	public String getAgSaciXMLScript(AgentParameters agp, boolean debug, boolean forceSync) {
		StringBuffer s = new StringBuffer("\t<startAgent "); 
        s.append("\n\t\tname=\""+agp.name+"\" "); 
        s.append("\n\t\tsociety.name=\""+project.getSocName()+"\" ");
        
        s.append("\n\t\tclass=\""+jason.infra.saci.SaciAgArch.class.getName()+"\"");

        String tmpAgClass = agp.agClass;
        if (tmpAgClass == null) {
        	tmpAgClass = jason.asSemantics.Agent.class.getName();
        }
        String tmpAgArchClass = agp.archClass;
        if (tmpAgArchClass == null) {
        	tmpAgArchClass = AgArch.class.getName();
        }
        
        File tmpAsSrc = new File(project.getDirectory() + File.separator + agp.asSource);
        s.append("\n\t\targs=\""+tmpAgArchClass+" "+tmpAgClass+" '"+tmpAsSrc.getAbsolutePath()+"'"+getSaciOptsStr(agp, debug, forceSync)+"\"");
        if (agp.qty > 1) {
        	s.append("\n\t\tqty=\""+agp.qty+"\" ");
        }
        if (agp.host != null) {
        	s.append("\n\t\thost="+agp.host);
        }
        s.append(" />");
		return s.toString().trim();
	}
	
	String getSaciOptsStr(AgentParameters agp, boolean debug, boolean forceSync) {
		String s = "";
		String v = "";
	    if (debug) {
	    	s += "verbose=2";
	    	v = ",";
	    }
	    if (forceSync || debug) {
	    	s += v+"synchronised=true";
	    	v = ",";
	    }
		Iterator i = agp.options.keySet().iterator();
		while (i.hasNext()) {
			String key = (String) i.next();
			if (!(debug && key.equals("verbose"))) {
				if (!( (forceSync || debug) && key.equals("synchronised"))) {
					s += v + key + "=" + changeQuotes((String)agp.options.get(key));
					v = ",";
				}
			}
		}
		if (s.length() > 0) {
			s = " options " + s;
		}
		return s;
	}

	public String getFullClassPath() {
		
		// add saci.jar in the default classpath
		
		String clPath = "\"$CLASSPATH\"";
		String indelim = "\"";
		String outdelim = "";
		if (System.getProperty("os.name").indexOf("indows") > 0) {
			clPath = "%CLASSPATH%";
			indelim = "";
			outdelim = "\"";
		}

		String saciJar = indelim + Config.get().getSaciJar() + indelim;
		if (outdelim.length() > 0) { // is windows
			return outdelim + saciJar + File.pathSeparator + project.getProjectClassPath().substring(1);
		} else {
			return project.getProjectClassPath() + File.pathSeparator + saciJar;
		}

				/*
				//+ File.pathSeparator + indelim + Config.get().getProperty(Config.LOG4J_JAR) + indelim
				+ File.pathSeparator + indelim + dDir + indelim
				+ File.pathSeparator + sLib + clPath + outdelim;
				*/
	}
	
	String changeQuotes(String s) {
		if (s.startsWith("\"") && s.endsWith("\"")) {
			return "'"+s.substring(1,s.length()-1)+"'";
		} else {
			return s;
		}
	}
}
