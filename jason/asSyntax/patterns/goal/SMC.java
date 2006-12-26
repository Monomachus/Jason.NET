package jason.asSyntax.patterns.goal;

import jason.asSyntax.Literal;
import jason.asSyntax.Plan;
import jason.asSyntax.PlanLibrary;
import jason.asSyntax.Pred;
import jason.asSyntax.directives.Directive;
import jason.asSyntax.directives.DirectiveProcessor;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of the Single-Minded Commitment pattern (see DALT 2006 papper)
 * 
 * @author jomi
 */
public class SMC implements Directive {

    static Logger logger = Logger.getLogger(SMC.class.getName());
    
    public boolean process(Pred directive, List<Plan> innerPlans, List<Literal> bels, PlanLibrary pl) {
        try {
            Literal goal = (Literal)directive.getTerm(0);
            Literal fail = (Literal)directive.getTerm(1);
            Literal subDir = Literal.parseLiteral("bcg("+goal+")");
            logger.fine("parameters="+goal+","+fail+","+subDir);
            Directive sd = DirectiveProcessor.getDirective(subDir.getFunctor());

            // apply sub directive
            if (sd.process(subDir, innerPlans, bels, pl)) {

                // add +f : true <- .drop_goal(g,false).
                pl.add(Plan.parse("+"+fail+" <- .drop_goal("+goal+",false)."));
                
                return true;
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE,"Directive error.", e);
        }
        return false;
    }
}
