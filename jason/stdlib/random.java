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
    
    @Override public int getMinArgs() { return 1; }
    @Override public int getMaxArgs() { return 1; }
    
    @Override protected void checkArguments(Term[] args) throws JasonException {
        super.checkArguments(args); // check number of arguments
        if (!args[0].isVar()) {
            throw JasonException.createWrongArgument(this,"first argument must be a variable.");                
        }
    }

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        checkArguments(args);
        return un.unifies(args[0], new NumberTermImpl(random.nextDouble()));
    }
}
