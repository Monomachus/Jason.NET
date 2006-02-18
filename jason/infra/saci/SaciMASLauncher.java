package jason.infra.saci;

import jason.runtime.MASLauncher;
import saci.launcher.Launcher;

public class SaciMASLauncher extends MASLauncher {
	StartSaci saciThread;
	Launcher l;

	boolean iHaveStartedSaci = false;
	
	public SaciMASLauncher() {
		stopOnProcessExit = false;
	}

	public void stopMAS() {
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
