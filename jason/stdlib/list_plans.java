package jason.stdlib;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Plan;
import jason.asSyntax.Term;
import jason.asSyntax.Trigger;

/** list all plans in PL */
public class list_plans extends DefaultInternalAction {

    @Override public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        Trigger te = null;
        if (args.length == 1 && args[0] instanceof Trigger)
            te = Trigger.tryToGetTrigger(args[0]);
        
        for (Plan p: ts.getAg().getPL()) {
            if (!p.getLabel().toString().startsWith("kqml")) { // do not list kqml plans
                if (te == null || new Unifier().unifies(p.getTrigger(), te)) {
                    ts.getLogger().info(p.toString());
                }
            }
        }
        return true;
    }
}
