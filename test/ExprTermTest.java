package test;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import jason.asSemantics.Unifier;
import jason.asSyntax.ExprTerm;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.NumberTermImpl;
import jason.asSyntax.Term;
import jason.asSyntax.VarTerm;
import junit.framework.TestCase;

/** JUnit test case for syntax package */
public class ExprTermTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
		Logger.getRootLogger().addAppender(new ConsoleAppender(new PatternLayout("[%c{1}] %m%n")));
    	Logger.getRootLogger().setLevel(Level.DEBUG);
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
		Term t1 = Term.parse("p(X*2)");
		Term t2 = Term.parse("p(Y)");
		Unifier u = new Unifier();
		u.unifies(new VarTerm("X"), new NumberTermImpl(5));
		assertTrue(u.unifies(t1,t2));
		//System.out.println("u="+u);
		NumberTerm yvl = (NumberTerm)u.get("Y");
		assertEquals(yvl, new NumberTermImpl(10));
	}
}
