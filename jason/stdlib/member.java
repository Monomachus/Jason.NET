package jason.stdlib;

import jason.JasonException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.ListTerm;
import jason.asSyntax.Term;

import java.util.ArrayList;
import java.util.List;

/**
 * List member implementation
 * 
 * @author jomi
 */
public class member extends DefaultInternalAction {
    
    /** .member(X,[a,b,c]), return [{X=a}, {X=b}, {X=c}] */
    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        try {
            Term member = args[0];

            if (!args[1].isList()) {
                throw new JasonException("The second parameter ('" + args[1] + "') of the internal action 'member' is not a list!");
            }
            ListTerm lt = (ListTerm)args[1];

            List<Unifier> answers = new ArrayList<Unifier>();
            Unifier newUn = (Unifier)un.clone(); // clone un so to not change it
            for (Term t: lt) {
                if (newUn.unifies(member, t)) {
                    // add this unification in answers
                    answers.add(newUn);
                    newUn = (Unifier)un.clone(); // create a new clone of un
                }
            }                
            return answers.iterator();
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new JasonException("The internal action 'member' has not received two arguments");
        } catch (Exception e) {
            throw new JasonException("Error in internal action 'member': " + e);
        }
    }
}
