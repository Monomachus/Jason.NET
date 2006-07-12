package test;

import jason.asSemantics.Agent;
import jason.asSemantics.Unifier;
import jason.asSyntax.Literal;
import jason.asSyntax.LogExprTerm;
import jason.asSyntax.Rule;

import java.util.Iterator;

import junit.framework.TestCase;

/** JUnit test case for syntax package */
public class RuleTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}
 
    public void testLogCons() {
        Agent ag = new Agent();
        ag.getBB().add(Literal.parseLiteral("a(10)"));
        ag.getBB().add(Literal.parseLiteral("a(20)"));
        ag.getBB().add(Literal.parseLiteral("a(30)"));
        ag.getBB().add(Literal.parseLiteral("b(20,10)"));
        ag.getBB().add(Literal.parseLiteral("c(x)"));
        ag.getBB().add(Literal.parseLiteral("c(y)"));
        ag.getBB().add(Literal.parseLiteral("c(20)"));
        
        // add r(X) :- a(X)
        Rule r = new Rule(Literal.parseLiteral("r(X)"), Literal.parseLiteral("a(X)"));
        ag.getBB().add(r);
        
        Iterator<Unifier> iun = Literal.parseLiteral("r(20)").logCons(ag, new Unifier());
        assertEquals(1,iteratorSize(iun));

        iun = Literal.parseLiteral("r(Y)").logCons(ag, new Unifier());
        assertEquals(3,iteratorSize(iun));

        // add v(X) :- a(X) & X > 15 | c(X)
        r = new Rule(Literal.parseLiteral("v(X)"), LogExprTerm.parseExpr("a(X) & X > 15 | c(X)"));
        ag.getBB().add(r);

        iun = Literal.parseLiteral("v(30)").logCons(ag, new Unifier());
        assertEquals(1,iteratorSize(iun));

        iun = Literal.parseLiteral("v(20)").logCons(ag, new Unifier());
        assertEquals(2,iteratorSize(iun));

        iun = Literal.parseLiteral("v(A)").logCons(ag, new Unifier());
        //while (iun.hasNext()) {
        //    System.out.println(iun.next());
        //}
        assertEquals(5,iteratorSize(iun));

        // add s(X) :- r(X)
        r = new Rule(Literal.parseLiteral("s(X)"), LogExprTerm.parseExpr("r(X)"));
        ag.getBB().add(r);
        iun = Literal.parseLiteral("s(X)").logCons(ag, new Unifier());
        assertEquals(3,iteratorSize(iun));
        
        // add t(a) :- s(X)
        r = new Rule(Literal.parseLiteral("t(a)"), LogExprTerm.parseExpr("s(X)"));
        ag.getBB().add(r);
        
        iun = Literal.parseLiteral("t(X)").logCons(ag, new Unifier());
        assertEquals(3,iteratorSize(iun));
    }

    public void testLogCons2() {
        Agent ag = new Agent();
        
        Rule r = new Rule(Literal.parseLiteral("r([],a(X),b(X,4))"), Literal.parseLiteral("true"));
        ag.getBB().add(r);

        Iterator<Unifier> iun = Literal.parseLiteral("r([],a(20),X)").logCons(ag, new Unifier());
        assertEquals(iun.next().get("X").toString(),"b(20,4)");

        iun = Literal.parseLiteral("r([],a(20),b(X,Y))").logCons(ag, new Unifier());
        Unifier u = iun.next();
        assertEquals(u.get("X").toString(),"20");
        assertEquals(u.get("Y").toString(),"4");
    
    }    

    public void testLogConsRec() {
        Agent ag = new Agent();
        
        // add 
        // min([],M,M).
        // min([C|T],V,M) :- C < V & min(T,C,M).
        // min([_|T],V,M) :- min(T,V,M).
        //ag.getBB().add(Literal.parseLiteral("min([],M,M)"));
        ag.getBB().add(new Rule(Literal.parseLiteral("min([],M,M)"), 
                LogExprTerm.parseExpr(".println(fim,M)")));
        ag.getBB().add(new Rule(Literal.parseLiteral("min([op(C)|T], op(V), M)"), 
                LogExprTerm.parseExpr("C < V & min(T,op(C),M)")));
        ag.getBB().add(new Rule(Literal.parseLiteral("min([op(C)|T], op(V), M)"), 
                LogExprTerm.parseExpr("C >= V & min(T,op(V),M)")));

        Iterator<Unifier> iun = Literal.parseLiteral("min([],op(20),op(M))").logCons(ag, new Unifier());
        assertTrue(iun.hasNext());
        Unifier u = iun.next();
        assertEquals(u.get("M").toString(),"20");

        Literal cons = Literal.parseLiteral("min([op(5),op(3),op(8),op(1),op(40)],op(1000),op(M))");
        Iterator<Literal> il = ag.getBB().getRelevant(cons);
        assertEquals(3,iteratorSize(il));
        
        iun = cons.logCons(ag, new Unifier());
        u = iun.next();
        //System.out.println(u);
        assertEquals(u.get("M").toString(),"1");
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
