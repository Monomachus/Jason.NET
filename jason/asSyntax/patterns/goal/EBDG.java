package jason.asSyntax.patterns.goal;

import jason.asSemantics.Agent;
import jason.asSyntax.PlanBody;
import jason.asSyntax.PlanBodyImpl;
import jason.asSyntax.Literal;
import jason.asSyntax.LogExpr;
import jason.asSyntax.LogicalFormula;
import jason.asSyntax.NumberTermImpl;
import jason.asSyntax.Plan;
import jason.asSyntax.Pred;
import jason.asSyntax.PlanBody.BodyType;
import jason.asSyntax.LogExpr.LogicalOp;
import jason.asSyntax.directives.Directive;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of the Exclusive BDG pattern (see DALT 2006 paper)
 * 
 * @author jomi
 */
public class EBDG implements Directive {

    static Logger logger = Logger.getLogger(EBDG.class.getName());
    
    public Agent process(Pred directive, Agent outerContent, Agent innerContent) {
        try {
            Agent newAg = new Agent();
            
            Literal goal = Literal.parseLiteral(directive.getTerm(0).toString());

            // add +!g : g <- true.
            newAg.getPL().add(Plan.parse("+!"+goal+" : " +goal+"."));
            
            // change all inner plans
            int i = 0;
            for (Plan p: innerContent.getPL()) {
                i++;
                // create p__f(i,g)
                Literal pi = new Literal("p__f");
				pi.addTerm(new NumberTermImpl(i));
                pi.addTerm(goal);
                
                // change context to "not p__f(i,g) & c"
                LogicalFormula context = p.getContext();
                if (context == null) {
                    p.setContext(new LogExpr(LogicalOp.not, pi));
                } else {
                    p.setContext(new LogExpr(new LogExpr(LogicalOp.not, pi), LogicalOp.and, context));
                }
                
                // change body
                // add +p__f(i,g)
                PlanBody b1 = new PlanBodyImpl(BodyType.addBel, pi);
                p.getBody().add(0, b1);
                // add ?g
                PlanBody b2 = new PlanBodyImpl(BodyType.test, (Literal)goal.clone());
                p.getBody().add(b2);
                newAg.getPL().add(p);
            }
            

            // add -!g : true <- !!g.
            newAg.getPL().add(Plan.parse("-!"+goal+" <- !!"+goal+"."));

            // add +g : true <- .abolish(p__f(_,g)); .succeed_goal(g).
            newAg.getPL().add(Plan.parse("+"+goal+" <- .abolish(p__f(_,"+goal+")); .succeed_goal("+goal+")."));

            // add -g <- .abolish(p__f(_,g)).
            newAg.getPL().add(Plan.parse("-"+goal+" <- .abolish(p__f(_,"+goal+"))."));
            
            return newAg;
        } catch (Exception e) {
            logger.log(Level.SEVERE,"Directive EBDG error.", e);
        }
        return null;
    }
}
