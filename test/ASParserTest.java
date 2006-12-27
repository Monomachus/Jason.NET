package test;

import jason.asSemantics.Agent;
import jason.asSemantics.Unifier;
import jason.asSyntax.BodyLiteral;
import jason.asSyntax.LogExpr;
import jason.asSyntax.LogicalFormula;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Plan;
import jason.asSyntax.RelExpr;
import jason.asSyntax.parser.ParseException;
import jason.asSyntax.parser.as2j;

import java.io.StringReader;
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
        Plan p = ag.getPL().get("l__0");
        assertEquals(p.getBody().size(), 1);
        assertEquals(p.getBody().get(0).getType(), BodyLiteral.BodyType.internalAction);
        assertTrue(ag.parseAS("examples/Auction/ag2.asl"));
        assertTrue(ag.parseAS("examples/Auction/ag3.asl"));
    }

    public void testLogicalExpr() {
        LogicalFormula t1 = LogExpr.parseExpr("(3 + 5) / 2 > 0");
        assertTrue(t1 != null);
        Iterator<Unifier> solve = ((RelExpr) t1).logicalConsequence(null, new Unifier());
        assertTrue(solve.hasNext());

        t1 = LogExpr.parseExpr("0 > ((3 + 5) / 2)");
        assertTrue(t1 != null);
        solve = ((RelExpr) t1).logicalConsequence(null, new Unifier());
        assertFalse(solve.hasNext());

        t1 = LogExpr.parseExpr("0 > (-5)");
        assertTrue(t1 != null);
        solve = ((RelExpr) t1).logicalConsequence(null, new Unifier());
        assertTrue(solve.hasNext());

        t1 = LogExpr.parseExpr("(((((((30) + -5)))))) / 2 > 5+3*8");
        assertTrue(t1 != null);
        solve = ((RelExpr) t1).logicalConsequence(null, new Unifier());
        assertFalse(solve.hasNext());
         
        t1 = LogExpr.parseExpr("-2 > -3");
        assertTrue(t1 != null);
        RelExpr r1 = (RelExpr) t1;
        NumberTerm lt1 = (NumberTerm)r1.getLHS();
        NumberTerm rt1 = (NumberTerm)r1.getRHS();
        assertEquals(lt1.solve(),-2.0);
        assertEquals(rt1.solve(),-3.0);
        //System.out.println(lt1.getClass().getName()+"="+lt1.compareTo(rt1));
        solve = r1.logicalConsequence(null, new Unifier());
        assertTrue(solve.hasNext());

        t1 = LogExpr.parseExpr("(3 - 5) > (-1 + -2)");
        assertTrue(t1 != null);
        solve = ((RelExpr)t1).logicalConsequence(null, new Unifier());
        assertTrue(solve.hasNext());
        
        t1 = LogExpr.parseExpr("(3 - 5) > -3 & 2 > 1");
        assertTrue(t1 != null);
        solve = ((LogExpr) t1).logicalConsequence(null, new Unifier());
        assertTrue(solve.hasNext());
        
        t1 = LogExpr.parseExpr("(3 - 5) > -3 & 0 > 1");
        assertTrue(t1 != null);
        solve = ((LogExpr) t1).logicalConsequence(null, new Unifier());
        assertFalse(solve.hasNext());

        t1 = LogExpr.parseExpr("(3 - 5) > -3 | 0 > 1");
        assertTrue(t1 != null);
        solve = ((LogExpr) t1).logicalConsequence(null, new Unifier());
        assertTrue(solve.hasNext());

        t1 = LogExpr.parseExpr("(3 > 5) | false");
        assertTrue(t1 != null);
        solve = ((LogExpr) t1).logicalConsequence(null, new Unifier());
        assertFalse(solve.hasNext());

        t1 = LogExpr.parseExpr("(3 > 5) | true");
        assertTrue(t1 != null);
        solve = ((LogExpr) t1).logicalConsequence(null, new Unifier());
        assertTrue(solve.hasNext());

        t1 = LogExpr.parseExpr("not 3 > 5");
        assertTrue(t1 != null);
        solve = ((LogExpr) t1).logicalConsequence(null, new Unifier());
        assertTrue(solve.hasNext());

    }

    public void testDirectives() {
        String 
        source =  " b(10). ";
        source += " { begin bc(at(X,Y), ebdg(at(X,Y))) } \n";
        source += "    +!at(X,Y) : b(X) <- go(X). ";
        source += "    +!at(X,Y) : not b(X) <- go(3). ";
        source += " { end }";


        try {
            as2j parser = new as2j(new StringReader(source));
            Agent a = new Agent();
            parser.agent(a);
            assertEquals(a.getPL().getPlans().size(), 7);

            source =  " { begin omc(at(X,Y), no_battery, no_beer) } \n";
            source += "    +!at(X,Y) : b(X) <- go(X). ";
            source += "    +!at(X,Y) : not b(X) <- go(3). ";
            source += " { end }";
            parser = new as2j(new StringReader(source));
            a = new Agent();
            parser.agent(a);
            assertTrue(a.getPL().getPlans().size() == 8);

            source =  " { begin mg(at(10,10)) } \n";
            source += "    +!at(X,Y) : b(X) <- go(X). ";
            source += "    +!at(X,Y) : not b(X) <- go(3). ";
            source += " { end }";
            parser = new as2j(new StringReader(source));
            a = new Agent();
            parser.agent(a);
            //for (Plan p: a.getPL().getPlans()) {
            //    System.out.println(p);
            //}
            assertTrue(a.getPL().getPlans().size() == 7);
            assertTrue(a.getBB().size() == 1);

            source =  " { begin sga(\"+go(X,Y)\", \"(at(home) & not c)\", at(X,Y)) } \n";
            source += " { end }";
            parser = new as2j(new StringReader(source));
            a = new Agent();
            parser.agent(a);
            //for (Plan p: a.getPL().getPlans()) {
            //    System.out.println(p);
            //}
            assertTrue(a.getPL().getPlans().size() == 5);
        
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
