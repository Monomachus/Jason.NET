package jason.asSyntax.patterns.goal;

import jason.asSyntax.*;
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
            Literal goal = Literal.parseLiteral(directive.getTerm(0).toString());
            Literal subDir;
            if (directive.getTermsSize() > 1) {
                subDir = Literal.parseLiteral(directive.getTerm(1).toString());
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
