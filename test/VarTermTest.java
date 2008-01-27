package test;

import jason.asSemantics.Unifier;
import jason.asSyntax.ArithExpr;
import jason.asSyntax.DefaultTerm;
import jason.asSyntax.ListTerm;
import jason.asSyntax.ListTermImpl;
import jason.asSyntax.Literal;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.NumberTermImpl;
import jason.asSyntax.Pred;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;
import jason.asSyntax.VarTerm;
import jason.asSyntax.ArithExpr.ArithmeticOp;
import jason.asSyntax.parser.SimpleCharStream;
import jason.asSyntax.parser.Token;
import jason.asSyntax.parser.as2jTokenManager;

import java.io.StringReader;
import java.util.Iterator;

import junit.framework.TestCase;

/** JUnit test case for syntax package */
public class VarTermTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }

    /** test when a var is ground with a Term or another var */
    public void testVarTermAsTerm() {
        VarTerm k = new VarTerm("K");
        Unifier u = new Unifier();
        u.unifies(k, new Structure("a1"));
        assertTrue("K".equals(k.toString()));
        k.apply(u);
        assertTrue("a1".equals(k.toString()));
        k.addTerm(new Structure("p1"));
        k.addTerm(new Structure("p2"));
        assertEquals(k.getArity(), 2);

        VarTerm x1 = new VarTerm("X1");
        VarTerm x2 = new VarTerm("X2");
        VarTerm x3 = new VarTerm("X3");
        VarTerm x4 = new VarTerm("X4");
        VarTerm x5 = new VarTerm("X5");
        VarTerm x6 = new VarTerm("X6");

        VarTerm x7 = new VarTerm("X7");
        VarTerm x8 = new VarTerm("X8");
        VarTerm x9 = new VarTerm("X9");

        u = new Unifier();
        u.unifies(x1,x2);
        u.unifies(x3,x4);
        u.unifies(x4,x5);
        u.unifies(x2,x3);
        u.unifies(x1,x6);

        u.unifies(x7,x8);
        u.unifies(x9,x8);

        u.unifies(x7,x4);
        
        u.unifies(x3,new Structure("a"));
        assertEquals(u.get(x1).toString(),"a");
        assertEquals(u.get(x2).toString(),"a");
        assertEquals(u.get(x3).toString(),"a");
        assertEquals(u.get(x4).toString(),"a");
        assertEquals(u.get(x5).toString(),"a");
        assertEquals(u.get(x6).toString(),"a");
        assertEquals(u.get(x7).toString(),"a");
        assertEquals(u.get(x8).toString(),"a");
        assertEquals(u.get(x9).toString(),"a");
        
        x1.apply(u);
        assertEquals(x1.toString(),"a");

        // unification with lists
        VarTerm v1 = new VarTerm("L");
        ListTerm lt = ListTermImpl.parseList("[a,B,a(B)]");
        u = new Unifier();
        u.unifies(new VarTerm("B"), new Structure("oi"));
        u.unifies(v1, lt); // L = [a,B,a(B)]
        v1.apply(u);      
        ListTerm vlt = (ListTerm) v1.getValue();
        assertFalse(vlt.equals(lt)); // the apply in var should not change the orginal list
        Iterator<Term> i = vlt.iterator();
        i.next();
        i.next();
        Term third = i.next();
        Term toi1 = DefaultTerm.parse("a(oi)");
        Term toi2 = DefaultTerm.parse("a(B)");
        toi2.apply(u);
        assertEquals(toi1,toi2);
        assertTrue(third.equals(toi1));
    }

    // test when a var is ground with a Pred
    public void testVarTermAsPred() {
        VarTerm k = new VarTerm("K");
        Unifier u = new Unifier();
        u.unifies(k, new Pred("p"));
        assertFalse(k.isPred());
        k.apply(u);
        assertTrue(k.isPred());
        assertFalse(k.hasAnnot());
        k.addAnnot(new Structure("annot1"));
        assertTrue(k.hasAnnot());

        k.addSource(new Structure("marcos"));
        assertEquals(k.getAnnots().size(), 2);
        k.delSources();
        assertEquals(k.getAnnots().size(), 1);

        // test with var not ground
        k = new VarTerm("K");
        u = new Unifier();
        u.unifies(k, Pred.parsePred("p[a]"));
        k.addAnnot(new Structure("annot1"));
        k.addAnnot(new Structure("annot2"));
        assertEquals(k.getAnnots().size(), 2);
    }

    // test when a var is ground with a Literal
    public void testVarTermAsLiteral() {
        VarTerm k = new VarTerm("K");
        Unifier u = new Unifier();
        assertTrue(k.isVar());
        Literal l = Literal.parseLiteral("~p(a1,a2)[n1,n2]");
        assertTrue(l.isLiteral());
        assertTrue(l.isPred());
        assertTrue(l.negated());
        assertTrue(u.unifies(k, l));
        assertFalse(k.isLiteral());
        k.apply(u);
        // System.out.println(k+" u="+u);
        assertFalse(k.isVar());
        assertTrue(k.isLiteral());
        assertTrue(k.negated());
    }

    // test when a var is ground with a List 
    public void testVarTermAsList() {
        VarTerm k = new VarTerm("K");
        Unifier u = new Unifier();
        Term l1 = (Term) ListTermImpl.parseList("[a,b,c]");
        assertTrue(l1.isList());
        assertTrue(u.unifies(k, l1));
        assertFalse(k.isList());
        // u.apply(k);
        // assertTrue(k.isList());
        // assertEquals(k.size(),3);

        ListTerm l2 = ListTermImpl.parseList("[d,e|K]");
        // System.out.println("l2="+l2);
        VarTerm nl = new VarTerm("NK");
        u.unifies(nl, (Term) l2);
        nl.apply(u);
        // System.out.println(nl+ " un="+u);
        assertEquals(nl.size(), 5);

        l2.apply(u);
        assertEquals(l2.size(), 5);
        assertEquals(l2.toString(), "[d,e,a,b,c]");
    }

    // test when a var is ground with a Number
    public void testVarTermAsNumber() throws Exception {
        VarTerm k = new VarTerm("K");
        Unifier u = new Unifier();
        NumberTermImpl n = new NumberTermImpl(10);
        assertTrue(n.isNumeric());
        assertFalse(n.isVar());
        assertTrue(u.unifies(k, n));
        k.apply(u);
        // System.out.println(k+" u="+u);
        assertTrue(k.isNumeric());
        assertFalse(k.isLiteral());

        ArithExpr exp = new ArithExpr(k, ArithmeticOp.plus, new NumberTermImpl(20));
        assertTrue(exp.solve() == 30d);
        NumberTerm nt = ArithExpr.parseExpr("5 div 2");
        assertTrue(nt.solve() == 2d);
        nt = ArithExpr.parseExpr("5 mod 2");
        assertTrue(nt.solve() == 1d);
    }

    public void testUnify() {
        // var with literal
        VarTerm k = new VarTerm("K");
        Literal l1 = Literal.parseLiteral("~p(a1,a2)[n1,n2]");
        Unifier u = new Unifier();
        assertTrue(u.unifies(k, l1));
        assertTrue(k.isVar());
        assertTrue(u.unifies(l1, k));

        k = new VarTerm("K");
        Literal l2 = Literal.parseLiteral("p(a1,a2)[n1,n2]");
        u = new Unifier();
        assertTrue(u.unifies(k, l1));
        // System.out.println(k+" - "+u);
        assertFalse(u.unifies(l2, k));

        Literal l3 = Literal.parseLiteral("~p(X,Y)[A1]");
        VarTerm k2 = new VarTerm("K");
        u = new Unifier();
        assertTrue(u.unifies(k2, l3));
        assertTrue(u.unifies(k2, l1));

        VarTerm v1 = VarTerm.parseVar("Y[b(2)]");
        VarTerm v2 = VarTerm.parseVar("X");
        u.clear();
        u.unifies(v2, Pred.parsePred("a(4)[b(2)]"));
        u.unifies(v1, v2);
        VarTerm vy = new VarTerm("Y");
        // Y[b(2)] = Y
        assertEquals(v1.hashCode(),vy.hashCode());
    }

    public void testVarWithAnnots1() {
        VarTerm v1 = VarTerm.parseVar("X[a,b,c]");
        VarTerm v2 = VarTerm.parseVar("X[a,b]");
        assertTrue(v1.equals(v2));
        v2.addAnnot(new Structure("c"));
        assertTrue(v1.equals(v2));
        assertTrue(v2.equals(v1));

        Unifier u = new Unifier();
        Pred p1 = Pred.parsePred("p(t1,t2)[a,c]");
        // X[a,b,c] = p[a,c] nok
        assertFalse(u.unifies(v1, p1));
        assertEquals("p(t1,t2)[a,c]",p1.toString());

        // p[a,c] = X[a,b,c] ok (X is p)
        assertTrue(u.unifies(p1, v1));
        assertEquals("p(t1,t2)[a,c]",p1.toString());
        assertEquals(u.get("X").toString(), "p(t1,t2)");

        p1.addAnnot(new Structure("b"));
        p1.addAnnot(new Structure("d"));
        u.clear();
        // p[a,c,b,d] = X[a,b,c] nok
        assertFalse(u.unifies(p1, v1));

        u.clear();
        // X[a,b,c] = p[a,c,b,d] ok (X is p)
        assertTrue(u.unifies(v1, p1));
        assertEquals(u.get("X").toString(), "p(t1,t2)");
    }

    public void testVarWithAnnots2() {
    	// test vars annots
    	
    	// X[a] = Y[a,b] - ok
    	VarTerm v1 = VarTerm.parseVar("X[a]");
    	VarTerm v2 = VarTerm.parseVar("Y[a,b]");
    	Unifier u = new Unifier();
    	assertTrue(u.unifies(v1, v2));

    	// X[a,b] = Y[a] - not ok
    	u = new Unifier();
    	assertFalse(u.unifies(v2, v1));
    }

    public void testVarWithAnnots3() {
    	// X[a,b,c,d] = Y[a,c|R] - ok and R=[b,d]
    	VarTerm v1 = VarTerm.parseVar("X[a,b,c,d]");
    	VarTerm v2 = VarTerm.parseVar("Y[a,c|R]");
    	Unifier u = new Unifier();
    	assertTrue(u.unifies(v1, v2));
        assertEquals(u.get("R").toString(),"[b,d]");
    }
    
    public void testVarWithAnnots4() {
    	// X[source(A)] = open[source(a)] - ok and A -> a
    	VarTerm v1 = VarTerm.parseVar("X[source(A)]");
    	Unifier u = new Unifier();
    	assertTrue(u.unifies(v1, Literal.parseLiteral("open[source(a)]")));
    	assertEquals(u.get("A").toString(),"a");
    	assertEquals(u.get("X").toString(),"open");

    	VarTerm v2 = VarTerm.parseVar("X[source(self)]");
    	u = new Unifier();
    	assertFalse(u.unifies(v2, Literal.parseLiteral("open[source(a)]")));
    }

    public void testVarWithAnnots5() {
    	// P -> open[source(a)]
    	// P[source(self)]
    	// apply on P is open[source(a),source(self)]?
    	Unifier u = new Unifier();
    	u.unifies(new VarTerm("P"), Literal.parseLiteral("open[source(a)]"));
    	VarTerm v1 = VarTerm.parseVar("P[source(self)]");
    	v1.apply(u);
    	assertEquals(v1.getAnnots().size(), 2);
    }


    public static void main(String[] a) {
        new VarTermTest().testVarWithAnnots3();
    }

    public void testSimple1() {
        Term um = new NumberTermImpl(1);
        Term dois = new NumberTermImpl(2);
        Term exp = ArithExpr.parse("X+1");
        Unifier u = new Unifier();
        u.unifies(new VarTerm("X"), new NumberTermImpl(1));
        // X+1 not unifies with 1
        exp.apply(u);
        assertFalse(u.unifies(exp, um));
        // X+1 unifies with 2
        assertTrue(u.unifies(exp, dois));
    }
    
    public void testSimple2() {
    	VarTerm v = new VarTerm("X");
    	assertFalse(v.isAtom());
    	assertTrue(v.isVar());
    	Term t;

    	as2jTokenManager tokens = new as2jTokenManager(new SimpleCharStream(new StringReader("Event")));
    	Token tk = tokens.getNextToken();
    	assertEquals(tk.kind, jason.asSyntax.parser.as2jConstants.VAR);
    	
    	t = VarTerm.parseVar("Ea");
    	assertFalse(t.isAtom());
    	assertTrue(t.isVar());
    	
    	t = DefaultTerm.parse("Event");
    	assertFalse(t.isAtom());
    	assertTrue(t.isVar());
    	
    }
    
    public void testUnify1() {
        Term a1 = DefaultTerm.parse("s(1,2)");
        Term a2 = DefaultTerm.parse("s(X1,X2)");
        Unifier u = new Unifier();
        assertTrue(u.unifies(new VarTerm("X1"),new VarTerm("X3")));
        assertTrue(u.unifies(a1,a2));
        assertEquals(u.get("X3").toString(),"1");
    }
    
    public void testInnerVarUnif() {
        Unifier u = new Unifier();
        Literal l = Literal.parseLiteral("op(X)");
        u.unifies(new VarTerm("M"), l);
        u.unifies(new VarTerm("M"), Literal.parseLiteral("op(1)"));
        l.apply(u);
        //assertEquals(u.get("M").toString(),"op(1)");
        assertEquals(l.toString(),"op(1)");
    }
    
    public void testUnnamedVar1() {
        Term a1 = DefaultTerm.parse("a(_,_)");
        Term a2 = DefaultTerm.parse("a(10,20)");
        Term a3 = DefaultTerm.parse("a(30,40)");
        Unifier u = new Unifier();
        assertTrue(u.unifies(a1,a2));
        assertFalse(u.unifies(a1,a3));
        a1.apply(u);
        assertEquals(a1.toString(), DefaultTerm.parse("a(10,20)").toString());
    }
    
    public void testUnnamedVar2() {
        Structure t1 = Structure.parse("a(Y)");
        assertFalse(t1.isGround());
        Structure t1c = (Structure)t1.clone();
        assertFalse(t1c.isGround());
        t1c.makeVarsAnnon();
        assertFalse(t1c.isGround());
        Term t1cc = (Term)t1c.clone();
        assertFalse(t1cc.isGround());
        
        Unifier u = new Unifier();
        VarTerm v = new VarTerm("X");
        assertTrue(v.isVar());
        u.unifies(v, t1cc);
        assertTrue(v.isVar());
        assertFalse(v.isGround());
        v.apply(u);
        assertFalse(v.isVar());
        assertFalse(v.isGround());        
    }
    
    public void testUnifClone() {
        VarTerm x1 = new VarTerm("X");
        VarTerm x2 = new VarTerm("X");
        assertEquals(x1,x2);
        
        Unifier u1 = new Unifier();
        u1.unifies(x1, new VarTerm("Y"));
        u1.unifies(x2, new VarTerm("Z"));
        Unifier u2 = (Unifier)u1.clone();
        Object o1 = u1.get("X");
        Object o2 = u2.get("X");
        assertEquals(o1,o2);
        
        assertEquals(u1,u2);
    }
    
    public void testApply() {
        VarTerm x = new VarTerm("X");
        VarTerm y = new VarTerm("Y");
        
        Unifier u = new Unifier();
        
        // X = Y 
        u.unifies(y, x);

        x = (VarTerm)x.clone();

        // Y = 10
        u.unifies(y, new NumberTermImpl(10));
        
        x.apply(u);
        assertEquals(x.toString(), "10");
    }
}
