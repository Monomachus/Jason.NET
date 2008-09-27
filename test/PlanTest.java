package test;

import jason.JasonException;
import jason.asSemantics.Unifier;
import jason.asSyntax.LiteralImpl;
import jason.asSyntax.Plan;
import jason.asSyntax.PlanBody;
import jason.asSyntax.PlanBodyImpl;
import jason.asSyntax.PlanLibrary;
import jason.asSyntax.Trigger;
import jason.asSyntax.VarTerm;
import jason.asSyntax.PlanBody.BodyType;
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
    	
    	List<Plan> pls = pl.getCandidatePlans(Trigger.parseTrigger("+p(3)"));
    	assertEquals(2, pls.size());

    	pls = pl.getCandidatePlans(Trigger.parseTrigger("+!p(3)"));
    	assertEquals(3, pls.size());	

    	pls = pl.getCandidatePlans(Trigger.parseTrigger("+!bla"));
    	assertEquals(1, pls.size());	

    	pls = pl.getCandidatePlans(Trigger.parseTrigger("+bla"));
    	assertEquals(0, pls.size());	
    }
    
    public void testParser1() {
        Plan p = Plan.parse("+te : a & b <- a1; a2; .print(a); !g1; !!g2; ?test1; 10 > 3; +b1; -b2; -+b3.");
        p = (Plan)p.clone();
        Iterator<PlanBody> i = ((PlanBodyImpl)p.getBody()).iterator();
        assertEquals( PlanBody.BodyType.action, ((PlanBody)i.next()).getBodyType());
        assertEquals( PlanBody.BodyType.action, ((PlanBody)i.next()).getBodyType());
        assertEquals( PlanBody.BodyType.internalAction, ((PlanBody)i.next()).getBodyType());
        assertEquals( PlanBody.BodyType.achieve, ((PlanBody)i.next()).getBodyType());
        assertEquals( PlanBody.BodyType.achieveNF, ((PlanBody)i.next()).getBodyType());
        assertEquals( PlanBody.BodyType.test, ((PlanBody)i.next()).getBodyType());
        assertEquals( PlanBody.BodyType.constraint, ((PlanBody)i.next()).getBodyType());
        assertEquals( PlanBody.BodyType.addBel, ((PlanBody)i.next()).getBodyType());
        assertEquals( PlanBody.BodyType.delBel, ((PlanBody)i.next()).getBodyType());
        assertTrue(i.hasNext());
        assertEquals( PlanBody.BodyType.delAddBel, ((PlanBody)i.next()).getBodyType());
        assertFalse(i.hasNext());
    }
    
    public void testDelete() {
        Plan p = Plan.parse("+te : a & b <- !a1; ?a2; .print(a); !g1.");
        assertEquals(4, p.getBody().getPlanSize());
        p.getBody().removeBody(0);
        assertEquals(3, p.getBody().getPlanSize());
        assertEquals(PlanBody.BodyType.test, p.getBody().getBodyType());
        p.getBody().removeBody(0); // 2
        p.getBody().removeBody(0); // 1
        assertEquals(1, p.getBody().getPlanSize());
        p.getBody().removeBody(0); // 1
        assertTrue(p.getBody().isEmptyBody());
    }
    
    public void testEqualsBodyLiteral() {
        PlanBody bl = new PlanBodyImpl(BodyType.achieve, new LiteralImpl("g1"));
        VarTerm v = new VarTerm("X");
        Unifier u = new Unifier();
        // X = !g1
        assertTrue(u.unifies(v, bl));
        v.apply(u);
        assertEquals(BodyType.achieve, v.getBodyType());
        assertEquals(bl.getBodyTerm(),v.getBodyTerm());
        Plan p = Plan.parse("+te : a & b <- !g1.");
        assertEquals(p.getBody(),v);
    }
    
    public void testUnifyBody() {
        Plan p1 = Plan.parse("+te : a & b <- !a1; ?a2; .print(a); !g1.");
        PlanBody bl = new PlanBodyImpl(BodyType.action, new VarTerm("A1"));
        bl.add(new PlanBodyImpl(BodyType.action, new VarTerm("A2")));
        bl.add(new PlanBodyImpl(BodyType.action, new VarTerm("A3")));
        //assertEquals(p1.getBody().getArity(), bl.getArity());
        Unifier u = new Unifier();
        assertTrue(u.unifies(p1.getBody(), bl));
        assertEquals("a1", u.get("A1").toString());
        assertEquals("a2", u.get("A2").toString());
        assertEquals(".print(a); !g1", u.get("A3").toString());  
    }
    
}
