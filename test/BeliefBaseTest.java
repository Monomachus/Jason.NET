package test;

import jason.asSemantics.Agent;
import jason.asSemantics.Unifier;
import jason.asSyntax.ListTermImpl;
import jason.asSyntax.Literal;
import jason.asSyntax.LogExprTerm;
import jason.asSyntax.Pred;
import jason.asSyntax.PredicateIndicator;
import jason.asSyntax.Term;
import jason.asSyntax.TermImpl;
import jason.asSyntax.VarTerm;
import jason.bb.BeliefBase;
import jason.bb.DefaultBeliefBase;
import jason.bb.JDBCPersistentBB;

import java.util.Iterator;

import junit.framework.TestCase;

/** JUnit test case for syntax package */
public class BeliefBaseTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testAdd() {
		Literal l1, l2, l3, l4, l5;
		BeliefBase bb = new DefaultBeliefBase();
		
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
		assertEquals(iteratorSize(bb.getPercepts()), 3);
		
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
		assertEquals(iteratorSize(bb.getRelevant(l4)), 1);
		assertEquals(bb.size(), 2);

		//System.out.println("remove grab(r1), pos(5,6)");
		//System.out.println("BB="+bb);
		//System.out.println("Percepts="+bb.getPercepts());
		assertEquals(iteratorSize(bb.getPercepts()), 2);
	
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
		assertTrue(((DefaultBeliefBase)bb).containsAsTerm(l2) != null);
		assertFalse(((DefaultBeliefBase)bb).containsAsTerm(l2).hasSubsetAnnot(l2)); //
		assertTrue(bb.remove(l2));

		l2.addAnnot(new TermImpl("b"));
		l2.addAnnot(BeliefBase.TPercept);
		l2.delAnnot(new TermImpl("a"));
		assertTrue(bb.remove(l2));
		//System.out.println("removed "+l2);
		//System.out.println("BB="+bb);
		//System.out.println("Percepts="+bb.getPercepts());
		assertEquals(iteratorSize(bb.getPercepts()), 0);
		assertEquals(bb.size(), 1);
		
		l3 = Literal.parseLiteral("pos[source(ag1)]");
		assertTrue(bb.remove(l3));
		
