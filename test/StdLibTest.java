package test;

import jason.asSemantics.Agent;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.ListTerm;
import jason.asSyntax.Literal;
import jason.asSyntax.Pred;
import jason.asSyntax.StringTerm;
import jason.asSyntax.Term;
import jason.stdlib.addAnnot;
import jason.stdlib.addPlan;
import jason.stdlib.getRelevantPlans;
import jason.stdlib.removePlan;

import java.util.Iterator;

import junit.framework.TestCase;

/** JUnit test case for stdlib package */
public class StdLibTest extends TestCase {

	public void testAddAnnot() {
		addAnnot aa = new addAnnot();
		Unifier u = new Unifier();
		Literal msg = Literal.parseLiteral("ok(10)");
		Term X = new Term("X");
		Term annot = Term.parse("source(jomi)");
		try {
			aa.execute(null, u, new Term[] { msg, annot, X });
		} catch (Exception e) {
			e.printStackTrace();
		}
		//System.out.println("X="+X);
		//System.out.println("u="+u);
		assertEquals(msg.toString(), "ok(10)");
		assertTrue( ((Pred)u.get("X")).hasAnnot(annot) );
	}
	
	public void testGetRelevantPlansAndAddPlan() {
		Agent ag = new Agent();
		StringTerm pt1 = new StringTerm("@t1 +a : g(10) <- .print(\"ok 10\").");
		ag.addPlan(pt1, new Term("nosource"));
		ag.addPlan(new StringTerm("@t2 +a : g(20) <- .print(\"ok 20\")."), new Term("nosource"));
		ag.addPlan(new StringTerm("@t3 +b : true <- true."), new Term("nosource"));
		//System.out.println(ag.getPS());
		TransitionSystem ts = new TransitionSystem(ag, null, null, null);

		Unifier u = new Unifier();
		StringTerm ste = new StringTerm("+a");
		Term X = new Term("X");
		//System.out.println(ag.getPS().getAllRelevant(Trigger.parseTrigger(ste.getFunctor())));
		try {
			new getRelevantPlans().execute(ts, u, new Term[] { ste, X });
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		ListTerm plans = (ListTerm)u.get("X");
		//System.out.println("plans="+plans);

		assertEquals(plans.size(), 2);
		
		// remove plan t1 from PS
		try {
			new removePlan().execute(ts, new Unifier(), new Term[] { pt1, new Term("nosource") });
		} catch (Exception e) {
			e.printStackTrace();
		}
		//ag.getPS().remove(0);
		//System.out.println("PS="+ag.getPS());
		assertEquals(ag.getPS().getPlans().size(), 2);
		
		
		// add plans returned from getRelevantPlans
		// using IA addPlan
		Iterator i = plans.iterator();
		while (i.hasNext()) {
			ListTerm lt = (ListTerm)i.next();
			try {
				new addPlan().execute(ts, new Unifier(), new Term[] { (StringTerm)lt.getTerm(), new Term("fromGR") });
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		
		// add again plans returned from getRelevantPlans
		// using IA addPlan receiving a list of plans
		try {
			new addPlan().execute(ts, new Unifier(), new Term[] { plans, new Term("fromLT") });
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
		ListTerm l1 = ListTerm.parseList("[a,b,c]");
		ListTerm l2 = ListTerm.parseList("[d,e,f]");
		ListTerm l3 = ListTerm.parseList("[a,b,c,d,e,f]");

		Term X = new Term("X");
		Unifier u = new Unifier();

		try {
			assertTrue(new jason.stdlib.concat().execute(null, u, new Term[] { l1, l2, X }));
		} catch (Exception e) {
			e.printStackTrace();
		}
		//System.out.println("u="+u);
		assertEquals( ((ListTerm)u.get("X")).size(), 6);
		assertEquals( ((ListTerm)u.get("X")), l3);
		
		try {
			assertTrue(new jason.stdlib.concat().execute(null, new Unifier(), new Term[] { l1, l2, l3 }));
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
	
}
