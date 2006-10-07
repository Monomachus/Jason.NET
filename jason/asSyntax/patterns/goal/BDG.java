package jason.asSyntax.patterns.goal;

import jason.asSyntax.Literal;
import jason.asSyntax.Plan;
import jason.asSyntax.PlanLibrary;
import jason.asSyntax.Pred;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of the Backtracking Declarative Goal pattern (see DALT 2006 papper)
 * 
 * @author jomi
 */
public class BDG extends DG {

    static Logger logger = Logger.getLogger(BDG.class.getName());
    
    public boolean process(Pred directive, List<Plan> innerPlans, List<Literal> bels, PlanLibrary pl) {
        try {
            // apply DG in the inner plans
            if (super.process(directive, innerPlans, bels, pl)) {

                Literal goal = (Literal)directive.getTerm(0);
    
                // add -!g : true <- !g.
                pl.add(Plan.parse("-!"+goal+" <- !"+goal+"."));
                
                return true;
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE,"Directive error.", e);
        }
        return false;
    }
}
