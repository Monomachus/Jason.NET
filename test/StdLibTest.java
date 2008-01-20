package test;

import jason.architecture.AgArch;
import jason.asSemantics.Agent;
import jason.asSemantics.Circumstance;
import jason.asSemantics.IntendedMeans;
import jason.asSemantics.Intention;
import jason.asSemantics.Option;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.DefaultTerm;
import jason.asSyntax.ListTerm;
import jason.asSyntax.ListTermImpl;
import jason.asSyntax.Literal;
import jason.asSyntax.NumberTermImpl;
import jason.asSyntax.Plan;
import jason.asSyntax.Pred;
import jason.asSyntax.StringTerm;
import jason.asSyntax.StringTermImpl;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;
import jason.asSyntax.Trigger;
import jason.asSyntax.VarTerm;
import jason.bb.BeliefBase;
import jason.infra.centralised.CentralisedAgArch;
import jason.stdlib.add_annot;
import jason.stdlib.add_plan;
import jason.stdlib.fail_goal;
import jason.stdlib.relevant_plans;
import jason.stdlib.remove_plan;
import jason.stdlib.succeed_goal;

import java.util.Iterator;

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
        intention1.push(new IntendedMeans(new Option(p,new Unifier()), null));
        
        p = Plan.parse("+!g1 : true <- !g2.");
        intention1.push(new IntendedMeans(new Option(p,new Unifier()), null));

        p = Plan.parse("+!g2 : true <- !g4; f;g.");
        intention1.push(new IntendedMeans(new Option(p,new Unifier()), null));
        
        p4 = Plan.parse("+!g4 : true <- h.");
        intention1.push(new IntendedMeans(new Option(p4,new Unifier()), null));

        p5 = Plan.parse("+!g5 : true <- i.");
        
        ag = new Agent();
        ag.getPL().add(Plan.parse("-!g1 : true <- j."));
    }

    public void testAddAnnot() {
        add_annot aa = new add_annot();
        Unifier u = new Unifier();

        Literal msg = Literal.parseLiteral("ok(10)");
        VarTerm X = new VarTerm("X");
        Term annot = DefaultTerm.parse("source(jomi)");
        try {
            aa.execute(null, u, new Term[] { msg, annot, X });
        } catch (Exception e) {
            e.printStackTrace();
        }
        // System.out.println("u="+u);
        assertEquals(msg.toString(), "ok(10)");
        assertTrue(((Pred) u.get("X")).hasAnnot(annot));

        // testing addAnnot with list
        ListTerm msgL = (ListTerm) DefaultTerm.parse("[ok(10),[ok(20),ok(30),[ok(40)|[ok(50),ok(60)]]]]");
        VarTerm Y = new VarTerm("Y");
        Term annotL = DefaultTerm.parse("source(rafa)");
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
        AgArch arch = new AgArch();
        arch.setArchInfraTier(new CentralisedAgArch());
        ag.setTS(new TransitionSystem(ag, null, null, arch));
        
        Literal l1 = Literal.parseLiteral("a(10,x)");
        assertFalse(l1.hasSource());
        ag.addBel(l1);
        ag.addBel(Literal.parseLiteral("a(20,y)"));
        ag.addBel(Literal.parseLiteral("a(30,x)"));
        assertEquals(ag.getBB().size(),3);
        
        Unifier u = new Unifier();
        Term X = DefaultTerm.parse("f(X)");
        Literal c = Literal.parseLiteral("a(X,x)");
        c.addAnnot(BeliefBase.TSelf);
        VarTerm L = new VarTerm("L");
        // System.out.println(ag.getPS().getAllRelevant(Trigger.parseTrigger(ste.getFunctor())));
        try {
            assertTrue((Boolean)new jason.stdlib.findall().execute(ag.getTS(), u, new Term[] { X, c, L }));
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

        ag.getPL().add(new StringTermImpl("@t2 +a : g(20) <- .print(\"ok 20\")."), new Structure("nosource"));
        ((Plan) ag.getPL().getPlans().get(1)).getLabel().addSource(new Structure("ag1"));
        ag.getPL().add(new StringTermImpl("@t3 +b : true <- true."), null);
        //System.out.println(ag.getPL());
        TransitionSystem ts = new TransitionSystem(ag, null, null, null);

        Unifier u = new Unifier();
        StringTerm ste = new StringTermImpl("+a");
        VarTerm X = new VarTerm("X");
        //System.out.println(ag.getPL().getAllRelevant(Trigger.parseTrigger(ste.getFunctor()).getPredicateIndicator()));
        try {
            new relevant_plans().execute(ts, u, new Term[] { (Term) ste, X });
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
            new remove_plan().execute(ts, new Unifier(), new Term[] { new Pred("t1") });
        } catch (Exception e) {
            e.printStackTrace();
        }
        // ag.getPS().remove(0);
        assertEquals(ag.getPL().getPlans().size(), 2);

        // add plans returned from getRelevantPlans
        // using IA addPlan
        Iterator<Term> i = plans.iterator();
        try {
            while (i.hasNext()) {
                StringTerm t = (StringTerm) i.next();
                new add_plan().execute(ts, new Unifier(), new Term[] { t, new Structure("fromGR") });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // add again plans returned from getRelevantPlans
        // using IA addPlan receiving a list of plans
        try {
            new add_plan().execute(ts, new Unifier(), new Term[] { (Term) plans, new Structure("fromLT") });
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
            assertTrue((Boolean)new remove_plan().execute(ts, new Unifier(), new Term[] { (Term) llt, new Pred("nosource") }));
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(ag.getPL().getPlans().size(), 3);

        // remove plan t2,t3 (source = self) from PS
        llt = ListTermImpl.parseList("[t2,t3]");
        try {
            assertTrue((Boolean)new remove_plan().execute(ts, new Unifier(), new Term[] { (Term) llt }));
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(ag.getPL().getPlans().size(), 2);

        // the plan t2 (first plan now) must have 3 sources
        assertEquals(ag.getPL().get("t2").getLabel().getSources().size(), 3);

    }

    public static void main(String[] a) {
        new StdLibTest().testSubString();
    }

    public void testConcat() {
        ListTerm l1 = ListTermImpl.parseList("[a,b,c]");
        ListTerm l2 = ListTermImpl.parseList("[d,e,f]");
        ListTerm l3 = ListTermImpl.parseList("[a,b,c,d,e,f]");

        VarTerm X = new VarTerm("X");
        Unifier u = new Unifier();

        try {
            assertTrue((Boolean)new jason.stdlib.concat().execute(null, u, new Term[] { l1, l2, X }));
        } catch (Exception e) {
            e.printStackTrace();
        }
        // System.out.println("u="+u);
        assertEquals(((ListTerm) u.get("X")).size(), 6);
        assertEquals(((ListTerm) u.get("X")), l3);

        l1 = ListTermImpl.parseList("[a,b,c]");
        l2 = ListTermImpl.parseList("[d,e,f]");
        l3 = ListTermImpl.parseList("[a,b,c,d,e,f]");

        try {
            assertTrue((Boolean)new jason.stdlib.concat().execute(null, new Unifier(), new Term[] { l1, l2, l3 }));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
	public void testSubString() {
        StringTerm s1 = new StringTermImpl("a");
        StringTerm s2 = new StringTermImpl("bbacca");

        Term t1 = DefaultTerm.parse("a(10)");
        Term t2 = DefaultTerm.parse("[1,b(xxx,a(10))]");

        VarTerm X = new VarTerm("X");

        Unifier u = new Unifier();
        try {
            assertTrue((Boolean)new jason.stdlib.substring().execute(null, u, new Term[] { s1, s2 }));
            Iterator<Unifier> i = (Iterator)new jason.stdlib.substring().execute(null, u, new Term[] { s1, s2 , X});
            assertEquals(i.next().get("X").toString(), "2");
            assertEquals(i.next().get("X").toString(), "5");
            assertFalse(i.hasNext());

            assertTrue((Boolean)new jason.stdlib.substring().execute(null, u, new Term[] { t1, t2}));
            i = (Iterator)new jason.stdlib.substring().execute(null, new Unifier(), new Term[] { t1, t2, X});
            assertTrue(i.hasNext());
            assertEquals(i.next().get("X").toString(), "9");
            assertFalse(i.hasNext());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void testDropGoal1() {
        assertEquals(intention1.size(), 4);
        Trigger g = Trigger.parseTrigger("+!g1");
        assertTrue(intention1.dropGoal(g, new Unifier()));
        assertEquals(intention1.size(), 1);
    }

    public void testDropGoal2() throws Exception {
        Circumstance c = new Circumstance();
        c.addIntention(intention1);
        TransitionSystem ts = new TransitionSystem(null, c, null, null);
        new succeed_goal().drop(ts, Literal.parseLiteral("g2"), new Unifier());
        assertEquals(intention1.size(), 1);
        intention1.push(new IntendedMeans(new Option(p4,new Unifier()), null));
        new succeed_goal().drop(ts, Literal.parseLiteral("g4"), new Unifier());
        assertTrue(intention1.isFinished());
    }

    public void testDropGoal3() throws Exception {
        Circumstance c = new Circumstance();
        c.addIntention(intention1);
        TransitionSystem ts = new TransitionSystem(ag, c, null, null);
        new fail_goal().drop(ts, Literal.parseLiteral("g2"), new Unifier());
        assertEquals(intention1.size(),2);
        assertEquals(c.getEvents().size(),1);
    }
    
    @SuppressWarnings("unchecked")
    public void testMember() throws Exception {
        ListTerm l1 = ListTermImpl.parseList("[a,b,c]");
        Term ta = DefaultTerm.parse("a");
        Term td = DefaultTerm.parse("d");
        
        // test member(a,[a,b,c])
        Unifier u = new Unifier();
        Iterator<Unifier> i = (Iterator<Unifier>)new jason.stdlib.member().execute(null, u, new Term[] { ta, l1});
        assertTrue(i != null);
        assertTrue(i.hasNext());
        assertTrue(i.next().size() == 0);

        // test member(d,[a,b,c])
        u = new Unifier();
        i = (Iterator<Unifier>)new jason.stdlib.member().execute(null, u, new Term[] { td, l1});
        assertFalse(i.hasNext());

        // test member(b(X),[a(1),b(2),c(3)])
        Term l2 = DefaultTerm.parse("[a(1),b(2),c(3)]");
        Term tb = DefaultTerm.parse("b(X)");
        u = new Unifier();
        i = (Iterator<Unifier>)new jason.stdlib.member().execute(null, u, new Term[] { tb, l2});
        assertTrue(i != null);
        assertTrue(i.hasNext());
        Unifier ru = i.next();
        assertTrue(u.size() == 0); // u should not be changed!
        assertTrue(ru.size() == 1);
        assertEquals(ru.get("X").toString(), "2");
        
        // test member(X,[a,b,c])
        Term tx = DefaultTerm.parse("X");
        u = new Unifier();
        i = (Iterator<Unifier>)new jason.stdlib.member().execute(null, u, new Term[] { tx, l1});
        assertTrue(iteratorSize(i) == 3);
        i = (Iterator<Unifier>)new jason.stdlib.member().execute(null, u, new Term[] { tx, l1});
        assertEquals(i.next().get("X").toString(),"a");
        assertEquals(i.next().get("X").toString(),"b");
        assertEquals(i.next().get("X").toString(),"c");
        assertFalse(i.hasNext());

        // test member(b(X),[a(1),b(2),c(3),b(4)])
        l2 = DefaultTerm.parse("[a(1),b(2),c(3),b(4)]");
        u = new Unifier();
        i = (Iterator<Unifier>)new jason.stdlib.member().execute(null, u, new Term[] { tb, l2});
        assertTrue(i != null);
        assertTrue(iteratorSize(i) == 2);
        i = (Iterator<Unifier>)new jason.stdlib.member().execute(null, u, new Term[] { tb, l2});
        assertEquals(i.next().get("X").toString(),"2");
        assertEquals(i.next().get("X").toString(),"4");
    }

    public void testDelete() throws Exception {
        ListTerm l1 = ListTermImpl.parseList("[a,b,a,c,a]");
        Term ta = DefaultTerm.parse("a");
        VarTerm v = new VarTerm("X");
        
        // test delete(a,[a,b,a,c,a])
        Unifier u = new Unifier();
        assertTrue((Boolean)new jason.stdlib.delete().execute(null, u, new Term[] { ta, l1, v}));
        assertEquals("[b,c]",u.get("X").toString());
        
        // test delete(3,[a,b,a,c,a])
        u = new Unifier();
        assertTrue((Boolean)new jason.stdlib.delete().execute(null, u, new Term[] { new NumberTermImpl(3), l1, v}));
        assertEquals("[a,b,a,a]",u.get("X").toString());

        // test delete(3,"abaca")
        u = new Unifier();
        assertTrue((Boolean)new jason.stdlib.delete().execute(null, u, new Term[] { new NumberTermImpl(3), new StringTermImpl("abaca"), v}));
        assertEquals("\"abaa\"",u.get("X").toString());

        // test delete("a","abaca")
        u = new Unifier();
        assertTrue((Boolean)new jason.stdlib.delete().execute(null, u, new Term[] { new StringTermImpl("a"), new StringTermImpl("abaca"), v}));
        assertEquals("\"bc\"",u.get("X").toString());
    }

    @SuppressWarnings("unchecked")
	private int iteratorSize(Iterator i) {
        int c = 0;
        while (i.hasNext()) {
            i.next();
            c++;
        }
        return c;
    }

}
