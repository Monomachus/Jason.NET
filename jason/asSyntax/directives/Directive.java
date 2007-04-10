package jason.asSyntax.directives;

import jason.asSemantics.Agent;
import jason.asSyntax.Pred;

/** 
 * Interface for all compiler directives (e.g. include and goal patterns).
 * 
 * <p>There are two kinds of directives: single directive and begin/end directive. 
 * The single directive does not have inner plans, as, for instance, the 
 * include:
 * <blockquote><code>
 *     ...<br>
 *     { include("a.asl") } <br>
 *     ...<br>
 * </code></blockquote>
 * 
 * <p>begin/end directives have inner plans, as used in goal patterns:
 * <blockquote><code>
 *     ...<br>
 *     { begin ebdg(g) } <br>
 *     +!g : bel <- action1. <br>
 *     +!g       <- action2. <br>
 *     { end } <br>
 *     ...<br>
 * </code></blockquote>
 * This pattern will change these two plans to:
 * <blockquote><code>
 *     +!g : g. <br>
 *     +!g : not (p__1(g)) & bel <- +p__1(g); action1; ?g. <br>
 *     +!g : not (p__2(g))       <- +p__2(g); action2; ?g. <br>
 *     -!g <- !g. <br>
 *      +g <- -p__1(g); -p__2(g); .dropGoal(g,true). <br>
 * </code></blockquote>
 * 
 * Goal patterns are proposed in the paper: <br> 
 * <blockquote> 
 * Jomi Fred Hubner, Rafael H. Bordini, and Michael Wooldridge. <br>
 * Programming declarative goals using plan patterns. <br> In Matteo
 * Baldoni and Ulle Endriss, editors, <i>Proceedings of the Fourth
 * International Workshop on Declarative Agent Languages and
 * Technologies</i> (DALT 2006), held with AAMAS 2006, 8th May, Hakodate,
 * Japan, pages 65-81. Springer, 2006. <br> 
 * </blockquote>
 * 
 * @author jomi
 */
public interface Directive {
    /**
     * This method is called to process the directive.
     * 
     * @param directive: the directive as defined in the source (e.g. "include("bla.asl")")
     * @param outterContent: the representation of the agent where the directive is being processed (the method should not change this agent state)
     * @param innerContent: the content (plans, beliefs, ...) inside the begin/end directive (as in goal patterns)
     * @return the agent (plans, bels, ...) with the result of the directive.
     */
    Agent process(Pred directive, Agent outterContent, Agent innerContent);
}
