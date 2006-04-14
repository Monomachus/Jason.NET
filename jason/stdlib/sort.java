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
//   $Date: 2006-02-22 18:21:12 -0300 (Wed, 22 Feb 2006) $
//   $Revision: 294 $
//   $Log$
//   Revision 1.8  2006/02/22 21:19:05  jomifred
//   The internalAction removePlan use plan's label as argument instead of plan's strings
//
//   Revision 1.7  2005/12/22 00:04:19  jomifred
//   ListTerm is now an interface implemented by ListTermImpl
//
//   Revision 1.6  2005/08/12 21:12:50  jomifred
//   add cvs keywords
//
//----------------------------------------------------------------------------


package jason.stdlib;

import jason.JasonException;
import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.ListTerm;
import jason.asSyntax.Term;

import java.util.Collections;

public class sort implements InternalAction {
    
	/**
	 * args[0] = the unsorted list
	 * args[1] = the sorted list
	 */
    public boolean execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        try {
            ListTerm l1 = (ListTerm) args[0].clone();
            ListTerm l2 = (ListTerm) args[1].clone();
            Collections.sort(l1);
            return un.unifies((Term)l1, (Term)l2);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new JasonException("The internal action 'sort' has not received two arguments");
        } catch (Exception e) {
            throw new JasonException("Error in internal action 'sorts': " + e);
        }    
    }
}
