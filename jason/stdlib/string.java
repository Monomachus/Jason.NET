package jason.stdlib;

import jason.JasonException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;

/**
  <p>Internal action: <b><code>.string</code></b>.

  <p>Description: checks whether the argument is a string, e.g.: "a". 

  <p>Parameter:<ul>
  <li>+ arg[0] (any term): the term to be checked.<br/>
  </ul>

  <p>Examples:<ul>
  <li> <code>.string("home page")</code>: true.
  <li> <code>.string(b(10))</code>: false.
  <li> <code>.string(b)</code>: false.
  <li> <code>.string(X)</code>: false if X is free, true if X is bound to a string.
  </ul>

  @see jason.stdlib.atom
  @see jason.stdlib.list
  @see jason.stdlib.literal
  @see jason.stdlib.number
  @see jason.stdlib.structure
  @see jason.stdlib.ground

*/
public class string extends DefaultInternalAction {

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        try {
            args[0].apply(un);
            return args[0].isString();
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new JasonException("The internal action 'string' has not received the required argument.");
        } catch (Exception e) {
            throw new JasonException("Error in internal action 'string': " + e);
        }
    }
}
