package jason.stdlib;

import jason.JasonException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.NumberTermImpl;
import jason.asSyntax.Term;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class date extends DefaultInternalAction {

    /** date(YY,MM,DD) */
    @Override
    public boolean execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        try {
            Calendar now = new GregorianCalendar();
            return un.unifies(args[0], new NumberTermImpl(now.get(Calendar.YEAR))) &&
                   un.unifies(args[1], new NumberTermImpl(now.get(Calendar.MONTH))) &&
                   un.unifies(args[2], new NumberTermImpl(now.get(Calendar.DAY_OF_MONTH)));
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new JasonException("The internal action 'date' has not received three arguments");
        } catch (Exception e) {
            throw new JasonException("Error in internal action 'date': " + e);
        }
    }
}
