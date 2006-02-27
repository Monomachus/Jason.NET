package jason.infra;

import jason.mas2j.WriteScriptsInfraTier;
import jason.runtime.MASLauncher;
import jason.runtime.RuntimeServicesInfraTier;

public interface InfrastructureFactory {
	public WriteScriptsInfraTier createWriteScripts();
	public MASLauncher createMASLauncher();
	public RuntimeServicesInfraTier createRuntimeServices();
}
