package test;

import jason.JasonException;
import jason.asSemantics.Agent;
import jason.asSemantics.Circumstance;
import jason.asSemantics.IntendedMeans;
import jason.asSemantics.Intention;
import jason.asSemantics.Option;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Literal;
import jason.asSyntax.Plan;
import jason.asSyntax.Trigger;
import jason.stdlib.dropGoal;
import junit.framework.TestCase;

/** JUnit test case for stdlib package */
public class StdLibTest extends TestCase {

    Intention intention1 = new Intention();
    Plan p4, p5;
    Agent ag;
    
    protected void setUp() throws Exception {
        super.setUp();
        
        intention1 = new Intention();
        Plan p = Plan.parse("+!g0 : true <- !g1; !g4.");
        intention1.push(new IntendedMeans(new Option(p,new Unifier())));
        
        p = Plan.parse("+!g1 : true <- !g2.");
        intention1.push(new IntendedMeans(new Option(p,new Unifier())));

        p = Plan.parse("+!g2 : true <- !g4; f;g.");
        intention1.push(new IntendedMeans(new Option(p,new Unifier())));
        
        p4 = Plan.parse("+!g4 : true <- h.");
        intention1.push(new IntendedMeans(new Option(p4,new Unifier())));

        p5 = Plan.parse("+!g5 : true <- i.");
        
        ag = new Agent();
        ag.getPL().add(Plan.parse("-!g1 : true <- j."));
    }

