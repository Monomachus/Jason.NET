package test;

import jason.D;
import jason.asSyntax.BeliefBase;
import jason.asSyntax.Literal;
import jason.asSyntax.Pred;
import jason.asSyntax.Term;
import junit.framework.TestCase;

/** JUnit test case for syntax package */
public class BeliefBaseTest extends TestCase {

	
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
		l3.addAnnot(D.TPercept);
		assertTrue(bb.add(l3));
		assertFalse(bb.add(l3));

		l3 = new Literal(true, new Pred("pos"));
		l3.addSource("ag1");
		assertTrue(bb.add(l3));

		// same as above, must not insert
		l3 = new Literal(true, new Pred("pos"));
		l3.addSource("ag1");
		assertFalse(bb.add(l3));
		
		l4 = new Literal(true, new Pred("pos"));
		l4.addTerm(new Term("1"));
		l4.addTerm(new Term("2"));
		l4.addAnnot(D.TPercept);
		assertTrue(bb.add(l4));

		l4 = new Literal(true, new Pred("pos"));
		l4.addTerm(new Term("1"));
		l4.addTerm(new Term("2"));
		l4.addAnnot(D.TPercept);
		assertFalse(bb.add(l4));

		l4 = new Literal(true, new Pred("pos"));
		l4.addTerm(new Term("5"));
		l4.addTerm(new Term("6"));
		l4.addAnnot(D.TPercept);
		assertTrue(bb.add(l4));

		l5 = new Literal(true, new Pred("garb"));
		l5.addTerm(new Term("r1"));
		assertTrue(bb.add(l5));
		
		System.out.println("BB="+bb);
		System.out.println("Percepts="+bb.getPercepts());
		assertEquals(bb.getPercepts().size(), 3);
		
		Literal lRel1 = new Literal(true, new Pred("pos"));
		System.out.println("Rel "+lRel1.getFunctorArity()+"="+bb.getRelevant(lRel1));

		Literal lRel2 = new Literal(true, new Pred("pos"));
		lRel2.addTerm(new Term("X"));
		lRel2.addTerm(new Term("Y"));
		System.out.println("Rel "+lRel2.getFunctorArity()+"="+bb.getRelevant(lRel2));

		
		// remove
		l5 = new Literal(true, new Pred("garb"));
		l5.addTerm(new Term("r1"));
		assertTrue(bb.remove(l5));
		assertEquals(bb.getRelevant(l5), null);

		l4 = new Literal(true, new Pred("pos"));
		l4.addTerm(new Term("5"));
		l4.addTerm(new Term("6"));
		l4.addAnnot(D.TPercept);
		assertTrue(bb.remove(l4));
		assertEquals(bb.getRelevant(l4).size(), 1);

		System.out.println("remove grab(r1), pos(5,6)");
		System.out.println("BB="+bb);
		System.out.println("Percepts="+bb.getPercepts());
		assertEquals(bb.getPercepts().size(), 2);
	
		l4 = new Literal(true, new Pred("pos"));
		l4.addTerm(new Term("1"));
		l4.addTerm(new Term("2"));
		l4.addAnnot(D.TPercept);
		assertTrue(bb.remove(l4));
		assertEquals(bb.getRelevant(l4), null);

		System.out.println("remove pos(1,2)");
		System.out.println("BB="+bb);
		System.out.println("Percepts="+bb.getPercepts());
		
		l2 = new Literal(true, new Pred("pos"));
		l2.addAnnot(new Term("a"));
		assertTrue(bb.contains(l2) != null);
		assertFalse(bb.contains(l2).hasSubsetAnnot(l2)); //
		assertFalse(bb.remove(l2));

		l2.addAnnot(new Term("b"));
		l2.addAnnot(D.TPercept);
		l2.addSource("ag1");
		bb.remove(l2);
		
		System.out.println("remove "+l2);
		System.out.println("BB="+bb);
		System.out.println("Percepts="+bb.getPercepts());
		assertEquals(bb.getPercepts().size(), 0);
	}

}
