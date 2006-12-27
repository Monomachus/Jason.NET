package jason.asSyntax.patterns.goal;

import jason.asSyntax.*;
import jason.asSyntax.directives.Directive;
import jason.asSyntax.directives.DirectiveProcessor;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of the  Open-Minded Commitment pattern (see DALT 2006 papper)
 * 
 * @author jomi
 */
public class OMC implements Directive {

    static Logger logger = Logger.getLogger(OMC.class.getName());
    
    public boolean process(Pred directive, List<Plan> innerPlans, List<Literal> bels, PlanLibrary pl) {
        try {
            Term goal = directive.getTerm(0);
            Term fail = directive.getTerm(1);
            Term motivation = directive.getTerm(2);
            Literal subDir = Literal.parseLiteral("bc("+goal+")");
            Directive sd = DirectiveProcessor.getDirective(subDir.getFunctor());

            // apply sub directive
            if (sd.process(subDir, innerPlans, bels, pl)) {

                // add +f : true <- .drop_goal(g,false).
                pl.add(Plan.parse("+"+fail+" <- .drop_goal("+goal+",false)."));

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
