package jason.asSemantics;

import jason.asSyntax.Term;

/**
 * Default implementation of the internal action interface (it simply returns false 
 * for the interface methods).
 * 
 * Useful to create new internal actions.
 * 
 * @author Jomi
 */
public class DefaultInternalAction implements InternalAction {
    
    public boolean suspendIntention() {
        return false;
    }

    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        return false;
    }
}
