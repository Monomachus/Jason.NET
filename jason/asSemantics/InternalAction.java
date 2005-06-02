package jason.asSemantics;

public interface InternalAction {
	boolean execute(TransitionSystem ts, Unifier un, String[] args) throws Exception;
}
