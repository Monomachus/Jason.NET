package jason.functions;

import jason.JasonException;
import jason.asSemantics.DefaultArithFunction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Literal;
import jason.asSyntax.Term;

import java.util.Iterator;

/** 
  <p>Function: <b><code>.count(N)</code></b>: counts the number of occurrences of a particular belief
  (pattern) in the agent's belief base, as the internal action .count.

  <p>Example:<ul> 

  <li> <code>.count(a(2,_))</code>: returns the number of beliefs
  that unify with <code>a(2,_)</code>.</li>
  
  </ul>
  
  @see jason.stdlib.count 

  @author Jomi 
*/
public class Count extends DefaultArithFunction  {

	private static final long serialVersionUID = 1L;

	public String getName() {
	    return ".count";
	}
	
	@Override
	public double evaluate(TransitionSystem ts, Term[] args) throws Exception {
	    if (ts == null) {
            throw new JasonException("The TransitionSystem parameter of the function '.count' can not be null.");
	    }
        Literal bel = (Literal)args[0]; // Literal.parseLiteral(args[0].toString());
        int n = 0;
        // find all "bel" entries in the belief base and builds up a list with them
        Iterator<Unifier> iu = bel.logicalConsequence(ts.getAg(), new Unifier());
        while (iu.hasNext()) {
            iu.next();
            n++;
        }
        return n;
	}

	@Override
	public boolean checkArity(int a) {
		return a == 1;
	}
	
	@Override
	public boolean allowUngroundTerms() {
	    return true;
	}
	
}