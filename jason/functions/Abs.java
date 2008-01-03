package jason.functions;

import jason.JasonException;
import jason.asSyntax.ArithFunction;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Term;

/** 
  <p>Function: <b><code>math.abs(N)</code></b>: encapsulates java Math.abs(N).
  
  <p>Examples:<ul>
  <li> <code>math.abs(1)</code>: returns 1.</li>
  <li> <code>math.abs(-1)</code>: returns 1.</li>
  </ul>
   
  @author Jomi 
*/
public class Abs extends ArithFunction  {

	private static final long serialVersionUID = 1L;
	public  static final String name = "math.abs";

	public Abs() {
		super(name,1);
	}
		
	private Abs(Abs a) { // used by clone
		super(a);
	}
	
	@Override
	public double evaluate(Term[] args) throws JasonException {
		if (args[0].isNumeric()) {
			double n = ((NumberTerm)args[0]).solve();
			return Math.abs(n);
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
        	return new Abs(this);
        }
	}
}
