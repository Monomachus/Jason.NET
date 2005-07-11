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
// http://www.csc.liv.ac.uk/~bordini
// http://www.inf.furb.br/~jomi
//----------------------------------------------------------------------------

package jIDE;

import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import saci.launcher.Launcher;
import saci.launcher.LauncherD;

/** runs an MAS */
class RunMAS extends AbstractAction {

	JasonID jasonID;

	MASRunner masRunner;
	Process saciProcess;

	RunMAS(JasonID jID) {
		super("Run MAS...", new ImageIcon(JasonID.class.getResource("/images/execute.gif")));
		jasonID = jID;
	}

	public void actionPerformed(ActionEvent e) {
		try {
			jasonID.output.setText("");
			jasonID.saveAllAct.actionPerformed(null);
			
			String jasonHome = jasonID.getConf().getProperty("jasonHome");
			if (!jasonID.checkJasonPath(jasonHome)) {
				System.err.println("The Jason home directory ("+jasonHome+") was not correctly set, the MAS may not run. Go to menu Edit->Preferences and set it.");
			}
			String javaHome = jasonID.getConf().getProperty("javaHome");
			if (!jasonID.checkJavaPath(javaHome)) {
				System.err.println("The Java home directory ("+javaHome+") was not correctly set, the MAS may not run. Go to menu Edit->Preferences and set it.");
			}
			
			
			boolean ok = jasonID.fMAS2jThread.foregroundCompile();
			if (ok) {
				jasonID.openAllASFiles(jasonID.fMAS2jThread.fParserMAS2J.getAgASFiles().values());
				ok = jasonID.fASParser.foregroundCompile();
				if (ok) {
					jasonID.runMASButton.setEnabled(false);
					jasonID.debugMASButton.setEnabled(false);

					// compile some files
					CompileThread compT = new CompileThread(jasonID.fMAS2jThread.fParserMAS2J.getAllUserJavaFiles());
					compT.start();

					if (jasonID.fMAS2jThread.fParserMAS2J.getArchitecture().equals("Centralised")) {
						masRunner = new MASRunnerCentralised(compT);
					} else if (jasonID.fMAS2jThread.fParserMAS2J.getArchitecture().equals("Saci")) {
						String saciHome = jasonID.getConf().getProperty("saciHome");
						if (jasonID.checkSaciPath(saciHome)) {
							StartSaci saciThread = null;
							Launcher l = getLauncher();
							if (l == null) {
								saciThread = new StartSaci();
								saciThread.start();
							}
							masRunner = new MASRunnerSaci(compT, saciThread);
						} else {
							System.err.println("Error: Saci home directory "+saciHome+" was not correctly set. Go to menu Edit->Preferences and set it.");
							return;
						}
					}
					masRunner.start();
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	void stopMAS() {
		if (masRunner != null) {
			masRunner.stopRunner();
		}
	}

	void exitJason() {
		if (saciProcess != null) { // i've created saci
			stopSaci();
		}
	}

	void stopSaci() {
		try {
			Launcher l = getLauncher();
			l.stop();
		} catch (Exception e) {
			try {
				saciProcess.destroy();
			} catch (Exception e2) {
			}
		}
		saciProcess = null;
	}

	Launcher getLauncher() {
		PrintStream err = System.err;
		Launcher l = null;
		try {
			System.setErr(jasonID.originalErr);
			l = LauncherD.getLauncher();
			return l;
		} catch (Exception e) {
			return null;
		} finally {
			System.setErr(err);
		}
	}

	class StartSaci extends Thread {

		//BufferedReader saciIn;
		//BufferedReader saciErr;

		boolean saciOk = false;

		StartSaci() {
			super("StartSaci");
		}
		
		public void run() {
			//stopSaci();
			try {
				//String command = getAsScriptCommand(jasonID.projectDirectory + File.separator + "saci-" + jasonID.getFileName());
				String command = getAsScriptCommand("saci-" + jasonID.fMAS2jThread.fParserMAS2J.getSocName(), true);
				saciProcess = Runtime.getRuntime().exec(command, null,
						//new File(JasonID.saciHome + File.separator + "bin"));
						new File(jasonID.projectDirectory));
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
				System.err.println("error running SACI:" + ex);
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
									jasonID.getFrame(),
									"Fail to automatically start saci! \nGo to \""
											+ jasonID.projectDirectory
											+ "\" directory and run the saci-"
											+ jasonID.getFileName()
											+ " script.\n\nClick 'ok' when the saci is running.");
					wait(1000);
					if (!saciOk) {
						JOptionPane
								.showMessageDialog(jasonID.getFrame(),
										"Saci is not running. Use centralised architecture to run the MAS");
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

		CompileThread(Set files) {
			super("CompileThread");
			this.files = files;
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
					String command = getAsScriptCommand("compile-" + jasonID.fMAS2jThread.fParserMAS2J.getSocName());
					System.out.println("Compiling user class with " + command);
					Process p = Runtime.getRuntime().exec(command, null, new File(jasonID.projectDirectory));
					p.waitFor();
					ok = !needsComp();
					if (!ok) {
							BufferedReader in = new BufferedReader(new InputStreamReader(p.getErrorStream()));
							while (in.ready()) {
								jasonID.output.append(in.readLine() + "\n");
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

				File javaF = new File(jasonID.projectDirectory
						+ File.separatorChar + file + ".java");
				File classF = new File(jasonID.projectDirectory
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
		//String runnerClass;
		CompileThread compT;
		
		Process masProcess = null;
		OutputStreamWriter processOut;

		MASRunner(CompileThread t) {
			super("MASRunner");
			compT = t;
		}

		void stopRunner() {
			if (masProcess != null) {
				masProcess.destroy();
			}
			jasonID.runMASButton.setEnabled(true);
			jasonID.debugMASButton.setEnabled(true);
			jasonID.stopMASButton.setEnabled(false);
			
		}

		public void run() {
			try {
				if (compT != null) {
					if (!compT.waitCompilation()) {
						return;
					}
				}
				String command = getAsScriptCommand(jasonID.fMAS2jThread.fParserMAS2J.getSocName());
				System.out.println("Executing MAS with " + command);
				masProcess = Runtime.getRuntime().exec(command, null,
						new File(jasonID.projectDirectory));

				//BufferedReader in = new BufferedReader(new InputStreamReader(
				//		masProcess.getInputStream()));
				
				processOut = new OutputStreamWriter(masProcess.getOutputStream());

				jasonID.stopMASButton.setEnabled(true);
				
				//BufferedReader err = new BufferedReader(new InputStreamReader(
				//		masProcess.getErrorStream()));
				
				//stop = false;
				
				/*
				if (masConsole != null) {
					masConsole.append("MAS execution\n");
					masConsole.append("--------------------------------------\n");
				}
				
				sleep(500);
				while (!stop) {// || saciProcess!=null) {
					while (!stop && in.ready()) {
						if (masConsole != null) {
							masConsole.append(in.readLine() + "\n");
						}
					}
					while (!stop && err.ready()) {
						if (masConsole != null) {
							masConsole.append(err.readLine() + "\n");
						}
					}
					sleep(250); // to not consume cpu
				}

				if (masConsole != null) {
					masConsole.append("\n------\n");
				}
				*/
			} catch (Exception e) {
				System.err.println("Execution error: " + e);
				e.printStackTrace();
			}
		}
	}
	
	class MASRunnerCentralised extends MASRunner {

		MASRunnerCentralised(CompileThread t) {
			super(t);
		}

		void stopRunner() {
			try {
				processOut.write("quit\n");
			} catch (Exception e) {
				System.err.println("Execution error: " + e);
				e.printStackTrace();
			}

			super.stopRunner();
		}
	}

	class MASRunnerSaci extends MASRunner {
		StartSaci saciT;
		
		MASRunnerSaci(CompileThread t, StartSaci st) {
			super(t);
			saciT = st;
		}

		void stopRunner() {
			try {
				Launcher l = getLauncher();
				if (l != null) {
					l.killFacilitatorAgs(jasonID.getFileName());
					l.killFacilitator(jasonID.getFileName());
					l.killFacilitatorAgs(jasonID.getFileName() + "-env");
					l.killFacilitator(jasonID.getFileName() + "-env");
				}
			} catch (Exception e) {
				System.err.println("Execution error: " + e);
				e.printStackTrace();
			}
			super.stopRunner();
		}

		public void run() {
			if (saciT != null) {
				if (!saciT.waitSaciOk()) {
					return;
				}
			}
			super.run();
		}
	}

	
	String getAsScriptCommand(String scriptName) {
		return getAsScriptCommand(scriptName, false); 
	}
	String getAsScriptCommand(String scriptName, boolean start) {
		if (System.getProperty("os.name").indexOf("indows") > 0) {
			//command = "command.com /e:2048 /c "+command+".bat";
			String sStart = "";
			if (start) {
				sStart = " start "; 
			}
			return "cmd /c " + sStart + scriptName + ".bat";
		} else {
			return "/bin/sh " + scriptName + ".sh";
		}
	}
	
}

