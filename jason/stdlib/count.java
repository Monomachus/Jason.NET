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
import jason.asSyntax.Literal;
import jason.asSyntax.NumberTermImpl;
import jason.asSyntax.Term;

import java.util.Iterator;

public class count extends DefaultInternalAction {

    /** .count(a(_), N) */
    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        try {
            Literal bel = Literal.parseLiteral(args[0].toString());
            if (bel == null) {
                throw new JasonException("The first parameter ('" + args[1] + "') of the internal action 'count' is not a literal!");
            }
            un.apply(bel);
            int n = 0;
            // find all bel in belief base and build a list with them
            Iterator<Unifier> iu = bel.logCons(ts.getAg(), un);
            while (iu.hasNext()) {
                iu.next();
                n++;
            }
            Term var = args[1];
            return un.unifies(var, new NumberTermImpl(n));
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new JasonException("The internal action 'count' has not received two arguments");
        } catch (Exception e) {
            throw new JasonException("Error in internal action 'count': " + e);
        }
    }
}
