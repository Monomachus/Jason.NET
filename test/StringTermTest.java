package test;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import jason.asSyntax.StringTerm;
import junit.framework.TestCase;

/** JUnit test case for syntax package */
public class StringTermTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
		Logger.getRootLogger().addAppender(new ConsoleAppender(new PatternLayout("[%c{1}] %m%n")));
    	Logger.getRootLogger().setLevel(Level.DEBUG);
	}
	
	public void testParsing() {
		StringTerm s = StringTerm.parseString("\"a\"");
		assertEquals(s, new StringTerm("\"a\""));
	}
}
