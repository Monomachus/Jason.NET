package jason.stdlib;

import jason.JasonException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;

/**
<p>Internal action: <b><code>.literal</code></b>.

<p>Description: check whether the argument is a literal (a structure and not a list),
 e.g.: "p", "p(1)", "p(1)[a,b]", "~p(1)[a,b]". 

<p>Parameters:<ul>
<li>+ arg[0] (any term): the term to be checked.<br/>
</ul>

<p>Examples:<ul>
<li> <code>.literal(b(10))</code>: success.
<li> <code>.literal(b)</code>: success.
<li> <code>.literal(10)</code>: fail.
<li> <code>.literal("Jason")</code>: fail.
<li> <code>.literal(X)</code>: fail if X is free and success if X is bind with a literal.
<li> <code>.literal(a(X))</code>: success.
<li> <code>.literal([a,b,c])</code>: fail.
<li> <code>.literal([a,b,c(X)])</code>: fail.
</ul>

  @see jason.stdlib.atom
  @see jason.stdlib.list
  @see jason.stdlib.number
  @see jason.stdlib.string
  @see jason.stdlib.structure
  @see jason.stdlib.var
  @see jason.stdlib.ground
*/
public class literal extends DefaultInternalAction {

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        try {
            Term t = (Term) args[0].clone();
            un.apply(t);
            return t.isStructure() && !t.isList();
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new JasonException("The internal action 'literal' has not received one argument");
        } catch (Exception e) {
            throw new JasonException("Error in internal action 'literal': " + e);
        }
    }
}
