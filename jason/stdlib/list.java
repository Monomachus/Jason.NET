package jason.stdlib;

import jason.JasonException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;

/**
  <p>Internal action: <b><code>.list</code></b>.

  <p>Description: checks whether the argument is a list, e.g.: "[a,b]", "[]". 

  <p>Parameter:<ul>
  <li>+ argument (any term): the term to be checked.<br/>
  </ul>

  <p>Examples:<ul>
  <li> <code>.list([a,b,c])</code>: true.
  <li> <code>.list([a,b,c(X)])</code>: true.
  <li> <code>.list(b(10))</code>: false.
  <li> <code>.list(10)</code>: false.
  <li> <code>.list("home page")</code>: false.
  <li> <code>.list(X)</code>: false if X is free, true if X is bound to a list.
  <li> <code>.list(a(X))</code>: false.
  </ul>

  @see jason.stdlib.atom
  @see jason.stdlib.literal
  @see jason.stdlib.number
  @see jason.stdlib.string
  @see jason.stdlib.structure
  @see jason.stdlib.ground

*/

public class list extends DefaultInternalAction {

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        try {
            if (args.length > 1) throw new JasonException("The internal action 'list' has received more than one argument.");
            return args[0].isList();
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new JasonException("The internal action 'list' has not received the required argument.");
        } catch (JasonException j) {
            throw j;
        } catch (Exception e) {
            throw new JasonException("Error in internal action 'list': " + e, e);
        }
    }
}
