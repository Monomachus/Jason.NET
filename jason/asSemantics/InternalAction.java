package jason.asSemantics;

import jason.asSyntax.Term;

public interface InternalAction {
	boolean execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception;
}
