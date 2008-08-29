
package jason.stdlib;

import jason.JasonException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.DefaultTerm;
import jason.asSyntax.StringTerm;
import jason.asSyntax.StringTermImpl;
import jason.asSyntax.Term;

/**
  <p>Internal action: <b><code>.term2string(T,S)</code></b>.

  <p>Description: converts the term T into a string S and vice-versa.

  <p>Parameters:<ul>
  <li>-/+ T (any term).<br/>
  <li>-/+ S (a string).<br/>
  </ul>

  <p>Examples:<ul>
  <li> <code>.substring(b,"b")</code>: true.
  <li> <code>.substring(b,X)</code>: unifies X to "b".
  <li> <code>.substring(X,"b")</code>: unified X to b.
  </ul>

  @see jason.stdlib.concat
  @see jason.stdlib.delete
  @see jason.stdlib.length
  @see jason.stdlib.reverse

*/
public class term2string extends DefaultInternalAction {
	
	private static InternalAction singleton = null;
	public static InternalAction create() {
		if (singleton == null) 
			singleton = new term2string();
		return singleton;
	}

    @Override
    public Object execute(TransitionSystem ts, final Unifier un, final Term[] args) throws Exception {
        try {
            // case 1, no vars
            if (!args[0].isVar() && args[1].isString()) {
                return args[0].toString().equals( ((StringTerm)args[1]).getString() );
            }
            
            // case 2, second is var
            if (!args[0].isVar() && args[1].isVar()) {
                return un.unifies(new StringTermImpl(args[0].toString()), args[1]);
            }
            
            // case 3, first is var
            if (args[0].isVar() && args[1].isString()) {
                return un.unifies(args[0], DefaultTerm.parse( ((StringTerm)args[1]).getString() ));
            }
            
            throw new JasonException("invalid case of term2string");
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new JasonException("The internal action 'term2string' has not received two arguments.");
        } catch (JasonException e) {
            throw e;
        } catch (Exception e) {
            throw new JasonException("Error in internal action 'term2string': " + e, e);
        }
    }
}
