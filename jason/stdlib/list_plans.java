package jason.stdlib;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Plan;
import jason.asSyntax.Term;

/** list all plans in PL */
public class list_plans extends DefaultInternalAction {

    @Override public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        for (Plan p: ts.getAg().getPL()) {
            if (!p.getLabel().toString().startsWith("kqml")) { // do not list kqml plans
                ts.getLogger().info(p.toString());
            }
        }
        return true;
    }
}
