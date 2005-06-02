package jason.stdlib;

import jason.D;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;

public class concat { 

    public static  boolean execute(TransitionSystem ts, Unifier un, String[] args) throws Exception  {
        Term l1 = Term.parse(args[0]);
		Term l2 = Term.parse(args[1]);
		Term l3 = Term.parse(args[2]);
		un.apply(l1);
		un.apply(l2);
		un.apply(l3);
		if (!l1.isList())
			return false;
		if (!l2.isList())
			return false;
		if (!l3.isVar())
			return false;

		Term c1 = (Term) l1.clone();
		Term c2 = (Term) l2.clone();
		Term t = c1;
		while (!t.getFunctor().equals(D.EmptyList)) {
			t = (Term) t.getTerms().get(1);
		}
		t.set(c2);
		un.unifies(l3, c1);
		return (true);
    }
}

