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
// http://www.csc.liv.ac.uk/~bordini
// http://www.inf.furb.br/~jomi
//----------------------------------------------------------------------------

package jason.stdlib;

import jIDE.RunCentralisedMAS;

import org.apache.log4j.Logger;

import jason.JasonException;
import jason.architecture.CentralisedAgArch;
import jason.asSemantics.Agent;
import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.StringTerm;
import jason.asSyntax.Term;

public class createAgent implements InternalAction {

    private static Logger logger = Logger.getLogger(createAgent.class);

	/* args[0] the agent name
	 * args[1] the agent code (as StringTerm)
	 */
	public boolean execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
		
		try {
			if (!(ts.getAgArch() instanceof CentralisedAgArch)) {
				throw new JasonException("Create agent is implement only for centralised architecture yet!");				
			}
            CentralisedAgArch agArch = new CentralisedAgArch();//(CentralisedAgArch)Class.forName(Agent.class.getName()).newInstance();
            
            Term name = (Term)args[0].clone();
            un.apply(name);
            agArch.setAgName(name.toString());
            
            StringTerm source = (StringTerm)args[1].clone();
            un.apply(source);
            
            // parameters for ini
            String[] agArgs = { Agent.class.getName(), source.getValue()};
            agArch.initAg(agArgs);
            
            agArch.setEnv( ((CentralisedAgArch)ts.getAgArch()).getEnv());
            agArch.setControl( ((CentralisedAgArch)ts.getAgArch()).getControl());
            agArch.getEnv().addAgent(agArch);
            agArch.start();
            logger.debug("Agent "+name+" created!");
            return true;
		} catch (IndexOutOfBoundsException e) {
			throw new JasonException("The internal action 'createAgent' has not received the right number of arguments");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
}
