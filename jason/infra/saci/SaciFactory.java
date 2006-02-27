package jason.infra.saci;

import jason.infra.InfrastructureFactory;
import jason.mas2j.WriteScriptsInfraTier;
import jason.runtime.MASLauncher;
import jason.runtime.RuntimeServicesInfraTier;

public class SaciFactory implements InfrastructureFactory {

	public WriteScriptsInfraTier createWriteScripts() {
		return new SaciWriteScripts();
	}
	
	public MASLauncher createMASLauncher() {
		return new SaciMASLauncher();
	}

	public RuntimeServicesInfraTier createRuntimeServices() {
		return new SaciRuntimeServices(null);
	}
}
