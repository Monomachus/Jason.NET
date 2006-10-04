package jason.stdlib;

import jason.JasonException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;

public class var extends DefaultInternalAction {

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        try {
            Term t = (Term) args[0].clone();
            un.apply(t);
            return t.isVar();
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new JasonException("The internal action 'var' has not received one argument");
        } catch (Exception e) {
            throw new JasonException("Error in internal action 'var': " + e);
        }
    }
}
