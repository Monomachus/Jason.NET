package jason.asSyntax.patterns.goal;

import jason.asSemantics.Agent;
import jason.asSyntax.BodyLiteral;
import jason.asSyntax.Literal;
import jason.asSyntax.Plan;
import jason.asSyntax.Pred;
import jason.asSyntax.BodyLiteral.BodyType;
import jason.asSyntax.directives.Directive;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of the Declarative Goal pattern (see DALT 2006 papper)
 * 
 * @author jomi
 */
public class DG implements Directive {

    static Logger logger = Logger.getLogger(DG.class.getName());

    public Agent process(Pred directive, Agent ag) {
        try {
            Agent newAg = new Agent();
            
            Literal goal = Literal.parseLiteral(directive.getTerm(0).toString());
            
            // add +!g : g <- true.
            newAg.getPL().add(Plan.parse("+!"+goal+" : " +goal+"."));
            
            // add ?g in the end of all inner plans
            for (Plan p: ag.getPL()) {
                BodyLiteral b = new BodyLiteral(BodyType.test, (Literal)goal.clone());
                p.getBody().add(b);
                newAg.getPL().add(p);
            }
            
            // add +g : true <- .succeed_goal(g).
            newAg.getPL().add(Plan.parse("+"+goal+" <- .succeed_goal("+goal+")."));
            
            return newAg;
        } catch (Exception e) {
            logger.log(Level.SEVERE,"Directive error.", e);
        }
        return null;
    }
}
