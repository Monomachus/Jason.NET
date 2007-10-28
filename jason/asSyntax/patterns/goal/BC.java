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
 * Implementation of the Blind Commitment pattern (see DALT 2006 paper)
 * 
 * @author jomi
 */
public class BC implements Directive {

    static Logger logger = Logger.getLogger(BC.class.getName());
    
    public Agent process(Pred directive, Agent outerContent, Agent innerContent) {
        try {
            Term goal = directive.getTerm(0);
            Literal subDir;
            if (directive.getArity() > 1) {
                subDir = Literal.parseLiteral(directive.getTerm(1).toString());
            } else {
                subDir = Literal.parseLiteral("bdg("+goal+")");
            }
            Directive sd = DirectiveProcessor.getDirective(subDir.getFunctor());

            // apply sub directive
            Agent newAg = sd.process(subDir, outerContent, innerContent); 
            if (newAg != null) {

                // add +!g : true <- !!g.
                newAg.getPL().add(Plan.parse("+!"+goal+" <- !!"+goal+"."));
                
                return newAg;
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE,"Directive error.", e);
        }
        return null;
    }
}
