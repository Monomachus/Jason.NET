package test;

import jason.asSemantics.Agent;
import jason.asSemantics.Unifier;
import jason.asSyntax.BodyLiteral;
import jason.asSyntax.LogExprTerm;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Plan;
import jason.asSyntax.RelExprTerm;
import jason.asSyntax.Term;

import java.util.Iterator;

import junit.framework.TestCase;

/** JUnit test case for syntax package */
public class ASParserTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testKQML() {
        Agent ag = new Agent();
        ag.setLogger(null);

        assertTrue(ag.parseAS("src/asl/kqmlPlans.asl"));
        assertTrue(ag.parseAS("examples/Auction/ag1.asl"));
        Plan p = ag.getPS().get("l__0");
        assertEquals(p.getBody().size(), 1);
        assertEquals(p.getBody().get(0).getType(), BodyLiteral.BodyType.internalAction);
        assertTrue(ag.parseAS("examples/Auction/ag2.asl"));
        assertTrue(ag.parseAS("examples/Auction/ag3.asl"));

        ag = new Agent();
        ag.setLogger(null);
        assertTrue(ag.parseAS("examples/Test/as/ag0.asl"));
        assertTrue(ag.parseAS("examples/Test/as/ag1.asl"));
    }

    public void testLogicalExpr() {
        Term t1 = LogExprTerm.parseExpr("(3 + 5) / 2 > 0");
        assertTrue(t1 != null);
        Iterator<Unifier> solve = ((RelExprTerm) t1).logCons(null, new Unifier());
        assertTrue(solve.hasNext());

        t1 = LogExprTerm.parseExpr("0 > ((3 + 5) / 2)");
        assertTrue(t1 != null);
        solve = ((RelExprTerm) t1).logCons(null, new Unifier());
        assertFalse(solve.hasNext());

        t1 = LogExprTerm.parseExpr("(((((((30) + -5)))))) / 2 > 5+3*8");
        assertTrue(t1 != null);
        solve = ((RelExprTerm) t1).logCons(null, new Unifier());
        assertFalse(solve.hasNext());

        t1 = LogExprTerm.parseExpr("-2 > -3");
        assertTrue(t1 != null);
        RelExprTerm r1 = (RelExprTerm) t1;
        NumberTerm lt1 = (NumberTerm)r1.getLHS();
        NumberTerm rt1 = (NumberTerm)r1.getRHS();
        assertEquals(lt1.solve(),-2.0);
        assertEquals(rt1.solve(),-3.0);
        //System.out.println(lt1.getClass().getName()+"="+lt1.compareTo(rt1));
        solve = r1.logCons(null, new Unifier());
        assertTrue(solve.hasNext());

        t1 = LogExprTerm.parseExpr("(3 - 5) > (-1 + -2)");
        assertTrue(t1 != null);
        solve = ((RelExprTerm)t1).logCons(null, new Unifier());
        assertTrue(solve.hasNext());
        
        t1 = LogExprTerm.parseExpr("(3 - 5) > -3 & 2 > 1");
        assertTrue(t1 != null);
        solve = ((LogExprTerm) t1).logCons(null, new Unifier());
        assertTrue(solve.hasNext());
        
        t1 = LogExprTerm.parseExpr("(3 - 5) > -3 & 0 > 1");
        assertTrue(t1 != null);
        solve = ((LogExprTerm) t1).logCons(null, new Unifier());
        assertFalse(solve.hasNext());

        t1 = LogExprTerm.parseExpr("(3 - 5) > -3 | 0 > 1");
        assertTrue(t1 != null);
        solve = ((LogExprTerm) t1).logCons(null, new Unifier());
        assertTrue(solve.hasNext());

        t1 = LogExprTerm.parseExpr("(3 > 5) | false");
        assertTrue(t1 != null);
        solve = ((LogExprTerm) t1).logCons(null, new Unifier());
        assertFalse(solve.hasNext());

        t1 = LogExprTerm.parseExpr("(3 > 5) | true");
        assertTrue(t1 != null);
        solve = ((LogExprTerm) t1).logCons(null, new Unifier());
        assertTrue(solve.hasNext());

        t1 = LogExprTerm.parseExpr("not 3 > 5");
        assertTrue(t1 != null);
        solve = ((LogExprTerm) t1).logCons(null, new Unifier());
        assertTrue(solve.hasNext());

    }
}
