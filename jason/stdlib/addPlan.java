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
import jason.asSyntax.ListTerm;
import jason.asSyntax.StringTerm;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;
import jason.asSyntax.DefaultTerm;

import java.util.Iterator;


/**
  <p>Internal action: <b><code>.addPlan</code></b>.
  
  <p>Description: adds plan(s) into the agent plan library.
  
  <p>Parameters:<ul>
  
  <li>+ arg[0] (string or list): the string representing the plan to
  be added. If it is a list, all strings of the list will be added as
  plans. The syntax of the string is the same as ordinary AgentSpeak
  code.<br/>
  
  <li><i>+ arg[1]</i> (structure - optional): the source of the
  plan. The default value is <code>self</code>.<br/>
  
  </ul>
  
  <p>Examples:<ul> 

  <li> <code>.addPlan("+b : true &lt;- .print(b).")</code>: adds the plan
  <code>+b : true &lt;- .print(b).</code> into the agent's plan library
  with a plan label annotated with <code>source(self)</code>.</li>

  <li> <code>.addPlan("+b : true &lt;- .print(b).", rafa)</code>: same as
  the previous example, but the source of the plan is agent
  "rafa".</li>

  <li> <code>.addPlan(["+b : true &lt;- .print(b).", "+b : bel &lt;-
  .print(bbel)."], rafa)</code>: adds both plans with "rafa" as they
  source.</li>

  </ul>

 */
public class addPlan extends DefaultInternalAction {

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        try {
            Term plans = DefaultTerm.parse(args[0].toString());

            Structure source = new Structure("self");
            if (args.length > 1) {
                source = (Structure) args[1].clone();
                un.apply(source);
            }

            if (plans.isList()) { // if arg[0] is a list of strings
                ListTerm lt = (ListTerm) plans;
                Iterator i = lt.iterator();
                while (i.hasNext()) {
                    ts.getAg().getPL().add((StringTerm) i.next(), source);
                }
            } else { // args[0] is a plan
                ts.getAg().getPL().add((StringTerm) plans, source);
            }
            return true;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new JasonException("The internal action 'addPlan' has not received two arguments (plan's string and source)");
        } catch (Exception e) {
            throw new JasonException("Error in internal action 'addPlan': " + e);
        }
    }
}
