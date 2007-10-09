//----------------------------------------------------------------------------
// Copyright (C) 2003  Rafael H. Bordini, Jomi F. Hubner, et al.
// 
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
// 
// To contact the authors:
// http://www.dur.ac.uk/r.bordini
// http://www.inf.furb.br/~jomi
//
//----------------------------------------------------------------------------

package jason.stdlib;

import jason.JasonException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Atom;
import jason.asSyntax.DefaultTerm;
import jason.asSyntax.ListTerm;
import jason.asSyntax.StringTerm;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;


/**
  <p>Internal action: <b><code>.add_plan</code></b>.
  
  <p>Description: adds plan(s) to the agent's plan library.
  
  <p>Parameters:<ul>
  
  <li>+ plan(s) (string or list): the string representing the plan to be
  added. If it is a list, each string in the list will be parsed into an
  AgentSpeak plan and added to the plan library. The syntax of the code within
  the string is the same as ordinary AgentSpeak code.<br/>
  
  <li><i>+ source</i> (atom [optional]): the source of the
  plan(s). The default value for the source is <code>self</code>.<br/>
  
  </ul>
  
  <p>Examples:<ul> 

  <li> <code>.add_plan("+b : true &lt;- .print(b).")</code>: adds the plan
  <code>+b : true &lt;- .print(b).</code> to the agent's plan library
  with a plan label annotated with <code>source(self)</code>.</li>

  <li> <code>.add_plan("+b : true &lt;- .print(b).", rafa)</code>: same as
  the previous example, but the source of the plan is agent
  "rafa".</li>

  <li> <code>.add_plan(["+b : true &lt;- .print(b).", "+b : bel &lt;-
  .print(bel)."], rafa)</code>: adds both plans with "rafa" as their
  sources.</li>

  </ul>

  @see jason.stdlib.plan_label
  @see jason.stdlib.relevant_plans
  @see jason.stdlib.remove_plan

 */
public class add_plan extends DefaultInternalAction {

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        try {
            Term plans = DefaultTerm.parse(args[0].toString());

            Structure source = new Atom("self");
            if (args.length > 1) {
                source = (Structure) args[1];
            }

            if (plans.isList()) { // arg[0] is a list of strings
                for (Term t: (ListTerm) plans) {
                    ts.getAg().getPL().add((StringTerm) t, source);
                }
            } else { // args[0] is a plan
                ts.getAg().getPL().add((StringTerm) plans, source);
            }
            return true;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new JasonException("The internal action 'add_plan' has not received two arguments (a plan as a string and the source).");
        } catch (Exception e) {
            throw new JasonException("Error in internal action 'add_plan': " + e, e);
        }
    }
}
