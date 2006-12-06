package jason.asSyntax.directives;

import jason.asSyntax.Literal;
import jason.asSyntax.Plan;
import jason.asSyntax.PlanLibrary;
import jason.asSyntax.Pred;
import jason.asSyntax.patterns.goal.BCG;
import jason.asSyntax.patterns.goal.BDG;
import jason.asSyntax.patterns.goal.DG;
import jason.asSyntax.patterns.goal.EBDG;
import jason.asSyntax.patterns.goal.MG;
import jason.asSyntax.patterns.goal.OMC;
import jason.asSyntax.patterns.goal.RCG;
import jason.asSyntax.patterns.goal.SGA;
import jason.asSyntax.patterns.goal.SMC;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/** 
 * This class maintains the set of directives and is used by the
 * parser to process them.
 * 
 * All available directives must be registered in this class using the
 * addDirective method.
 * 
 * @author jomi
 *
 */
public class DirectiveProcessor {
    static Logger logger = Logger.getLogger(DirectiveProcessor.class.getName());

    private static Map<String,Directive> directives = new HashMap<String,Directive>();
    
    public static void addDirective(String id, Directive d) {
        directives.put(id,d);
    }
    public static Directive getDirective(String id) {
        return directives.get(id);
    }
    public static Directive removeDirective(String id) {
        return directives.remove(id);
    }
    
    // add known directives
    static {
        addDirective("include", new Include());

        addDirective("dg", new DG());
        addDirective("bdg", new BDG());
        addDirective("ebdg", new EBDG());
        addDirective("bcg", new BCG());
        addDirective("smc", new SMC());
        addDirective("rcg", new RCG());
        addDirective("omc", new OMC());
        addDirective("mg", new MG());
        addDirective("sga", new SGA());
    }
    
    public static void process(Pred directive, List<Plan> innerPlans, List<Literal> bels, PlanLibrary pl) {
        try {
            logger.fine("Processing directive "+directive);
            Directive d = directives.get(directive.getFunctor());
            if (d != null) {
                d.process(directive, innerPlans, bels, pl);
            } else {
                logger.log(Level.SEVERE, "Unknown directive "+directive);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error processing directive "+directive,e);
        }
    }

}
