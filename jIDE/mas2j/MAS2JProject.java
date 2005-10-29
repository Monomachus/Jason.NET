// ----------------------------------------------------------------------------
// Copyright (C) 2003 Rafael H. Bordini, Jomi F. Hubner, et al.
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//
// To contact the authors:
// http://www.dur.ac.uk/r.bordini
// http://www.inf.furb.br/~jomi
//
// CVS information:
//   $Date$
//   $Revision$
//   $Log$
//   Revision 1.1  2005/10/29 21:46:22  jomifred
//   add a new class (MAS2JProject) to store information parsed by the mas2j parser. This new class also create the project scripts
//
//
//----------------------------------------------------------------------------

package jIDE.mas2j;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Represents a MAS2J project (usually created from a .mas2j file)
 */
public class MAS2JProject {

	String soc;

	String envClass = null; 
	String envHost = null;
	
	String controlClass = null;
	String controlHost = null;
	
	boolean debug = false;

	String architecture = "Centralised";

	String projectDir = "." + File.separator;
	String saciJar = "saci.jar";
	String saciHome = null;
	String jasonJar = "jason.jar";
	String log4jJar = "log4j.jar";
	String javaHome = File.separator;

	
	List agents = new ArrayList();
	
	
	public void setProjectDir(String d) {
		if (d != null) {
			projectDir = d;
			if (projectDir.length() > 0) {
				if (!projectDir.endsWith(File.separator)) {
					projectDir += File.separator;
				}
			}
		}
	}
	
	public String getXmlScriptFile() {
		return projectDir + soc + ".xml";
	}

	public void setJasonJar(String s) {
		jasonJar = s;
	}

	public void setLog4jJar(String s) {
		log4jJar = s;
	}

	public void setSaciJar(String s) {
		saciJar = s;
		try {
			saciHome = new File(saciJar).getParent().toString();
		} catch (Exception e) {
			saciHome = null;
		}
	}

	public void setJavaHome(String s) {
		if (s != null) {
			javaHome = new File(s).getAbsolutePath();
			if (!javaHome.endsWith(File.separator)) {
				javaHome += File.separator;
			}
		}
	}

	public void setArchitecture(String a) {
		architecture = a;
	}
	public String getArchitecture() {
		return architecture;
	}

	public void setEnvClass(String e) {
		envClass = e;
	}
	public String getEnvClass() {
		return envClass;
	}
	
	public void setEnvHost(String h) {
		envHost = h;
	}

	public void setSocName(String s) {
		soc = s;
	}

	public String getSocName() {
		return soc;
	}

	public void setControlClass(String sControl) {
		controlClass = sControl;
	}
	public String getControlClass() {
		return controlClass;
	}
	public void setControlHost(String h) {
		controlHost = h;
	}

	public void debugOn() {
		debug = true;
	}
	public void debugOff() {
		debug = false;
	}

	public void initAgMaps() {
		agents = new ArrayList();
	}
	public void addAgent(AgentParameters a) {
		agents.add(a);
	}
	public AgentParameters getAg(String name) {
		Iterator i = agents.iterator();
		while (i.hasNext()) {
			AgentParameters a = (AgentParameters)i.next();
			if (a.name.equals(name)) {
				return a;
			}
		}
		return null;
	}
	
	public Set getAllASFiles() {
		Set files = new HashSet();
		Iterator iag = agents.iterator();
		while (iag.hasNext()) {
			AgentParameters agp = (AgentParameters)iag.next();
			if (agp.asSource != null) {
				files.add(new File(projectDir + File.separator + agp.asSource));
			}
		}
		return files;
	}
	
	public Set getAllUserJavaFiles() {
		Set files = new HashSet();
		Iterator iag = agents.iterator();
		while (iag.hasNext()) { 
			AgentParameters agp = (AgentParameters)iag.next();
			if (agp.agClass != null) files.add(agp.agClass.replace('.', '/'));
			if (agp.archClass != null) files.add(agp.archClass.replace('.', '/'));
		}
		if (getEnvClass() != null) {
			files.add(getEnvClass().replace('.', '/'));
		}
		return files;
	}

	public Set getAllUserJavaDirectories() {
		Set directories = new HashSet();
		Iterator ifiles = getAllUserJavaFiles().iterator();
		while (ifiles.hasNext()) {
			String dir = new File(ifiles.next() + ".java").getParent();
			if (dir == null) { // no parent
				dir = ".";
			}
			directories.add(dir);
		}
		return directories;
	}

	public void writeXMLScript() {
		try {
			writeXMLScript(new PrintStream(new FileOutputStream(getXmlScriptFile())));
		} catch (Exception e) {
			System.err.println("Error writing XML script!"+e);
		}
	}
	
