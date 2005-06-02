package jason.stdlib;

import jason.JasonException;
import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.ListTerm;
import jason.asSyntax.Term;

public class concat implements InternalAction {

	/**
	 * Concat list args[0] with args[1] and unifies with args[2]
	 */
	public boolean execute(TransitionSystem ts, Unifier un, Term[] args)	throws Exception {
		Term l1 = (Term)args[0].clone();
		Term l2 = (Term)args[1].clone();
		Term l3 = (Term)args[2].clone();
		un.apply(l1);
		un.apply(l2);
		un.apply(l3);
		if (!l1.isList()) {
			throw new JasonException("arg[0] is not a list (concat)");
		}
		if (!l2.isList()) {
			throw new JasonException("arg[1] is not a list (concat)");
		}
		if (!l3.isVar() && !l3.isList()) {
			throw new JasonException("arg[2] is not a list or variable (concat)");
		}
		
		ListTerm l1l = (ListTerm)l1;
		l1l.concat((ListTerm)l2);
		return un.unifies(l3, l1l);
	}
}
