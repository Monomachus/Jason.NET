package jason.asSemantics;

import jason.asSyntax.Term;

import java.io.Serializable;

/**
 * Default implementation of the internal action interface (it simply returns false 
 * for the interface methods).
 * 
 * Useful to create new internal actions.
 * 
 * @author Jomi
 */
public class DefaultInternalAction implements InternalAction, Serializable {
    
    private static final long serialVersionUID = 1L;

    public boolean suspendIntention() {
        return false;
    }
    
    public boolean canBeUsedInContext() {
        return true;
    }

    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        return false;
    }
}
