package jason.functions;

import jason.JasonException;
import jason.asSyntax.ArithFunction;
import jason.asSyntax.ListTerm;
import jason.asSyntax.StringTerm;
import jason.asSyntax.Term;

/** 
  <p>Function: <b><code>.length(L)</code></b>: returns the size of either the list or string L.
  
  <p>Examples:<ul>
  <li> <code>.length("aa")</code>: returns 2.</li>
  <li> <code>.length([a,b,c])</code>: returns 3.</li>
  </ul>
   
  @author Jomi 
*/
public class Length extends ArithFunction  {

	private static final long serialVersionUID = 1L;
	public  static final String name = ".length";

	public Length() {
		super(name,1);
	}
		
	private Length(Length a) { // used by clone
		super(a);
	}
	
	@Override
	public double evaluate(Term[] args) throws JasonException {
		if (args[0].isList()) {
			return ((ListTerm)args[0]).size();
		} else if (args[0].isString()) {
			return ((StringTerm)args[0]).getString().length();
		} else {
			throw new JasonException("The argument '"+args[0]+"' is not a list or a string!");
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
        	return new Length(this);
        }
	}
}
