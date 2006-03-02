package jason.infra.centralised;

import jason.jeditplugin.Config;
import jason.jeditplugin.MASLauncher;
import jason.mas2j.MAS2JProject;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Iterator;

public class CentralisedMASLauncher extends MASLauncher {

	public CentralisedMASLauncher(MAS2JProject project) {
		super(project);
	}
	
	public void stopMAS() {
		try {
			if (processOut != null) {
				processOut.write(1);//"quit"+System.getProperty("line.separator"));
			}
		} catch (Exception e) {
			System.err.println("Execution error: " + e);
			e.printStackTrace();
		}

		super.stopMAS();
	}

	/** write the scripts necessary to run the project */	
	public void writeScripts(boolean debug) {
		try {
			String classPath = getProjectClassPath();
			String javaHome = Config.get().getJavaHome();
			
			String dirsToCompile = "";
			Iterator i = project.getAllUserJavaDirectories().iterator();
			while (i.hasNext()) {
				dirsToCompile += " " + i.next() + File.separator + "*.java";
			}

			PrintWriter out;
			
			// -- windows scripts
			if (System.getProperty("os.name").indexOf("indows") > 0) {
				out = new PrintWriter(new FileWriter(project.getDirectory() + project.getSocName() + ".bat"));
				out.println("@echo off\n");
				out.println("rem this file was generated by Jason\n");
				if (javaHome != null) {
					out.println("set PATH=\"" + javaHome + "bin\";%PATH%\n");
				}
				String sDebug = "";
				if (debug) {
					sDebug = " -debug";
				}
				out.println("java -classpath " + classPath + " "
						+ jason.infra.centralised.RunCentralisedMAS.class.getName() + " \""
						+ project.getProjectFile().getAbsolutePath() + "\" "+sDebug); //soc + ".xml\" ");
				out.close();

				if (dirsToCompile.length() > 0) {
					out = new PrintWriter(new FileWriter(project.getDirectory() + "compile-" + project.getSocName() + ".bat"));
					out.println("@echo off\n");
					out.println("rem  this file was generated by Jason\n");
					if (javaHome != null) {
						out.println("set PATH=\"" + javaHome + "bin\";%PATH%\n");
					}
					out.println("echo compiling user classes...");
					out.println("javac -classpath " + classPath + " " + dirsToCompile + "\n\n");
					out.println("echo ok");
					out.close();
				}

			} else {
				// ---- unix scripts
				// the script to run the MAS
				out = new PrintWriter(new FileWriter(project.getDirectory() + project.getSocName() + ".sh"));
				out.println("#!/bin/sh\n");
				out.println("# this file was generated by Jason\n");
				if (javaHome != null) {
					out.println("export PATH=\"" + javaHome + "bin\":$PATH\n");
				}
				String sDebug = "";
				if (debug) {
					sDebug = " -debug";
				}
				out.println("java -classpath " + classPath + " "
						+ jason.infra.centralised.RunCentralisedMAS.class.getName() + " \""
						+ project.getProjectFile().getAbsolutePath() + "\" "+sDebug); //soc + ".xml\"");
				out.close();

				// out = new PrintWriter(new FileWriter(destDir+"c"+soc+".sh"));
				if (dirsToCompile.length() > 0) {
					out = new PrintWriter(new FileWriter(project.getDirectory() + "compile-" + project.getSocName() + ".sh"));
					out.println("#!/bin/sh\n");
					out.println("# this file was generated by Jason\n");
					if (javaHome != null) {
						out.println("export PATH=\"" + javaHome + "bin\":$PATH\n");
					}
					out.println("echo -n \"        compiling user classes...\"");
					out.println("# compile files " + project.getAllUserJavaFiles());
					out.println("# on " + project.getAllUserJavaDirectories());
					out.println("javac -classpath " + classPath + " " + dirsToCompile + "\n");
					out.println("chmod u+x *.sh");
					out.println("echo ok");
					out.close();
				}
			}
		} catch (Exception e) {
			System.err.println("mas2j: could not write " + project.getSocName() + ".sh");
			e.printStackTrace();
		}
	}

}
