// ----------------------------------------------------------------------------
// Copyright (C) 2003 Rafael H. Bordini, Jomi F. Hubner, et al.
// 
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
// 
// To contact the authors:
// http://www.dur.ac.uk/r.bordini
// http://www.inf.furb.br/~jomi
//
// CVS information:
//   $Date$
//   $Revision$
//   $Log$
//   Revision 1.10  2006/01/04 03:00:47  jomifred
//   using java log API instead of apache log
//
//   Revision 1.9  2005/12/23 00:51:00  jomifred
//   StringTerm is now an interface implemented by StringTermImpl
//
//   Revision 1.8  2005/08/12 22:20:10  jomifred
//   add cvs keywords
//
//
//----------------------------------------------------------------------------

package jason.stdlib;

import jason.JasonException;
import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.StringTerm;
import jason.asSyntax.Term;

public class print implements InternalAction {

	public boolean execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
		if (args.length == 0) {
			throw new JasonException(".print without parameters!");
		}

		StringBuffer sout = new StringBuffer("saying: ");
		for (int i = 0; i < args.length; i++) {
			if (args[i].isString()) {
				StringTerm st = (StringTerm)args[i];
				sout.append(st.getString());
			} else {
				Term t = (Term)args[i].clone();
				un.apply(t);
				if (! t.isVar()) {
					sout.append(t);
				} else {
					sout.append(t+"<no-value>");
				}
			}
		}
		ts.getLogger().info(sout.toString());

		return true;
	}
}