package jason.asSyntax;

import jason.asSyntax.parser.as2j;

import java.io.StringReader;

import org.apache.log4j.Logger;

public class ExprTerm extends VarTerm implements NumberTerm {

	public static final int EOplus  = 1; 
	public static final int EOminus = 2; 
	public static final int EOtimes = 3; 
	public static final int EOdiv   = 4; 
	public static final int EOmod   = 5; 
	public static final int EOpow   = 6; 

	private NumberTerm lhs;
	private int op = 0;
	private NumberTerm rhs;

	static private Logger logger = Logger.getLogger(ExprTerm.class.getName());
	
	public ExprTerm() {
		super();
	}
	
	public ExprTerm(NumberTerm t1, int oper, NumberTerm t2) {
		lhs = t1;
		op = oper;
		rhs = t2;
	}

	public ExprTerm(int oper, NumberTerm t1) {
		op = oper;
		lhs = t1;
	}

	/** will return some Term that can be evaluated as Number */
    public static NumberTerm parseExpr(String sExpr) {
        as2j parser = new as2j(new StringReader(sExpr));
        try {
            return (NumberTerm)parser.arithmeticExpression();
        } catch (Exception e) {
            logger.error("Error parsing expression "+sExpr,e);
			return null;
        }
    }
	
	/** make a hard copy of the terms */
	public Object clone() {
		// do not call constructor with term parameter!
		ExprTerm t = new ExprTerm();
		if (lhs != null) {
			t.lhs = (NumberTerm) lhs.clone();
		}

		t.op = this.op;
		
		if (rhs != null) {
			t.rhs = (NumberTerm) rhs.clone();
		}
		return t;
	}
	

	public boolean equals(Object t) {
		try {
			ExprTerm eprt = (ExprTerm)t;
			if (lhs == null && eprt.lhs != null) {
				return false;
			}
			if (lhs != null && !lhs.equals(eprt.lhs)) {
				return false;
			}
			
			if (op != eprt.op) {
				return false;
			}

			if (rhs == null && eprt.rhs != null) {
				return false;
			}
			if (rhs != null && !rhs.equals(eprt.rhs)) {
				return false;
			}
			return true;
		} catch (ClassCastException e) {
			return false;
		}
	}
	
	/** gets the Operation of this ExprTerm */
	public int getOp() {
		return op;
	}
	
	/** gets the LHS of this ExprTerm */
	public NumberTerm getLHS() {
		return lhs;
	}
	
	/** gets the RHS of this ExprTerm */
	public NumberTerm getRHS() {
		return rhs;
	}
	
	
	/*
	// for unifier compatibility
	public int getTermsSize() {
		if (rhs == null)
			return 2; // unary operator
		else
			return 3; // lhs + op + rhs
	}
	
	// for unifier compatibility
	public Term getTerm(int i) {
		if (i == 0) {
			return new Term(getOpStr());
		}
		if (i == 1) {
			return (Term)lhs;
		}
		if (i == 2) {
			return rhs;
		}
		return null;
	}
	*/
	
	/** return the this ListTerm elements (0=Term, 1=ListTerm) */
/*	public List getTerms() {
		List l = new ArrayList(2);
		if (term != null) {
			l.add(term);
		}
		if (next != null) {
			l.add(next);
		}
		return l;
	}
*/	

	public void addTerm(Term t) {
		logger.warn("Do not use addTerm in expressions!");
	}

	public boolean isExpr() {
		return !hasValue();
	}
	public boolean isVar() {
		return false;
	}
	
	public boolean isUnary() {
		return rhs == null;
	}

	public boolean isGround() {
		return lhs.isGround() && rhs.isGround();
	}
	
	public double solve() {
		//try {
			//ExprTerm et = (ExprTerm)lhs;
			//l = et.solve();
		//} catch (Exception e) {
		double l = lhs.solve();
		//}
		if (rhs==null && op == EOminus) {
			return -l;
		} else if (rhs != null) {

			//try {
			//	ExprTerm et = (ExprTerm)rhs;
			//	r = et.solve();
			//} catch (Exception e) {
			double r = rhs.solve();
			//}
			switch (op) {
			case EOplus:  return l + r;
			case EOminus: return l - r;
			case EOtimes: return l * r;
			case EOdiv:   return l / r;
			case EOmod:   return l % r;
			case EOpow:   return Math.pow(l,r);
			}
		}
		logger.error("ERROR IN EXPRESION!");
		return 0;
	}

	public String getOpStr() {
		switch (op) {
		case EOplus:  return "+";
		case EOminus: return "-";
		case EOtimes: return "*";
		case EOdiv:   return "/";
		case EOmod:   return "%";
		case EOpow:   return "**";
		}
		return "?";
	}
	
	
	public String toString() {
		if (hasValue()) {
			return getValue().toString();
		} else {
			if (rhs==null) {
				return "("+getOpStr()+lhs+")";
			} else {
				return "("+lhs+getOpStr()+rhs+")";
			}
		}
	}
}
