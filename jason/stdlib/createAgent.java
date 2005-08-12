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
//   Revision 1.6  2005/08/12 23:33:49  jomifred
//   support for saci arch in IA createAgent
//
//----------------------------------------------------------------------------

package jason.stdlib;

import jIDE.JasonID;
import jason.JasonException;
import jason.architecture.CentralisedAgArch;
import jason.architecture.SaciAgArch;
import jason.asSemantics.Agent;
import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.StringTerm;
import jason.asSyntax.Term;

import java.io.File;

import org.apache.log4j.Logger;

import saci.launcher.Command;
import saci.launcher.Launcher;
import saci.launcher.LauncherD;

public class createAgent implements InternalAction {

    private static Logger logger = Logger.getLogger(createAgent.class);

	/* args[0] the agent name
	 * args[1] the agent code (as StringTerm)
	 */
	public boolean execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
		
		try {
            Term name = (Term)args[0].clone();
            un.apply(name);
            
            StringTerm source = (StringTerm)args[1].clone();
            un.apply(source);
            
            File fSource = new File(source.getValue());
            if (! fSource.exists()) {
            	
            	// try to get the project directory (only when running inside JasonID)
            	if (JasonID.currentJasonID != null) {
            		logger.debug("trying to find the source at "+JasonID.currentJasonID.getProjectDirectory());
            		fSource = new File(JasonID.currentJasonID.getProjectDirectory()+File.separator+source.getValue());
                    if (! fSource.exists()) {
                		throw new JasonException("The file source "+source+" was not found!");
                    }
                    logger.debug("Ok, found "+fSource.getAbsolutePath());
            	} else {
            		throw new JasonException("The file source "+source+" was not found!");
            	}
            }
            
            if (ts.getAgArch() instanceof CentralisedAgArch) {
            	return createCentralisedAg(ts, name.toString(), fSource.getAbsolutePath());
            } else if (ts.getAgArch() instanceof SaciAgArch) {
            	return createSaciAg(ts, name.toString(), fSource.getAbsolutePath());
            } else {
				throw new JasonException("Create agent is currently implemented only for the Centralised architecture!");				
			}
		} catch (IndexOutOfBoundsException e) {
			throw new JasonException("The internal action 'createAgent' received a wrong number of arguments");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	boolean createSaciAg(TransitionSystem ts, String name, String source) {
		try {
			logger.debug("Creating saci agent from source "+source);
    
			// gets the saci launcher
			Launcher l = LauncherD.getLauncher();
			Command c1 = new Command(Command.START_AGENT);
			c1.addArg("class", SaciAgArch.class.getName());
			c1.addArg("name", name);
			c1.addArg("society.name", ((SaciAgArch)ts.getAgArch()).getSociety());;
			c1.addArg("args", Agent.class.getName() + " " + source);
			//c1.addArg("host", "?");
			l.execCommand(c1);
			/*
            agArch.setEnv( ((CentralisedAgArch)ts.getAgArch()).getEnv());
            agArch.setControl( ((CentralisedAgArch)ts.getAgArch()).getControl());
            if (agArch.getTS().getSettings().isSync()) {
            	agArch.getTS().getSettings().setSync(true);
            }
            agArch.getEnv().addAgent(agArch);
            agArch.start();
            */
            logger.debug("Agent "+name+" created!");
            return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	boolean createCentralisedAg(TransitionSystem ts, String name, String source) {
		try {
			logger.debug("Creating centralised agent from source "+source);
            // parameters for ini
			
            String[] agArgs = { Agent.class.getName(), source};

            CentralisedAgArch agArch = new CentralisedAgArch();//(CentralisedAgArch)Class.forName(Agent.class.getName()).newInstance();
            agArch.setAgName(name.toString());
            agArch.initAg(agArgs);
            agArch.setEnv( ((CentralisedAgArch)ts.getAgArch()).getEnv());
            agArch.setControl( ((CentralisedAgArch)ts.getAgArch()).getControl());
            if (agArch.getTS().getSettings().isSync()) {
            	agArch.getTS().getSettings().setSync(true);
            }
            agArch.getEnv().addAgent(agArch);
            agArch.start();
            logger.debug("Agent "+name+" created!");
            return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;	
	}
}
