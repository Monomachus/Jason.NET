package jason.infra;

import jason.jeditplugin.MASLauncher;
import jason.mas2j.WriteScriptsInfraTier;
import jason.runtime.RuntimeServicesInfraTier;

public interface InfrastructureFactory {
	public WriteScriptsInfraTier createWriteScripts();
	public MASLauncher createMASLauncher();
	public RuntimeServicesInfraTier createRuntimeServices();
}
