package jason.asSemantics;

import jason.asSyntax.Term;

/**
 * Default implementation of the internal action interface. Useful to
 * create new internal actions.
 * 
 * @author jomi
 */
public class DefaultInternalAction implements InternalAction {
    
    public boolean suspendIntention() {
        return false;
    }

    public boolean execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        return true;
    }

}
