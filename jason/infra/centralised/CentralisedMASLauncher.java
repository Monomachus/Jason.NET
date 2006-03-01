package jason.infra.centralised;

import jason.jeditplugin.MASLauncher;

public class CentralisedMASLauncher extends MASLauncher {

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
}
