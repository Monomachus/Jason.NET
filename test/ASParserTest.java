package test;

import jIDE.JasonID;
import jason.asSemantics.Agent;
import junit.framework.TestCase;

/** JUnit test case for syntax package */
public class ASParserTest extends TestCase {

	public void testKQML() {
		Agent ag = new Agent();
		assertTrue(ag.parseAS(JasonID.class.getResource("/asl/kqmlPlans.asl")));
		assertTrue(ag.parseAS("examples/Auction/ag1.asl"));
		assertTrue(ag.parseAS("examples/Auction/ag2.asl"));
		assertTrue(ag.parseAS("examples/Auction/ag3.asl"));
		//System.out.println("code="+ag.getPS());
		
		ag = new Agent();
		assertTrue(ag.parseAS("examples/Simple/as/ag0.asl"));
		assertTrue(ag.parseAS("examples/Simple/as/ag1.asl"));		
	}
}
