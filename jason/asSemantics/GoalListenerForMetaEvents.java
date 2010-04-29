package jason.asSemantics;

import jason.asSyntax.ASSyntax;
import jason.asSyntax.Atom;
import jason.asSyntax.Literal;
import jason.asSyntax.StringTermImpl;
import jason.asSyntax.Trigger;
import jason.asSyntax.Trigger.TEOperator;
import jason.asSyntax.Trigger.TEType;

public class GoalListenerForMetaEvents implements GoalListener {

    private TransitionSystem ts;
    
    public GoalListenerForMetaEvents(final TransitionSystem ts) {
        this.ts = ts;
        
        ts.getC().addEventListener(new CircumstanceListener() {
            
            public void intentionDropped(Intention i) {
                for (IntendedMeans im: i.getIMs()) 
                    if (im.getTrigger().isAddition() && im.getTrigger().isGoal()) 
                        goalFinished(im.getTrigger());                         
            }
            
            public void intentionSuspended(Intention i, String reason) {
                for (IntendedMeans im: i.getIMs()) 
                    if (im.getTrigger().isAddition() && im.getTrigger().isGoal()) 
                        goalSuspended(im.getTrigger(), reason);                         
            }
            
            public void intentionResumed(Intention i) {
                for (IntendedMeans im: i.getIMs()) 
                    if (im.getTrigger().isAddition() && im.getTrigger().isGoal()) 
                        goalResumed(im.getTrigger());                         
            }
            
            public void eventAdded(Event e) {
                if (e.getTrigger().isAddition() && e.getTrigger().isGoal())
                    goalStarted(e);
            }
            
            public void intentionAdded(Intention i) {  }
        });
    }

    public void goalStarted(Event goal) {
        generateGoalStateEvent(goal.getTrigger().getLiteral(), TEType.achieve, GoalStates.started, null);
    }
    
    public void goalFailed(Trigger goal) {
        generateGoalStateEvent(goal.getLiteral(), goal.getType(), GoalStates.failed, null);
    }

    public void goalFinished(Trigger goal) {
        generateGoalStateEvent(goal.getLiteral(), goal.getType(), GoalStates.finished, null);
    }

    public void goalResumed(Trigger goal) {
        generateGoalStateEvent(goal.getLiteral(), goal.getType(), GoalStates.resumed, null);
    }

    public void goalSuspended(Trigger goal, String reason) {
        generateGoalStateEvent(goal.getLiteral(), goal.getType(), GoalStates.suspended, reason);
    }

    private void generateGoalStateEvent(Literal goal, TEType type, GoalStates state, String reason) {
        goal = goal.forceFullLiteralImpl().copy();
        goal.addAnnot( ASSyntax.createStructure("state", new Atom(state.toString())));
        if (reason != null)
            goal.addAnnot( ASSyntax.createStructure("reason", new StringTermImpl(reason)));
        Trigger eEnd = new Trigger(TEOperator.goalState, type, goal);
        if (ts.getAg().getPL().hasCandidatePlan(eEnd)) {
            ts.getC().addEvent(new Event(eEnd, null)); // TODO: discuss whether put this event on top of i or null
        }
    }

}
