package jason.jeditplugin;

/** 
 * This interface is implemented by objects that
 * wants to be notified by changes in the MAS execution state
 * (like JasonIDE).
 */
public interface RunProjectListener {
	public void masFinished();
}
