package jason.asSyntax.patterns.goal;

import jason.asSyntax.Literal;
import jason.asSyntax.LogExprTerm;
import jason.asSyntax.Plan;
import jason.asSyntax.PlanLibrary;
import jason.asSyntax.Pred;
import jason.asSyntax.StringTerm;
import jason.asSyntax.Term;
import jason.asSyntax.Trigger;
import jason.asSyntax.directives.Directive;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of the Sequenced Goal Adoption pattern (see DALT 2006 papper)
 * 
 * @author jomi
 */
public class SGA implements Directive {

    static Logger logger = Logger.getLogger(SGA.class.getName());
    
    public boolean process(Pred directive, List<Plan> innerPlans, List<Literal> bels, PlanLibrary pl) {
        try {
            Trigger trigger = Trigger.parseTrigger(((StringTerm)directive.getTerm(0)).getString());
            Term context = LogExprTerm.parse(((StringTerm)directive.getTerm(1)).getString());
            Literal goal = (Literal)directive.getTerm(2);

            // add t : not f__l(_) & c <- !f__g(g).
            pl.add(Plan.parse(trigger+" : not f__l(_) & " +context +" <- !f__g("+goal+")."));
            
            // add t : f__l(_) & c <- +f__l(g).
            pl.add(Plan.parse(trigger+" : f__l(_) & (" +context +") <- +f__l("+goal+")."));
            
            // add +!fg(g) : true <- +fl(g); !g; -fl(g)
            pl.add(Plan.parse("+!f__g("+goal+") <- +f__l("+goal+"); !"+goal+"; -f__l("+goal+")."));            
            
            // add -!fg(g) : true <- -fl(g)
            pl.add(Plan.parse("-!f__g("+goal+") <- -f__l("+goal+")."));            

            // add -fl(_) : fg(g) <- !fg(g)
            pl.add(Plan.parse("-f__l("+goal+") : f__l("+goal+") <- !f__g("+goal+")."));            

            return true;
        } catch (Exception e) {
            logger.log(Level.SEVERE,"Directive DG error.", e);
        }
        return false;
    }
}
