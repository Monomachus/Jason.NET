package jason.asSyntax;

import jason.asSyntax.parser.ParseException;
import jason.asSyntax.parser.as2j;

import java.io.StringReader;

/**
  Factory for objects used in Jason AgentSpeak syntax.
  
  <p><b>Examples of Term</b>:
  <pre> 
  import static jason.asSyntax.ASSyntax.*;

  ...
  Atom       a = createAtom("a"); 
  NumberTerm n = createNumber(3);
  StringTerm s = createString("s");
  Structure  t = createStructure("p", createAtom("a")); // t = p(a) 
  ListTerm   l = createList(); // empty list
  ListTerm   f = createList(createAtom("a"), createStructure("b", createNumber(5))); // f = [a,b(5)]
  
  // or use a parsing (easier but slow)
  Term n = parseTerm("5");
  Term t = parseTerm("p(a)");
  Term l = parseTerm("[a,b(5)]");
  </pre> 
  
  <p><b>Examples of Literal</b>:
  <pre> 
  import static jason.asSyntax.ASSyntax.*;

  ...
  // create the literal 'p'
  Literal l1 = createLiteral("p"); 
             
  // create the literal 'p(a,3)'
  Literal l2 = createLiteral("p", createAtom("a"), createNumber(3)); 
             
  // create the literal 'p(a,3)[s,"s"]'
  Literal l3 = createLiteral("p", createAtom("a"), createNumber(3))
                            .addAnnots(createAtom("s"), createString("s"));
             
  // create the literal '~p(a,3)[s,"s"]'
  Literal l4 = createLiteral(Literal.LNeg, "p", createAtom("a"), createNumber(3))
                            .addAnnots(createAtom("s"), createString("s"));
                             
  // or use a parsing (easier but slow)
  Literal l4 = parseLiteral("~p(a,3)[s]");
  </pre> 
 
  @hidden
  
  @author Jomi
 
 */
public class ASSyntax {

    
    /** 
     * Creates a new positive literal, the first argument is the functor (a string)
     * and the n remainder arguments are terms. see documentation of this
     * class for examples of use.
     */
    public static Literal createLiteral(String functor, Term... terms) {
        return new LiteralImpl(functor).addTerms(terms);
    }

    /** 
     * Creates a new literal, the first argument is either Literal.LPos or Literal.LNeg,
     * the second is the functor (a string),
     * and the n remainder arguments are terms. see documentation of this
     * class for examples of use.
     */
    public static Literal createLiteral(boolean positive, String functor, Term... terms) {
        return new LiteralImpl(positive, functor).addTerms(terms);
    }

    /** 
     * Creates a new structure term, is the functor (a string),
     * and the n remainder arguments are terms.
     */
    public static Structure createStructure(String functor, Term... terms) {
        int size = (terms == null || terms.length == 0 ? 3 : terms.length);
        return (Structure)new Structure(functor, size).addTerms(terms);
    }

    /** creates a new Atom term */
    public static Atom createAtom(String functor) {
        return new Atom(functor);
    }

    /** creates a new number term */
    public static NumberTerm createNumber(double vl) {
        return new NumberTermImpl(vl);
    }
    
    /** creates a new string term */
    public static StringTerm createString(String s) {
        return new StringTermImpl(s);
    }

    /** creates a new variable term */
    public static VarTerm createVar(String functor) {
        return new VarTerm(functor);
    }

    /** Creates a new list with n elements, n can be 0 */
    public static ListTerm createList(Term... terms) {
        ListTerm l = new ListTermImpl();
        ListTerm tail = l;
        for (Term t: terms)
            tail = tail.append(t);
        return l;
    }

    
    /** creates a new literal by parsing a string */
    public static Literal parseLiteral(String sLiteral) throws ParseException {
        return new as2j(new StringReader(sLiteral)).literal();
    }
    
    /** creates a new structure (a kind of term) by parsing a string */
    public static Structure parseStructure(String sTerm) throws ParseException {
        Term t = new as2j(new StringReader(sTerm)).term();
        if (t instanceof Structure) 
            return (Structure)t;
        else
            return new Structure((Literal)t);
    }

    /** creates a new term by parsing a string */
    public static Term parseTerm(String sTerm) throws ParseException {
        return new as2j(new StringReader(sTerm)).term();
    }
    
    /** creates a new plan by parsing a string */
    public static Plan parsePlan(String sPlan) throws ParseException {
        return new as2j(new StringReader(sPlan)).plan();
    }

    /** creates a new trigger by parsing a string */
    public static Trigger parseTrigger(String sTe) throws ParseException {
        return new as2j(new StringReader(sTe)).trigger();
    }

    /** creates a new list by parsing a string */
    public static ListTerm parseList(String sList) throws ParseException {
        return new as2j(new StringReader(sList)).list();
    }
    
    /** creates a new logical formula  by parsing a string */
    public static LogicalFormula parseFormula(String sExpr) throws ParseException {
        return (LogicalFormula)new as2j(new StringReader(sExpr)).log_expr();
    }

    /** creates a new plan's body */
    public static PlanBody parsePlanBody(String sExpr) throws ParseException {
        return (PlanBody)new as2j(new StringReader(sExpr)).plan_body();
    }
}
