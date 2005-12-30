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
// CVS information:
//   $Date$
//   $Revision$
//   $Log$
//   Revision 1.5  2005/12/30 20:40:40  jomifred
//   new features: unnamed var, var with annots, TE as var
//
//   Revision 1.4  2005/08/12 22:20:10  jomifred
//   add cvs keywords
//
//
//----------------------------------------------------------------------------


package jason.stdlib;

import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Literal;
import jason.asSyntax.Pred;
import jason.asSyntax.Term;

public class unifies implements InternalAction {
    public boolean execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
    	// try to cast both to Literal
    	try {
            return un.unifies((Literal)args[0], (Literal)args[1]);    		
    	} catch (Exception e1) {
    		// try to cast both to Pred
    		try {
    			return un.unifies((Pred)args[0], (Pred)args[1]);
    		} catch (Exception e2) {
    			// use args as Terms
    			return un.unifies(args[0], args[1]);
    		}
    	}
    }
}
