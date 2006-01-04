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
//   Revision 1.16  2006/01/04 03:00:47  jomifred
//   using java log API instead of apache log
//
//----------------------------------------------------------------------------

package jason.stdlib;

import jIDE.JasonID;
import jason.JasonException;
import jason.architecture.AgArch;
import jason.architecture.CentralisedAgArch;
import jason.architecture.SaciAgArch;
import jason.asSemantics.Agent;
import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.StringTerm;
import jason.asSyntax.Term;
import jason.control.CentralisedExecutionControl;
import jason.environment.CentralisedEnvironment;

import java.io.File;
import java.util.logging.Logger;

import saci.launcher.Command;
import saci.launcher.Launcher;
import saci.launcher.LauncherD;

public class createAgent implements InternalAction {

    private static Logger logger = Logger.getLogger(createAgent.class.getName());

	/** args[0] is the agent name
	 *  args[1] is the agent code (as StringTerm)
	 */
	public boolean execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
		
		try {
            Term name = (Term)args[0].clone();
            un.apply(name);
            
            StringTerm source = (StringTerm)args[1].clone();
            un.apply((Term)source);
            
            File fSource = new File(source.getString());
            if (! fSource.exists()) {
            	
            	// try to get the project directory (only when running inside JasonID)
            	if (JasonID.currentJasonID != null) {
            		logger.fine("trying to find the source at "+JasonID.currentJasonID.getProjectDirectory());
            		fSource = new File(JasonID.currentJasonID.getProjectDirectory()+File.separator+source.getString());
                    if (! fSource.exists()) {
                		throw new JasonException("The file source "+source+" was not found!");
                    }
                    logger.fine("Ok, found "+fSource.getAbsolutePath());
            	} else {
            		throw new JasonException("The file source "+source+" was not found!");
            	}
            }
            
            if ( ((AgArch)ts.getAgArch()).getInfraArch() instanceof CentralisedAgArch) {
            	CentralisedAgArch ag = (CentralisedAgArch)((AgArch)ts.getAgArch()).getInfraArch();
            	return createCentralisedAg(name.toString(), fSource.getAbsolutePath(), ag.getEnv(), ag.getControl(), ts.getSettings().isSync());
            } else if (((AgArch)ts.getAgArch()).getInfraArch() instanceof SaciAgArch) {
            	SaciAgArch ag = (SaciAgArch)((AgArch)ts.getAgArch()).getInfraArch();
            	return createSaciAg(name.toString(), ag.getSociety(), fSource.getAbsolutePath(), ts.getSettings().isSync());
            } else {
				throw new JasonException("Create agent is currently implemented only for the Centralised/Saci infrastructure!");				
			}
		} catch (IndexOutOfBoundsException e) {
			throw new JasonException("The internal action 'createAgent' received a wrong number of arguments");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public boolean createSaciAg(String name, String socName, String source, boolean isSync) {
		try {
			logger.fine("Creating saci agent from source "+source);

			String extraOp = "";
			if (isSync) {
				extraOp = " options verbose=2,synchronised=true";
			}
			// gets the saci launcher
			Launcher l = LauncherD.getLauncher();
			Command c1 = new Command(Command.START_AGENT);
			c1.addArg("class", SaciAgArch.class.getName());
			c1.addArg("name", name);
			c1.addArg("society.name", socName);
			c1.addArg("args", AgArch.class.getName()+" "+Agent.class.getName() + " " + source + extraOp);
			//c1.addArg("host", "?");
			l.execCommand(c1);
            logger.fine("Agent "+name+" created!");
            return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean createCentralisedAg(String name, String source, CentralisedEnvironment env, CentralisedExecutionControl control, boolean isSync) {
		try {
			logger.fine("Creating centralised agent from source "+source);
            // parameters for ini
			
            String[] agArgs = { AgArch.class.getName(), Agent.class.getName(), source};

            CentralisedAgArch agArch = new CentralisedAgArch();//(CentralisedAgArch)Class.forName(Agent.class.getName()).newInstance();
            agArch.setAgName(name.toString());
            agArch.initAg(agArgs);
            agArch.setEnv(env);
            agArch.setControl(control);
            if (isSync) {
            	agArch.getUserAgArch().getTS().getSettings().setSync(true);
            }
            agArch.getEnv().addAgent(agArch.getUserAgArch());
            agArch.start();
            logger.fine("Agent "+name+" created!");
            return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;	
	}
}
