package test;

import jason.JasonException;
import jason.asSemantics.Agent;
import jason.asSyntax.BodyLiteral;
import jason.asSyntax.Plan;
import junit.framework.TestCase;

/** JUnit test case for syntax package */
public class ASParserTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testKQML() {

        Agent ag = new Agent();
        ag.setLogger(null);

        assertTrue(ag.parseAS(JasonException.class.getResource("/asl/kqmlPlans.asl")));
        assertTrue(ag.parseAS("examples/Auction/ag1.asl"));
        Plan p = ag.getPS().get("l__0");
        assertEquals(p.getBody().size(),1);
        assertEquals(p.getBody().get(0).getType(), BodyLiteral.BodyType.internalAction);
        assertTrue(ag.parseAS("examples/Auction/ag2.asl"));
        assertTrue(ag.parseAS("examples/Auction/ag3.asl"));

        ag = new Agent();
        ag.setLogger(null);
        assertTrue(ag.parseAS("examples/Test/as/ag0.asl"));
        assertTrue(ag.parseAS("examples/Test/as/ag1.asl"));
    }
}
