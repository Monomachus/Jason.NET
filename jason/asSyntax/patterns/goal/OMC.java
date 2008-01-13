package jason.asSyntax.patterns.goal;

import jason.asSemantics.Agent;
import jason.asSyntax.Literal;
import jason.asSyntax.Plan;
import jason.asSyntax.Pred;
import jason.asSyntax.Term;
import jason.asSyntax.directives.Directive;
import jason.asSyntax.directives.DirectiveProcessor;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of the  Open-Minded Commitment pattern (see DALT 2006 paper)
 * 
 * @author jomi
 */
public class OMC implements Directive {

    static Logger logger = Logger.getLogger(OMC.class.getName());
    
    public Agent process(Pred directive, Agent outerContent, Agent innerContent) {
        try {
            Term goal = directive.getTerm(0);
            Term fail = directive.getTerm(1);
            Term motivation = directive.getTerm(2);
            Literal subDir = Literal.parseLiteral("bc("+goal+")");
            Directive sd = DirectiveProcessor.getDirective(subDir.getFunctor());

            // apply sub directive
            Agent newAg = sd.process(subDir, outerContent, innerContent); 
            if (newAg != null) {
                // add +f : true <- .fail_goal(g).
                Plan pf = Plan.parse("+"+fail+" <- .fail_goal("+goal+").");
                pf.setSrc(outerContent+"/"+directive);
                newAg.getPL().add(pf);

                // add -m : true <- .succeed_goal(g).
                Plan pm = Plan.parse("-"+motivation+" <- .succeed_goal("+goal+").");
                pm.setSrc(outerContent+"/"+directive);
                newAg.getPL().add(pm);
                
                return newAg;
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE,"Directive error.", e);
        }
        return null;
    }
}
