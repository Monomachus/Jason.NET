package jason.infra;

import jason.jeditplugin.MASLauncherInfraTier;
import jason.runtime.RuntimeServicesInfraTier;

/**
 * Every infrastructure for Jason must implement this interface.
 * The interface provide methods for JasonIDE, user runtime classes, ....
 * 
 * @author jomi
 */
public interface InfrastructureFactory {
	public MASLauncherInfraTier createMASLauncher();
	public RuntimeServicesInfraTier createRuntimeServices();
}
