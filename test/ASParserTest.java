package test;

import static jason.asSyntax.ASSyntax.createAtom;
import static jason.asSyntax.ASSyntax.createLiteral;
import static jason.asSyntax.ASSyntax.createNumber;
import static jason.asSyntax.ASSyntax.createString;
import jason.architecture.AgArch;
import jason.asSemantics.Agent;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.ListTerm;
import jason.asSyntax.Literal;
import jason.asSyntax.LogExpr;
import jason.asSyntax.LogicalFormula;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Plan;
import jason.asSyntax.PlanBody;
import jason.asSyntax.RelExpr;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;
import jason.asSyntax.parser.ParseException;
import jason.asSyntax.parser.as2j;
import jason.infra.centralised.CentralisedAgArch;
import jason.mas2j.MAS2JProject;
import jason.mas2j.parser.mas2j;

import java.io.File;
import java.io.FileInputStream;
import java.io.StringReader;
import java.util.Iterator;

import junit.framework.TestCase;

/** JUnit test case for syntax package */
public class ASParserTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testKQML() {
        Agent ag = new Agent();
        ag.setLogger(null);
        AgArch arch = new AgArch();
        arch.setArchInfraTier(new CentralisedAgArch());
        ag.setTS(new TransitionSystem(ag, null, null, arch));

