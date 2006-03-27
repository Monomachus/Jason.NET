package jason.infra.centralised;

import jason.architecture.AgArch;
import jason.asSemantics.Agent;
import jason.runtime.RuntimeServicesInfraTier;
import jason.runtime.Settings;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/** This class implements the centralised version of the runtime services. */
public class CentralisedRuntimeServices implements RuntimeServicesInfraTier {

	private static Logger logger = Logger.getLogger(CentralisedRuntimeServices.class.getName());

	public boolean createAgent(String agName, String agSource, String agClass, String archClass, Settings stts) throws Exception {
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("Creating centralised agent "+agName+"from source "+agSource+"(agClass="+agClass+", archClass="+archClass+", settings="+stts);
		}
        // parameters for ini
		
		if (agClass == null) agClass = Agent.class.getName();
		if (archClass == null) archClass = AgArch.class.getName();
		if (stts == null) stts = new Settings();

		while (RunCentralisedMAS.getRunner().getEnvironmentInfraTier().getAgent(agName) != null) {
			agName += "_a";
		}
		
        CentralisedAgArch agArch = new CentralisedAgArch();
        agArch.setAgName(agName);
        agArch.initAg(archClass, agClass, agSource, stts);
        agArch.setEnvInfraTier(RunCentralisedMAS.getRunner().getEnvironmentInfraTier());
        agArch.setControlInfraTier(RunCentralisedMAS.getRunner().getControllerInfraTier());
        agArch.getEnvInfraTier().addAgent(agArch.getUserAgArch());
        agArch.start();
        logger.fine("Agent "+agName+" created!");
        return true;
	}

	public Set getAgentsName() {
		return RunCentralisedMAS.getRunner().getEnvironmentInfraTier().getAgents().keySet();
	}

	public int getAgentsQty() {
		return RunCentralisedMAS.getRunner().getEnvironmentInfraTier().getAgents().size();
	}

	public boolean killAgent(String agName) {
		logger.fine("Killing centralised agent "+agName);
		CentralisedEnvironment env = RunCentralisedMAS.getRunner().getEnvironmentInfraTier();
		AgArch aa =	env.getAgent(agName);
		aa.getArchInfraTier().stopAg();
		env.delAgent(aa);
        return true;
	}

	public void stopMAS() throws Exception {
		RunCentralisedMAS.getRunner().finish();
	}
}
