package test;

import jason.asSemantics.Agent;
import jason.asSemantics.Circumstance;
import jason.asSemantics.IntendedMeans;
import jason.asSemantics.Option;
import jason.asSemantics.TransitionSystem;
import jason.asSyntax.Literal;
import jason.asSyntax.StringTerm;
import jason.asSyntax.StringTermImpl;
import jason.asSyntax.Term;
import jason.asSyntax.Trigger;

import java.util.List;

import junit.framework.TestCase;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

/** JUnit test case for syntax package */
public class TSTest extends TestCase {
	
	protected void setUp() throws Exception {
		super.setUp();
		Logger.getRootLogger().addAppender(new ConsoleAppender(new PatternLayout("[%c{1}] %m%n")));
    	Logger.getRootLogger().setLevel(Level.DEBUG);
	}
	
	public void testRelevant() {
		Agent ag = new Agent();
		Circumstance c = new Circumstance();
		StringTerm pt1 = new StringTermImpl("@t1 +a(X) : g(10) <- .print(\"ok 10\").");
		ag.addPlan(pt1, new Term("nosource"));
		ag.addPlan(new StringTermImpl("@t2 +a(X) : true <- .print(\"ok 20\")."), new Term("nosource"));
		ag.addPlan(new StringTermImpl("@t3 +b : true <- true."), new Term("nosource"));
		TransitionSystem ts = new TransitionSystem(ag, c, null, null);
		Literal content = Literal.parseLiteral("~alliance");
		content.addSource("ag1");

		Trigger te1 = Trigger.parseTrigger("+a(10)");
		
		try {
			List rp = ts.relevantPlans(te1);
			//System.out.println("RP="+rp);
			assertEquals(rp.size(), 2);
			
			ts.applicablePlans(rp);
			//System.out.println("AP="+rp);
			assertEquals(rp.size(), 1);
			
			Option opt = ag.selectOption(rp);
			IntendedMeans im = new IntendedMeans(opt);
			//System.out.println(im);
		} catch (Exception e) {
			e.printStackTrace();
		}

		Trigger te2 = Trigger.parseTrigger("+a(20)");
		
		try {
			List rp = ts.relevantPlans(te2);
			//System.out.println("RP="+rp);
			assertEquals(rp.size(), 2);
			
			ts.applicablePlans(rp);
			//System.out.println("AP="+rp);
			assertEquals(rp.size(), 1);
			
			Option opt = ag.selectOption(rp);
			IntendedMeans im = new IntendedMeans(opt);
			//System.out.println(im);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}
