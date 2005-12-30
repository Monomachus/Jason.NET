package test;

import jason.asSemantics.Agent;
import jason.asSemantics.Intention;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.BeliefBase;
import jason.asSyntax.ListTerm;
import jason.asSyntax.ListTermImpl;
import jason.asSyntax.Literal;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.NumberTermImpl;
import jason.asSyntax.Pred;
import jason.asSyntax.StringTerm;
import jason.asSyntax.StringTermImpl;
import jason.asSyntax.Term;
import jason.asSyntax.VarTerm;
import jason.stdlib.addAnnot;
import jason.stdlib.addPlan;
import jason.stdlib.getRelevantPlans;
import jason.stdlib.removePlan;

import java.util.Iterator;

import junit.framework.TestCase;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

/** JUnit test case for stdlib package */
public class StdLibTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
		Logger.getRootLogger().addAppender(new ConsoleAppender(new PatternLayout("[%c{1}] %m%n")));
    	Logger.getRootLogger().setLevel(Level.INFO);
	}
	
	public void testAddAnnot() {
		addAnnot aa = new addAnnot();
		Unifier u = new Unifier();

		Literal msg = Literal.parseLiteral("ok(10)");
		VarTerm X = new VarTerm("X");
		Term annot = Term.parse("source(jomi)");
		try {
			aa.execute(null, u, new Term[] { msg, annot, X });
		} catch (Exception e) {
			e.printStackTrace();
		}
		//System.out.println("u="+u);
		assertEquals(msg.toString(), "ok(10)");
		assertTrue( ((Pred)u.get("X")).hasAnnot(annot) );

		// testing addAnnot with list
		ListTerm msgL = (ListTerm)Term.parse("[ok(10),[ok(20),ok(30),[ok(40)|[ok(50),ok(60)]]]]");
		VarTerm Y = new VarTerm("Y");
		Term annotL = Term.parse("source(rafa)");
		assertEquals(msgL.toString(), "[ok(10),[ok(20),ok(30),[ok(40),ok(50),ok(60)]]]");
		try {
			aa.execute(null, u, new Term[] { (Term)msgL, annotL, Y });
		} catch (Exception e) {
			e.printStackTrace();
		}
		//System.out.println("u="+u);
		assertEquals(((ListTerm)u.get("Y")).toString(), "[ok(10)[source(rafa)],[ok(20)[source(rafa)],ok(30)[source(rafa)],[ok(40)[source(rafa)],ok(50)[source(rafa)],ok(60)[source(rafa)]]]]" );
	}
	
	
	public void testFindAll() {
		Agent ag = new Agent();
		ag.setLogger(null);
		ag.addBel(Literal.parseLiteral("a(10)"), BeliefBase.TPercept, null, Intention.EmptyInt);
		ag.addBel(Literal.parseLiteral("a(20)"), BeliefBase.TPercept, null, Intention.EmptyInt);
		ag.addBel(Literal.parseLiteral("a(30)"), BeliefBase.TPercept, null, Intention.EmptyInt);

		TransitionSystem ts = new TransitionSystem(ag, null, null, null);

		Unifier u = new Unifier();
		VarTerm X = new VarTerm("X");
		Literal c = Literal.parseLiteral("a(X)");
		c.addAnnot(BeliefBase.TPercept);
		VarTerm L = new VarTerm("L");
		//System.out.println(ag.getPS().getAllRelevant(Trigger.parseTrigger(ste.getFunctor())));
		try {
			assertTrue(new jason.stdlib.findall().execute(ts, u, new Term[] { X, c, L }));
		} catch (Exception e) {
			e.printStackTrace();
		}
		//System.out.println("L="+L);
		ListTerm lt = (ListTerm)u.get("L");
		assertEquals(lt.size(),3);
	}
	
	public void testGetRelevantPlansAndAddPlan() {
		Agent ag = new Agent();
		ag.setLogger(null);
		StringTerm pt1 = new StringTermImpl("@t1 +a : g(10) <- .print(\"ok 10\").");
		ag.addPlan(pt1, new Term("nosource"));
		ag.addPlan(new StringTermImpl("@t2 +a : g(20) <- .print(\"ok 20\")."), new Term("nosource"));
		ag.addPlan(new StringTermImpl("@t3 +b : true <- true."), new Term("nosource"));
		//System.out.println(ag.getPS());
		TransitionSystem ts = new TransitionSystem(ag, null, null, null);

		Unifier u = new Unifier();
		StringTerm ste = new StringTermImpl("+a");
		VarTerm X = new VarTerm("X");
		//System.out.println(ag.getPS().getAllRelevant(Trigger.parseTrigger(ste.getFunctor())));
		try {
			new getRelevantPlans().execute(ts, u, new Term[] { (Term)ste, X });
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		ListTerm plans = (ListTerm)u.get("X");
		//System.out.println("plans="+plans);

		assertEquals(plans.size(), 2);

		assertEquals(ag.getPS().getPlans().size(), 3);
		// remove plan t1 from PS
		try {
			new removePlan().execute(ts, new Unifier(), new Term[] { (Term)pt1, new Term("nosource") });
		} catch (Exception e) {
			e.printStackTrace();
		}
		//ag.getPS().remove(0);
		//System.out.println("PS="+ag.getPS());
		assertEquals(ag.getPS().getPlans().size(), 2);
		
		
		// add plans returned from getRelevantPlans
		// using IA addPlan
		Iterator i = plans.iterator();
		try {
			while (i.hasNext()) {
				StringTerm t = (StringTerm)i.next();
				new addPlan().execute(ts, new Unifier(), new Term[] { (Term)t, new Term("fromGR") });
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		
		// add again plans returned from getRelevantPlans
		// using IA addPlan receiving a list of plans
		try {
			new addPlan().execute(ts, new Unifier(), new Term[] { (Term)plans, new Term("fromLT") });
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// the plan t2 (first plan now) must have 3 sources
		assertEquals(ag.getPS().get(0).getLabel().getSources().size(), 3);

		// the plan t1 (third plan now) must have 2 sources
		assertEquals(ag.getPS().get(2).getLabel().getSources().size(), 2);
		
		//System.out.println("PS="+ag.getPS());
	}
	
	
	public void testConcat() {
		ListTerm l1 = ListTermImpl.parseList("[a,b,c]");
		ListTerm l2 = ListTermImpl.parseList("[d,e,f]");
		ListTerm l3 = ListTermImpl.parseList("[a,b,c,d,e,f]");

		VarTerm X = new VarTerm("X");
		Unifier u = new Unifier();

		try {
			assertTrue(new jason.stdlib.concat().execute(null, u, new Term[] { (Term)l1, (Term)l2, X }));
		} catch (Exception e) {
			e.printStackTrace();
		}
		//System.out.println("u="+u);
		assertEquals( ((ListTerm)u.get("X")).size(), 6);
		assertEquals( ((ListTerm)u.get("X")), l3);
		
		try {
			assertTrue(new jason.stdlib.concat().execute(null, new Unifier(), new Term[] { (Term)l1, (Term)l2, (Term)l3 }));
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}

	public void testGT() {
		NumberTerm x = new NumberTermImpl(20);
		NumberTerm y = new NumberTermImpl(100);
		VarTerm vy = new VarTerm("Y");
		Unifier u = new Unifier();
		u.unifies(vy,(Term)y);
		try {
			assertTrue(new jason.stdlib.gt().execute(null, u, new Term[] { vy, (Term)x }));
			assertFalse(new jason.stdlib.gt().execute(null, u, new Term[] { (Term)x, vy }));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		StringTerm sx = new StringTermImpl("a");
		StringTerm sy = new StringTermImpl("b");
		VarTerm vsy = new VarTerm("SY");
		u.unifies(vsy,(Term)sy);
		try {
			assertTrue(new jason.stdlib.gt().execute(null, u, new Term[] { vsy, (Term)sx }));
			assertFalse(new jason.stdlib.gt().execute(null, u, new Term[] { (Term)sx, vsy }));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
