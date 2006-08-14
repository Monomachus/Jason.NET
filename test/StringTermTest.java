package test;

import jason.asSyntax.StringTerm;
import jason.asSyntax.StringTermImpl;
import junit.framework.TestCase;

/** JUnit test case for syntax package */
public class StringTermTest extends TestCase {

	public void testParsing() {
		StringTerm s = StringTermImpl.parseString("\"a\"");
        assertEquals(s.getString(), "a");
        assertEquals(s.toString(), "\"a\"");
		assertEquals(s, new StringTermImpl("a"));
        
		//s = StringTermImpl.parseString("\"a(\\\\\"k\\\\\")\"");
        //System.out.println(s);
        //assertEquals(s.getString(), "a(\"k\")");
	}
}
