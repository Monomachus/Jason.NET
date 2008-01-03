package jason.asSyntax.directives;

import jason.asSemantics.Agent;
import jason.asSyntax.ArithFunction;
import jason.asSyntax.Pred;
import jason.asSyntax.StringTerm;
import jason.functions.Abs;
import jason.functions.Length;
import jason.functions.Max;
import jason.functions.Min;
import jason.functions.Random;
import jason.functions.Round;
import jason.functions.Sqrt;

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

    // add known functions
    static {
        addFunction(Abs.class);
        addFunction(Max.class);
        addFunction(Min.class);
        addFunction(Length.class);
        addFunction(Random.class);
        addFunction(Round.class);
        addFunction(Sqrt.class);
    }
        
    public static void addFunction(Class<? extends ArithFunction> c) {
		try {
			ArithFunction af = c.newInstance(); 
	    	functions.put(af.getFunctor(),c);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Error registering function "+c.getName(),e);
		}
    }
    public static Class<? extends ArithFunction> getFunction(String id) {
        return functions.get(id);
    }
    public static Class<? extends ArithFunction> removeFunction(String id) {
        return functions.remove(id);
    }
    
    public static ArithFunction create(String id) {
    	Class<? extends ArithFunction> c = functions.get(id);
    	if (c != null) {
    		try {
    			return c.newInstance();    			
    		} catch (Exception e) {
    			logger.log(Level.SEVERE, "Error creating function class",e);
    		}
    	}
    	return null;
    }
    
    @SuppressWarnings("unchecked")
	public Agent process(Pred directive, Agent outerContent, Agent innerContent) {
    	try {
    		String classname = ((StringTerm)directive.getTerm(0)).getString();
			addFunction((Class<ArithFunction>)Class.forName(classname));
		} catch (ClassNotFoundException e) {
			logger.log(Level.SEVERE, "Error processing directive register_function.",e);
		}
    	return null;
    }
}