    /*
    public void testAddAnnot() {
        addAnnot aa = new addAnnot();
        Unifier u = new Unifier();

        Literal msg = Literal.parseLiteral("ok(10)");
        VarTerm X = new VarTerm("X");
        Term annot = TermImpl.parse("source(jomi)");
        try {
            aa.execute(null, u, new Term[] { msg, annot, X });
        } catch (Exception e) {
            e.printStackTrace();
        }
        // System.out.println("u="+u);
        assertEquals(msg.toString(), "ok(10)");
        assertTrue(((Pred) u.get("X")).hasAnnot(annot));

        // testing addAnnot with list
        ListTerm msgL = (ListTerm) TermImpl.parse("[ok(10),[ok(20),ok(30),[ok(40)|[ok(50),ok(60)]]]]");
        VarTerm Y = new VarTerm("Y");
        Term annotL = TermImpl.parse("source(rafa)");
        assertEquals(msgL.toString(), "[ok(10),[ok(20),ok(30),[ok(40),ok(50),ok(60)]]]");
        try {
            aa.execute(null, u, new Term[] { (Term) msgL, annotL, Y });
        } catch (Exception e) {
            e.printStackTrace();
        }
        // System.out.println("u="+u);
        assertEquals(((ListTerm) u.get("Y")).toString(),
                "[ok(10)[source(rafa)],[ok(20)[source(rafa)],ok(30)[source(rafa)],[ok(40)[source(rafa)],ok(50)[source(rafa)],ok(60)[source(rafa)]]]]");
    }

    public void testFindAll() {
        Agent ag = new Agent();
        ag.setLogger(null);
        
        Literal l1 = Literal.parseLiteral("a(10,x)");
        assertFalse(l1.hasSource());
        ag.addBel(l1);
        ag.addBel(Literal.parseLiteral("a(20,y)"));
        ag.addBel(Literal.parseLiteral("a(30,x)"));
        assertEquals(ag.getBB().size(),3);
        
        TransitionSystem ts = new TransitionSystem(ag, null, null, null);

        Unifier u = new Unifier();
        Term X = TermImpl.parse("f(X)");
        Literal c = Literal.parseLiteral("a(X,x)");
        c.addAnnot(BeliefBase.TSelf);
        VarTerm L = new VarTerm("L");
        // System.out.println(ag.getPS().getAllRelevant(Trigger.parseTrigger(ste.getFunctor())));
        try {
            assertTrue(new jason.stdlib.findall().execute(ts, u, new Term[] { X, c, L }));
        } catch (Exception e) {
            e.printStackTrace();
        }
        ListTerm lt = (ListTerm) u.get("L");
        //System.out.println("found=" + lt);
        assertEquals(lt.size(), 2);
    }

    public void testGetRelevantPlansAndAddPlan() {
        Agent ag = new Agent();
        ag.setLogger(null);
        StringTerm pt1 = new StringTermImpl("@t1 +a : g(10) <- .print(\"ok 10\").");
        Plan pa = ag.getPL().add(pt1, null);
        assertTrue(pa != null);
        assertEquals(pa.toASString(),"@t1[source(self)] +a : g(10) <- .print(\"ok 10\").");

        ag.getPL().add(new StringTermImpl("@t2 +a : g(20) <- .print(\"ok 20\")."), new TermImpl("nosource"));
        ((Plan) ag.getPL().getPlans().get(1)).getLabel().addSource(new TermImpl("ag1"));
        ag.getPL().add(new StringTermImpl("@t3 +b : true <- true."), null);
        // System.out.println(ag.getPS());
        TransitionSystem ts = new TransitionSystem(ag, null, null, null);

        Unifier u = new Unifier();
        StringTerm ste = new StringTermImpl("+a");
        VarTerm X = new VarTerm("X");
        // System.out.println(ag.getPS().getAllRelevant(Trigger.parseTrigger(ste.getFunctor())));
        try {
            new getRelevantPlans().execute(ts, u, new Term[] { (Term) ste, X });
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertTrue(ag.getPL().getPlans().get(0).equals(Plan.parse(pt1.getString())));

        ListTerm plans = (ListTerm) u.get("X");
        //System.out.println("plans="+plans);

        assertEquals(plans.size(), 2);

        assertEquals(ag.getPL().getPlans().size(), 3);
        // remove plan t1 from PS
        try {
            new removePlan().execute(ts, new Unifier(), new Term[] { new TermImpl("t1") });
        } catch (Exception e) {
            e.printStackTrace();
        }
        // ag.getPS().remove(0);
        assertEquals(ag.getPL().getPlans().size(), 2);

        // add plans returned from getRelevantPlans
        // using IA addPlan
        Iterator i = plans.iterator();
        try {
            while (i.hasNext()) {
                StringTerm t = (StringTerm) i.next();
                new addPlan().execute(ts, new Unifier(), new Term[] { t, new TermImpl("fromGR") });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // add again plans returned from getRelevantPlans
        // using IA addPlan receiving a list of plans
        try {
            new addPlan().execute(ts, new Unifier(), new Term[] { (Term) plans, new TermImpl("fromLT") });
        } catch (Exception e) {
            e.printStackTrace();
        }

        // the plan t2 (first plan now) must have 4 sources
        assertEquals(ag.getPL().get("t2").getLabel().getSources().size(), 4);

        // the plan t1 (third plan now) must have 2 sources
        assertEquals(ag.getPL().get("t1").getLabel().getSources().size(), 2);

        // remove plan t2,t3 (source = nosource) from PS
        ListTerm llt = ListTermImpl.parseList("[t2,t3]");
        try {
            assertTrue(new removePlan().execute(ts, new Unifier(), new Term[] { (Term) llt, new TermImpl("nosource") }));
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(ag.getPL().getPlans().size(), 3);

        // remove plan t2,t3 (source = self) from PS
        llt = ListTermImpl.parseList("[t2,t3]");
        try {
            assertTrue(new removePlan().execute(ts, new Unifier(), new Term[] { (Term) llt }));
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(ag.getPL().getPlans().size(), 2);

        // the plan t2 (first plan now) must have 3 sources
        assertEquals(ag.getPL().get("t2").getLabel().getSources().size(), 3);

    }

    public void testConcat() {
        ListTerm l1 = ListTermImpl.parseList("[a,b,c]");
        ListTerm l2 = ListTermImpl.parseList("[d,e,f]");
        ListTerm l3 = ListTermImpl.parseList("[a,b,c,d,e,f]");

        VarTerm X = new VarTerm("X");
        Unifier u = new Unifier();

        try {
            assertTrue(new jason.stdlib.concat().execute(null, u, new Term[] { (Term) l1, (Term) l2, X }));
        } catch (Exception e) {
            e.printStackTrace();
        }
        // System.out.println("u="+u);
        assertEquals(((ListTerm) u.get("X")).size(), 6);
        assertEquals(((ListTerm) u.get("X")), l3);

        try {
            assertTrue(new jason.stdlib.concat().execute(null, new Unifier(), new Term[] { (Term) l1, (Term) l2, (Term) l3 }));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    */
    
    public void testDropGoal1() {
        assertEquals(intention1.size(), 4);
        Trigger g = Trigger.parseTrigger("+!g1");
        assertTrue(intention1.dropGoal(g, new Unifier()));
        assertEquals(intention1.size(), 1);
    }

    public void testDropGoal2() throws JasonException {
        Circumstance c = new Circumstance();
        c.addIntention(intention1);
        TransitionSystem ts = new TransitionSystem(null, c, null, null);
        new dropGoal().drop(ts, Literal.parseLiteral("g2"), true, new Unifier());
        assertEquals(intention1.size(), 1);
        intention1.push(new IntendedMeans(new Option(p4,new Unifier())));
        new dropGoal().drop(ts, Literal.parseLiteral("g4"), true, new Unifier());
        assertTrue(intention1.isFinished());
    }

    public void testDropGoal3() throws JasonException {
        Circumstance c = new Circumstance();
        c.addIntention(intention1);
        TransitionSystem ts = new TransitionSystem(ag, c, null, null);
        new dropGoal().drop(ts, Literal.parseLiteral("g2"), false, new Unifier());
        assertEquals(intention1.size(),2);
        assertEquals(c.getEvents().size(),1);
    }

}
