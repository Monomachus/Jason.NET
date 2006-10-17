package test;

import jason.asSemantics.Unifier;
import jason.asSyntax.ListTerm;
import jason.asSyntax.ListTermImpl;
import jason.asSyntax.Literal;
import jason.asSyntax.NumberTermImpl;
import jason.asSyntax.Pred;
import jason.asSyntax.Term;
import jason.asSyntax.TermImpl;
import jason.asSyntax.Trigger;
import jason.asSyntax.VarTerm;
import jason.bb.BeliefBase;

import java.util.Collections;

import junit.framework.TestCase;

/** JUnit test case for syntax package */
public class TermTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testEquals() {
		TermImpl t1, t2, t3;
		t1 = new TermImpl("pos");
		t2 = new TermImpl((TermImpl)t1);
		t3 = new TermImpl(); t3.setFunctor("pos");
		assertTrue(t1.equals(t2));
		assertTrue(t1.equals(t3));
		
		t1.addTerm(new TermImpl("a"));
		assertFalse(t1.equals(t2));
		
		t2.addTerm(new TermImpl("a"));
		assertTrue(t1.equals(t2));
        assertTrue(t2.equals(t1));
        assertEquals(t1.hashCode(),t2.hashCode());

		Term targ1 = new TermImpl("b");
		targ1.addTerm(new TermImpl("1"));
		Term targ2 = new TermImpl("b");
		targ2.addTerm(new TermImpl("2"));

		t1.addTerm(targ1);
		assertFalse(t1.equals(t2));
		
		Term targ1a = new TermImpl("b");
		targ1a.addTerm(new TermImpl("1"));
		t3.addTerm(new TermImpl("a"));
		t3.addTerm(targ1a);
		assertTrue(t1.equals(t3));
		
		// tests with variables
		t1.addTerm(new TermImpl("c"));
		t3.addTerm(new VarTerm("X"));
		assertFalse(t1.equals(t3));
		
		Literal l3 = new Literal(true, new Pred("pos"));
		l3.addAnnot(BeliefBase.TPercept);
		Literal l4 = new Literal(true, new Pred("pos"));
		l4.addAnnot(BeliefBase.TPercept);
		assertEquals(l3, l4);
		
        Term tpos = new TermImpl("pos");
		assertTrue(l3.equals(tpos));
		assertTrue(new TermImpl("pos").equals(l3));
		//System.out.println(new Term("pos")+"="+l3+" --> "+new Term("pos").equals(l3));

		assertFalse(new Pred("pos").equals(l3));
		assertTrue(new Pred("pos").equalsAsTerm(l3));
		Pred panot = new Pred("pos");
		panot.addAnnot(new TermImpl("bla"));
		assertTrue(l3.equalsAsTerm(panot));
		
		// basic VarTerm test
		assertTrue(new VarTerm("X").equals(new VarTerm("X")));
		assertFalse(new VarTerm("X").equals(new VarTerm("Y")));
		assertFalse(new VarTerm("X").equals(new TermImpl("X")));
		
		VarTerm x1 = new VarTerm("X1");
		x1.setValue(new TermImpl("a"));
		assertFalse(x1.equals(new VarTerm("X1")));
		
		VarTerm x2 = new VarTerm("X2");
		x2.setValue(new TermImpl("a"));
		assertTrue(x1.equals(x2));
        assertTrue(x2.equals(x1));
        assertEquals(x1.hashCode(), x2.hashCode());
        
