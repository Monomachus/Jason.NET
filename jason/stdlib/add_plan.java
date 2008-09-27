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

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.DefaultTerm;
import jason.asSyntax.ListTerm;
import jason.asSyntax.StringTerm;
import jason.asSyntax.Term;
import jason.bb.BeliefBase;


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
  
  <li><i>+ position</i> (atom [optional]): if value is "before" the plan
  will be added in the begin of the plan library. 
  The default value is <code>end</code>.<br/>

  </ul>
  
  Note that if only two parameter is informed, the second will be the source and not
  the position.
  
  <p>Examples:<ul> 

  <li> <code>.add_plan("+b : true &lt;- .print(b).")</code>: adds the plan
  <code>+b : true &lt;- .print(b).</code> to the agent's plan library
  with a plan label annotated with <code>source(self)</code>.</li>

  <li> <code>.add_plan("+b : true &lt;- .print(b).", rafa)</code>: same as
  the previous example, but the source of the plan is agent
  "rafa".</li>

  <li> <code>.add_plan("+b : true &lt;- .print(b).", rafa, begin)</code>: same as
  the previous example, but the plan is added in the begin of the plan library.</li>

  <li> <code>.add_plan(["+b : true &lt;- .print(b).", "+b : bel &lt;-
  .print(bel)."], rafa)</code>: adds both plans with "rafa" as their
  sources.</li>

  </ul>

  @see jason.stdlib.plan_label
  @see jason.stdlib.relevant_plans
  @see jason.stdlib.remove_plan

  @author Jomi
 */
public class add_plan extends DefaultInternalAction {

    @Override public int getMinArgs() { return 1; }
    @Override public int getMaxArgs() { return 3; }

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        checkArguments(args);

        Term plans = DefaultTerm.parse(args[0].toString());

        Term source = BeliefBase.ASelf;
        if (args.length > 1)
            source = args[1];

        boolean before = false;
        if (args.length > 2)
            before = args[2].toString().equals("begin");

        if (plans.isList()) { // arg[0] is a list of strings
            for (Term t: (ListTerm) plans)
                ts.getAg().getPL().add((StringTerm) t, source);
        } else { // args[0] is a plan
            ts.getAg().getPL().add((StringTerm) plans, source, before);
        }
        return true;
    }
}
