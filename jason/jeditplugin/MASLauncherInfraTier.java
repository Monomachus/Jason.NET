package jason.jeditplugin;

import jason.mas2j.MAS2JProject;

/** Used by the Jason IDE to launch an MAS. 
 *  Each infrastructure should implements it.
 */
public interface MASLauncherInfraTier extends Runnable {

	public void setProject(MAS2JProject project);
	public void setListener(RunProjectListener listener);

	public void writeScripts(boolean debug);

	public void stopMAS();
}