        Term ta = new TermImpl("a");
        assertTrue(x1.equals(ta));
        assertTrue(ta.equals(x1));
        assertEquals(x1.hashCode(), ta.hashCode());
	}

	public void testUnifies() {
		assertTrue(new Unifier().unifies(new TermImpl("a"), new TermImpl("a")));
		assertTrue(new Unifier().unifies(new TermImpl("a"), new VarTerm("X")));
		
		Unifier u = new Unifier();
		VarTerm b = new VarTerm("B");
		VarTerm x = new VarTerm("X");
		assertTrue(u.unifies(b, x));
		assertTrue(u.unifies(new TermImpl("a"), x));
		//System.out.println("u="+u);
		assertEquals(u.get(b).toString(), "a");
		assertEquals(u.get(x).toString(), "a");
		u.apply(b);
		//System.out.println("x="+x);
		//System.out.println("b="+b);
		assertEquals(b.toString(), "a");
		assertEquals(x.toString(), "X");
		
		u = new Unifier();
		TermImpl t1, t2, t3;
		
		t1 = new TermImpl("pos");
		t2 = new TermImpl(t1);
		t3 = new TermImpl(t1);

		t1.addTerm(new TermImpl("1"));
		t1.addTerm(new TermImpl("2"));

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
		
		assertTrue(z1.isVar()); // z1 is still a var
		assertTrue(z2.isVar()); // z2 is still a var
		
		assertTrue(u.unifies(z2,new TermImpl("a")));
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

		p1.addTerm(new TermImpl("1"));
		p2.addTerm(new TermImpl("1"));
		
		p2.addAnnot(new TermImpl("percept"));
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

		p1.addTerm(new TermImpl("1"));
		p2.addTerm(new TermImpl("1"));
		
		p1.addAnnot(new VarTerm("X"));
		p2.addAnnot(new TermImpl("ag1"));
		//System.out.println("p1="+p1+"; p2="+p2);
		assertTrue(u.unifies(p1, p2));
		//System.out.println("u="+u);
		
		p1.addAnnot(new TermImpl("ag2"));
		p2.addAnnot(new VarTerm("Y"));
		//System.out.println("p1="+p1+"; p2="+p2);
		u = new Unifier();
		assertTrue(u.unifies(p1, p2));
		//System.out.println("u="+u);
		
		p1.addAnnot(new VarTerm("Z"));
		p2.addAnnot(new TermImpl("ag3"));
		p2.addAnnot(new TermImpl("ag4"));
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

		p1.clearAnnots();
		p1.addAnnot(new TermImpl("ag2"));
		p2.clearAnnots();
		p2.addAnnot(new TermImpl("ag1"));
		p2.addAnnot(new TermImpl("ag2"));
		p2.addAnnot(new TermImpl("ag3"));
		//System.out.println("p1="+p1+"; p2="+p2);
		u = new Unifier();
		assertTrue(u.unifies(p1, p2));
		//System.out.println("u="+u);
	}
    
    public void testAnnotsUnify3() {
        //Pred p1 = Pred.parsePred("p1[a,X")
    }
	
	public void testTrigger() {
		Pred p1 = new Pred("pos");

		p1.addTerm(new VarTerm("X"));
		p1.addTerm(new VarTerm("Y"));
	}
	
	public void testTriggetAnnot() {
		Literal content = Literal.parseLiteral("~alliance");
		content.addSource(new TermImpl("ag1"));
		Literal received = new Literal(Literal.LPos, new Pred("received"));
		received.addTerm(new TermImpl("ag1"));
		received.addTerm(new TermImpl("tell"));
		received.addTerm(content);
		received.addTerm(new TermImpl("id1"));
		
		Trigger t1 = new Trigger(Trigger.TEAdd, Trigger.TEBel, received);

		Literal received2 = new Literal(Literal.LPos, new Pred("received"));
		received2.addTerm(new VarTerm("S"));
		received2.addTerm(new TermImpl("tell"));
		received2.addTerm(new VarTerm("C"));
		received2.addTerm(new VarTerm("M"));
		
		Trigger t2 = new Trigger(Trigger.TEAdd, Trigger.TEBel, received2);
		
		//System.out.println("t1 = "+t1);
		//System.out.println("t2 = "+t2);
		Unifier u = new Unifier();
		assertTrue(u.unifies(t1,t2));
		//System.out.println(u);
		u.apply(t2.getLiteral());
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
		content.addSource(new TermImpl("ag1"));
		Literal l1 = new Literal(Literal.LPos, new Pred("received"));
		l1.addTerm(new TermImpl("ag1"));
		l1.addTerm(new TermImpl("tell"));
		l1.addTerm(content);
		l1.addTerm(new TermImpl("id1"));

		
		Literal l2 = Literal.parseLiteral("received(S,tell,C,M)");
		Unifier u = new Unifier();
		assertTrue(u.unifies(l1,l2));
		//System.out.println(u);
		u.apply(l2);
		//System.out.println("l2 with apply = "+l2);
		assertEquals(l1.toString(), l2.toString());
        
        assertFalse(new Unifier().unifies(Literal.parseLiteral("c(x)"), Literal.parseLiteral("c(20)")));
        assertTrue(new Unifier().unifies(Literal.parseLiteral("c(20)"), Literal.parseLiteral("c(20)")));
        assertTrue(new Unifier().unifies(Literal.parseLiteral("c(X)"), Literal.parseLiteral("c(20)")));
		
	}
	
	public void testSubsetAnnot() {
		Pred p1 = Pred.parsePred("p1(t1,t2)[a1,a(2,3),a(3)]");
		Pred p2 = Pred.parsePred("p2(t1,t2)[a(2,3),a(3)]");
		assertTrue(p2.hasSubsetAnnot(p1));
		assertFalse(p1.hasSubsetAnnot(p2));
		
		Pred p3 = Pred.parsePred("p2(t1,t2)[a(A,_),a(X)]");
		Unifier u = new Unifier();
		assertTrue(p3.hasSubsetAnnot(p2,u));
		assertEquals(u.get("A").toString(),"2");
		assertEquals(u.get("X").toString(),"3");
		assertTrue(p3.hasSubsetAnnot(p1,u));
        
        Pred p4 = Pred.parsePred("p1(t1,t2)[a1|T]");
        u = new Unifier();
        assertTrue(p1.hasSubsetAnnot(p4, u));
        assertEquals(u.get("T").toString(), "[a(2,3),a(3)]");

        Pred p5 = Pred.parsePred("p1(t1,t2)[a1|[a(2,3),a(3)]]");
        u = new Unifier();
        assertTrue(p1.hasSubsetAnnot(p5, u));

        Pred p6 = Pred.parsePred("p1(t1,t2)[a1|T]");
        u = new Unifier();
        assertTrue(p6.hasSubsetAnnot(p1, u));
        assertEquals(u.get("T").toString(), "[a(2,3),a(3)]");
        assertTrue(p1.hasSubsetAnnot(p6, u));
    }
	
	public void testAnnotUnifAsList() {
		Pred p1 = Pred.parsePred("p[b(2),x]");
		Pred p2 = Pred.parsePred("p[a,b(2),c]");
		Unifier u = new Unifier();
		
		assertFalse(u.unifies(p1,p2));
		
		p1 = Pred.parsePred("p(t1,t2)[z,a(1),a(2,3),a(3)]");
		p2 = Pred.parsePred("p(t1,B)[a(X)|R]");

		assertTrue(u.unifies(p2,p1));
		assertEquals(u.get("R").toString(),"[z,a(2,3),a(3)]");
		
		u = new Unifier();
		assertTrue(u.unifies(p1,p2));
		
		u.apply(p2);
		assertEquals(p2.toString(),"p(t1,t2)[a(1),z,a(2,3),a(3)]");
	}
    
    public void testCompare() {
        Pred p1 = Pred.parsePred("a");
        Pred p2 = Pred.parsePred("b");
        
        assertEquals(p1.compareTo(p2), -1);
        assertEquals(p2.compareTo(p1), 1);
        assertEquals(p1.compareTo(p1), 0);
        
        p1 = Pred.parsePred("a(3)");
        p2 = Pred.parsePred("a(100)");
        
        assertEquals(p1.compareTo(p2), -1);
        assertEquals(p2.compareTo(p1), 1);
        
        ListTerm l = ListTermImpl.parseList("[b,c,g,casa,f(10),[3,4],[3,1],f(4)]");
        Collections.sort(l);
        assertEquals(l.toString(), "[b,c,casa,f(4),f(10),g,[3,1],[3,4]]");
    }
    
    public void testUnify4() {
        Term a1 = TermImpl.parse("a(1)");
        Term a2 = TermImpl.parse("a(X+1)");
        Unifier u = new Unifier();
        u.unifies(new VarTerm("X"),new NumberTermImpl(0));
        assertFalse(a1.equals(a2));   
    }
}
