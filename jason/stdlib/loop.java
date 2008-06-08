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
import jason.asSemantics.IntendedMeans;
import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.LogicalFormula;
import jason.asSyntax.ObjectTerm;
import jason.asSyntax.ObjectTermImpl;
import jason.asSyntax.PlanBody;
import jason.asSyntax.PlanBodyImpl;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;
import jason.asSyntax.PlanBody.BodyType;

import java.util.Iterator;

/** 
Implementation of <b>while</b>. 

<p>Syntax:
<pre>
  while ( <i>logical formula</i> ) {
     <i>plan_body</i>
  };
</pre>
</p>

<p>while <i>logical formula</i> holds, the <i>plan_body</i> is executed.</p>

<p>Example:
<pre>
+event : context
  <- ....
     while(vl(X) & X > 10) { // where vl(X) is a belief
       .print("value > 10");
        -+vl(X+1)
     };
     ....
</pre>
The unification resulted from the evaluation of the logical formula is used only inside the loop,
i.e., the unification after the while is the same as before.
</p>

@see jason.stdlib.foreach for

*/
public class loop extends DefaultInternalAction {

	private static InternalAction singleton = null;
	public static InternalAction create() {
		if (singleton == null) 
			singleton = new loop();
		return singleton;
	}
	
    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        try {
        	
        	IntendedMeans im = ts.getC().getSelectedIntention().peek();
        	PlanBody whileia = im.getCurrentStep();

        	// store a backup of the unifier for the next iteration
        	
            // if the IA has a backup unifier, use that (it is an object term)
            if (args.length != 3) {
	            // first execution of while
            	if ( !(args[0] instanceof LogicalFormula))
            		throw new JasonException("The first argument of while must be a logical formula.");
	            if ( !args[1].isPlanBody())
	        		throw new JasonException("The second argument of while must be a plan body term.");
            	
            	// add backup unifier in the IA
	        	((Structure)whileia.getBodyTerm()).addTerm(new ObjectTermImpl(un.clone()));
            } else {
            	// restore the unifier of previous iterations
            	Unifier ubak = (Unifier)((ObjectTerm)args[2]).getObject();
            	un.clear();
            	un.compose(ubak);
            }
            
            LogicalFormula logExpr = (LogicalFormula)args[0]; //.clone();
            //logExpr.apply(un); // need to apply since the internal action literal for while does not apply
            Iterator<Unifier> iu = logExpr.logicalConsequence(ts.getAg(), un); 
            if (iu.hasNext()) {	
	            
            	un.compose(iu.next());
            	
            	PlanBody whattoadd = (PlanBody)args[1]; //.clone(); 
	            whattoadd.add(new PlanBodyImpl(BodyType.internalAction, (Term)whileia.getBodyTerm().clone())); //(PlanBody)whileia.clone()); // add the while after 
	        	whattoadd.setAsBodyTerm(false);
        		whileia.add(1,whattoadd);
	        	//System.out.println("new body="+whileia.getBodyNext());
            }
            return true;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new JasonException("'while' has not received the required arguments.");
        } catch (JasonException e) {
        	throw e;
        } catch (Exception e) {
            throw new JasonException("Error in 'while': " + e, e);
        }
    }
}
