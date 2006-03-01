package jason.jeditplugin;

import jason.mas2j.MAS2JProject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;

/** Used by the Jason IDE to launch an MAS. 
 *  Each infrastructure extends this class.
 */
public abstract class MASLauncher extends Thread {
	protected MAS2JProject project;
	protected boolean stop = false;
	protected boolean stopOnProcessExit = true;
	protected RunProjectListener listener;

	protected Process masProcess = null;
	protected OutputStream processOut;

	public MASLauncher() {
		super("MAS-Launcher");
	}

	public void setProject(MAS2JProject project) {
		this.project = project;
	}

	public void setListener(RunProjectListener listener) {
		this.listener = listener;		
	}
	
	public void stopMAS() {
		if (masProcess != null) {
			masProcess.destroy();
		}
		stop = true;
	}

	public void run() {
		try {
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

	public static String getAsScriptCommand(String scriptName) {
		return getAsScriptCommand(scriptName, false); 
	}
	
	public static String getAsScriptCommand(String scriptName, boolean start) {
		if (System.getProperty("os.name").indexOf("indows") > 0) {
			String sStart = " ";
			if (start) {
				sStart = " start "; 
			}
			return Config.get().getShellCommand() + sStart + scriptName + ".bat";
		} else {
			return Config.get().getShellCommand() + " " + scriptName + ".sh";
		}
	}	
	
}
