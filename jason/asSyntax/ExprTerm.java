package jason.asSyntax;

import jason.asSyntax.parser.as2j;

import java.io.StringReader;

public class ExprTerm extends Term {

	private Term lhs;
	private Term op;
	private Term rhs;

	public static final Term EOplus  = new Term("+"); 
	public static final Term EOminus = new Term("-"); 
	public static final Term EOtimes = new Term("*"); 
	public static final Term EOdiv   = new Term("/"); 
	public static final Term EOmod   = new Term("%"); 
	public static final Term EOpow   = new Term("**"); 
	
	public ExprTerm() {
		super();
	}
	
	public ExprTerm(Term t1, Term oper, Term t2) {
		lhs = t1;
		op = oper;
		rhs = t2;
	}

	public ExprTerm(Term oper, Term t1) {
		op = oper;
		lhs = t1;
	}

    public static ExprTerm parseExpr(String sExpr) {
        as2j parser = new as2j(new StringReader(sExpr));
        try {
            return (ExprTerm)parser.expression();
        } catch (Exception e) {
            System.err.println("Error parsing expression "+sExpr);
            e.printStackTrace();
			return null;
        }
    }
	
	/** make a hard copy of the terms */
	public Object clone() {
		// do not call constructor with term parameter!
		ExprTerm t = new ExprTerm();
		if (lhs != null) {
			t.lhs = (Term)this.lhs.clone();
		}
		if (op != null) {
			t.op = (Term)this.op.clone();
		}
		if (rhs != null) {
			t.rhs = (Term)this.rhs.clone();
		}
		return t;
	}
	

	public boolean equals(Object t) {
		try {
			ExprTerm tAsTerm = (ExprTerm)t;
			if (lhs == null && tAsTerm.lhs != null) {
				return false;
			}
			if (lhs != null && !lhs.equals(tAsTerm.lhs)) {
				return false;
			}
			if (op == null && tAsTerm.op != null) {
				return false;
			}
			if (op != null && !op.equals(tAsTerm.op)) {
				return false;
			}
			if (rhs == null && tAsTerm.rhs != null) {
				return false;
			}
			if (rhs != null && !rhs.equals(tAsTerm.rhs)) {
				return false;
			}
			return true;
		} catch (ClassCastException e) {
			return false;
		}
	}
	
	/** gets the Operation of this ExprTerm */
	public Term getOp() {
		return op;
	}
	/** gets the LHS of this ExprTerm */
	public Term getLHS() {
		return lhs;
	}
	/** gets the RHS of this ExprTerm */
	public Term getRHS() {
		return rhs;
	}
	
	
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
			return op;
		}
		if (i == 1) {
			return lhs;
		}
		if (i == 2) {
			return rhs;
		}
		return null;
	}
	
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
		System.err.println("Do not use addTerm in expressions!");
	}

	public boolean isExpr() {
		return true;
	}
	public boolean isUnary() {
		return rhs == null;
	}

	public boolean isGround() {
		return lhs.isGround() && rhs.isGround();
	}
	
	public double solve() {
		double l;
		double r;
		try {
			ExprTerm et = (ExprTerm)lhs;
			l = et.solve();
		} catch (Exception e) {
			l = lhs.toDouble();
		}
		if(rhs==null && op.equals(EOminus))
			return -l;
		else if (rhs != null) {
			try {
				ExprTerm et = (ExprTerm)rhs;
				r = et.solve();
			} catch (Exception e) {
				r = rhs.toDouble();
			}
			if (op.equals(EOplus)){
				return l + r;
			}
			else if (op.equals(EOminus)){
				return l - r;
			}
			else if (op.equals(EOtimes)){
				return l * r;
			}
			else if (op.equals(EOdiv)){
				return l / r;
			}
			else if (op.equals(EOmod)){
				return l % r;
			}
			else if (op.equals(EOpow)){
				return Math.pow(l,r);
			}
		}
		System.err.println("ERROR IN EXPRESION!");
		return 0;
	}
	
	public String toString() {
		if (rhs==null)
			return op+" "+lhs;
		return lhs+" "+op+" "+rhs;
	}
		
}
