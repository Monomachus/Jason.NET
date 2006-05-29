package test;

import jason.asSemantics.Agent;
import jason.asSemantics.Unifier;
import jason.asSyntax.BeliefBase;
import jason.asSyntax.ListTermImpl;
import jason.asSyntax.Literal;
import jason.asSyntax.LogExprTerm;
import jason.asSyntax.Pred;
import jason.asSyntax.Term;
import jason.asSyntax.TermImpl;
import jason.asSyntax.VarTerm;

import java.util.Iterator;

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
		l2.addAnnot(new TermImpl("a"));
		assertTrue(bb.add(l2));
		assertFalse(bb.add(l2));
		assertEquals(bb.size(),1);

		l3 = new Literal(true, new Pred("pos"));
		l3.addAnnot(new TermImpl("b"));
		l3.addAnnot(BeliefBase.TPercept);
		assertTrue(bb.add(l3));
		assertFalse(bb.add(l3));
		assertEquals(bb.size(),1);

		l3 = new Literal(true, new Pred("pos"));
		l3.addSource(new TermImpl("ag1"));
		assertTrue(bb.add(l3));

		// same as above, must not insert
		l3 = new Literal(true, new Pred("pos"));
		l3.addSource(new TermImpl("ag1"));
		assertFalse(bb.add(l3));
		
		l4 = new Literal(true, new Pred("pos"));
		l4.addTerm(new TermImpl("1"));
		l4.addTerm(new TermImpl("2"));
		l4.addAnnot(BeliefBase.TPercept);
		assertTrue(bb.add(l4));

		l4 = new Literal(true, new Pred("pos"));
		l4.addTerm(new TermImpl("1"));
		l4.addTerm(new TermImpl("2"));
		l4.addAnnot(BeliefBase.TPercept);
		assertFalse(bb.add(l4));
		assertEquals(bb.size(),2);

		l4 = new Literal(true, new Pred("pos"));
		l4.addTerm(new TermImpl("5"));
		l4.addTerm(new TermImpl("6"));
		l4.addAnnot(BeliefBase.TPercept);
		assertTrue(bb.add(l4));

		l5 = new Literal(true, new Pred("garb"));
		l5.addTerm(new TermImpl("r1"));
		assertTrue(bb.add(l5));
		
		//System.out.println("BB="+bb);
		//System.out.println("Percepts="+bb.getPercepts());
		assertEquals(bb.getPercepts().size(), 3);
		
		//Literal lRel1 = new Literal(true, new Pred("pos"));
		//System.out.println("Rel "+lRel1.getFunctorArity()+"="+bb.getRelevant(lRel1));

		Literal lRel2 = new Literal(true, new Pred("pos"));
		lRel2.addTerm(new VarTerm("X"));
		lRel2.addTerm(new VarTerm("Y"));
		//System.out.println("Rel "+lRel2.getFunctorArity()+"="+bb.getRelevant(lRel2));
		assertEquals(bb.size(), 4);
		
		// remove
		l5 = new Literal(true, new Pred("garb"));
		l5.addTerm(new TermImpl("r1"));
		assertTrue(bb.remove(l5));
		assertEquals(bb.getRelevant(l5), null);
		assertEquals(bb.size(), 3);

		l4 = new Literal(true, new Pred("pos"));
		l4.addTerm(new TermImpl("5"));
		l4.addTerm(new TermImpl("6"));
		l4.addAnnot(BeliefBase.TPercept);
		assertTrue(bb.remove(l4));
		assertEquals(bb.getRelevant(l4).size(), 1);
		assertEquals(bb.size(), 2);

		//System.out.println("remove grab(r1), pos(5,6)");
		//System.out.println("BB="+bb);
		//System.out.println("Percepts="+bb.getPercepts());
		assertEquals(bb.getPercepts().size(), 2);
	
		l4 = new Literal(true, new Pred("pos"));
		l4.addTerm(new TermImpl("1"));
		l4.addTerm(new TermImpl("2"));
		l4.addAnnot(BeliefBase.TPercept);
		assertTrue(bb.remove(l4));
		assertEquals(bb.getRelevant(l4), null);
		assertEquals(bb.size(), 1);

		//System.out.println("remove pos(1,2)");
		//System.out.println("BB="+bb);
		//System.out.println("Percepts="+bb.getPercepts());
		
		l2 = new Literal(true, new Pred("pos"));
		l2.addAnnot(new TermImpl("a"));
		assertTrue(bb.contains(l2) != null);
		assertFalse(bb.contains(l2).hasSubsetAnnot(l2)); //
		assertTrue(bb.remove(l2));

		l2.addAnnot(new TermImpl("b"));
		l2.addAnnot(BeliefBase.TPercept);
		l2.delAnnot(new TermImpl("a"));
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
		Agent ag = new Agent();
		ag.getBS().add(Literal.parseLiteral("pos(2,3)"));
		Unifier u = new Unifier();

		Literal l = ag.believes(Literal.parseLiteral("pos(_,_)"), u);
		assertTrue(l != null);
		assertEquals(l, Literal.parseLiteral("pos(2,3)"));
		
		assertTrue(ag.getBS().remove(l));
	}
    
    public void testLogCons() {
        Agent ag = new Agent();
        ag.getBS().add(Literal.parseLiteral("a(10)"));
        ag.getBS().add(Literal.parseLiteral("a(20)"));
        ag.getBS().add(Literal.parseLiteral("b(20,10)"));
        ag.getBS().add(Literal.parseLiteral("c(x)"));
        ag.getBS().add(Literal.parseLiteral("c(y)"));
        Term texpr;
        
        Iterator<Unifier> iun = Literal.parseLiteral("a(X)").logCons(ag, new Unifier());
        int c = 0;
        while (iun.hasNext()) {
            iun.next();
            c++;
        }
        assertEquals(c,2);

        iun = Literal.parseLiteral("b(X,_)").logCons(ag, new Unifier());
        assertTrue(iun.hasNext());
        Unifier un = iun.next();
        assertTrue(un.get("X").toString().equals("20"));
        
        
        // test not
        texpr = LogExprTerm.parseExpr("not a(5)");
        assertTrue(texpr.logCons(ag, new Unifier()).hasNext());
        texpr = LogExprTerm.parseExpr("not a(10)");
        assertFalse(texpr.logCons(ag, new Unifier()).hasNext());
        
        
        // test and
        texpr = LogExprTerm.parseExpr("a(X) & c(Y) & a(Z)");
        iun = texpr.logCons(ag, new Unifier());
        c = 0;
        while (iun.hasNext()) {
            Unifier u = iun.next();
            //System.out.println(u);
            c++;
        }
        assertEquals(c,8);
        
        // test or
        texpr = LogExprTerm.parseExpr("a(X) | c(Y)");
        iun = texpr.logCons(ag, new Unifier());
        c = 0;
        while (iun.hasNext()) {
            Unifier u = iun.next();
            //System.out.println(u);
            c++;
        }
        assertEquals(c,4);
        
        // test rel
        texpr = LogExprTerm.parseExpr("a(X) & a(Y) & Y > X");
        iun = texpr.logCons(ag, new Unifier());
        c = 0;
        while (iun.hasNext()) {
            Unifier u = iun.next();
            c++;
        }
        assertEquals(c,1);

        texpr = LogExprTerm.parseExpr("a(X) & a(Y) & X <= Y");
        iun = texpr.logCons(ag, new Unifier());
        c = 0;
        while (iun.hasNext()) {
            Unifier u = iun.next();
            c++;
        }
        assertEquals(c,3);
        
    }
}
