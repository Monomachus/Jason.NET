package test;

import jason.asSemantics.Unifier;
import jason.asSyntax.ArithExpr;
import jason.asSyntax.ListTerm;
import jason.asSyntax.ListTermImpl;
import jason.asSyntax.Literal;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.NumberTermImpl;
import jason.asSyntax.Pred;
import jason.asSyntax.Term;
import jason.asSyntax.TermImpl;
import jason.asSyntax.VarTerm;
import jason.asSyntax.ArithExpr.ArithmeticOp;

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
        u.unifies(k, new TermImpl("a1"));
        assertTrue("K".equals(k.toString()));
        u.apply(k);
        assertTrue("a1".equals(k.toString()));
        k.addTerm(new TermImpl("p1"));
        k.addTerm(new TermImpl("p2"));
        assertEquals(k.getTermsSize(), 2);

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
        
        u.unifies(x3,new TermImpl("a"));
        assertEquals(u.get(x1).toString(),"a");
        assertEquals(u.get(x2).toString(),"a");
        assertEquals(u.get(x3).toString(),"a");
        assertEquals(u.get(x4).toString(),"a");
        assertEquals(u.get(x5).toString(),"a");
        assertEquals(u.get(x6).toString(),"a");
        assertEquals(u.get(x7).toString(),"a");
        assertEquals(u.get(x8).toString(),"a");
        assertEquals(u.get(x9).toString(),"a");
        
        u.apply(x1);
        assertEquals(x1.toString(),"a");

        // unification with lists
        VarTerm v1 = new VarTerm("L");
        ListTerm lt = ListTermImpl.parseList("[a,B,a(B)]");
        u = new Unifier();
        u.unifies(new VarTerm("B"), new TermImpl("oi"));
        u.unifies(v1, lt);
        u.apply(v1);
        lt = (ListTerm) v1.getValue();
        Iterator i = lt.iterator();
        i.next();
        i.next();
        Term third = (Term) i.next();
        Term toi1 = TermImpl.parse("a(oi)");
        Term toi2 = TermImpl.parse("a(B)");
        u.apply(toi2);
        assertEquals(toi1,toi2);
        assertTrue(third.equals(toi1));
    }

    // test when a var is ground with a Pred
    public void testVarTermAsPred() {
        VarTerm k = new VarTerm("K");
        Unifier u = new Unifier();
        u.unifies(k, new Pred("p"));
        assertFalse(k.isPred());
        u.apply(k);
        assertTrue(k.isPred());
        assertFalse(k.hasAnnot());
        k.addAnnot(new TermImpl("annot1"));
        assertTrue(k.hasAnnot());

        k.addSource(new TermImpl("marcos"));
        assertEquals(k.getAnnots().size(), 2);
        k.delSources();
        assertEquals(k.getAnnots().size(), 1);

        // test with var not ground
        k = new VarTerm("K");
        u = new Unifier();
        u.unifies(k, Pred.parsePred("p[a]"));
        k.addAnnot(new TermImpl("annot1"));
        k.addAnnot(new TermImpl("annot2"));
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
        u.apply(k);
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
        u.apply(nl);
        // System.out.println(nl+ " un="+u);
        assertEquals(nl.size(), 5);

        u.apply((Term) l2);
        assertEquals(l2.size(), 5);
        assertEquals(l2.toString(), "[d,e,a,b,c]");
    }

    // test when a var is ground with a Number
    public void testVarTermAsNumber() {
        VarTerm k = new VarTerm("K");
        Unifier u = new Unifier();
        NumberTermImpl n = new NumberTermImpl(10);
        assertTrue(n.isNumeric());
        assertFalse(n.isVar());
        assertTrue(u.unifies(k, n));
        u.apply(k);
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

    public void testVarWithAnnots() {
        VarTerm v1 = VarTerm.parseVar("X[a,b,c]");
        VarTerm v2 = VarTerm.parseVar("X[a,b]");
        assertTrue(v1.equals(v2));
        v2.addAnnot(new TermImpl("c"));
        assertTrue(v1.equals(v2));
        assertTrue(v2.equals(v1));

        Unifier u = new Unifier();
        Pred p1 = Pred.parsePred("p(t1,t2)[a,c]");
        // X[a,b,c] = p[a,c] nok
        assertFalse(u.unifies(v1, p1));
        assertEquals(p1.toString(),"p(t1,t2)[a,c]");

        // p[a,c] = X[a,b,c] ok (X is p)
        assertTrue(u.unifies(p1, v1));
        assertEquals(p1.toString(),"p(t1,t2)[a,c]");
        assertEquals(u.get("X").toString(), "p(t1,t2)");

        p1.addAnnot(new TermImpl("b"));
        p1.addAnnot(new TermImpl("d"));
        u.clear();
        // p[a,c,b,d] = X[a,b,c] nok
        assertFalse(u.unifies(p1, v1));

        u.clear();
        // X[a,b,c] = p[a,c,b,d] ok (X is p)
        assertTrue(u.unifies(v1, p1));
        assertEquals(u.get("X").toString(), "p(t1,t2)");
    }

    public void testSimple1() {
        Term um = new NumberTermImpl(1);
        Term dois = new NumberTermImpl(2);
        Term exp = ArithExpr.parse("X+1");
        Unifier u = new Unifier();
        u.unifies(new VarTerm("X"), new NumberTermImpl(1));
        // X+1 not unifies with 1
        u.apply(exp);
        assertFalse(u.unifies(exp, um));
        // X+1 unifies with 2
        assertTrue(u.unifies(exp, dois));
    }
    
    public void testUnify1() {
        Term a1 = TermImpl.parse("s(1,2)");
        Term a2 = TermImpl.parse("s(X1,X2)");
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
        u.apply(l);
        //assertEquals(u.get("M").toString(),"op(1)");
        assertEquals(l.toString(),"op(1)");
    }
    
    public void testUnnamedVar1() {
        Term a1 = TermImpl.parse("a(_,_)");
        Term a2 = TermImpl.parse("a(10,20)");
        Term a3 = TermImpl.parse("a(30,40)");
        Unifier u = new Unifier();
        assertTrue(u.unifies(a1,a2));
        assertFalse(u.unifies(a1,a3));
        u.apply(a1);
        assertEquals(a1.toString(), TermImpl.parse("a(10,20)").toString());
    }
    
    public void testUnnamedVar2() {
        Term t1 = TermImpl.parse("a(Y)");
        assertFalse(t1.isGround());
        Term t1c = (Term)t1.clone();
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
        u.apply(v);
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
}