        assertTrue(ag.parseAS(new File("src/asl/kqmlPlans.asl")));
        assertTrue(ag.parseAS(new File("examples/auction/ag1.asl")));
        Plan p = ag.getPL().get("l__0");
        assertEquals(p.getBody().getPlanSize(), 1);
        assertEquals(((PlanBody)p.getBody()).getBodyType(), PlanBody.BodyType.internalAction);
        assertTrue(ag.parseAS(new File("examples/auction/ag2.asl")));
        assertTrue(ag.parseAS(new File("examples/auction/ag3.asl")));
    }

    public void testLogicalExpr() throws Exception {
        LogicalFormula t1 = LogExpr.parseExpr("(3 + 5) / 2 > 0");
        assertTrue(t1 != null);
        Iterator<Unifier> solve = ((RelExpr) t1).logicalConsequence(null, new Unifier());
        assertTrue(solve.hasNext());

        t1 = LogExpr.parseExpr("0 > ((3 + 5) / 2)");
        assertTrue(t1 != null);
        solve = ((RelExpr) t1).logicalConsequence(null, new Unifier());
        assertFalse(solve.hasNext());

        t1 = LogExpr.parseExpr("0 > (-5)");
        assertTrue(t1 != null);
        solve = ((RelExpr) t1).logicalConsequence(null, new Unifier());
        assertTrue(solve.hasNext());

        t1 = LogExpr.parseExpr("(((((((30) + -5)))))) / 2 > 5+3*8");
        assertTrue(t1 != null);
        solve = ((RelExpr) t1).logicalConsequence(null, new Unifier());
        assertFalse(solve.hasNext());
         
        t1 = LogExpr.parseExpr("-2 > -3");
        assertTrue(t1 != null);
        RelExpr r1 = (RelExpr) t1;
        NumberTerm lt1 = (NumberTerm)r1.getTerm(0);
        NumberTerm rt1 = (NumberTerm)r1.getTerm(1);
        assertEquals(lt1.solve(),-2.0);
        assertEquals(rt1.solve(),-3.0);
        //System.out.println(lt1.getClass().getName()+"="+lt1.compareTo(rt1));
        solve = r1.logicalConsequence(null, new Unifier());
        assertTrue(solve.hasNext());

        t1 = LogExpr.parseExpr("(3 - 5) > (-1 + -2)");
        assertTrue(t1 != null);
        solve = ((RelExpr)t1).logicalConsequence(null, new Unifier());
        assertTrue(solve.hasNext());
        
        t1 = LogExpr.parseExpr("(3 - 5) > -3 & 2 > 1");
        assertTrue(t1 != null);
        solve = ((LogExpr) t1).logicalConsequence(null, new Unifier());
        assertTrue(solve.hasNext());
        
        t1 = LogExpr.parseExpr("(3 - 5) > -3 & 0 > 1");
        assertTrue(t1 != null);
        solve = ((LogExpr) t1).logicalConsequence(null, new Unifier());
        assertFalse(solve.hasNext());

        t1 = LogExpr.parseExpr("(3 - 5) > -3 | 0 > 1");
        assertTrue(t1 != null);
        solve = ((LogExpr) t1).logicalConsequence(null, new Unifier());
        assertTrue(solve.hasNext());

        t1 = LogExpr.parseExpr("(3 > 5) | false");
        assertTrue(t1 != null);
        solve = ((LogExpr) t1).logicalConsequence(null, new Unifier());
        assertFalse(solve.hasNext());

        t1 = LogExpr.parseExpr("(3 > 5) | true");
        assertTrue(t1 != null);
        solve = ((LogExpr) t1).logicalConsequence(null, new Unifier());
        assertTrue(solve.hasNext());

        t1 = LogExpr.parseExpr("not 3 > 5");
        assertTrue(t1 != null);
        solve = ((LogExpr) t1).logicalConsequence(null, new Unifier());
        assertTrue(solve.hasNext());

    }

    public void testDirectives() throws Exception {
        String 
        source =  " b(10). ";
        source += " { begin bc(at(X,Y), ebdg(at(X,Y))) } \n";
        source += "    +!at(X,Y) : b(X) <- go(X,Y). \n";
        source += "    +!at(X,Y) : not b(X) <- go(3,Y). \n";
        source += " { end }";


        as2j parser = new as2j(new StringReader(source));
        Agent a = new Agent();
        parser.agent(a);
        assertEquals(a.getPL().getPlans().size(), 7);

        source =  " { begin omc(at(X,Y), no_battery, no_beer) } \n";
        source += "    +!at(X,Y) : b(X) <- go(X,Y). ";
        source += "    +!at(X,Y) : not b(X) <- go(3,Y). ";
        source += " { end }";
        parser = new as2j(new StringReader(source));
        a = new Agent();
        a.setASLSrc("test1");
        parser.agent(a);
        assertTrue(a.getPL().getPlans().size() == 8);
        
        source =  " { begin mg(at(10,10)) } \n";
        source += "    +!at(X,Y) : b(X) <- go(X,Y). ";
        source += "    +!at(X,Y) : not b(X) <- go(3,Y). ";
        source += " { end }";
        parser = new as2j(new StringReader(source));
        a = new Agent();
        parser.agent(a);
        //for (Plan p: a.getPL().getPlans()) {
        //    System.out.println(p);
        //}
        assertTrue(a.getPL().getPlans().size() == 7);
        assertTrue(a.getInitialBels().size() == 1);

        source =  " { begin sga(\"+go(X,Y)\", \"(at(home) & not c)\", at(X,Y)) } \n";
        source += " { end }";
        parser = new as2j(new StringReader(source));
        a = new Agent();
        parser.agent(a);
        //for (Plan p: a.getPL().getPlans()) {
        //    System.out.println(p);
        //}
        assertTrue(a.getPL().getPlans().size() == 5);
        
    }
    
    public void testParsingPlanBodyTerm1() throws ParseException {
        Literal l = Literal.parseLiteral("p( {a1(f);a2}, a3, {!g}, {?b;.print(oi) }, 10)");
        assertEquals("p({ a1(f); a2 },a3,{ !g },{ ?b; .print(oi) },10)", l.toString());
        assertEquals(5,l.getArity());
        assertTrue(l.getTerm(0) instanceof PlanBody);
        assertTrue(l.getTerm(0).isPlanBody());
        PlanBody pb = (PlanBody)l.getTerm(0);
        assertTrue(pb.isBodyTerm());

        assertFalse(l.getTerm(1).isPlanBody());
        assertTrue(l.getTerm(2).isPlanBody());
        assertTrue(l.getTerm(3).isPlanBody());
        assertFalse(l.getTerm(4).isPlanBody());
    }
    
    public void testParsingPlanBodyTerm2() throws ParseException {
        Unifier un = new Unifier();
        Term t = ASSyntax.parseTerm("{ +a(10) }");
        assertTrue(t.isPlanBody());
        assertEquals("{ +a(10) }", t.toString());
        
        t = ASSyntax.parseTerm("{ -a; +b }");
        assertEquals("{ -a; +b }", t.toString());
        assertTrue(t.isPlanBody());
        PlanBody pb = (PlanBody)t;
        assertEquals(2, pb.getPlanSize());

        t = ASSyntax.parseTerm("{ -a : b <- c1; c2 }");
        assertEquals("{ -a : b <- c1; c2 }", t.toString());

        t = ASSyntax.parseTerm("{ +!a(10) }");
        assertEquals("{ +!a(10) }", t.toString());
        assertTrue(t.isStructure());
        Structure s = (Structure)t;
        assertEquals(2, s.getArity());
        assertEquals("te", s.getFunctor());
        Term te = ASSyntax.parseTerm("{ +!a(X) }");
        assertTrue(un.unifies(t,te));

        t = ASSyntax.parseTerm("{ !a }");
        assertTrue(t.isPlanBody());
        pb = (PlanBody)t;
        assertEquals(1, pb.getPlanSize());
        assertEquals("a", pb.getBodyTerm().toString());
        assertEquals(PlanBody.BodyType.achieve, pb.getBodyType());
        
        t = ASSyntax.parseTerm("{ +!a <- +b }");
        assertEquals("{ +!a <- +b }", t.toString());
        assertTrue(t.isStructure());
        s = (Structure)t;
        assertEquals(4, s.getArity());
        assertEquals("plan", s.getFunctor());

        t = ASSyntax.parseTerm("{ +a <- +c }");
        assertEquals("{ +a <- +c }", t.toString());
        assertTrue(t.isStructure());
        s = (Structure)t;
        assertEquals(4, s.getArity());
        assertEquals("plan", s.getFunctor());
        assertEquals(Literal.LTrue, s.getTerm(2));
    }    

    public void testParsingAllSources() {
        parseDir(new File("./examples"));
        parseDir(new File("./demos"));
        parseDir(new File("./applications/jason-moise"));
        parseDir(new File("./applications/jason-team"));
        parseDir(new File("./doc/mini-tutorial"));        
        parseDir(new File("../Jason-applications/examples-site-jBook"));
        parseDir(new File("../Jason-applications/Tests"));
    }
    
    public void parseDir(File dir) {
        if (!dir.exists())
            return;
        if (dir.getName().endsWith(".svn"))
            return;
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f: files) {
                try {
                    if (f.isDirectory()) {
                        parseDir(f);
                    } else if (f.getName().endsWith(MAS2JProject.AS_EXT)) {
                        as2j parser = new as2j(new FileInputStream(f));
                        parser.agent((Agent)null);
                    } else if (f.getName().endsWith(MAS2JProject.EXT)) {
                        mas2j parser = new mas2j(new FileInputStream(f));
                        parser.mas();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    fail("Error parsing "+f+": "+e);
                }
            }
        }
    }
    
    public void testFactory() throws ParseException {
        // create the literal 'p'
        Literal l = createLiteral("p"); 
        assertEquals("p", l.toString());
        
        // create the literal 'p(a,3)'
        l = createLiteral("p", createAtom("a"), createNumber(3)); 
        assertEquals("p(a,3)", l.toString());
             
        // create the literal 'p(a,3)[s,"s"]'
        l = createLiteral("p", createAtom("a"), createNumber(3))
                             .addAnnots(createAtom("s"), createString("s"));
        assertEquals("p(a,3)[s,\"s\"]", l.toString());
             
        // create the literal '~p(a,3)[s,"s"]'
        l = createLiteral(Literal.LNeg, "p", createAtom("a"), createNumber(3))
                             .addAnnots(createAtom("s"), createString("s"));
        assertEquals("~p(a,3)[s,\"s\"]", l.toString());
        l = ASSyntax.parseLiteral(l.toString());
        assertEquals("~p(a,3)[s,\"s\"]", l.toString());
        
        ListTerm   ll = ASSyntax.createList(); // empty list
        assertEquals("[]", ll.toString());
        
        ll = ASSyntax.createList(createAtom("a"), createLiteral("b", createNumber(5))); 
        assertEquals("[a,b(5)]", ll.toString());
        
        ll = ASSyntax.parseList(ll.toString());
        assertEquals("[a,b(5)]", ll.toString());
    }
    
}
