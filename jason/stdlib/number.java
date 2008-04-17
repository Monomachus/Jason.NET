package jason.stdlib;

import jason.JasonException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;

/**
  <p>Internal action: <b><code>.number</code></b>.

  <p>Description: checks whether the argument is a number. 

  <p>Parameter:<ul>
  <li>+ argument (any term): the term to be checked.<br/>
  </ul>

  <p>Examples:<ul>
  <li> <code>.number(10)</code>: true.
  <li> <code>.number(10.34)</code>: true.
  <li> <code>.number(b(10))</code>: false.
  <li> <code>.number("home page")</code>: false.
  <li> <code>.number(X)</code>: false if X is free, true if X is bound to a number.
  </ul>

  @see jason.stdlib.atom
  @see jason.stdlib.list
  @see jason.stdlib.literal
  @see jason.stdlib.string
  @see jason.stdlib.structure
  @see jason.stdlib.ground

*/
public class number extends DefaultInternalAction {
	
	private static InternalAction singleton = null;
	public static InternalAction create() {
		if (singleton == null) 
			singleton = new number();
		return singleton;
	}

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        try {
            return args[0].isNumeric();
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new JasonException("The internal action 'number' has not received the required argument.");
        } catch (Exception e) {
            throw new JasonException("Error in internal action 'number': " + e, e);
        }
    }
}
