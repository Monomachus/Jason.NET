package test;

import jason.asSyntax.StringTerm;
import junit.framework.TestCase;

/** JUnit test case for syntax package */
public class StringTermTest extends TestCase {

	public void testParsing() {
		StringTerm s = StringTerm.parseString("\"a\"");
		assertEquals(s, new StringTerm("\"a\""));
	}
}
