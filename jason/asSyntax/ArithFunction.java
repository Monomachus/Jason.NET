package jason.asSyntax;

import jason.JasonException;
import jason.asSemantics.Unifier;
import jason.asSyntax.parser.as2j;

import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public abstract class ArithFunction extends Structure implements NumberTerm {

    private static Logger logger = Logger.getLogger(ArithFunction.class.getName());
	
    private NumberTerm fValue = null; // value, when evaluated

    /** returns some Term that can be evaluated as Number */
    public static NumberTerm parseExpr(String sExpr) {
        as2j parser = new as2j(new StringReader(sExpr));
        try {
            return (NumberTerm) parser.arithm_expr();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error parsing expression " + sExpr, e);
            return null;
        }
    }

	public ArithFunction(String functor, int termsSize) {
		super(functor, termsSize);
	}

	public ArithFunction(ArithFunction af) {
		super(af);
		fValue = af.fValue;
	}
	
	public NumberTerm getValue() {
		return fValue;
	}
	
	@Override
	public boolean isStructure() {
		return false;
	}

	@Override
	public boolean isNumeric() {
		return true;
	}

    @Override
    public boolean isArithExpr() {
        return !isEvaluated();
    }

    /** returns true if the expression was already evaluated */
    public boolean isEvaluated() {
        return fValue != null;
    }

    @Override
    public boolean isGround() {
        return isEvaluated() || super.isGround();
    }

    /** evaluates/computes the function based on the args */
	abstract public double evaluate(Term[] args) throws JasonException ;
	
	/** returns true if a is a good number of arguments for the function */
	abstract public boolean checkArity(int a);
	
	@Override
	public boolean apply(Unifier u) {
    	if (isEvaluated()) 
    		return false;
    	
    	super.apply(u);
    	if (!isGround()) {
    		logger.warning(getErrorMsg()+ " -- this function has unground arguments and can not be evaluated!");
    		return false;
    	}
    	
    	try {
			fValue = new NumberTermImpl(evaluate(getTermsArray()));
	    	return true;
		} catch (JasonException e) {
    		logger.log(Level.SEVERE, getErrorMsg()+ " -- "+ e);
		}
		return false;
    }
	
	public double solve() {
		if (isEvaluated())
			return fValue.solve();
		else
	    	try {
	    		return evaluate(getTermsArray());
	    	} catch (JasonException e) {
	    		logger.log(Level.SEVERE, getErrorMsg()+ " -- "+ e);
	    		return 0;
	    	}	    	
	}

    @Override
    public boolean equals(Object t) {
        if (t == null) return false;
        if (isEvaluated()) return fValue.equals(t);
        return super.equals(t);
    }

    @Override
    public int compareTo(Term o) {
        try {
            NumberTerm st = (NumberTerm)o;
            if (solve() > st.solve()) return 1;
            if (solve() < st.solve()) return -1;
        } catch (Exception e) {}
        return 0;    
    }

    @Override
    protected int calcHashCode() {
        if (isEvaluated())
        	return fValue.hashCode();
        else
        	return super.calcHashCode();
    }

    @Override
    public String toString() {
        if (isEvaluated())
            return fValue.toString();
        else
        	return super.toString();
    }

    @Override    
    public String getErrorMsg() {
        return "Error in '"+this+"' ("+ super.getErrorMsg() + ")";       
    }
		
	public Element getAsDOM(Document document) {
        if (isEvaluated()) {
            return fValue.getAsDOM(document);
        } else {
            Element u = (Element) document.createElement("expression");
            u.setAttribute("type", "arithmetic");
            Element r = (Element) document.createElement("right");
            r.appendChild(super.getAsDOM(document)); // put the left argument indeed!
            u.appendChild(r);
            return u;
        }
	}
}
