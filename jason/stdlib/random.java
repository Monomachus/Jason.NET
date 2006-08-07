package jason.stdlib;

import jason.JasonException;
import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.NumberTermImpl;
import jason.asSyntax.Term;

import java.util.Random;

public class random implements InternalAction {
    
    private Random random = new Random();    
	
    /**
	 * args[0] is the variable that unifies the random value (from 0..1)
	 */
    public boolean execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        try {
            if (!args[0].isVar()) {
                throw new JasonException("The first argument of the internal action 'random' is not a variable.");                
            }
            return un.unifies(args[0], new NumberTermImpl(random.nextDouble()));
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new JasonException("The internal action 'random' has not received one argument.");
        } catch (Exception e) {
            throw new JasonException("Error in internal action 'random': " + e);
        }    
    }
}
