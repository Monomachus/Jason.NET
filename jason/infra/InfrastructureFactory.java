package jason.infra;

import jason.jeditplugin.MASLauncher;
import jason.mas2j.MAS2JProject;
import jason.runtime.RuntimeServicesInfraTier;

public interface InfrastructureFactory {
	public MASLauncher createMASLauncher(MAS2JProject project);
	public RuntimeServicesInfraTier createRuntimeServices();
}
