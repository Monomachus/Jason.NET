package jason.infra.saci;

import jason.jeditplugin.MASLauncher;
import jason.mas2j.MAS2JProject;

import java.io.File;

import javax.swing.JOptionPane;

import saci.launcher.Launcher;
import saci.launcher.LauncherD;

class StartSaci extends Thread {

	boolean saciOk = false;
	Process saciProcess;
	MAS2JProject project;
	
	StartSaci(MAS2JProject project) {
		super("StartSaci");
		this.project = project;
	}

	Launcher getLauncher() {
		//PrintStream err = System.err;
		Launcher l = null;
		try {
			//if (jasonID != null) {
			//	System.setErr(jasonID.myOut.originalErr);
			//}
			l = LauncherD.getLauncher();
			return l;
		} catch (Exception e) {
			return null;
		} finally {
			//System.setErr(err);
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
			String command = MASLauncher.getAsScriptCommand("saci-" + project.getSocName(), true);
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
