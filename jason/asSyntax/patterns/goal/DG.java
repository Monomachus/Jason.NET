package jason.asSyntax.patterns.goal;

import jason.asSyntax.*;
import jason.asSyntax.BodyLiteral.BodyType;
import jason.asSyntax.directives.Directive;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of the Declarative Goal pattern (see DALT 2006 papper)
 * 
 * @author jomi
 */
public class DG implements Directive {

    static Logger logger = Logger.getLogger(DG.class.getName());

    public boolean process(Pred directive, List<Plan> innerPlans, List<Literal> bels, PlanLibrary pl) {
        try {
            Literal goal = Literal.parseLiteral(directive.getTerm(0).toString());
            // add +!g : g <- true.
            pl.add(Plan.parse("+!"+goal+" : " +goal+"."));
            
            // add ?g in the end of all inner plans
            for (Plan p: innerPlans) {
                BodyLiteral b = new BodyLiteral(BodyType.test, (Literal)goal.clone());
                p.getBody().add(b);
                pl.add(p);
            }
            
            // add +g : true <- .succeed_goal(g).
            pl.add(Plan.parse("+"+goal+" <- .succeed_goal("+goal+")."));
            
            return true;
        } catch (Exception e) {
            logger.log(Level.SEVERE,"Directive error.", e);
        }
        return false;
    }
}
