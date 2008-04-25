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

import java.util.Iterator;

import jason.JasonException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.IntendedMeans;
import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.LogicalFormula;
import jason.asSyntax.PlanBody;
import jason.asSyntax.Term;

// TODO: comments
public class conditional extends DefaultInternalAction {

	private static InternalAction singleton = null;
	public static InternalAction create() {
		if (singleton == null) 
			singleton = new conditional();
		return singleton;
	}
	
    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        try {
        	if ( !(args[0] instanceof LogicalFormula))
        		throw new JasonException("The first argument of .if must be a logical formula.");
        	
            LogicalFormula logExpr = (LogicalFormula)args[0];
            PlanBody whattoadd = null;
            
            Iterator<Unifier> iu = logExpr.logicalConsequence(ts.getAg(), un);
            if (iu.hasNext()) {	// .if THEN
	            if ( !args[1].isPlanBody())
	        		throw new JasonException("The second argument of .if must be a plan body term.");
                whattoadd = (PlanBody)args[1];
            	un.compose(iu.next());
            } else if (args.length == 3) { // .if ELSE
	            if ( !args[2].isPlanBody())
	        		throw new JasonException("The third argument of .if must be a plan body term.");
                whattoadd = (PlanBody)args[2];
            }

            if (whattoadd != null) {
	        	IntendedMeans im = ts.getC().getSelectedIntention().peek();
	        	PlanBody ifia = im.getCurrentStep();
	        	whattoadd.setAsBodyTerm(false);
	        	ifia.add(1,whattoadd);
            }
            return true;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new JasonException("The internal action 'if' has not received the required arguments.");
        } catch (JasonException e) {
        	throw e;
        } catch (Exception e) {
            throw new JasonException("Error in internal action 'if': " + e, e);
        }
    }
}
