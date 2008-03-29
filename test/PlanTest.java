package test;

import jason.JasonException;
import jason.asSemantics.Unifier;
import jason.asSyntax.BodyLiteral;
import jason.asSyntax.Plan;
import jason.asSyntax.PlanLibrary;
import jason.asSyntax.Trigger;
import jason.asSyntax.VarTerm;
import jason.asSyntax.BodyLiteral.BodyType;
import jason.asSyntax.parser.ParseException;

import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

public class PlanTest extends TestCase {

    public void testAnnots() {
        Plan p1 = Plan.parse("@l[atomic,breakpoint] +e.");
        Plan p2 = Plan.parse("+e : c <- a.");
        assertTrue(p1.isAtomic());
        assertFalse(p2.isAtomic());
        assertTrue(p1.hasBreakpoint());
        assertFalse(p2.hasBreakpoint());

        Plan p3 = (Plan) p1.clone();
        assertTrue(p3.isAtomic());
        assertTrue(p3.hasBreakpoint());
    }
    
    public void testRelevant() throws JasonException, ParseException {
    	PlanLibrary pl = new PlanLibrary();
    	pl.add(Plan.parse("+p(0) <- .print(a)."));
    	pl.add(Plan.parse("+p(X) : X > 0 <- .print(a)."));
    	
    	pl.add(Plan.parse("+!p(0) <- .print(a)."));
    	pl.add(Plan.parse("+!p(X) : X > 0 <- .print(a)."));

    	pl.add(Plan.parse("+!X <- .print(a)."));
    	
    	List<Plan> pls = pl.getAllRelevant(Trigger.parseTrigger("+p(3)"));
    	assertEquals(2, pls.size());

    	pls = pl.getAllRelevant(Trigger.parseTrigger("+!p(3)"));
    	assertEquals(3, pls.size());	

    	pls = pl.getAllRelevant(Trigger.parseTrigger("+!bla"));
    	assertEquals(1, pls.size());	

    	pls = pl.getAllRelevant(Trigger.parseTrigger("+bla"));
    	assertEquals(0, pls.size());	
    }
    
    public void testParser() {
        Plan p = Plan.parse("+te : a & b <- a1; a2; .print(a); !g1; !!g2; ?test1; 10 > 3; +b1; -b2; -+b3.");
        p = (Plan)p.clone();
        Iterator<BodyLiteral> i = p.getBody().iterator();
        assertEquals( BodyLiteral.BodyType.action, ((BodyLiteral)i.next()).getType());
        assertEquals( BodyLiteral.BodyType.action, ((BodyLiteral)i.next()).getType());
        assertEquals( BodyLiteral.BodyType.internalAction, ((BodyLiteral)i.next()).getType());
        assertEquals( BodyLiteral.BodyType.achieve, ((BodyLiteral)i.next()).getType());
        assertEquals( BodyLiteral.BodyType.achieveNF, ((BodyLiteral)i.next()).getType());
        assertEquals( BodyLiteral.BodyType.test, ((BodyLiteral)i.next()).getType());
        assertEquals( BodyLiteral.BodyType.constraint, ((BodyLiteral)i.next()).getType());
        assertEquals( BodyLiteral.BodyType.addBel, ((BodyLiteral)i.next()).getType());
        assertEquals( BodyLiteral.BodyType.delBel, ((BodyLiteral)i.next()).getType());
        assertEquals( BodyLiteral.BodyType.delAddBel, ((BodyLiteral)i.next()).getType());
        assertFalse(i.hasNext());
    }

    public void testDelete() {
        Plan p = Plan.parse("+te : a & b <- !a1; ?a2; .print(a); !g1.");
        assertEquals(4, p.getBody().size());
        p.getBody().remove(0);
        assertEquals(3, p.getBody().size());
        assertEquals(BodyLiteral.BodyType.test, p.getBody().getType());
        p.getBody().remove(0); // 2
        p.getBody().remove(0); // 1
        assertEquals(1, p.getBody().size());
        p.getBody().remove(0); // 1
        assertTrue(p.getBody().isEmpty());
    }
    
    public void testUnifyBody() {
        Plan p1 = Plan.parse("+te : a & b <- !a1; ?a2; .print(a); !g1.");
        BodyLiteral bl = new BodyLiteral(BodyType.action, new VarTerm("A1"));
        bl.add(new BodyLiteral(BodyType.action, new VarTerm("A2")));
        bl.add(new BodyLiteral(BodyType.action, new VarTerm("A3")));
        assertEquals(p1.getBody().getArity(), bl.getArity());
        Unifier u = new Unifier();
        assertTrue(u.unifies(p1.getBody(), bl));
        assertEquals("a1", u.get("A1").toString());
        assertEquals("a2", u.get("A2").toString());
        assertEquals(".print(a); !g1", u.get("A3").toString());
    }
    
}
