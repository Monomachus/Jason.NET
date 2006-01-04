package test;

import jason.asSyntax.StringTerm;
import jason.asSyntax.StringTermImpl;
import junit.framework.TestCase;

/** JUnit test case for syntax package */
public class StringTermTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
		//Logger.getRootLogger().addAppender(new ConsoleAppender(new PatternLayout("[%c{1}] %m%n")));
    	//Logger.getRootLogger().setLevel(Level.DEBUG);
	}
	
	public void testParsing() {
		StringTerm s = StringTermImpl.parseString("\"a\"");
		assertEquals(s, new StringTermImpl("\"a\""));
	}
}
