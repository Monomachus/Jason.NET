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
 * Implementation of the  Relativised Commitment Goal pattern (see DALT 2006 papper)
 * 
 * @author jomi
 */
public class RCG implements Directive {

    static Logger logger = Logger.getLogger(RCG.class.getName());
    
    public boolean process(Pred directive, List<Plan> innerPlans, List<Literal> bels, PlanLibrary pl) {
        try {
            Literal goal = (Literal)directive.getTerm(0);
            Literal motivation = (Literal)directive.getTerm(1);
            Literal subDir = Literal.parseLiteral("bcg("+goal+")");
            logger.fine("parameters="+goal+","+motivation+","+subDir);
            Directive sd = DirectiveProcessor.getDirective(subDir.getFunctor());

            // apply sub directive
            if (sd.process(subDir, innerPlans, bels, pl)) {

                // add -m : true <- .dropGoal(g,true).
                pl.add(Plan.parse("-"+motivation+" <- .dropGoal("+goal+",true)."));
                
                return true;
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE,"Directive error.", e);
        }
        return false;
    }
}
