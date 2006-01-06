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
//   Revision 1.28  2006/01/06 12:05:37  jomifred
//   operator - removes bel from BB and changes the current unifier.
//
//   Revision 1.27  2006/01/04 02:54:41  jomifred
//   using java log API instead of apache log
//
//   Revision 1.26  2005/12/30 20:40:16  jomifred
//   new features: unnamed var, var with annots, TE as var
//
//   Revision 1.23  2005/12/08 20:05:01  jomifred
//   changes for JasonIDE plugin
//
//   Revision 1.20  2005/11/17 20:11:50  jomifred
//   fix a bug in openning a project
//
//   Revision 1.17  2005/10/29 21:46:22  jomifred
//   add a new class (MAS2JProject) to store information parsed by the mas2j parser. This new class also create the project scripts
//
//   Revision 1.16  2005/09/20 17:00:26  jomifred
//   load classes from the project lib directory
//
//   Revision 1.15  2005/08/14 23:29:40  jomifred
//   use new java class to run process
//
//   Revision 1.14  2005/08/12 23:29:11  jomifred
//   support for saci arch in IA createAgent
//
//   Revision 1.13  2005/08/12 21:08:23  jomifred
//   add cvs keywords
//
//----------------------------------------------------------------------------

package jIDE;

import jason.mas2j.MAS2JProject;
import jason.runtime.MASConsoleGUI;
import jason.runtime.RunCentralisedMAS;

import java.awt.event.ActionEvent;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import saci.launcher.Launcher;
import saci.launcher.LauncherD;

// TODO: when using jdk 1.5, change RunProcess to ProcessBuilder as
// explained at http://java.sun.com/developer/JDCTechTips/2005/tt0727.html#1

/** runs an MAS */
public class RunMAS extends AbstractAction {

	JasonID jasonID = null;
	RunningMASListener listener;
	MASRunner masRunner;

	public RunMAS(JasonID jID) {
		super("Run MAS...", new ImageIcon(JasonID.class.getResource("/images/execute.gif")));
		jasonID = jID;
		listener = jID;
	}

	public RunMAS(RunningMASListener l) {
		super("Run MAS...");
		listener = l;
	}

