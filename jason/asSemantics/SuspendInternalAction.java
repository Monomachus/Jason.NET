package jason.asSemantics;

import jason.asSyntax.InternalActionLiteral;
import jason.asSyntax.PlanBody;
import jason.asSyntax.PlanBodyImpl;
import jason.asSyntax.Term;
import jason.asSyntax.PlanBody.BodyType;

import java.util.concurrent.TimeUnit;

/** 

  This class can be used in place of DefaultInternalAction to create IA that
  suspend/resume the intention.

  Example: a plan should to ask something to an user and wait until he/she answer some.
  If DefaultInternalAction is used for that, all the agent thread is suspended until
  the answer. With SuspendInternalAction, only the intention using the IA is
  suspended. See demos/gui/gui1.  

  @author jomi
*/
public abstract class SuspendInternalAction implements InternalAction {

    private static int actcount  = 0;
    
    public boolean canBeUsedInContext() {
        return false;
    }

    public boolean suspendIntention() {
        return true;
    }
    
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        return false;
    }
    
    /**
     * Suspend the current intention, put it in the PendingIntention (PI) structure and assigns it to a key.
     *  
     * @param basekey   the base key to form final key used to get the intention back from PI (e.g. "moise", "cartago", ...)
     * @param timeout   the max time the intention will be in PI, the value 0 means until "resume"
     * @return the final key used to store the intention in PI
     */
    public String suspend(final TransitionSystem ts, String basekey, int timeout) {
        final String key = basekey + "/" + (actcount++); 
        final Circumstance C = ts.getC();
        Intention i = C.getSelectedIntention();
        i.setSuspended(true);
        C.getPendingIntentions().put(key, i);
        
        if (timeout > 0) {
            // schedule a future test of the end of the action
            ts.getAg().getScheduler().schedule( new Runnable() {
                public void run() {
                    // finish the IA by timeout
                    if (C.getPendingIntentions().get(key) != null) { // test if the intention is still there
                        timeout(ts,key);
                    }
                }
            }, timeout, TimeUnit.MILLISECONDS);
        }
        return key;
    }
    
    /** called back when some intention should be resumed/failed by timeout */
    abstract public void timeout(TransitionSystem ts, String intentionKey);
    
    /** resume the intention identified by intentionKey */
    public void resume(TransitionSystem ts, String intentionKey) {
        resume(ts, intentionKey, false);
    }

    /** fails the intention identified by intentionKey */
    public void fail(TransitionSystem ts, String intentionKey) {
        resume(ts, intentionKey, true);
    }

    synchronized private void resume(TransitionSystem ts, String intentionKey, boolean abort) {
        Circumstance C = ts.getC();
        Intention pi = C.getPendingIntentions().remove(intentionKey);
        if (pi != null) {
            pi.setSuspended(false);
            C.addIntention(pi); // add it back in I
            pi.peek().removeCurrentStep(); // remove the internal action that put the intention in suspend
            
            if (abort) {
                // fail the IA
                PlanBody pbody = pi.peek().getPlan().getBody();
                pbody.add(0, new PlanBodyImpl(BodyType.internalAction, new InternalActionLiteral(".fail")));
            }
            ts.getUserAgArch().getArchInfraTier().wake();
        }
    }
}
