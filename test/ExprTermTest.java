package test;

import jason.asSemantics.Unifier;
import jason.asSyntax.ExprTerm;
import jason.asSyntax.Literal;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.NumberTermImpl;
import jason.asSyntax.Term;
import jason.asSyntax.VarTerm;
import junit.framework.TestCase;

/** JUnit test case for syntax package */
public class ExprTermTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
		//Logger.getRootLogger().addAppender(new ConsoleAppender(new PatternLayout("[%c{1}] %m%n")));
    	//Logger.getRootLogger().setLevel(Level.DEBUG);
	}

	public void testSolve() {
		NumberTerm nb = ExprTerm.parseExpr("-(3+5*(4----1))*-1-15");
		//System.out.println(nb+"="+nb.solve());
		assertTrue(nb.solve() == 13d);
		
		nb = ExprTerm.parseExpr("3+5.1*2");
		//System.out.println(nb+"="+nb.solve());
		assertTrue(nb.solve() == 13.2);
	}
	public void testApply() {
		NumberTerm nb = ExprTerm.parseExpr("(30-X)/(2*X)");
		Unifier u = new Unifier();
		u.unifies(new VarTerm("X"), new NumberTermImpl(5));
		u.apply( (Term)nb );
		//System.out.println(nb+"="+nb.solve());
		assertTrue(nb.solve() == 2.5);		
	}

	public void testUnify() {
		Literal t1 = (Literal)Literal.parseLiteral("p(X*2)").clone();
		Literal t2 = Literal.parseLiteral("p(Y)");
		Unifier u = new Unifier();
		u.unifies(new VarTerm("H"), new NumberTermImpl(5));
		u.unifies(new VarTerm("X"), new VarTerm("H"));
		assertTrue(u.unifies(t1,t2));
		u.apply(t1);
		assertEquals(t1.toString(),"p(10)");
		NumberTerm yvl = (NumberTerm)u.get("Y");
		assertEquals(yvl, new NumberTermImpl(10));
		u.apply(t2);
		assertEquals(t2.toString(),"p(10)");
	}

	public void testAddAddAdd() {
		Literal t1 = Literal.parseLiteral("p(X+1)");
		Unifier u = new Unifier();
		u.unifies(new VarTerm("X"), new NumberTermImpl(0));
		u.apply(t1);
		
		u = new Unifier();
		u.unifies(Literal.parseLiteral("p(CurVl)"), t1);
		u.unifies(new VarTerm("CurVl"), new VarTerm("X"));
		t1 = Literal.parseLiteral("p(X+1)");
		u.apply(t1);
		
		u = new Unifier();
		u.unifies(Literal.parseLiteral("p(CurVl)"), t1);
		u.unifies(new VarTerm("CurVl"), new VarTerm("X"));
		t1 = Literal.parseLiteral("p(X+1)");
		u.apply(t1);

		assertEquals(t1.toString(), "p(3)");
	}

}
