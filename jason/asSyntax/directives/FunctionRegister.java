package jason.asSyntax.directives;

import jason.asSemantics.Agent;
import jason.asSemantics.ArithFunction;
import jason.asSyntax.ArithFunctionTerm;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Pred;
import jason.asSyntax.StringTerm;
import jason.functions.Abs;
import jason.functions.Count;
import jason.functions.Length;
import jason.functions.Max;
import jason.functions.Min;
import jason.functions.Random;
import jason.functions.Round;
import jason.functions.RuleToFunction;
import jason.functions.Sqrt;
import jason.functions.ceil;
import jason.functions.e;
import jason.functions.floor;
import jason.functions.log;
import jason.functions.pi;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/** 
 * This class maintains the set of arithmetic functions available for the AS parser.
 * 
 * @author Jomi
 */
public class FunctionRegister implements Directive {
    static Logger logger = Logger.getLogger(FunctionRegister.class.getName());

    private static Map<String,Class<? extends ArithFunction>> functions = new HashMap<String,Class<? extends ArithFunction>>();
    private static Map<String,Integer>                        arities   = new HashMap<String,Integer>();

    // add known functions
    static {
        addFunction(Abs.class);
        addFunction(Max.class);
        addFunction(Min.class);
        addFunction(Length.class);
        addFunction(Random.class);
        addFunction(Round.class);
        addFunction(Sqrt.class);
        addFunction(Count.class);
        addFunction(pi.class);
        addFunction(e.class);
        addFunction(floor.class);
        addFunction(ceil.class);
        addFunction(log.class);
    }
        
    /** register a function implemented in Java */
    public static void addFunction(Class<? extends ArithFunction> c) {
		try {
			ArithFunction af = c.newInstance();
			if (functions.get(af.getName()) != null)
			    logger.warning("Registering the function "+af.getName()+"  twice! The first register is lost.");
	    	functions.put(af.getName(),c);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Error registering function "+c.getName(),e);
		}
    }
    
    /** register a function implemented in AS (by a rule) */
    public static void addFunction(String ruleHead, int arity) {
        try {
            if (functions.get(ruleHead) != null)
                logger.warning("Registering the function "+ruleHead+"  twice! The first register is lost.");
            functions.put(ruleHead, RuleToFunction.class);
            arities.put(ruleHead, arity);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error registering function "+ruleHead,e);
        }
    }
    
    public static Class<? extends ArithFunction> getFunction(String id) {
        return functions.get(id);
    }
    
    public static Class<? extends ArithFunction> removeFunction(String id) {
        return functions.remove(id);
    }
    
    public static ArithFunctionTerm create(String id) {
    	Class<? extends ArithFunction> c = functions.get(id);
    	if (c != null) {
    		try {
                ArithFunction af = c.newInstance(); 
                if (af instanceof RuleToFunction && arities.containsKey(id)) {
                    ((RuleToFunction)af).setRule(id);
                    ((RuleToFunction)af).setArity(arities.get(id));
                }
    			return new ArithFunctionTerm(af);    			
    		} catch (Exception e) {
    			logger.log(Level.SEVERE, "Error creating function class",e);
    		}
    	}
    	return null;
    }
    
    @SuppressWarnings("unchecked")
	public Agent process(Pred directive, Agent outerContent, Agent innerContent) {
    	try {
    	    String id = ((StringTerm)directive.getTerm(0)).getString();
    	    if (directive.getArity() == 1) {
    	        // it is implemented in java
                addFunction((Class<ArithFunction>)Class.forName(id));
    	    } else if (directive.getArity() == 2) {
    	        // is is implemented in AS
    	        addFunction(id, (int)((NumberTerm)directive.getTerm(1)).solve());
    	    } else {
    	        // error
                logger.log(Level.SEVERE, "Wrong number of arguments for register_function "+directive);
    	    }
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Error processing directive register_function.",e);
		}
    	return null;
    }
}
