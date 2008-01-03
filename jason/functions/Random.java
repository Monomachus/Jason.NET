package jason.functions;

import jason.JasonException;
import jason.asSyntax.ArithFunction;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Term;

/** 
  <p>Function: <b><code>math.random(N)</code></b>: encapsulates java Math.random;
  If N is not informed: returns a value greater than or equal to 0.0 and less than 1.0;
  If N is informed: returns a value greater than or equal to 0.0 and less than N.
  
  <p>Examples:<ul>
  <li> <code>math.random</code>: returns the random number between 0 and 1.</li>
  <li> <code>math.random(10)</code>: returns the random number between 0 and 10.</li>
  </ul>
   
  @author Jomi 
*/
public class Random extends ArithFunction  {

	private static final long serialVersionUID = 1L;
	public  static final String name = "math.random";

	public Random() {
		super(name,1);
	}
		
	private Random(Random a) { // used by clone
		super(a);
	}
	
	@Override
	public double evaluate(Term[] args) throws JasonException {
		if (args.length == 1 && args[0].isNumeric()) {
			return Math.random() * ((NumberTerm)args[0]).solve();
		} else {
			return Math.random();
		}
	}

	@Override
	public boolean checkArity(int a) {
		return a == 0 || a == 1;
	}
	
	@Override
	public Object clone() {
		return new Random(this);
	}
}
