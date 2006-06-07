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
import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.StringTerm;
import jason.asSyntax.Term;
import jason.runtime.RuntimeServicesInfraTier;

import java.io.File;

public class createAgent implements InternalAction {

    /**
     * args[0] is the agent name; args[1] is the agent code (as StringTerm)
     */
    public boolean execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {

        try {
            Term name = (Term) args[0].clone();
            un.apply(name);

            StringTerm source = (StringTerm) args[1].clone();
            un.apply((Term) source);

            File fSource = new File(source.getString());
            if (!fSource.exists()) {
                throw new JasonException("The file source " + source + " was not found!");
            }

            RuntimeServicesInfraTier rs = ts.getUserAgArch().getArchInfraTier().getRuntimeServices();
            return rs.createAgent(name.toString(), fSource.getAbsolutePath(), null, null, null, ts.getSettings());

        } catch (IndexOutOfBoundsException e) {
            throw new JasonException("The internal action 'createAgent' received a wrong number of arguments");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
