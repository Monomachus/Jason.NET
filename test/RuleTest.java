package test;

import jason.asSemantics.Agent;
import jason.asSemantics.Unifier;
import jason.asSyntax.Literal;
import jason.asSyntax.LogExprTerm;
import jason.asSyntax.Rule;

import java.util.Iterator;

import junit.framework.TestCase;

/** JUnit test case for syntax package */
public class RuleTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

    
    public void testLogCons() {
        Agent ag = new Agent();
        ag.getBB().add(Literal.parseLiteral("a(10)"));
        ag.getBB().add(Literal.parseLiteral("a(20)"));
        ag.getBB().add(Literal.parseLiteral("a(30)"));
        ag.getBB().add(Literal.parseLiteral("b(20,10)"));
        ag.getBB().add(Literal.parseLiteral("c(x)"));
        ag.getBB().add(Literal.parseLiteral("c(y)"));
        ag.getBB().add(Literal.parseLiteral("c(20)"));
        
        // add r(X) :- a(X)
        Rule r = new Rule(Literal.parseLiteral("r(X)"), Literal.parseLiteral("a(X)"));
        ag.getBB().add(r);
        
        Iterator<Unifier> iun = Literal.parseLiteral("r(20)").logCons(ag, new Unifier());
        assertEquals(1,iteratorSize(iun));

        iun = Literal.parseLiteral("r(Y)").logCons(ag, new Unifier());
        assertEquals(3,iteratorSize(iun));

        // add v(X) :- a(X) & X > 15 | c(X)
        r = new Rule(Literal.parseLiteral("v(X)"), LogExprTerm.parseExpr("a(X) & X > 15 | c(X)"));
        ag.getBB().add(r);

        iun = Literal.parseLiteral("v(30)").logCons(ag, new Unifier());
        assertEquals(1,iteratorSize(iun));

        iun = Literal.parseLiteral("v(20)").logCons(ag, new Unifier());
        assertEquals(2,iteratorSize(iun));

        iun = Literal.parseLiteral("v(A)").logCons(ag, new Unifier());
        assertEquals(5,iteratorSize(iun));

        // add s(X) :- r(X)
        r = new Rule(Literal.parseLiteral("s(X)"), LogExprTerm.parseExpr("r(X)"));
        ag.getBB().add(r);
        iun = Literal.parseLiteral("s(X)").logCons(ag, new Unifier());
        assertEquals(3,iteratorSize(iun));
        
        // add t(a) :- s(X)
        r = new Rule(Literal.parseLiteral("t(a)"), LogExprTerm.parseExpr("s(X)"));
        ag.getBB().add(r);
        
        iun = Literal.parseLiteral("t(X)").logCons(ag, new Unifier());
        assertEquals(3,iteratorSize(iun));
        /*
        while (iun.hasNext()) {
            System.out.println(iun.next());
        }
        */
    }
        
    private int iteratorSize(Iterator i) {
        int c = 0;
        while (i.hasNext()) {
            i.next();
            c++;
        }
        return c;
    }
    
}
