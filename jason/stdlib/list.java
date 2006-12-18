package jason.stdlib;

import jason.JasonException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;

/**
<p>Internal action: <b><code>.list</code></b>.

<p>Description: check whether the argument is a list, e.g.: "[a,b]", "[]". 

<p>Parameters:<ul>
<li>+ arg[0] (any term): the term to be checked.<br/>
</ul>

<p>Examples:<ul>
<li> <code>.list([a,b,c])</code>: success.
<li> <code>.list([a,b,c(X)])</code>: success.
<li> <code>.list(b(10))</code>: fail.
<li> <code>.list(10)</code>: fail.
<li> <code>.list("home page")</code>: fail.
<li> <code>.list(X)</code>: fail if X is free and success if X is bind with a list.
<li> <code>.list(a(X))</code>: fail.
</ul>

  @see jason.stdlib.atom
  @see jason.stdlib.literal
  @see jason.stdlib.number
  @see jason.stdlib.string
  @see jason.stdlib.structure
  @see jason.stdlib.var
  @see jason.stdlib.ground

*/public class list extends DefaultInternalAction {

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        try {
            Term t = (Term) args[0].clone();
            un.apply(t);
            return t.isList();
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new JasonException("The internal action 'list' has not received one argument");
        } catch (Exception e) {
            throw new JasonException("Error in internal action 'list': " + e);
        }
    }
}
