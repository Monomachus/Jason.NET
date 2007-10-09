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

package jason.infra.jade;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jason.asSemantics.Message;
import jason.asSyntax.Literal;
import jason.asSyntax.Structure;
import jason.environment.Environment;
import jason.environment.EnvironmentInfraTier;
import jason.mas2j.ClassParameters;
import jason.runtime.RuntimeServicesInfraTier;

import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class implements the Jade version of the environment
 * infrastructure tier. 
 * 
 * @author Jomi
 */
@SuppressWarnings("serial")
public class JadeEnvironment extends JadeAg implements EnvironmentInfraTier {

    private static Logger logger = Logger.getLogger(JadeEnvironment.class.getName());

    public static String actionOntology     = "AS-actions";
    public static String perceptionOntology = "AS-perception";
    
    private Environment userEnv;

    public JadeEnvironment() {
    }

    @Override
    public void setup()  {
        // create the user environment
        try {
            Object[] args = getArguments();
            if (args != null && args.length > 0) {
                if (args[0] instanceof ClassParameters) { // it is an mas2j parameter
                    ClassParameters ep = (ClassParameters)args[0];
                    userEnv = (Environment) Class.forName(ep.className).newInstance();
                    userEnv.setEnvironmentInfraTier(this);
                    userEnv.init(ep.getParametersArray());
                } else {
                    userEnv = (Environment) Class.forName(args[0].toString()).newInstance();
                    userEnv.setEnvironmentInfraTier(this);
                    //userEnv.init(ep.getParametersArray());
                    if (args.length > 1) {
                        logger.warning("Environment arguments is not implemented yet (ask it to us if you need)!");
                    }
                }
            } else {
                logger.warning("Using default environment.");
                userEnv = new Environment();
                userEnv.setEnvironmentInfraTier(this);                
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error in setup Jade Environment", e);
        }

        // DF register
        DFAgentDescription dfa = new DFAgentDescription();
        dfa.setName(getAID());
        ServiceDescription vc = new ServiceDescription();
        vc.setType("jason");
        vc.setName(RunJadeMAS.environmentName);
        dfa.addServices(vc);
        try {
            DFService.register(this,dfa);
        } catch (FIPAException e) {
            logger.log(Level.SEVERE, "Error registering environment in DF", e);
        }

        try {
            // add a message handler to answer perception asks
            // and actions asks
            addBehaviour(new CyclicBehaviour() {
                ACLMessage m;
                public void action() {
                    m = receive();
                    if (m == null) {
                        block(1000);
                    } else {
                        // is getPerceps
                        if (m.getContent().equals("getPercepts")) {
                            ACLMessage r = m.createReply();
                            r.setPerformative(ACLMessage.INFORM);
                            try {
                                List<Literal> percepts = userEnv.getPercepts(m.getSender().getLocalName());
                                if (percepts == null) {
                                    r.setContent("nothing_new");
                                } else {
                                    synchronized (percepts) {
                                        r.setContent(percepts.toString());
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            send(r);
                            
                        // is action?    
                        } else if (m.getOntology().equals(actionOntology)) {
                            ACLMessage r = m.createReply();
                            r.setPerformative(ACLMessage.INFORM);
                            try {
                                Structure action = Structure.parse(m.getContent());
                                userEnv.scheduleAction(m.getSender().getLocalName(), action, r);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }                            
                        }
                    }
                }
            });

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error starting agent", e);
        }
    }

    @Override
    protected void takeDown() {
        if (userEnv != null) userEnv.stop();
    }
    
    public void actionExecuted(String agName, Structure actTerm, boolean success, Object infraData) {
        try {
            ACLMessage r = (ACLMessage)infraData;
            if (success) {
                r.setContent("ok");
            } else {
                r.setContent("error");
            }
            send(r);
        } catch (Exception e) {
            e.printStackTrace();
        }                            
    }

    public void informAgsEnvironmentChanged() {
        broadcast(new Message("tell", null, null, "environmentChanged"));
    }

    public void informAgsEnvironmentChanged(Collection<String> agentsToNotify) {
        try {
            if (agentsToNotify == null) {
                informAgsEnvironmentChanged();
            } else {
                ACLMessage m = new ACLMessage(ACLMessage.INFORM);
                m.setContent("environmentChanged");
                for (String ag: agentsToNotify) {
                    m.addReceiver(new AID(ag, AID.ISLOCALNAME));
                }
                send(m);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error sending notifyEvents ", e);
        }
    }

    public RuntimeServicesInfraTier getRuntimeServices() {
        return new JadeRuntimeServices(getContainerController(), this);
    }
}
