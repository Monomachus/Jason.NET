package jason.infra.saci;

import java.util.logging.Level;
import java.util.logging.Logger;

import jason.runtime.MASLauncher;
import saci.launcher.Launcher;

public class SaciMASLauncher extends MASLauncher {
	StartSaci saciThread;
	Launcher l;

	boolean iHaveStartedSaci = false;
	
	private static Logger logger = Logger.getLogger(SaciMASLauncher.class.getName());

	
	public SaciMASLauncher() {
		stopOnProcessExit = false;
	}

	public void stopMAS() {
		try {
			new SaciRuntimeServices(project.getSocName()).stopMAS();
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Error stoping saci MAS", e);
		}
		if (iHaveStartedSaci) {
			saciThread.stopSaci();
		}
		super.stopMAS();
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
