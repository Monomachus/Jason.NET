package jason.stdlib;

import jason.JasonException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;

/**
<p>Internal action: <b><code>.string</code></b>.

<p>Description: check whether the argument is a string, e.g.: "a". 

<p>Parameters:<ul>
<li>+ arg[0] (any term): the term to be checked.<br/>
</ul>

<p>Examples:<ul>
<li> <code>.string("home page")</code>: success.
<li> <code>.string(b(10))</code>: fail.
<li> <code>.string(b)</code>: fail.
<li> <code>.string(X)</code>: fail if X is free and success if X is bind with a string.
</ul>

*/
public class string extends DefaultInternalAction {

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        try {
            Term t = (Term) args[0].clone();
            un.apply(t);
            return t.isString();
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new JasonException("The internal action 'string' has not received one argument");
        } catch (Exception e) {
            throw new JasonException("Error in internal action 'string': " + e);
        }
    }
}
