package test;

import jason.asSyntax.StringTerm;
import jason.asSyntax.StringTermImpl;
import junit.framework.TestCase;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

/** JUnit test case for syntax package */
public class StringTermTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
		Logger.getRootLogger().addAppender(new ConsoleAppender(new PatternLayout("[%c{1}] %m%n")));
    	Logger.getRootLogger().setLevel(Level.DEBUG);
	}
	
	public void testParsing() {
		StringTerm s = StringTermImpl.parseString("\"a\"");
		assertEquals(s, new StringTermImpl("\"a\""));
	}
}
