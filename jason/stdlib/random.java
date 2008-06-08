package jason.stdlib;

import jason.JasonException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.NumberTermImpl;
import jason.asSyntax.Term;

import java.util.Random;

/**
  <p>Internal action: <b><code>.random(<i>N</i>)</code></b>.
  
  <p>Description: unifies <i>N</i> with a random number between 0 and 1.
  
  <p>Parameter:<ul>
  
  <li>- value (number): the variable to receive the random value<br/>
  
  </ul>
  
  <p>Example:<ul> 

  <li><code>.random(X)</code>.</li>

  </ul>

  @see jason.functions.Random function version

*/
public class random extends DefaultInternalAction {
	
	private static InternalAction singleton = null;
	public static InternalAction create() {
		if (singleton == null) 
			singleton = new random();
		return singleton;
	}
    
    private Random random = new Random();    
	
    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        try {
            if (!args[0].isVar()) {
                throw new JasonException("The first argument of the internal action 'random' is not a variable.");                
            }
            return un.unifies(args[0], new NumberTermImpl(random.nextDouble()));
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new JasonException("The internal action 'random' has not received the required argument.");
        } catch (Exception e) {
            throw new JasonException("Error in internal action 'random': " + e, e);
        }    
    }
}
