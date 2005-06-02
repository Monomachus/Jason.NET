package test;

import jason.asSemantics.Unifier;
import jason.asSyntax.ListTerm;
import jason.asSyntax.Term;
import junit.framework.TestCase;

/** JUnit test case for syntax package */
public class ListTermTest extends TestCase {

	ListTerm l1, l2, l3,l4,l5;
	
	protected void setUp() throws Exception {
		super.setUp();

		l1 = ListTerm.parseList("[a,b,c]");
		l2 = ListTerm.parseList("[a(1,2),b(r,t)|T]");
		l3 = ListTerm.parseList("[A|T]");
		l4 = ListTerm.parseList("[X,b,T]");
		l5 = ListTerm.parseList("[[b,c]]");
		//System.out.println(l1+"\n"+l2+"\n"+l3+"\n"+l4);
		//System.out.println("l5="+l5);
	}

	public void testSize() {
		assertEquals(l1.size(), 3);
		assertEquals(l2.size(), 2);
		assertEquals(l3.size(), 1);
		assertEquals(l4.size(), 3);
		assertEquals(l5.size(), 1);
		
		ListTerm l = new ListTerm();
		l.add(new Term("a"));
		l.add(new Term("a"));
		l.add(new Term("a"));
		assertEquals(l.size(), 3);		
	}
	
	public void testUnify() {
		assertTrue( new Unifier().unifies(l1,ListTerm.parseList("[a,b,c]")));
	    assertTrue( new Unifier().unifies(l1,ListTerm.parseList("[A,B,C]")));
		assertFalse( new Unifier().unifies(l1,ListTerm.parseList("[a,b]")));
		assertFalse( new Unifier().unifies(l1,ListTerm.parseList("[a,b,d]")));

		Unifier u2 = new Unifier();
	    assertTrue(u2.unifies(l1,new Term("X")));
		//System.out.println("u2="+u2);

		Unifier u3 = new Unifier();
	    assertTrue(	u3.unifies(l1,l3));
		assertEquals( ((ListTerm)u3.get("T")).toString(), "[b,c]");
		//System.out.println("u3="+u3);

		Unifier u4 = new Unifier();
	    assertTrue(u4.unifies(l1,l4));
		//System.out.println("u4="+u4);

		Unifier u5 = new Unifier();
		// [a,b,c] = [X|[b,c]]
		ListTerm lt5 = ListTerm.parseList("[X|[b,c]]");
		//System.out.println("lt5="+lt5);
	    assertTrue(u5.unifies(l1,lt5));
		//System.out.println("u5="+u5);
		
	}
	
	
	public void testClone() {
		assertEquals(l1.size(), ((ListTerm)l1.clone()).size());
		assertEquals(l1, l1.clone());
	}

	public void testEquals() {
		assertTrue(l1.equals(l1));
		assertTrue(l1.equals(ListTerm.parseList("[a,b,c]")));

		assertFalse(l1.equals(l2));
		assertFalse(l1.equals(ListTerm.parseList("[a,b,d]")));
	}

}
