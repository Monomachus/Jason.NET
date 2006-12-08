package jason.stdlib;

import jason.JasonException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;

/**
<p>Internal action: <b><code>.atom</code></b>.

<p>Description: check whether the argument is an atom (a structure with arity 0), e.g.: "p". 
Numbers, strings, and unground variables are not structures.

<p>Parameters:<ul>
<li>+ arg[0] (any term): the term to be checked.<br/>
</ul>

<p>Examples:<ul>
<li> <code>.atom(b(10))</code>: fail.
<li> <code>.atom(b)</code>: success.
<li> <code>.atom(10)</code>: fail.
<li> <code>.atom("home page")</code>: fail.
<li> <code>.atom(X)</code>: fail if X is free and success if X is bind with an atom.
<li> <code>.atom(a(X))</code>: fail.
<li> <code>.atom(a[X])</code>: fail.
<li> <code>.atom([a,b,c])</code>: fail.
<li> <code>.atom([a,b,c(X)])</code>: fail.
</ul>

*/
public class atom extends DefaultInternalAction {

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        try {
            Term t = (Term) args[0].clone();
            un.apply(t);
            return t.isStructure() && !t.isList() && t.isConstant();
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new JasonException("The internal action 'structure' has not received one argument");
        } catch (Exception e) {
            throw new JasonException("Error in internal action 'structure': " + e);
        }
    }
}
