package jason.functions;

import jason.JasonException;
import jason.asSemantics.DefaultArithFunction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.InternalActionLiteral;
import jason.asSyntax.Literal;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Term;
import jason.asSyntax.VarTerm;

import java.util.Iterator;

/** 

   Wraps a rule into a function.
   
  @author Jomi 
*/
public class RuleToFunction extends DefaultArithFunction  {

    private String rule  = null;
    private int    arity = -1;
    
    public void setRule(String r) {
        rule = r;
    }
    
    public void setArity(int a) { 
        arity = a; 
    }
    
    
    @Override
    public boolean checkArity(int a) {
        return a == arity;
    }

	@Override
	public double evaluate(TransitionSystem ts, Term[] args) throws Exception {
	    // create a literal to perform the query
	    Literal r;
	    if (rule.indexOf(".") > 0) // is internal action
	        r = new InternalActionLiteral(rule);
	    else
	        r = new Literal(rule);
	    for (Term t: args)
	        r.addTerm(t);
	    VarTerm answer = new VarTerm("__RuleToFunctionResult");
	    r.addTerm(answer);
	    
	    // query the BB
	    Iterator<Unifier> i = r.logicalConsequence( (ts == null ? null : ts.getAg()), new Unifier());
	    if (i.hasNext()) {
	        Term value = i.next().get(answer);
	        if (value.isNumeric())
	            return ((NumberTerm)value).solve();
	        else
	            throw new JasonException("The result of "+r+"("+value+") is not numeric!");	            
	    } else 
	        throw new JasonException("No solution for found for rule "+r);
	}
	
}
