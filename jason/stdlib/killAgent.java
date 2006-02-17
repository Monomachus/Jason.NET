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
//   Revision 1.3  2006/02/17 13:13:16  jomifred
//   change a lot of method/classes names and improve some comments
//
//   Revision 1.2  2006/01/04 03:00:47  jomifred
//   using java log API instead of apache log
//
//   Revision 1.1  2005/08/18 13:32:45  jomifred
//   initial implementation (based on createAg)
//
//----------------------------------------------------------------------------

package jason.stdlib;

import jason.JasonException;
import jason.architecture.CentralisedAgArch;
import jason.architecture.SaciAgArch;
import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;
import jason.environment.CentralisedEnvironment;

import java.util.Iterator;
import java.util.logging.Logger;

import saci.launcher.AgentId;
import saci.launcher.Launcher;
import saci.launcher.LauncherD;

public class killAgent implements InternalAction {

    private static Logger logger = Logger.getLogger(killAgent.class.getName());

	/* args[0] the agent name */
	public boolean execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
		
		try {
            Term name = (Term)args[0].clone();
            un.apply(name);
            
            if (ts.getUserAgArch().getArchInfraTier() instanceof CentralisedAgArch) {
    			CentralisedAgArch ag = (CentralisedAgArch)ts.getUserAgArch().getArchInfraTier();
            	return killCentralisedAg(name.toString(), ag.getEnvInfraTier());

            } else if (ts.getUserAgArch().getArchInfraTier() instanceof SaciAgArch) {
    			SaciAgArch ag = (SaciAgArch)ts.getUserAgArch().getArchInfraTier();
            	return killSaciAg(name.toString(), ag.getSociety());
            
            } else {
				throw new JasonException("Create agent is currently implemented only for Centralised/Saci infrastructure!");				
			}
		} catch (IndexOutOfBoundsException e) {
			throw new JasonException("The internal action 'killAgent' received a wrong number of arguments");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public boolean killSaciAg(String name, String socName) {
		try {
			logger.fine("Killing Saci agent "+name);
			
			// gets the saci launcher
			Launcher l = LauncherD.getLauncher();
			Iterator i = l.getAllAgentsID().iterator();
			while (i.hasNext()) {
				AgentId aid = (AgentId)i.next();
				if (aid.getName().equals(name) && aid.getSociety().equals(socName)) {
					return l.killAg(aid).booleanValue();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean killCentralisedAg(String name, CentralisedEnvironment env) {
		try {
			logger.fine("Killing centralised agent "+name);
			env.getAgent(name).stopAg();
            return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;	
	}
}
