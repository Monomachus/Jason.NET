package test;

import jason.JasonException;
import jason.asSemantics.Agent;
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
		assertTrue(ag.parseAS("examples/Auction/ag2.asl"));
		assertTrue(ag.parseAS("examples/Auction/ag3.asl"));
		//System.out.println("code="+ag.getPS());
		
		ag = new Agent();
		ag.setLogger(null);
		assertTrue(ag.parseAS("examples/Test/as/ag0.asl"));
		assertTrue(ag.parseAS("examples/Test/as/ag1.asl"));		
	}
}
