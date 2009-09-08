package jason.asSemantics;

import jason.JasonException;
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

    public boolean suspendIntention()   { return false;  }
    public boolean canBeUsedInContext() { return true;  }
    public boolean applyBeforeExecute() { return true; } 

    public int getMinArgs() { return 0; }
    public int getMaxArgs() { return Integer.MAX_VALUE; }
    
    protected void checkArguments(Term[] args) throws JasonException {
        if (args.length < getMinArgs() || args.length > getMaxArgs())
            throw JasonException.createWrongArgumentNb(this);            
    }

    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        return false;
    }
}
