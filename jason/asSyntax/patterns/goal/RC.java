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
 * Implementation of the  Relativised Commitment pattern (see DALT 2006 papper)
 * 
 * @author jomi
 */
public class RC implements Directive {

    static Logger logger = Logger.getLogger(RC.class.getName());
    
    public boolean process(Pred directive, List<Plan> innerPlans, List<Literal> bels, PlanLibrary pl) {
        try {
            Literal goal = (Literal)directive.getTerm(0);
            Literal motivation = (Literal)directive.getTerm(1);
            Literal subDir = Literal.parseLiteral("bc("+goal+")");
            logger.fine("parameters="+goal+","+motivation+","+subDir);
            Directive sd = DirectiveProcessor.getDirective(subDir.getFunctor());

            // apply sub directive
            if (sd.process(subDir, innerPlans, bels, pl)) {

                // add -m : true <- .drop_goal(g,true).
                pl.add(Plan.parse("-"+motivation+" <- .drop_goal("+goal+",true)."));
                
                return true;
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE,"Directive error.", e);
        }
        return false;
    }
}
