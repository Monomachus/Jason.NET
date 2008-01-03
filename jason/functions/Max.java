package jason.functions;

import jason.JasonException;
import jason.asSyntax.ArithFunction;
import jason.asSyntax.ListTerm;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Term;

/** 
<p>Function: <b><code>math.max(N1,N2)</code></b>: encapsulates java Math.max(N1,N2).
   It also returns the max number of a list.

<p>Examples:<ul>
<li> <code>math.max(1,3)</code>: returns 3.</li>
<li> <code>math.max([3,a,"s",5])</code>: returns 5.</li>
</ul>
 
@author Jomi 
*/
public class Max extends ArithFunction  {

	private static final long serialVersionUID = 1L;
	public  static final String name = "math.max";
	
	public Max() {
		super(name,2);
	}
		
	private Max(Max a) { // used by clone
		super(a);
	}
	
	@Override
	public double evaluate(Term[] args) throws JasonException {
		if (args[0].isNumeric() && args[1].isNumeric()) {
			double n0 = ((NumberTerm)args[0]).solve();
			double n1 = ((NumberTerm)args[1]).solve();
			return Math.max(n0,n1);
		} else if (args[0].isList()) {
			double max = Double.MIN_VALUE;
			for (Term t: (ListTerm)args[0]) {
				if (t.isNumeric()) {
					double n = ((NumberTerm)t).solve();
					if (n > max)
						max = n;
				}
			}
			return max;
		}
		throw new JasonException(name+" is not implemented for type '"+args[0]+"'.");
	}

	@Override
	public boolean checkArity(int a) {
		return a == 1 || a == 2;
	}
	
	@Override
	public Object clone() {
        if (isEvaluated()) {
            return getValue();
        } else {
        	return new Max(this);
        }
	}
}
