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
 * Implementation of the  Maintenance Goal pattern (see DALT 2006 papper)
 * 
 * @author jomi
 */
public class MG implements Directive {

    static Logger logger = Logger.getLogger(MG.class.getName());
    
    public boolean process(Pred directive, List<Plan> innerPlans, List<Literal> bels, PlanLibrary pl) {
        try {
            Literal goal = (Literal)directive.getTerm(0);
            Literal subDir;
            if (directive.getTermsSize() > 1) {
                subDir = (Literal)directive.getTerm(1);
            } else {
                subDir = Literal.parseLiteral("bc("+goal+")");
            }
            Directive sd = DirectiveProcessor.getDirective(subDir.getFunctor());

            // add bel g
            bels.add(goal);
            
            // add -g : true <- !g.
            pl.add(Plan.parse("-"+goal+" <- !"+goal+"."));

            // apply sub directive
            if (sd.process(subDir, innerPlans, bels, pl)) {
                return true;
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE,"Directive error.", e);
        }
        return false;
    }
}