	public void actionPerformed(ActionEvent e) {
		try {
			jasonID.output.setText("");
			jasonID.saveAllAct.actionPerformed(null);
			if (jasonID.fMAS2jThread.foregroundCompile() && jasonID.fASParser.foregroundCompile()) {
				// the foregroun do it. jasonID.openAllASFiles(jasonID.fMAS2jThread.fCurrentProject.getAllASFiles());
				jasonID.runMASButton.setEnabled(false);
				jasonID.debugMASButton.setEnabled(false);
				jasonID.stopMASButton.setEnabled(true);
				run(jasonID.fMAS2jThread.fCurrentProject);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void run(MAS2JProject project) {
		try {
			String jasonJar = Config.get().getJasonJar();
			if (!Config.checkJar(jasonJar)) {
				System.err.println("The path to the jason.jar file ("+jasonJar+") was not correctly set, the MAS may not run. Go to menu Plugins->Plugins Options->Jason to configure the path.");
			}
			String javaHome = Config.get().getJavaHome();
			if (!Config.checkJavaHomePath(javaHome)) {
				System.err.println("The Java home directory ("+javaHome+") was not correctly set, the MAS may not run. Go to the Plugins->Options->Jason menu to configure the path.");
			}
			
			// compile some files
			CompileThread compT = new CompileThread(project.getAllUserJavaFiles(), project);
			compT.start();
			if (masRunner != null) {
				masRunner.stopRunner();
			}

			if (project.isCentArch()) {
				if (Config.get().runAsInternalTread()) {
					masRunner = new MASRunnerInsideJIDE(compT, project);
				} else {
					masRunner = new MASRunnerCentralised(compT, project);
				}
			} else if (project.isSaciArch()) {
				String saciJar = Config.get().getSaciJar();
				if (Config.checkJar(saciJar)) {
					masRunner = new MASRunnerSaci(compT, project);
				} else {
					System.err.println("The path to the saci.jar file ("+saciJar+") was not correctly set. Go to menu Edit->Preferences to configure the path.");
					return;
				}
			}
			masRunner.start();
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void stopMAS() {
		if (masRunner != null) {
			masRunner.stopRunner();
		}
		masRunner = null;
	}


	class StartSaci extends Thread {

		//BufferedReader saciIn;
		//BufferedReader saciErr;

		boolean saciOk = false;
		Process saciProcess;
		MAS2JProject project;
		
		StartSaci(MAS2JProject project) {
			super("StartSaci");
			this.project = project;
		}

		Launcher getLauncher() {
			PrintStream err = System.err;
			Launcher l = null;
			try {
				if (jasonID != null) {
					System.setErr(jasonID.myOut.originalErr);
				}
				l = LauncherD.getLauncher();
				return l;
			} catch (Exception e) {
				return null;
			} finally {
				System.setErr(err);
			}
		}
		
		void stopSaci() {
			try {
				getLauncher().stop();
			} catch (Exception e) {
				try {
					saciProcess.destroy();
				} catch (Exception e2) {
				}
			}
			saciProcess = null;
		}

		public void run() {
			//stopSaci();
			try {
				//String command = getAsScriptCommand(jasonID.projectDirectory + File.separator + "saci-" + jasonID.getFileName());
				String command = getAsScriptCommand("saci-" + project.getSocName(), true);
				saciProcess = Runtime.getRuntime().exec(command, null,
						//new File(JasonID.saciHome + File.separator + "bin"));
						new File(project.getDirectory()));
				//saciIn = new BufferedReader(new InputStreamReader(saciProcess.getInputStream()));
				//saciErr = new BufferedReader(new InputStreamReader(saciProcess.getErrorStream()));
				System.out.println("running saci with " + command);

				int tryCont = 0;
				while (tryCont < 30) {
					tryCont++;
					sleep(1000);
					Launcher l = getLauncher();
					if (l != null) {
						saciOk = true;
						stopWaitSaciOk();
						break;
					}
				}
			} catch (Exception ex) {
				System.err.println("error running saci:" + ex);
			} finally {
				stopWaitSaciOk();
			}
		}

		synchronized void stopWaitSaciOk() {
			notifyAll();
		}

		synchronized boolean waitSaciOk() {
			try {
				wait(20000); // waits 20 seconds
				if (!saciOk) {
					JOptionPane
							.showMessageDialog(
									null,
									"Fail to automatically start saci! \nGo to \""
											+ project.getDirectory()
											+ "\" directory and run the saci-"
											+ project.getSocName()
											+ " script.\n\nClick 'ok' when saci is running.");
					wait(1000);
					if (!saciOk) {
						JOptionPane
								.showMessageDialog(null,
										"Saci might not be properly installed or configure. Use the centralised architecture to run your MAS");
					}
				}
			} catch (Exception e) {
			}
			return saciOk;
		}
	}

	class CompileThread extends Thread {
		private boolean ok = true;
		private boolean finished = false;
		private Set files;
		MAS2JProject project;

		CompileThread(Set files, MAS2JProject project) {
			super("CompileThread");
			this.files = files;
			this.project = project;
		}

		synchronized boolean waitCompilation() {
			while (!finished) {
				try {
					wait(2000); // waits 2 seconds
				} catch (Exception e) {
				}
			}
			return ok;
		}

		synchronized void stopWaiting() {
			notifyAll();
		}

		public void run() {
			try {
				if (needsComp()) {
					String command = getAsScriptCommand("compile-" + project.getSocName());
					System.out.println("Compiling user class with " + command);
					Process p = Runtime.getRuntime().exec(command, null, new File(project.getDirectory()));
					p.waitFor();
					ok = !needsComp();
					if (!ok) {
							BufferedReader in = new BufferedReader(new InputStreamReader(p.getErrorStream()));
							while (in.ready()) {
								System.out.println(in.readLine());
								//jasonID.output.append(in.readLine() + "\n");
							}
					}
				}
			} catch (Exception e) {
				System.err.println("Compilation error: " + e);
			} finally {
				finished = true;
				stopWaiting();
			}
		}
		
		boolean needsComp() {
			Iterator ifiles = files.iterator();
			while (ifiles.hasNext()) {
				String file = (String) ifiles.next();

				File javaF = new File(project.getDirectory()
						+ File.separatorChar + file + ".java");
				File classF = new File(project.getDirectory()
						+ File.separatorChar + file + ".class");
				//System.out.println(classF.lastModified()+" > "+javaF.lastModified());
				if (javaF.exists() && !classF.exists()) {
					return true;
				} else if (javaF.exists() && classF.exists()) {
					if (classF.lastModified() < javaF.lastModified()) {
						return true;
					}
				}
			}
			return false;
		}
	}

	class MASRunner extends Thread {
		CompileThread compT;
		MAS2JProject project;
		protected boolean stop = false;
		boolean stopOnProcessExit = true;
		
		Process masProcess = null;
		OutputStream processOut;

		MASRunner(CompileThread t, MAS2JProject project) {
			super("MASRunner");
			compT = t;
			this.project = project;
		}

		void stopRunner() {
			if (masProcess != null) {
				masProcess.destroy();
			}
			stop = true;
		}

		public void run() {
			try {
				if (compT != null) {
					if (!compT.waitCompilation()) {
						return;
					}
				}
				String command = getAsScriptCommand(project.getSocName());
				System.out.println("Executing MAS with " + command);
				masProcess = Runtime.getRuntime().exec(command, null,
						new File(project.getDirectory()));

				BufferedReader in = new BufferedReader(new InputStreamReader(masProcess.getInputStream()));
				BufferedReader err = new BufferedReader(new InputStreamReader(	masProcess.getErrorStream()));
				processOut = masProcess.getOutputStream();

				sleep(500);
				stop = false;
				while (!stop) {// || saciProcess!=null) {
					while (!stop && in.ready()) {
						System.out.println(in.readLine());
					}
					while (!stop && err.ready()) {
						System.out.println(err.readLine());
					}
					sleep(250); // to not consume cpu
					
					if (stopOnProcessExit) {
						try {
							masProcess.exitValue();
							// no exception when the process has finished
							stop = true;
						} catch (Exception e) {}
					}
				}
				
			} catch (Exception e) {
				System.err.println("Execution error: " + e);
				e.printStackTrace();
			} finally {
				if (listener != null) {
					listener.masFinished();
				}
			}
		}
	}
	
	class MASRunnerCentralised extends MASRunner {

		MASRunnerCentralised(CompileThread t, MAS2JProject project) {
			super(t, project);
		}

		void stopRunner() {
			try {
				if (processOut != null) {
					processOut.write(1);//"quit"+System.getProperty("line.separator"));
				}
			} catch (Exception e) {
				System.err.println("Execution error: " + e);
				e.printStackTrace();
			}

			super.stopRunner();
		}
	}

	
	class MASRunnerInsideJIDE extends MASRunner {
		Method getRunnerMethod = null;
		Method finishMethod = null;
		
		MASRunnerInsideJIDE(CompileThread t, MAS2JProject project) {
			super(t, project);
		}

		void stopRunner() {
			try {
				//RunCentralisedMAS.getRunner().finish();
				if (getRunnerMethod != null && finishMethod != null) {
					finishMethod.invoke(getRunnerMethod.invoke(null,null),null);
				}
			} catch (Exception e) {
				System.err.println("Execution error: " + e);
				e.printStackTrace();
			}

			super.stopRunner();
		}

		public void run() {
			try {
				if (compT != null) {
					if (!compT.waitCompilation()) {
						return;
					}
				}
				
				File fXML = new File(project.getDirectory() + File.separator + project.getSocName()+".xml");
				System.out.println("Running MAS with "+fXML.getAbsolutePath());
				// create a new RunCentralisedMAS (using my class loader to not cache user classes and to find user project directory)
				Class rmas = new MASClassLoader(project.getDirectory()).loadClass(RunCentralisedMAS.class.getName());
				Class[] classParameters = { (new String[2]).getClass() };
				Method executeMethod = rmas.getDeclaredMethod("main", classParameters);
				classParameters = new Class[0];
				getRunnerMethod = rmas.getDeclaredMethod("getRunner", classParameters);
				finishMethod = rmas.getDeclaredMethod("finish", classParameters);
				
				Object objectParameters[] = { new String[] {fXML.getAbsolutePath(), "insideJIDE"} };
				// Static method, no instance needed
				executeMethod.invoke(null, objectParameters);
				//((RunCentralisedMAS)rmas.newInstance()).main(new String[] {fXML.getAbsolutePath(), "insideJIDE"});

				stop = false;
				while (!stop) {
					sleep(250); // to not consume cpu
					//if (RunCentralisedMAS.getRunner() == null) {
					if (getRunnerMethod.invoke(null, null) == null) {
						stop = true;
					}
				}
			} catch (Exception e) {
				System.err.println("Execution error: " + e);
				e.printStackTrace();
			} finally {
				if (listener != null) {
					listener.masFinished();
				}
				getRunnerMethod = null;
				finishMethod = null;
			}
		}
	}
	
	class MASRunnerSaci extends MASRunner {
		StartSaci saciThread;
		Launcher l;

		boolean iHaveStartedSaci = false;
		
		MASRunnerSaci(CompileThread t, MAS2JProject project) {
			super(t, project);
			stopOnProcessExit = false;
		}

		void stopRunner() {
			try {
				String socName = project.getSocName();
				if (l != null) {
					l.killFacilitatorAgs(socName);
					l.killFacilitator(socName);
					l.killFacilitatorAgs(socName + "-env");
					l.killFacilitator(socName + "-env");
				}
			} catch (Exception e) {
				System.err.println("Execution error: " + e);
				e.printStackTrace();
			}
			if (iHaveStartedSaci) {
				saciThread.stopSaci();
			}
			super.stopRunner();
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
	}

	
	String getAsScriptCommand(String scriptName) {
		return getAsScriptCommand(scriptName, false); 
	}
	String getAsScriptCommand(String scriptName, boolean start) {
		if (System.getProperty("os.name").indexOf("indows") > 0) {
			//command = "command.com /e:2048 /c "+command+".bat";
			String sStart = " ";
			if (start) {
				sStart = " start "; 
			}
			return Config.get().getShellCommand() + sStart + scriptName + ".bat";
		} else {
			return Config.get().getShellCommand() + " " + scriptName + ".sh";
		}
	}
	
	
	class MASClassLoader extends ClassLoader {
		
		String MASDirectory;
		JarFile jf;
		List libDirJars = new ArrayList();
		
		MASClassLoader(String masDir) {
			MASDirectory = masDir;
			try {
				jf = new JarFile(Config.get().getJasonJar());
				
				// create the jars from application lib directory
				if (MASDirectory != null) {
					File lib = new File(MASDirectory + File.separator + "lib");
					// add all jar files in lib dir
					if (lib.exists()) {
						File[] fs = lib.listFiles();
						for (int i=0; i<fs.length; i++) {
							if (fs[i].getName().endsWith(".jar")) {
								libDirJars.add(new JarFile(fs[i].getAbsolutePath()));
							}
						}
					}
					
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		public Class loadClass(String name) throws ClassNotFoundException {
			//System.out.println("loadClass " + name);
			if (! name.equals(MASConsoleGUI.class.getName())) { // let super loader to get MASConsoleGUI, since it must be shared between RunCentMAS and jIDE
				if (! name.equals(JasonID.class.getName())) { // let super loader to get MASConsoleGUI, since it must be shared between RunCentMAS and jIDE
					if (name.startsWith("jason.") || name.startsWith("jIDE.")) {
						//System.out.println("loading new ");
						Class c = findClassInJar(jf, name);
						if (c == null) {
							System.out.println("does not find class "+name+" in jason.jar");
						} else {
							return c;
						}
					}
				}
			}
			return super.loadClass(name);
		}

		public Class findClass(String name) {
			byte[] b = findClassBytes(MASDirectory, name);
			if (b != null) {
				//System.out.println(name + " found ");
				return defineClass(name, b, 0, b.length);
			} else {
				// try in lib dir
				Iterator i = libDirJars.iterator();
				while (i.hasNext()) {
					JarFile j = (JarFile)i.next();
					Class c = findClassInJar(j, name);
					if (c != null) {
						return c;
					}
				}
				System.err.println("Error loading class "+name);
				return null;
			}
		}

		public byte[] findClassBytes(String dir, String className) {
			try {
				String pathName = dir + File.separatorChar	+ className.replace('.', File.separatorChar) + ".class";
				FileInputStream inFile = new FileInputStream(pathName);
				byte[] classBytes = new byte[inFile.available()];
				inFile.read(classBytes);
				return classBytes;
			} catch (java.io.IOException ioEx) {
				//ioEx.printStackTrace();
				return null;
			}
		}

		public Class findClassInJar(JarFile jf, String className) {
			try {
				if (jf == null) 
					return null;
				JarEntry je = jf.getJarEntry(className.replace('.', '/') + ".class"); // must be '/' since inside jar / is used not \
				if (je == null) {
					return null;
				}
				InputStream in = new BufferedInputStream(jf.getInputStream(je));
				//System.out.println(c.getResource("/"+className.replace('.', File.separatorChar) + ".class"));
				//InputStream in = new BufferedInputStream(c.getResource("/"+className.replace('.', File.separatorChar) + ".class").openStream());
				byte[] classBytes = new byte[in.available()];
				in.read(classBytes);
				//System.out.println(" found "+className+ " size="+classBytes.length);
				return defineClass(className, classBytes, 0, classBytes.length);
			} catch (Exception e) {
				System.err.println("Error loading class "+className);
				e.printStackTrace();
				return null;
			}
		}
	}	
}
