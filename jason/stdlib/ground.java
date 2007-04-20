package jason.stdlib;

import jason.JasonException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;

/**
<p>Internal action: <b><code>.ground</code></b>.

<p>Description: checks whether the argument is ground, i.e., it has no free
variables. Numbers, Strings, and Atoms are always ground.

<p>Parameters:<ul>
<li>+ argument (any term): the term to be checked.<br/>
</ul>

<p>Examples:<ul>
<li> <code>.ground(b(10))</code>: true.
<li> <code>.ground(10)</code>: true.
<li> <code>.ground(X)</code>: false only if X is free.
<li> <code>.ground(a(X))</code>: false only if X is free.
<li> <code>.ground([a,b,c])</code>: true.
<li> <code>.ground([a,b,c(X)])</code>: false only if X is free.
</ul>

  @see jason.stdlib.atom
  @see jason.stdlib.list
  @see jason.stdlib.literal
  @see jason.stdlib.number
  @see jason.stdlib.string
  @see jason.stdlib.structure

*/
public class ground extends DefaultInternalAction {

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        try {
            return args[0].isGround();
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new JasonException("The internal action 'ground' has not received the required argument.");
        } catch (Exception e) {
            throw new JasonException("Error in internal action 'ground': " + e, e);
        }
    }
}