	public void writeXMLScript(PrintStream out) {
		out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		out.println("<?xml-stylesheet href=\"http://www.inf.furb.br/~jomi/jason/saci/applications.xsl\" type=\"text/xsl\" ?>");
		out.println("<saci>");
		out.println("<application id=\"" + soc + "\">");

		out.println("<script id=\"run\">\n");

		out.println("\t<killSocietyAgents society.name=\"" + soc + "\" />");
		out.println("\t<killFacilitator society.name=\"" + soc + "\" />");
		out.println("\t<startSociety society.name=\"" + soc + "\" />\n");

		out.println("\t<killSocietyAgents society.name=\"" + soc + "-env\" />");
		out.println("\t<killFacilitator society.name=\"" + soc + "-env\" />");
		out.println("\t<startSociety society.name=\"" + soc + "-env\" />\n");

		// environment
		out.println("\t<startAgent "); 
        out.println("\t\tname=\"environment\" "); 
        out.println("\t\tsociety.name=\""+soc+"-env\" "); 
        String tmpEnvClass;
        if (envClass == null) {
            tmpEnvClass = jason.environment.Environment.class.getName();
        } else {
            tmpEnvClass = envClass;
        }        
        if (architecture.equals("Saci")) {
            out.println("\t\targs=\""+tmpEnvClass+"\" ");
            tmpEnvClass = jason.environment.SaciEnvironment.class.getName();
        }
        out.println("\t\tclass=\""+tmpEnvClass+"\" ");
        if (envHost != null) {
        	out.println("\t\thost="+envHost); 
        }
        out.println("\t/>"); 		
		
		// agents
		Iterator iag = agents.iterator();
		while (iag.hasNext()) {
			AgentParameters agp = (AgentParameters)iag.next();
			out.println(agp.getAsXMLScript(projectDir, soc, architecture, debug, controlClass != null));
		}
		
		// controller
		if (controlClass != null) {
			out.println("\t<startAgent "); 
			out.println("\t\tname=\"controller\" "); 
			out.println("\t\tsociety.name=\""+soc+"-env\" "); 

            String fControlClass = controlClass;
            if (architecture.equals("Saci")) {
            	fControlClass = jason.control.SaciExecutionControl.class.getName();
            	out.println("\t\targs=\""+controlClass+"\"");
            }
            out.println("\t\tclass=\""+fControlClass+"\" ");
            if (controlHost != null) {
            	out.println("\t\thost="+controlHost);
            }
            out.println("\t/>");
		}
		out.println("\n</script>");
		out.println("</application>");
		out.println("</saci>");
		out.close();
	}

	public String getFullClassPath() {
		String clPath = "\"$CLASSPATH\"";
		String indelim = "\"";
		String outdelim = "";
		if (System.getProperty("os.name").indexOf("indows") > 0) {
			clPath = "%CLASSPATH%";
			indelim = "";
			outdelim = "\"";
		}

		String dDir = projectDir;
		if (dDir.endsWith(File.separator)) {
			dDir = dDir.substring(0, dDir.length() - 1);
		}

		String sLib = "";
		File lib = new File(dDir + File.separator + "lib");
		// add all jar files in lib dir
		if (lib.exists()) {
			File[] fs = lib.listFiles();
			for (int i = 0; i < fs.length; i++) {
				if (fs[i].getName().endsWith(".jar")) {
					sLib += indelim + fs[i].getAbsolutePath() + indelim
							+ File.pathSeparator;
				}
			}
		}

		return outdelim + "." + File.pathSeparator + indelim + jasonJar
				+ indelim + File.pathSeparator + indelim + saciJar + indelim
				+ File.pathSeparator + indelim + log4jJar + indelim
				+ File.pathSeparator + indelim + dDir + indelim
				+ File.pathSeparator + sLib + clPath + outdelim;
	}

