package jason.asSyntax.patterns.goal;

import jason.asSyntax.*;
import jason.asSyntax.directives.Directive;
import jason.asSyntax.directives.DirectiveProcessor;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of the  Relativised Commitment pattern (see DALT 2006 papper)
 * 
 * @author jomi
 */
public class RC implements Directive {

    static Logger logger = Logger.getLogger(RC.class.getName());
    
    public boolean process(Pred directive, List<Plan> innerPlans, List<Literal> bels, PlanLibrary pl) {
        try {
            Term goal = directive.getTerm(0);
            Term motivation = directive.getTerm(1);
            Literal subDir = Literal.parseLiteral("bc("+goal+")");
            //logger.fine("parameters="+goal+","+motivation+","+subDir);
            Directive sd = DirectiveProcessor.getDirective(subDir.getFunctor());

            // apply sub directive
            if (sd.process(subDir, innerPlans, bels, pl)) {

                // add -m : true <- .succeed_goal(g).
                pl.add(Plan.parse("-"+motivation+" <- .succeed_goal("+goal+")."));
                
                return true;
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE,"Directive error.", e);
        }
        return false;
    }
}
