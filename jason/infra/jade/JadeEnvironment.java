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
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.ControllerException;
import jason.asSemantics.Message;
import jason.asSyntax.Structure;
import jason.environment.Environment;
import jason.environment.EnvironmentInfraTier;
import jason.mas2j.ClassParameters;
import jason.runtime.RuntimeServicesInfraTier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
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

    private Environment userEnv;

    static Logger logger = Logger.getLogger(JadeEnvironment.class.getName());

    public JadeEnvironment() {
    }

    @Override
    public void setup()  {
        // create the user environment
        try {
            Object[] args = getArguments();
            if (args != null && args.length > 0 && args[0] instanceof ClassParameters) { // it is an mas2j parameter
                ClassParameters ep = (ClassParameters)args[0];
                userEnv = (Environment) Class.forName(ep.className).newInstance();
                userEnv.setEnvironmentInfraTier(this);
                userEnv.init(ep.getParametersArray());
            } else {
                logger.warning("Environment parameters is not implemented yet!");
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error in setup Jade Environment", e);
        }

        try {
            // add a message handler to answer perception asks
            // this handler filter is
            // . content: getPercepts
            final MessageTemplate pt = MessageTemplate.MatchContent("getPercepts");
            addBehaviour(new CyclicBehaviour() {
                public void action() {
                    ACLMessage m = receive(pt);
                    if (m == null) {
                        block();
                    } else {
                        ACLMessage r = new ACLMessage(ACLMessage.INFORM);
                        r.addReceiver(m.getSender());
                        r.setInReplyTo(m.getReplyWith());
                        ArrayList percepts = (ArrayList)userEnv.getPercepts(m.getSender().getLocalName());
                        try {
                            if (percepts == null) {
                                r.setContentObject("nothing_new");
                            } else {
                                synchronized (percepts) {
                                    r.setContentObject(percepts);
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        send(r);
                    }
                }
            });

            // add a message handler to answer action asks
            final MessageTemplate at = MessageTemplate.MatchOntology("AS-actions");
            addBehaviour(new CyclicBehaviour() {
                public void action() {
                    ACLMessage m = receive(at);
                    if (m == null) {
                        block();
                    } else {
                        ACLMessage r = new ACLMessage(ACLMessage.INFORM);
                        r.addReceiver(m.getSender());
                        r.setInReplyTo(m.getReplyWith());
                        r.setOntology(m.getOntology());
                        Structure action;
                        try {
                            action = (Structure)m.getContentObject();
                            if (userEnv.executeAction(m.getSender().getLocalName(), action)) {
                                r.setContent("ok");
                            } else {
                                r.setContent("error");
                            }
                            send(r);
                        } catch (Exception e) {
                            e.printStackTrace();
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
    
    public void informAgsEnvironmentChanged() {
        try {
            broadcast(new Message("tell", null, null, "environmentChanged"));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error sending notifyEvents ", e);
        }
    }

    public void informAgsEnvironmentChanged(Collection agentsToNotify) {
        try {
            if (agentsToNotify == null) {
                informAgsEnvironmentChanged();
            } else {
                ACLMessage m = new ACLMessage(ACLMessage.INFORM);
                m.setContent("environmentChanged");
                Iterator i = agentsToNotify.iterator();
                while (i.hasNext()) {
                    m.addReceiver(new AID(i.next().toString(), AID.ISLOCALNAME));
                }
                send(m);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error sending notifyEvents ", e);
        }
    }

    public RuntimeServicesInfraTier getRuntimeServices() {
        try {
            return new JadeRuntimeServices(getContainerController().getPlatformController());
        } catch (ControllerException e) {
            e.printStackTrace();
        }
        return null;
    }
}