	public void writeScripts() {
		try {

			String classPath = getFullClassPath();

			String dirsToCompile = "";
			Iterator i = getAllUserJavaDirectories().iterator();
			while (i.hasNext()) {
				dirsToCompile += " " + i.next() + File.separator + "*.java";
			}

			PrintWriter out;
			
			// -- windows scripts
			if (System.getProperty("os.name").indexOf("indows") > 0) {
				out = new PrintWriter(new FileWriter(projectDir + soc + ".bat"));
				out.println("@echo off\n");
				out.println("rem this file was generated by mas2j parser\n");
				if (javaHome != null) {
					out.println("set PATH=\"" + javaHome + "bin\";%PATH%\n");
				}
				if (architecture.equals("Saci")) {
					out.println("java -classpath " + classPath + " "
							+ saci.tools.runApplicationScript.class.getName()
							+ " \"" + soc + ".xml\"");
				} else if (architecture.equals("Centralised")) {
					out.println("java -classpath " + classPath + " "
							+ jIDE.RunCentralisedMAS.class.getName() + " \""
							+ soc + ".xml\" ");
				}
				out.close();

				out = new PrintWriter(new FileWriter(projectDir + "compile-" + soc + ".bat"));
				out.println("@echo off\n");
				out.println("rem  this file was generated by mas2j parser\n");
				if (javaHome != null) {
					out.println("set PATH=\"" + javaHome + "bin\";%PATH%\n");
				}
				if (dirsToCompile.length() > 0) {
					out.println("echo compiling user classes...");
					out.println("javac -classpath " + classPath + " " + dirsToCompile + "\n\n");
				} else {
					out.println("echo no files to compile...");
				}
				out.println("echo ok");
				out.close();

				if (architecture.equals("Saci")) {
					out = new PrintWriter(new FileWriter(projectDir + "saci-"
							+ soc + ".bat"));
					out.println("@echo off");
					out
							.println("rem this file was generated by mas2j parser\n");
					if (javaHome != null) {
						out
								.println("set PATH=\"" + javaHome
										+ "bin\";%PATH%\n");
					}
					out.println("set CLASSPATH=" + classPath + "\n");
					// out.println("cd \""+saciHome+"\"");
					// out.println("saci &");
					out.println("java -Djava.security.policy=\"jar:file:"
							+ saciJar + "!/policy\" saci.tools.SaciMenu");
					out.close();
				}
			} else {
				// ---- unix scripts
				// the script to run the MAS
				out = new PrintWriter(new FileWriter(projectDir + soc + ".sh"));
				out.println("#!/bin/sh\n");
				out.println("# this file was generated by mas2j parser\n");
				if (javaHome != null) {
					out.println("export PATH=\"" + javaHome + "bin\":$PATH\n");
				}
				if (architecture.equals("Saci")) {
					out.println("java -classpath " + classPath + " "
							+ saci.tools.runApplicationScript.class.getName()
							+ " \"" + soc + ".xml\"");
				} else if (architecture.equals("Centralised")) {
					out.println("java -classpath " + classPath + " "
							+ jIDE.RunCentralisedMAS.class.getName() + " \""
							+ soc + ".xml\"");
				}
				out.close();

				// out = new PrintWriter(new FileWriter(destDir+"c"+soc+".sh"));
				out = new PrintWriter(new FileWriter(projectDir + "compile-" + soc
						+ ".sh"));
				out.println("#!/bin/sh\n");
				out.println("# this file was generated by mas2j parser\n");
				if (javaHome != null) {
					out.println("export PATH=\"" + javaHome + "bin\":$PATH\n");
				}
				if (dirsToCompile.length() > 0) {
					out
							.println("echo -n \"        compiling user classes...\"");
					out.println("# compile files " + getAllUserJavaFiles());
					out.println("# on " + getAllUserJavaDirectories());
					out.println("javac -classpath " + classPath + " "
							+ dirsToCompile + "\n");
				} else {
					out.println("echo -n \"        no files to compile...\"");
				}
				out.println("chmod u+x *.sh");
				out.println("echo ok");
				out.close();

				if (architecture.equals("Saci")) {
					out = new PrintWriter(new FileWriter(projectDir + "saci-"
							+ soc + ".sh"));
					out.println("#!/bin/sh");
					out.println("# this file was generated by mas2j parser\n");
					if (javaHome != null) {
						out.println("export PATH=\"" + javaHome
								+ "bin\":$PATH\n");
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
							+ saciJar + "!/policy\" saci.tools.SaciMenu");
					out.close();
				}
			}
		} catch (Exception e) {
			System.err.println("mas2j: could not write " + soc + ".sh");
			e.printStackTrace();
		}
	}


	public String toString() {
		StringBuffer s = new StringBuffer("MAS " + getSocName() + " {\n");
		s.append("   infrastructure: "+getArchitecture()+"\n");
		s.append("   environment: "+getEnvClass());
		if (envHost != null) {
			s.append(" at "+envHost);
		}
		s.append("\n");
		
		if (getControlClass() != null) {
			s.append("   executionControl: "+getControlClass());
			if (controlHost != null) {
				s.append(" at "+controlHost);
			}
			s.append("\n");
		}
		
		// agents
		s.append("   agents:\n");
		Iterator i = agents.iterator();
		while (i.hasNext()) {
			s.append("       "+i.next());
			s.append(";\n");
		}
		s.append("}");
		return s.toString();
	}

}
