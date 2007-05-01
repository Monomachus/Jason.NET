package jason.asSyntax;

import jason.asSemantics.Agent;
import jason.asSemantics.Unifier;
import jason.util.ToDOM;

import java.util.Iterator;

/**
 * Represents a logical formula (p, p & q, not p, 3 > X, ...) which can be 
 * evaluated into a truth value.
 * 
 * @author Jomi
 */
public interface LogicalFormula extends Cloneable, ToDOM {
    /**
     * Checks whether the formula is a
     * logical consequence of the belief base.
     * 
     * Returns an iterator for all unifiers that are consequence.
     */
    public Iterator<Unifier> logicalConsequence(Agent ag, Unifier un);

    /** 
     *  Applies variables's values in an unifier to the variables in the formula.
     *  Returns true if some variable was applied.  
     */
    public boolean apply(Unifier u);

    public Object clone();
}
