package test;

import jason.asSemantics.Agent;
import jason.asSemantics.Unifier;
import jason.asSyntax.BeliefBase;
import jason.asSyntax.ListTermImpl;
import jason.asSyntax.Literal;
import jason.asSyntax.Pred;
import jason.asSyntax.Term;
import jason.asSyntax.VarTerm;
import junit.framework.TestCase;

/** JUnit test case for syntax package */
public class BeliefBaseTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testAdd() {
		Literal l1, l2, l3, l4, l5;
		BeliefBase bb = new BeliefBase();
		
		l1 = new Literal(true, new Pred("pos"));
		assertTrue(bb.add(l1));
		
		assertFalse(bb.add(new Literal(true, new Pred("pos"))));

		l2 = new Literal(true, new Pred("pos"));
		l2.addAnnot(new Term("a"));
		assertTrue(bb.add(l2));
		assertFalse(bb.add(l2));

		l3 = new Literal(true, new Pred("pos"));
		l3.addAnnot(new Term("b"));
		l3.addAnnot(BeliefBase.TPercept);
		assertTrue(bb.add(l3));
		assertFalse(bb.add(l3));

		l3 = new Literal(true, new Pred("pos"));
		l3.addSource(new Term("ag1"));
		assertTrue(bb.add(l3));

		// same as above, must not insert
		l3 = new Literal(true, new Pred("pos"));
		l3.addSource(new Term("ag1"));
		assertFalse(bb.add(l3));
		
		l4 = new Literal(true, new Pred("pos"));
		l4.addTerm(new Term("1"));
		l4.addTerm(new Term("2"));
		l4.addAnnot(BeliefBase.TPercept);
		assertTrue(bb.add(l4));

		l4 = new Literal(true, new Pred("pos"));
		l4.addTerm(new Term("1"));
		l4.addTerm(new Term("2"));
		l4.addAnnot(BeliefBase.TPercept);
		assertFalse(bb.add(l4));

		l4 = new Literal(true, new Pred("pos"));
		l4.addTerm(new Term("5"));
		l4.addTerm(new Term("6"));
		l4.addAnnot(BeliefBase.TPercept);
		assertTrue(bb.add(l4));

		l5 = new Literal(true, new Pred("garb"));
		l5.addTerm(new Term("r1"));
		assertTrue(bb.add(l5));
		
		//System.out.println("BB="+bb);
		//System.out.println("Percepts="+bb.getPercepts());
		assertEquals(bb.getPercepts().size(), 3);
		
		Literal lRel1 = new Literal(true, new Pred("pos"));
		//System.out.println("Rel "+lRel1.getFunctorArity()+"="+bb.getRelevant(lRel1));

		Literal lRel2 = new Literal(true, new Pred("pos"));
		lRel2.addTerm(new VarTerm("X"));
		lRel2.addTerm(new VarTerm("Y"));
		//System.out.println("Rel "+lRel2.getFunctorArity()+"="+bb.getRelevant(lRel2));

		
		// remove
		l5 = new Literal(true, new Pred("garb"));
		l5.addTerm(new Term("r1"));
		assertTrue(bb.remove(l5));
		assertEquals(bb.getRelevant(l5), null);

		l4 = new Literal(true, new Pred("pos"));
		l4.addTerm(new Term("5"));
		l4.addTerm(new Term("6"));
		l4.addAnnot(BeliefBase.TPercept);
		assertTrue(bb.remove(l4));
		assertEquals(bb.getRelevant(l4).size(), 1);

		//System.out.println("remove grab(r1), pos(5,6)");
		//System.out.println("BB="+bb);
		//System.out.println("Percepts="+bb.getPercepts());
		assertEquals(bb.getPercepts().size(), 2);
	
		l4 = new Literal(true, new Pred("pos"));
		l4.addTerm(new Term("1"));
		l4.addTerm(new Term("2"));
		l4.addAnnot(BeliefBase.TPercept);
		assertTrue(bb.remove(l4));
		assertEquals(bb.getRelevant(l4), null);

		//System.out.println("remove pos(1,2)");
		//System.out.println("BB="+bb);
		//System.out.println("Percepts="+bb.getPercepts());
		
		l2 = new Literal(true, new Pred("pos"));
		l2.addAnnot(new Term("a"));
		assertTrue(bb.contains(l2) != null);
		assertFalse(bb.contains(l2).hasSubsetAnnot(l2)); //
		assertTrue(bb.remove(l2));

		l2.addAnnot(new Term("b"));
		l2.addAnnot(BeliefBase.TPercept);
		l2.delAnnot(new Term("a"));
		assertTrue(bb.remove(l2));
		//System.out.println("removed "+l2);
		//System.out.println("BB="+bb);
		//System.out.println("Percepts="+bb.getPercepts());
		assertEquals(bb.getPercepts().size(), 0);
		assertEquals(bb.size(), 1);
		
		l3 = Literal.parseLiteral("pos[source(ag1)]");
		assertTrue(bb.remove(l3));
		
		//System.out.println("removed "+l3);
		//System.out.println("BB="+bb);
		assertEquals(bb.size(), 0);
	}
	
	public void testRemWithList() {
		Unifier u = new Unifier();
		BeliefBase bb = new BeliefBase();
		Literal s = Literal.parseLiteral("seen(L)");
		assertTrue(u.unifies(new VarTerm("L"), (Term)ListTermImpl.parseList("[a,b]")));
		//System.out.println("u="+u);
		u.apply(s);
		bb.add(s);

		VarTerm b1 = new VarTerm("B1");
		u.unifies(b1, Literal.parseLiteral("seen([a,b])"));
		u.apply(b1);
		//System.out.println("b1="+b1);
		//System.out.println("test 1");
		assertTrue(b1.equalsAsTerm(Literal.parseLiteral("seen([a,b])")));
		assertTrue(b1.equalsAsTerm(s));
		assertTrue(bb.remove(b1));
	}
	
	public void testRemWithUnnamedVar() {
		BeliefBase bb = new BeliefBase();
		
		Agent ag = new Agent();
		ag.getBS().add(Literal.parseLiteral("pos(2,3)"));
		Unifier u = new Unifier();

		Literal l = ag.believes(Literal.parseLiteral("pos(_,_)"), u);
		assertTrue(l != null);
		assertEquals(l, Literal.parseLiteral("pos(2,3)"));
		
		assertTrue(ag.getBS().remove(l));
	}	
}
