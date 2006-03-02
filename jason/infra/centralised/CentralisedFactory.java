package jason.infra.centralised;

import jason.infra.InfrastructureFactory;
import jason.jeditplugin.MASLauncher;
import jason.mas2j.MAS2JProject;
import jason.runtime.RuntimeServicesInfraTier;

public class CentralisedFactory implements InfrastructureFactory {

	public MASLauncher createMASLauncher(MAS2JProject project) {
		return new CentralisedMASLauncher(project);
	}
	
	public RuntimeServicesInfraTier createRuntimeServices() {
		return new CentralisedRuntimeServices();
	}

}
