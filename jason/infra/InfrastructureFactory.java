package jason.infra;

import jason.mas2j.WriteScriptsInfraTier;
import jason.runtime.MASLauncher;

public interface InfrastructureFactory {
	public WriteScriptsInfraTier createWriteScripts();
	public MASLauncher createMASLauncher();
}