		//System.out.println("removed "+l3);
		//System.out.println("BB="+bb);
		assertEquals(bb.size(), 0);
	}
	
	public void testRemWithList() {
		Unifier u = new Unifier();
		BeliefBase bb = new DefaultBeliefBase();
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
		ag.getBB().add(Literal.parseLiteral("pos(2,3)"));
		Unifier u = new Unifier();

		Literal l = ag.believes(Literal.parseLiteral("pos(_,_)"), u);
		assertTrue(l != null);
		assertEquals(l, Literal.parseLiteral("pos(2,3)"));
		
		assertTrue(ag.getBB().remove(l));
	}
    
    public void testLogCons() {
        Agent ag = new Agent();
        ag.getBB().add(Literal.parseLiteral("a(10)"));
        ag.getBB().add(Literal.parseLiteral("a(20)"));
        ag.getBB().add(Literal.parseLiteral("b(20,10)"));
        ag.getBB().add(Literal.parseLiteral("c(x)"));
        ag.getBB().add(Literal.parseLiteral("c(y)"));
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
    
    public void testPercept() {
        BeliefBase bb = new DefaultBeliefBase();
        assertTrue(bb.add(Literal.parseLiteral("a[source(percept)]")));
        assertTrue(bb.add(Literal.parseLiteral("a[ag1]")));
        assertEquals(iteratorSize(bb.getPercepts()),1);

        // remove annots ag1
        assertTrue(bb.remove(Literal.parseLiteral("a[ag1]")));
        assertEquals(iteratorSize(bb.getPercepts()),1);
        assertTrue(bb.remove(Literal.parseLiteral("a[source(percept)]")));
        assertEquals(bb.size(),0);
        assertEquals(iteratorSize(bb.getPercepts()),0);

        // add again and remove percept first
        assertTrue(bb.add(Literal.parseLiteral("a[source(percept)]")));
        assertTrue(bb.add(Literal.parseLiteral("a[ag1]")));
        assertTrue(bb.remove(Literal.parseLiteral("a[source(percept)]")));
        assertEquals(bb.size(),1);
        assertEquals(iteratorSize(bb.getPercepts()),0);
    }
    
    public void testJDBCBB() {
        BeliefBase bb = new JDBCPersistentBB();
        bb.init(null, new String[] {
                "org.hsqldb.jdbcDriver",
                "jdbc:hsqldb:bookstore",
                "sa",
                "",
                "[book(5,book),book_author(2,book_author),author(2,author),test(2,testtable)]"
                });
        
        bb.abolish(new PredicateIndicator("book",5));
        bb.abolish(new PredicateIndicator("author",2));
        bb.abolish(new PredicateIndicator("book_author",2));
        bb.abolish(new PredicateIndicator("test",2));
        assertEquals(bb.size(),0);

        assertTrue(bb.add(Literal.parseLiteral("test(30)")));
        assertEquals(bb.size(),1);
        Literal l;
        
        // add authors
        assertTrue(bb.add(Literal.parseLiteral("author(1,\"Rafael H. Bordini\")")));
        assertFalse(bb.add(Literal.parseLiteral("author(1,\"Rafael H. Bordini\")")));
        assertTrue(bb.add(Literal.parseLiteral("author(2,\"Mehdi Dastani\")")));
        assertTrue(bb.add(Literal.parseLiteral("author(3,\"Jurgen Dix\")")));
        assertTrue(bb.add(Literal.parseLiteral("author(4,\"Amal El Fallah Seghrouchni\")")));
        assertTrue(bb.add(Literal.parseLiteral("author(5,\"Michael Wooldridge\")")));
        assertEquals(bb.size(),6);
        
        // add books
        l = Literal.parseLiteral("book(1,\"Multi-Agent Programming : Languages, Platforms and Applications\", \"Springer\", 2005, \"0387245685\")");
        assertTrue(bb.add(l));
        assertFalse(bb.add(l));
        // add book authors
        assertTrue(bb.add(Literal.parseLiteral("book_author(1,1)")));
        assertTrue(bb.add(Literal.parseLiteral("book_author(1,2)")));
        assertTrue(bb.add(Literal.parseLiteral("book_author(1,3)")));
        assertTrue(bb.add(Literal.parseLiteral("book_author(1,4)")));
        assertEquals(bb.size(),11);

        // add another book
        l = Literal.parseLiteral("book(2,\"Another Multi-Agent Programming : Languages, Platforms and Applications\", \"Springer\", 2005, \"0387245685\")");
        assertTrue(bb.add(l));

        l = Literal.parseLiteral("book(3,\"An introduction to multiagent systems\", \"John Wiley & Sons\", 2002, \"\")");
        assertTrue(bb.add(l));
        assertTrue(bb.add(Literal.parseLiteral("book_author(3,5)")));
        assertEquals(bb.size(),14);

        // test with legacy table
        ((JDBCPersistentBB)bb).test();
        // test add two records
        assertEquals(bb.size(),16);
        assertTrue(bb.add(Literal.parseLiteral("publisher(10,\"Prentice Hall\")")));
        assertFalse(bb.add(Literal.parseLiteral("publisher(10,\"Prentice Hall\")")));
        assertEquals(bb.size(),17);

        // test annots
        l = Literal.parseLiteral("test(t1(a(10),b(20)),[v1,30,\"a vl\"])[annot1,source(carlos)]");
        assertTrue(bb.add(l));
        assertFalse(bb.add(l));
        Literal linbb = ((JDBCPersistentBB)bb).containsAsTerm(l);
        assertTrue(l.getTerm(0).equals(linbb.getTerm(0)));
        assertTrue(l.getTerm(1).equals(linbb.getTerm(1)));
        assertTrue(l.equals(linbb));
        l = Literal.parseLiteral("test(t1(a(10),b(20)),[v1,30,\"a vl\"])[annot2]");
        assertTrue(bb.add(l));
        linbb = ((JDBCPersistentBB)bb).containsAsTerm(l);
        assertEquals(linbb.getAnnots().size(),3);
        l = Literal.parseLiteral("test(t1(a(10),b(20)),[v1,30,\"a vl\"])[annot2]");
        assertFalse(bb.add(l));
        linbb = ((JDBCPersistentBB)bb).containsAsTerm(l);
        assertEquals(linbb.getAnnots().size(),3);

        // test negated
        int size = bb.size();
        l = Literal.parseLiteral("test(a,b)");
        assertTrue(bb.add(l));
        l = Literal.parseLiteral("~test(a,b)");
        assertTrue(bb.add(l));
        assertEquals(bb.size(),size+2);
        
        // test get all
        //Iterator<Literal> il = bb.getAll();
        //while (il.hasNext()) {
        //    System.out.println(il.next());
        //}
        assertEquals(iteratorSize(bb.getAll()),size+2);

        // test remove
        size = bb.size();
        assertTrue(bb.remove(Literal.parseLiteral("test(a,b)")));
        assertFalse(bb.remove(Literal.parseLiteral("test(a,b)")));
        assertTrue(bb.remove(Literal.parseLiteral("publisher(10,\"Prentice Hall\")")));
        l = Literal.parseLiteral("test(t1(a(10),b(20)),[v1,30,\"a vl\"])[annot2]");
        assertTrue(bb.remove(l));
        linbb = ((JDBCPersistentBB)bb).containsAsTerm(l);
        assertEquals(linbb.getAnnots().size(),2);
        assertEquals(bb.size(),size-2);
        
        // test getRelevant
        //Iterator ir = bb.getRelevant(Literal.parseLiteral("book_author(_,_)"));
        //while (ir.hasNext()) {
        //    System.out.println(ir.next());
        //}
        assertEquals(iteratorSize(bb.getRelevant(Literal.parseLiteral("book_author(_,_)"))),5);
        
        bb.stop();
    }
    
    private int iteratorSize(Iterator i) {
        int c = 0;
        while (i.hasNext()) {
            i.next();
            c++;
        }
        return c;
    }
    
}
