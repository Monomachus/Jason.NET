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
//   Revision 1.1  2006/02/27 18:43:52  jomifred
//   creation of the RuntimeServices interface
//
//   Revision 1.7  2005/12/31 16:29:58  jomifred
//   add operator =..
//
//   Revision 1.6  2005/12/23 00:51:00  jomifred
//   StringTerm is now an interface implemented by StringTermImpl
//
//   Revision 1.5  2005/12/22 00:04:19  jomifred
//   ListTerm is now an interface implemented by ListTermImpl
//
//   Revision 1.4  2005/11/09 23:39:01  jomifred
//   works for strings, numbers, ...
//
//   Revision 1.3  2005/08/12 22:20:10  jomifred
//   add cvs keywords
//
//
//----------------------------------------------------------------------------

package jason.stdlib;

import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Term;

public class wait implements InternalAction {

	/**
	 * args[0] is the time (in ms)
	 */
	public boolean execute(TransitionSystem ts, Unifier un, Term[] args)	throws Exception {
		NumberTerm time = (NumberTerm)args[0].clone();
		un.apply((Term)time);
		try {
			Thread.sleep((long)time.solve());
		} catch (Exception e) {		}
		return true;
	}
}
