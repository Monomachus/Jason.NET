package test;

import jIDE.JasonID;
import jason.asSemantics.Agent;
import junit.framework.TestCase;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

/** JUnit test case for syntax package */
public class ASParserTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
		Logger.getRootLogger().addAppender(new ConsoleAppender(new PatternLayout("[%c{1}] %m%n")));
    	Logger.getRootLogger().setLevel(Level.INFO);
	}

	public void testKQML() {

		Agent ag = new Agent();
		ag.setLogger(null);
		
		assertTrue(ag.parseAS(JasonID.class.getResource("/asl/kqmlPlans.asl")));
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
