package jason.functions;

import jason.JasonException;
import jason.asSyntax.ArithFunction;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Term;

/** 
  <p>Function: <b><code>math.sqrt(N)</code></b>: encapsulates java Math.sqrt(N);
  returns the correctly rounded positive square root of N.
  
  <p>Example:<ul>
  <li> <code>math.sqrt(9)</code>: returns 3.</li>
  </ul>
   
  @author Jomi 
*/
public class Sqrt extends ArithFunction  {

	private static final long serialVersionUID = 1L;
	public  static final String name = "math.sqrt";

	public Sqrt() {
		super(name,1);
	}
		
	private Sqrt(Sqrt a) { // used by clone
		super(a);
	}
	
	@Override
	public double evaluate(Term[] args) throws JasonException {
		if (args[0].isNumeric()) {
			double n = ((NumberTerm)args[0]).solve();
			return Math.sqrt(n);
		} else {
			throw new JasonException("The argument '"+args[0]+"' is not numeric!");
		}
	}

	@Override
	public boolean checkArity(int a) {
		return a == 1;
	}
	
	@Override
	public Object clone() {
        if (isEvaluated()) {
            return getValue();
        } else {
        	return new Sqrt(this);
        }
	}
}
