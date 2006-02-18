package jason.infra.saci;

import jason.infra.InfrastructureFactory;
import jason.mas2j.WriteScriptsInfraTier;
import jason.runtime.MASLauncher;

public class SaciFactory implements InfrastructureFactory {

	public WriteScriptsInfraTier createWriteScripts() {
		return new SaciWriteScripts();
	}
	
	public MASLauncher createMASLauncher() {
		return new SaciMASLauncher();
	}
}
