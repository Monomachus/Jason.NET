package jason.asSemantics;

import jason.JasonException;
import jason.asSyntax.InternalActionLiteral;
import jason.asSyntax.PlanBody;
import jason.asSyntax.PlanBodyImpl;
import jason.asSyntax.Term;
import jason.asSyntax.PlanBody.BodyType;

import java.util.concurrent.TimeUnit;

/** 

  This class can be used in place of DefaultInternalAction to create an IA that
  suspend the intention while it is being executed.

  Example: a plan may ask something to an user and wait the answer.
  If DefaultInternalAction is used for that, all the agent thread is blocked until
  the answer. With ConcurrentInternalAction, only the intention using the IA is
  suspended. See demos/gui/gui1.  

  The code of an internal action that extends this class looks like:
  
  <pre>
  public class ...... extends ConcurrentInternalAction {

    public Object execute(final TransitionSystem ts, Unifier un, final Term[] args) throws Exception {
        ....
        
        final String key = suspendInt(ts, "gui", 5000); // suspend the intention (max 5 seconds) 

        startInternalAction(ts, new Runnable() { // to not block the agent thread, start a thread that performs the task and resume the intention latter
            public void run() {
            
                .... the code of the IA .....
                
                if ( ... all Ok ...)
                    resumeInt(ts, key); // resume the intention with success
                else
                    failInt(ts, key); // resume the intention with fail
            }
        });
        
        ...
    }
    
    public void timeout(TransitionSystem ts, String intentionKey) { // called back when the intention should be resumed/failed by timeout (after 5 seconds in this example)
        ... this method have to decide what to do with actions finished by timeout: resume or fail
        ... to call resumeInt(ts,intentionKey) or failInt(ts, intentionKey)
    }
  }
  </pre>
  
  @author jomi
*/
public abstract class ConcurrentInternalAction implements InternalAction {

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
     * @param ts        the "engine" of the agent
     * @param basekey   the base key to form final key used to get the intention back from PI (e.g. "moise", "cartago", ...)
     * @param timeout   the max time the intention will be in PI, the value 0 means until "resume"
     * @return the final key used to store the intention in PI, this key is used the resume the intention
     */
    public String suspendInt(final TransitionSystem ts, String basekey, int timeout) {
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
    
    public void startInternalAction(TransitionSystem ts, Runnable code) {
        ts.getAg().getScheduler().execute(code);
        //new Thread(code).start();
    }
    
    /** called back when some intention should be resumed/failed by timeout */
    abstract public void timeout(TransitionSystem ts, String intentionKey);
    
    /** resume the intention identified by intentionKey */
    public void resumeInt(TransitionSystem ts, String intentionKey) {
        resume(ts, intentionKey, false);
    }

    /** fails the intention identified by intentionKey */
    public void failInt(TransitionSystem ts, String intentionKey) {
        resume(ts, intentionKey, true);
    }

    synchronized private void resume(TransitionSystem ts, String intentionKey, boolean abort) {
        Circumstance C = ts.getC();
        Intention pi = C.getPendingIntentions().remove(intentionKey);
        if (pi != null) {
            pi.setSuspended(false);
            pi.peek().removeCurrentStep(); // remove the internal action that put the intention in suspend
            try {
                ts.applyClrInt(pi);
            } catch (JasonException e) {
                e.printStackTrace();
            }
            
            if (abort) {
                // fail the IA
                PlanBody pbody = pi.peek().getPlan().getBody();
                pbody.add(0, new PlanBodyImpl(BodyType.internalAction, new InternalActionLiteral(".fail")));
            }
            C.addIntention(pi); // add it back in I
            ts.getUserAgArch().getArchInfraTier().wake();
        }
    }
}
