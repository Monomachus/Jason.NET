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
import jason.asSemantics.Message;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;

public class broadcast extends DefaultInternalAction {

    @Override
	public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
		Term ilf = null;
		Term pcnt = null;

		try {
			ilf = (Term) args[0].clone();
			if (!ilf.isGround()) {
				throw new JasonException("The Ilf Force parameter of the internal action 'broadcast' is not a ground term!");
			}

			pcnt = (Term)args[1].clone();
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new JasonException("The internal action 'broadcast' has not received two arguments");
		}
		un.apply(pcnt);
		if (!pcnt.isGround()) {
			throw new JasonException("The content of the message '" + pcnt + "' is not ground!");
		}

		Message m = new Message(ilf.toString(), null, null, pcnt.toString());

		try {
			ts.getUserAgArch().broadcast(m);
			return true;
		} catch (Exception e) {
			throw new JasonException("Error broadcasting message " + pcnt);
		}
	}

}
