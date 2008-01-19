package test;

import jason.JasonException;
import jason.asSyntax.Plan;
import jason.asSyntax.PlanLibrary;
import jason.asSyntax.Trigger;

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
    
    public void testRelevant() throws JasonException {
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
}
