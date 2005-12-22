package test;

import jason.asSemantics.Unifier;
import jason.asSyntax.ListTerm;
import jason.asSyntax.ListTermImpl;
import jason.asSyntax.Term;
import jason.asSyntax.VarTerm;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

/** JUnit test case for syntax package */
public class ListTermTest extends TestCase {

	ListTerm l1, l2, l3,l4,l5;
	
	protected void setUp() throws Exception {
		super.setUp();

		Logger.getRootLogger().addAppender(new ConsoleAppender(new PatternLayout("[%c{1}] %m%n")));
    	Logger.getRootLogger().setLevel(Level.DEBUG);

		l1 = ListTermImpl.parseList("[a,b,c]");
		l2 = ListTermImpl.parseList("[a(1,2),b(r,t)|T]");
		l3 = ListTermImpl.parseList("[A|T]");
		l4 = ListTermImpl.parseList("[X,b,T]");
		l5 = ListTermImpl.parseList("[[b,c]]");
		//System.out.println("l1="+l1+"\nl2="+l2+"\nl3="+l3+"\nl4="+l4);
		//System.out.println("l5="+l5);
	}

	public void testSize() {
		assertEquals(l1.size(), 3);
		assertEquals(l2.size(), 2);
		assertEquals(l3.size(), 1);
		assertEquals(l4.size(), 3);
		assertEquals(l5.size(), 1);
		
		ListTerm l = new ListTermImpl();
		l.add(new Term("a"));
		l.add(new Term("a"));
		l.add(new Term("a"));
		assertEquals(l.size(), 3);		
	}
	
	public void testUnify() {
		assertTrue( new Unifier().unifies((Term)l1,(Term)ListTermImpl.parseList("[a,b,c]")));
	    assertTrue( new Unifier().unifies((Term)l1,(Term)ListTermImpl.parseList("[A,B,C]")));
		assertFalse( new Unifier().unifies((Term)l1,(Term)ListTermImpl.parseList("[a,b]")));
		assertFalse( new Unifier().unifies((Term)l1,(Term)ListTermImpl.parseList("[a,b,d]")));

		Unifier u2 = new Unifier();
	    assertTrue(u2.unifies((Term)l1,new VarTerm("X")));
		//System.out.println("u2="+u2);

		Unifier u3 = new Unifier();
	    assertTrue(	u3.unifies((Term)l1,(Term)l3));
		assertEquals( ((ListTerm)u3.get("T")).toString(), "[b,c]");
		//System.out.println("u3="+u3);

		Unifier u4 = new Unifier();
	    assertTrue(u4.unifies((Term)l1,(Term)l4));
		//System.out.println("u4="+u4);

		Unifier u5 = new Unifier();
		// [a,b,c] = [X|[b,c]]
		ListTerm lt5 = ListTermImpl.parseList("[X|[b,c]]");
		//System.out.println("lt5="+lt5);
	    assertTrue(u5.unifies((Term)l1,(Term)lt5));
		//System.out.println("u5="+u5);
		
	}
	
	public void testAddRemove() {
		l1.add(new Term("d"));
		l1.add(0, new Term("a1"));
		l1.add(1, new Term("a2"));
		assertEquals(new Term("a2"), l1.get(1));
		assertEquals(l1.size(), 6);
		
		List lal = new ArrayList();
		lal.add(new Term("b1"));
		lal.add(new Term("b2"));
		l1.addAll(4, lal);
		assertEquals(l1.size(), 8);
		
		//System.out.println(l1);
		assertEquals(new Term("a1"), l1.remove(0));
		assertEquals(new Term("b"), l1.remove(2));
		assertTrue(l1.remove(new Term("b1")));
		assertTrue(l1.remove(new Term("d")));
		assertTrue(l1.remove(new Term("a2")));
		assertEquals(l1.size(), 3);
		
		//System.out.println(l1);
	}
	
	public void testClone() {
		assertEquals(l1.size(), ((ListTerm)l1.clone()).size());
		assertEquals(l1, l1.clone());
	}

	public void testEquals() {
		assertTrue(l1.equals(l1));
		assertTrue(l1.equals(ListTermImpl.parseList("[a,b,c]")));

		assertFalse(l1.equals(l2));
		assertFalse(l1.equals(ListTermImpl.parseList("[a,b,d]")));
	}

}
