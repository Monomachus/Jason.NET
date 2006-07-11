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
//----------------------------------------------------------------------------

package jason.stdlib;

import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.StringTerm;
import jason.asSyntax.Term;

import java.util.logging.Level;

public class println implements InternalAction {

	protected String getNewLine() {
		return "\n";
	}
	
	public boolean execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
		if (args.length == 0) {
			return true;
			//throw new JasonException(".print without parameters!");
		}

		StringBuffer sout = new StringBuffer("");
        try {
    		if (ts.getSettings().logLevel() != Level.WARNING) {
    			sout = new StringBuffer("saying: ");
    		}
        } catch (Exception e) {}
		
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

        if (ts != null && ts.getSettings().logLevel() != Level.WARNING) {
            ts.getLogger().info(sout.toString());
        } else {
            System.out.print(sout.toString() + getNewLine());
        }

		return true;
	}
}