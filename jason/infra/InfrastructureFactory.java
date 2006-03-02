package jason.infra;

import jason.jeditplugin.MASLauncherInfraTier;
import jason.mas2j.MAS2JProject;
import jason.runtime.RuntimeServicesInfraTier;

public interface InfrastructureFactory {
	public MASLauncherInfraTier createMASLauncher();
	public RuntimeServicesInfraTier createRuntimeServices();
}
