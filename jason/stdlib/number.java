package jason.stdlib;

import jason.JasonException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;

/**
<p>Internal action: <b><code>.number</code></b>.

<p>Description: check whether the argument is a number. 

<p>Parameters:<ul>
<li>+ arg[0] (any term): the term to be checked.<br/>
</ul>

<p>Examples:<ul>
<li> <code>.number(10)</code>: success.
<li> <code>.number(10.34)</code>: success.
<li> <code>.number(b(10))</code>: fail.
<li> <code>.number("home page")</code>: fail.
<li> <code>.number(X)</code>: fail if X is free and success if X is bind with a number.
</ul>

*/public class number extends DefaultInternalAction {

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        try {
            Term t = (Term) args[0].clone();
            un.apply(t);
            return t.isNumeric();
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new JasonException("The internal action 'number' has not received one argument");
        } catch (Exception e) {
            throw new JasonException("Error in internal action 'number': " + e);
        }
    }
}
