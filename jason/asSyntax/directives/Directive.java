package jason.asSyntax.directives;

import jason.asSyntax.Literal;
import jason.asSyntax.Plan;
import jason.asSyntax.PlanLibrary;
import jason.asSyntax.Pred;

import java.util.List;

public interface Directive {
    boolean process(Pred directive, List<Plan> innerPlans, List<Literal> bels, PlanLibrary pl);
}
