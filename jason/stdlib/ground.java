package jason.stdlib;

import jason.JasonException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;

/**
<p>Internal action: <b><code>.ground</code></b>.

<p>Description: check whether the argument is ground, i.e., has
no free variable. Numbers and Strins are always ground.

<p>Parameters:<ul>
<li>+ arg[0] (any term): the term to be checked.<br/>
</ul>

<p>Examples:<ul>
<li> <code>.ground(b(10))</code>: success.
<li> <code>.ground(10)</code>: success.
<li> <code>.ground(X)</code>: fail if X is free.
<li> <code>.ground(a(X))</code>: fail if X is free.
<li> <code>.ground([a,b,c])</code>: success.
<li> <code>.ground([a,b,c(X)])</code>: fail if X is free.
</ul>

  @see jason.stdlib.atom
  @see jason.stdlib.list
  @see jason.stdlib.literal
  @see jason.stdlib.number
  @see jason.stdlib.string
  @see jason.stdlib.structure
  @see jason.stdlib.var

*/
public class ground extends DefaultInternalAction {

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        try {
            Term t = (Term) args[0].clone();
            un.apply(t);
            return t.isGround();
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new JasonException("The internal action 'ground' has not received one argument");
        } catch (Exception e) {
            throw new JasonException("Error in internal action 'ground': " + e);
        }
    }
}
