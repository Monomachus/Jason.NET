package jason.infra.centralised;

import jason.infra.InfrastructureFactory;
import jason.mas2j.WriteScriptsInfraTier;
import jason.runtime.MASLauncher;

public class CentralisedFactory implements InfrastructureFactory {

	public WriteScriptsInfraTier createWriteScripts() {
		return new CentralisedWriteScripts();
	}
	
	public MASLauncher createMASLauncher() {
		return new CentralisedMASLauncher();
	}
}
