package jason.stdlib;

import jason.JasonException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.NumberTermImpl;
import jason.asSyntax.Term;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**

  <p>Internal action: <b><code>.time(HH,MM,SS)</code></b>.
  
  <p>Description: gets the current time (hour, minute, and seconds).

  <p>Parameters:<ul>
  
  <li>+/- arg[0] (number): the hour (0--23).</li>
  <li>+/- arg[1] (number): the minutes (0--59).</li>
  <li>+/- arg[2] (number): the seconds (0--59).</li>
  
  </ul>
  
  <p>Examples:<ul> 

  <li> <code>.time(H,M,S)</code>: unifies H with the current hour, M
  with the current minutes, and S with the current seconds.</li>

  <li> <code>.time(15,_,_)</code>: succeed if now is 3pm.</li>

  </ul>

  @see jason.stdlib.date
  
 */
public class time extends DefaultInternalAction {

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
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
