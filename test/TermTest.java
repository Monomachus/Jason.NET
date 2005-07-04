package test;

import jason.asSemantics.Unifier;
import jason.asSyntax.BeliefBase;
import jason.asSyntax.DefaultLiteral;
import jason.asSyntax.ListTerm;
import jason.asSyntax.Literal;
import jason.asSyntax.Pred;
import jason.asSyntax.Term;
import jason.asSyntax.Trigger;
import jason.asSyntax.VarTerm;

import java.util.Iterator;

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
		t3.addTerm(new VarTerm("X"));
		assertFalse(t1.equals(t3));
		
		Literal l3 = new Literal(true, new Pred("pos"));
		l3.addAnnot(BeliefBase.TPercept);
		Literal l4 = new Literal(true, new Pred("pos"));
		l4.addAnnot(BeliefBase.TPercept);
		assertEquals(l3, l4);
		
		assertTrue(l3.equals(new Term("pos")));
		assertTrue(new Term("pos").equals(l3));
		//System.out.println(new Term("pos")+"="+l3+" --> "+new Term("pos").equals(l3));

		assertFalse(new Pred("pos").equals(l3));
		assertTrue(new Pred("pos").equalsAsTerm(l3));
		Pred panot = new Pred("pos");
		panot.addAnnot(new Term("bla"));
		assertTrue(l3.equalsAsTerm(panot));
		
		// basic VarTerm test
		assertTrue(new VarTerm("X").equals(new VarTerm("X")));
		assertFalse(new VarTerm("X").equals(new VarTerm("Y")));
		assertFalse(new VarTerm("X").equals(new Term("X")));
		
		VarTerm x1 = new VarTerm("X1");
		x1.setValue(new Term("a"));
		assertFalse(x1.equals(new VarTerm("X1")));
		
		VarTerm x2 = new VarTerm("X2");
		x2.setValue(new Term("a"));
		assertTrue(x1.equals(x2));
	}

	public void testUnifies() {
		
		assertTrue(new Unifier().unifies(new Term("a"), new Term("a")));
		assertTrue(new Unifier().unifies(new Term("a"), new VarTerm("X")));
		
		Unifier u = new Unifier();
		VarTerm b = new VarTerm("B");
		VarTerm x = new VarTerm("X");
		assertTrue(u.unifies(b, x));
		assertTrue(u.unifies(new Term("a"), x));
		//System.out.println("u="+u);
		assertEquals(u.get("B").toString(), "a");
		assertEquals(u.get("X").toString(), "a");
		u.apply(b);
		//System.out.println("x="+x);
		//System.out.println("b="+b);
		assertEquals(b.toString(), "a");
		assertEquals(x.toString(), "X");
		
		u = new Unifier();
		Term t1, t2, t3;
		
		t1 = new Term("pos");
		t2 = new Term(t1);
		t3 = new Term(t1);

		t1.addTerm(new Term("1"));
		t1.addTerm(new Term("2"));

		t2.addTerm(new VarTerm("X"));
		t2.addTerm(new VarTerm("X"));
		assertFalse(u.unifies(t1,t2));

		u = new Unifier();
		t3.addTerm(new VarTerm("X"));
		t3.addTerm(new VarTerm("Y"));
		//System.out.println(t1+"="+t3);
		assertTrue(	u.unifies(t1,t3));
		//System.out.println("u="+u);
	
		// Test var unified with var
		u = new Unifier();
		VarTerm z1 = new VarTerm("Z1");
		VarTerm z2 = new VarTerm("Z2");
		VarTerm z3 = new VarTerm("Z3");
		VarTerm z4 = new VarTerm("Z4");
		// Z1 = Z2 = Z3 = Z4
		assertTrue(u.unifies(z1,z2));
		assertTrue(u.unifies(z2,z3));
		assertTrue(u.unifies(z2,z4));
		
		//System.out.println("u="+u);
		assertEquals(u.get("Z1"), null);
		assertEquals(u.get("Z2"), null);
		
		assertTrue(z1.isVar()); // z1 is still a var
		assertTrue(z2.isVar()); // z2 is still a var
		
		assertTrue(u.unifies(z2,new Term("a")));
		//System.out.println("u="+u);
		assertEquals(u.get("Z1").toString(), "a");
		assertEquals(u.get("Z2").toString(), "a");
		assertEquals(u.get("Z3").toString(), "a");
		assertEquals(u.get("Z4").toString(), "a");
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
		
		p1.addAnnot(new VarTerm("X"));
		p2.addAnnot(new Term("ag1"));
		//System.out.println("p1="+p1+"; p2="+p2);
		assertTrue(u.unifies(p1, p2));
		//System.out.println("u="+u);
		
		p1.addAnnot(new Term("ag2"));
		p2.addAnnot(new VarTerm("Y"));
		//System.out.println("p1="+p1+"; p2="+p2);
		u = new Unifier();
		assertTrue(u.unifies(p1, p2));
		//System.out.println("u="+u);
		
		p1.addAnnot(new VarTerm("Z"));
		p2.addAnnot(new Term("ag3"));
		p2.addAnnot(new Term("ag4"));
		//System.out.println("p1="+p1+"; p2="+p2);
		u = new Unifier();
		assertTrue(u.unifies(p1, p2));
		//System.out.println("u="+u);

		p1.addAnnot(new VarTerm("X1"));
		p1.addAnnot(new VarTerm("X2"));
		p1.addAnnot(new VarTerm("X3"));
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

		p1.addTerm(new VarTerm("X"));
		p1.addTerm(new VarTerm("Y"));

		Trigger g = new Trigger(Trigger.TEAdd,Trigger.TEAchvG,new Literal(DefaultLiteral.LDefPos, p1));
		//System.out.println("g="+g);
		
	}
	
	public void testTriggetAnnot() {
		Literal content = Literal.parseLiteral("~alliance");
		content.addSource("ag1");
		Literal received = new Literal(Literal.LPos, new Pred("received"));
		received.addTerm(new Term("ag1"));
		received.addTerm(new Term("tell"));
		received.addTerm(content);
		received.addTerm(new Term("id1"));
		
		Trigger t1 = new Trigger(Trigger.TEAdd, Trigger.TEBel, received);

		Literal received2 = new Literal(Literal.LPos, new Pred("received"));
		received2.addTerm(new VarTerm("S"));
		received2.addTerm(new Term("tell"));
		received2.addTerm(new VarTerm("C"));
		received2.addTerm(new VarTerm("M"));
		
		Trigger t2 = new Trigger(Trigger.TEAdd, Trigger.TEBel, received2);
		
		//System.out.println("t1 = "+t1);
		//System.out.println("t2 = "+t2);
		Unifier u = new Unifier();
		assertTrue(u.unifies(t1,t2));
		//System.out.println(u);
		u.apply(t2);
		//System.out.println("t2 with apply = "+t2);
		
		assertEquals(t1.toString(), t2.toString());
		
		Trigger t3 = Trigger.parseTrigger("+!bid_normally(1)");
		Trigger t4 = Trigger.parseTrigger("+!bid_normally(N)");
		u = new Unifier();
		u.unifies(t3,t4);
		//System.out.println("u="+u);
		assertEquals(u.get("N").toString(), "1");
		
	}
	
	public void testLiteralUnify() {
		Literal content = Literal.parseLiteral("~alliance");
		content.addSource("ag1");
		Literal l1 = new Literal(Literal.LPos, new Pred("received"));
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
	
	public void testVarTerm() {
		VarTerm x1 = new VarTerm("X1");
		VarTerm x2 = new VarTerm("X2");
		VarTerm x3 = new VarTerm("X3");
		
		x1.setValue(x2);
		x2.setValue(x3);
		
		x3.setValue(new Term("a"));
		// x1's value is x3's value (a)
		assertEquals(x1.getValue().toString(), "a");
		
		
		// test x1 -> x2 -> x3 -> x1!
		assertFalse(x3.setValue(x1));
		
		VarTerm v1 = new VarTerm("L");
		ListTerm lt = ListTerm.parseList("[a,B,a(B)]");
		Unifier u = new Unifier();
		u.unifies(new VarTerm("B"), new Term("oi"));
		u.unifies(v1, lt);
		u.apply(v1);
		lt = (ListTerm)v1.getValue();
		Iterator i = lt.termsIterator();
		i.next();i.next();
		Term third = (Term)i.next();
		assertTrue(third.equals(Term.parse("a(oi)")));
		
	}
}
