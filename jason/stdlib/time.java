package jason.stdlib;

import jason.JasonException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.NumberTermImpl;
import jason.asSyntax.Term;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class time extends DefaultInternalAction {

    /** time(HH,MM,SS) */
    @Override
    public boolean execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        try {
            Calendar now = new GregorianCalendar();
            return un.unifies(args[0], new NumberTermImpl(now.get(Calendar.HOUR))) &&
                   un.unifies(args[1], new NumberTermImpl(now.get(Calendar.MINUTE))) &&
                   un.unifies(args[2], new NumberTermImpl(now.get(Calendar.SECOND)));
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new JasonException("The internal action 'time' has not received three arguments");
        } catch (Exception e) {
            throw new JasonException("Error in internal action 'time': " + e);
        }
    }
}
