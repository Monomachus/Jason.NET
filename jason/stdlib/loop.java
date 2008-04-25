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

// TODO: comments
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
        	if ( !(args[0] instanceof LogicalFormula))
        		throw new JasonException("The first argument of .while must be a logical formula.");
        	
        	IntendedMeans im = ts.getC().getSelectedIntention().peek();
        	PlanBody whileia = im.getCurrentStep();

        	// store a backup of the unifier for the next iteration
        	
            // if the IA has a backup unifier, use that (it is an object term)
            if (args.length == 3) {
            	// restore the unifier of previous iterations
            	Unifier ubak = (Unifier)((ObjectTerm)args[2]).getObject();
            	un.clear();
            	un.compose(ubak);
            } else {
	            // add backup unifier in the IA
            	//ubak = (Unifier)un.clone();
	        	((Structure)whileia.getBodyTerm()).addTerm(new ObjectTermImpl(un.clone()));
            }
            
            LogicalFormula logExpr = (LogicalFormula)args[0].clone();
            logExpr.apply(un); // need to apply since the internal action literal for while does not apply
            Iterator<Unifier> iu = logExpr.logicalConsequence(ts.getAg(), un); //(Unifier)un.clone());
            if (iu.hasNext()) {	
	            if ( !args[1].isPlanBody())
	        		throw new JasonException("The second argument of .while must be a plan body term.");
	            
            	un.compose(iu.next());
            	
            	PlanBody whattoadd = (PlanBody)args[1]; //.clone(); 
	            whattoadd.add(new PlanBodyImpl(BodyType.internalAction, (Term)whileia.getBodyTerm().clone())); //(PlanBody)whileia.clone()); // add the while after 
	        	whattoadd.setAsBodyTerm(false);
	        	if (whileia.getPlanSize() == 1)
	        		whileia.add(whattoadd);
	        	else
	        		whileia.add(1,whattoadd);
	        	//System.out.println("new body="+whileia.getBodyNext());
            }
            return true;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new JasonException("The internal action 'while' has not received the required arguments.");
        } catch (JasonException e) {
        	throw e;
        } catch (Exception e) {
            throw new JasonException("Error in internal action 'while': " + e, e);
        }
    }
}
