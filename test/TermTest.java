package test;

import jason.D;
import jason.asSemantics.Unifier;
import jason.asSyntax.Literal;
import jason.asSyntax.Pred;
import jason.asSyntax.Term;
import jason.asSyntax.Trigger;
import junit.framework.TestCase;

/** JUnit test case for syntax package */
public class TermTest extends TestCase {

	public void testEquals() {
		Term t1, t2, t3;
		t1 = new Term("pos");
		t2 = new Term(t1);
		t3 = new Term(); t3.setFunctor("pos");
		assertTrue(t1.equals(t2));
		assertTrue(t1.equals(t3));
		
		t1.addTerm(new Term("a"));
		assertFalse(t1.equals(t2));
		
		t2.addTerm(new Term("a"));
		assertTrue(t1.equals(t2));

		Term targ1 = new Term("b");
		targ1.addTerm(new Term("1"));
		Term targ2 = new Term("b");
		targ2.addTerm(new Term("2"));

		t1.addTerm(targ1);
		assertFalse(t1.equals(t2));
		
		Term targ1a = new Term("b");
		targ1a.addTerm(new Term("1"));
		t3.addTerm(new Term("a"));
		t3.addTerm(targ1a);
		assertTrue(t1.equals(t3));
		
		// tests with variables
		t1.addTerm(new Term("c"));
		t3.addTerm(new Term("X"));
		assertFalse(t1.equals(t3));
		
		Literal l3 = new Literal(true, new Pred("pos"));
		l3.addAnnot(D.TPercept);
		Literal l4 = new Literal(true, new Pred("pos"));
		l4.addAnnot(D.TPercept);
		assertEquals(l3, l4);
		
		assertTrue(l3.equals(new Term("pos")));
		assertTrue(new Term("pos").equals(l3));
		//System.out.println(new Term("pos")+"="+l3+" --> "+new Term("pos").equals(l3));

		assertFalse(new Pred("pos").equals(l3));
		assertTrue(new Pred("pos").equalsAsTerm(l3));
		Pred panot = new Pred("pos");
		panot.addAnnot(new Term("bla"));
		assertTrue(l3.equalsAsTerm(panot));
	}

	public void testUnifies() {
		Unifier u = new Unifier();
		Term t1, t2, t3;
		
		t1 = new Term("pos");
		t2 = new Term(t1);
		t3 = new Term(t1);

		t1.addTerm(new Term("1"));
		t1.addTerm(new Term("2"));

		t2.addTerm(new Term("X"));
		t2.addTerm(new Term("X"));
		assertFalse(u.unifies(t1,t2));

		t3.addTerm(new Term("X"));
		t3.addTerm(new Term("Y"));
		assertTrue(u.unifies(t1,t3));
	}

	public void testAnnotsUnify1() {
		Unifier u = new Unifier();
		Pred p1, p2;
		
		p1 = new Pred("pos");
		p2 = new Pred("pos");

		p1.addTerm(new Term("1"));
		p2.addTerm(new Term("1"));
		
		p2.addAnnot(new Term("percept"));
		//System.out.println("p1="+p1+"; p2="+p2);
		assertTrue(u.unifies(p1, p2));
	}
	public static void main(String[] a) {
		new TermTest().testAnnotsUnify1();
	}

	public void testAnnotsUnify2() {
		Unifier u = new Unifier();
		Pred p1, p2;
		
		p1 = new Pred("pos");
		p2 = new Pred("pos");

		p1.addTerm(new Term("1"));
		p2.addTerm(new Term("1"));
		
		p1.addAnnot(new Term("X"));
		p2.addAnnot(new Term("ag1"));
		//System.out.println("p1="+p1+"; p2="+p2);
		assertTrue(u.unifies(p1, p2));
		//System.out.println("u="+u);
		
		p1.addAnnot(new Term("ag2"));
		p2.addAnnot(new Term("Y"));
		//System.out.println("p1="+p1+"; p2="+p2);
		u = new Unifier();
		assertTrue(u.unifies(p1, p2));
		//System.out.println("u="+u);
		
		p1.addAnnot(new Term("Z"));
		p2.addAnnot(new Term("ag3"));
		p2.addAnnot(new Term("ag4"));
		//System.out.println("p1="+p1+"; p2="+p2);
		u = new Unifier();
		assertTrue(u.unifies(p1, p2));
		//System.out.println("u="+u);

		p1.addAnnot(new Term("X1"));
		p1.addAnnot(new Term("X2"));
		p1.addAnnot(new Term("X3"));
		//System.out.println("p1="+p1+"; p2="+p2);
		u = new Unifier();
		assertFalse(u.unifies(p1, p2));
		//System.out.println("u="+u);

		p1.clearAnnot();
		p1.addAnnot(new Term("ag2"));
		p2.clearAnnot();
		p2.addAnnot(new Term("ag1"));
		p2.addAnnot(new Term("ag2"));
		p2.addAnnot(new Term("ag3"));
		//System.out.println("p1="+p1+"; p2="+p2);
		u = new Unifier();
		assertTrue(u.unifies(p1, p2));
		//System.out.println("u="+u);
	}
	
	public void testTrigger() {
		Pred p1 = new Pred("pos");

		p1.addTerm(new Term("X"));
		p1.addTerm(new Term("Y"));

		Trigger g = new Trigger(D.TEAdd,D.TEAchvG,new Literal(D.LDefPos, p1));
		//System.out.println("g="+g);
		
	}
	
	public void testTriggetAnnot() {
		Literal content = Literal.parseLiteral("~alliance");
		content.addSource("ag1");
		Literal received = new Literal(D.LPos, new Pred("received"));
		received.addTerm(new Term("ag1"));
		received.addTerm(new Term("tell"));
		received.addTerm(content);
		received.addTerm(new Term("id1"));
		
		Trigger t1 = new Trigger(D.TEAdd, D.TEBel, received);

		Literal received2 = new Literal(D.LPos, new Pred("received"));
		received2.addTerm(new Term("S"));
		received2.addTerm(new Term("tell"));
		received2.addTerm(new Literal(D.LPos, new Pred("C")));
		received2.addTerm(new Term("M"));
		
		Trigger t2 = new Trigger(D.TEAdd, D.TEBel, received2);
		
		//System.out.println("t1 = "+t1);
		//System.out.println("t2 = "+t2);
		Unifier u = new Unifier();
		assertTrue(u.unifies(t1,t2));
		//System.out.println(u);
		u.apply(t2);
		//System.out.println("t2 with apply = "+t2);
		
		assertEquals(t1.toString(), t2.toString());
	}
	
	public void testLiteralUnify() {
		Literal content = Literal.parseLiteral("~alliance");
		content.addSource("ag1");
		Literal l1 = new Literal(D.LPos, new Pred("received"));
		l1.addTerm(new Term("ag1"));
		l1.addTerm(new Term("tell"));
		l1.addTerm(content);
		l1.addTerm(new Term("id1"));

		
		Literal l2 = Literal.parseLiteral("received(S,tell,C,M)");
		Unifier u = new Unifier();
		assertTrue(u.unifies(l1,l2));
		//System.out.println(u);
		u.apply(l2);
		//System.out.println("l2 with apply = "+l2);
		assertEquals(l1.toString(), l2.toString());
		
	}
}
