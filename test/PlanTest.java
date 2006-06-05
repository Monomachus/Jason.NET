package test;

import jason.asSyntax.Plan;
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
}
