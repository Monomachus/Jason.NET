package jason.infra.saci;

import jason.infra.InfrastructureFactory;
import jason.jeditplugin.MASLauncher;
import jason.mas2j.MAS2JProject;
import jason.runtime.RuntimeServicesInfraTier;

public class SaciFactory implements InfrastructureFactory {

	public MASLauncher createMASLauncher(MAS2JProject project) {
		return new SaciMASLauncher(project);
	}

	public RuntimeServicesInfraTier createRuntimeServices() {
		return new SaciRuntimeServices(null);
	}
}
