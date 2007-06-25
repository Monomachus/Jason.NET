//----------------------------------------------------------------------------
// Copyright (C) 2003  Rafael H. Bordini and Jomi F. Hubner
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

package jason.infra.jade;

import jade.core.ProfileImpl;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jason.JasonException;
import jason.control.ExecutionControlGUI;
import jason.infra.centralised.RunCentralisedMAS;
import jason.mas2j.AgentParameters;
import jason.mas2j.ClassParameters;
import jason.mas2j.MAS2JProject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Runs MASProject using jade infrastructure.
 * 
 * This class reads the mas2j project and create the 
 * corresponding agents.
 * 
 * @author Jomi
 */
public class RunJadeMAS extends RunCentralisedMAS {

    private static Logger logger = Logger.getLogger(RunJadeMAS.class.getName());
    
    AgentController envc, crtc;
    Map<String,AgentController> ags = new HashMap<String,AgentController>();

    public static void main(String[] args) {
        runner = new RunJadeMAS();
        runner.init(args);
    }
    
    @Override
    protected void createButtons() {
        // TODO: add start RMA, Sniffer, DF buttons
        // TODO: add debug button
    }
    
    @Override
    protected void createAg(MAS2JProject project, boolean debug) throws JasonException {
        try {
            jade.core.Runtime jadeRT = jade.core.Runtime.instance();
            // TODO: use userArgs
            ContainerController cc = jadeRT.createMainContainer(new ProfileImpl()); 
    
            // create environment
            logger.fine("Creating environment " + project.getEnvClass());
            envc = cc.createNewAgent("environment", JadeEnvironment.class.getName(), new Object[] { project.getEnvClass() });

            // create the agents
            for (AgentParameters ap : project.getAgents()) {
                try {
                    ap.setupDefault();
                    String agName = ap.name;
    
                    String tmpAsSrc = ap.asSource.toString();
                    if (!tmpAsSrc.startsWith(File.separator) && !project.getDirectory().equals("."+File.separator)) {
                        tmpAsSrc = project.getDirectory() + tmpAsSrc;
                    }                    
                    for (int cAg = 0; cAg < ap.qty; cAg++) {
                        String numberedAg = agName;
                        if (ap.qty > 1) numberedAg += (cAg + 1);
                        logger.fine("Creating agent " + numberedAg + " (" + (cAg + 1) + "/" + ap.qty + ")");
                        AgentController ac = cc.createNewAgent(numberedAg, JadeAgArch.class.getName(), new Object[] { ap, debug, project.getControlClass() != null });
                        ags.put(numberedAg,ac);
                    }
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Error creating agent " + ap.name, e);
                }
            }
    
            // create controller
            ClassParameters controlClass = project.getControlClass();
            if (debug && controlClass == null) {
                controlClass = new ClassParameters(ExecutionControlGUI.class.getName());
            }
            if (controlClass != null) {
                logger.fine("Creating controller " + controlClass);
                crtc = cc.createNewAgent("controller", JadeExecutionControl.class.getName(), new Object[] { controlClass });
            }
           
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error creating agents: ", e);            
        }
    }

    @Override
    protected void startAgs() {
        try {
            envc.start();
            if (crtc != null) crtc.start();
            
            // run the agents
            for (AgentController ag : ags.values()) {
                ag.start();
            }
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error starting agents.", e);            
        }
    }
    
    @Override
    public void finish() {
        try {
            new JadeRuntimeServices().stopMAS();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error stopping system.", e);            
        }
    }
}
