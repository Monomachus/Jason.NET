package jason.stdlib;

import jason.JasonException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;

/**
<p>Internal action: <b><code>.var</code></b>.

<p>Description: check whether the argument is a variable. 

<p>Parameters:<ul>
<li>+ arg[0] (any term): the term to be checked.<br/>
</ul>

<p>Examples:<ul>
<li> <code>.var(X)</code>: success if X is free and fail otherwise.
<li> <code>.var(b)</code>: fail.
<li> <code>.var(10)</code>: fail.
<li> <code>.var("home page")</code>: fail.
<li> <code>.var(a(X))</code>: fail.
<li> <code>.var([a,b,c])</code>: fail.
</ul>

  @see jason.stdlib.atom
  @see jason.stdlib.list
  @see jason.stdlib.literal
  @see jason.stdlib.number
  @see jason.stdlib.string
  @see jason.stdlib.structure
  @see jason.stdlib.ground

*/
public class var extends DefaultInternalAction {

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        try {
            Term t = args[0];
            un.apply(t);
            return t.isVar();
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new JasonException("The internal action 'var' has not received one argument");
        } catch (Exception e) {
            throw new JasonException("Error in internal action 'var': " + e);
        }
    }
}
