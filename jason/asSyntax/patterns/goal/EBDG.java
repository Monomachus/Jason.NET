package jason.asSyntax.patterns.goal;

import jason.asSyntax.BodyLiteral;
import jason.asSyntax.Literal;
import jason.asSyntax.LogExpr;
import jason.asSyntax.LogicalFormula;
import jason.asSyntax.NumberTermImpl;
import jason.asSyntax.Plan;
import jason.asSyntax.PlanLibrary;
import jason.asSyntax.Pred;
import jason.asSyntax.BodyLiteral.BodyType;
import jason.asSyntax.LogExpr.LogicalOp;
import jason.asSyntax.directives.Directive;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of the Exclusive BDG pattern (see DALT 2006 papper)
 * 
 * @author jomi
 */
public class EBDG implements Directive {

    static Logger logger = Logger.getLogger(EBDG.class.getName());
    
    public boolean process(Pred directive, List<Plan> innerPlans, List<Literal> bels, PlanLibrary pl) {
        try {
            Literal goal = (Literal)directive.getTerm(0);

            // add +!g : g <- true.
            pl.add(Plan.parse("+!"+goal+" : " +goal+"."));
            
            // change all inner plans
            int i = 0;
            for (Plan p: innerPlans) {
                i++;
                // create p__f(i,g)
                Literal pi = new Literal(Literal.LPos, "p__f");
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
                BodyLiteral b1 = new BodyLiteral(BodyType.addBel, pi);
                p.getBody().add(0, b1);
                // add ?g
                BodyLiteral b2 = new BodyLiteral(BodyType.test, (Literal)goal.clone());
                p.getBody().add(b2);
                pl.add(p);
            }
            

            // add -!g : true <- !!g.
            pl.add(Plan.parse("-!"+goal+" <- !!"+goal+"."));

            // add +g : true <- .abolish(p__f(_,g)); .dropGoal(g,true).
            pl.add(Plan.parse("+"+goal+" <- .abolish(p__f(_,"+goal+")); .drop_goal("+goal+",true)."));

            // add -g <- .abolish(p__f(_,g)).
            pl.add(Plan.parse("-"+goal+" <- .abolish(p__f(_,"+goal+"))."));
            
            return true;
        } catch (Exception e) {
            logger.log(Level.SEVERE,"Directive DG error.", e);
        }
        return false;
    }
}
