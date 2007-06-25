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

import jade.core.Agent;
import jason.environment.Environment;
import jason.environment.EnvironmentInfraTier;
import jason.mas2j.ClassParameters;
import jason.runtime.RuntimeServicesInfraTier;

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class implements the Jade version of the environment
 * infrastructure tier. 
 */
public class JadeEnvironment extends Agent implements EnvironmentInfraTier {

    private Environment userEnv;

    static Logger       logger = Logger.getLogger(JadeEnvironment.class.getName());

    public JadeEnvironment() {
    }

    @Override
    public void setup()  {
        // create the user environment
        try {
            Object[] args = getArguments();
            if (args[0] instanceof ClassParameters) { // it is an mas2j parameter
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
            // . performative: ask-all
            // . language: all
            // . ontology: AS-Perception
            /*
            mbox.addMessageHandler("getPercepts", "ask-all", null, "AS-Perception", new MessageHandler() {
                public boolean processMessage(saci.Message m) {
                    saci.Message r = null;
                    try {
                        r = new saci.Message("(tell)");
                        r.put("receiver", m.get("sender"));
                        r.put("in-reply-to", m.get("reply-with"));
                        r.put("ontology", m.get("ontology"));

                        List percepts = userEnv.getPercepts(m.get("sender").toString());
                        if (percepts != null) {
                            synchronized (percepts) {
                                r.put("content", percepts.toString());
                            }
                        }
                        mbox.sendMsg(r);

                    } catch (Exception e) {
                        logger.log(Level.SEVERE, "Error sending message " + r, e);
                    }
                    return true; // no other message handler gives this
                                    // message
                }
            });

            // add a message handler to answer action asks
            // this handler filter is
            // . content: execute
            // . performative: ask
            // . language: all
            // . ontology: AS-Action
            mbox.addMessageHandler("execute", "ask", null, "AS-Action", new MessageHandler() {
                public boolean processMessage(saci.Message m) {
                    saci.Message r = null;
                    try {
                        r = new saci.Message("(tell)");
                        r.put("receiver", m.get("sender"));
                        r.put("in-reply-to", m.get("reply-with"));
                        r.put("ontology", m.get("ontology"));
                        String sender = m.get("sender").toString();
                        Structure action = Structure.parse((String) m.get("action"));

                        // logger.info("doing: "+action);

                        if (userEnv.executeAction(sender, action)) {
                            r.put("content", "ok");
                        } else {
                            r.put("content", "error");
                        }

                        if (mbox != null) { // the agent could be out meanwhile
                            mbox.sendMsg(r);
                        }
                    } catch (Exception e) {
                        logger.log(Level.SEVERE, "Error sending message " + e, e);
                    }
                    return true; // no other message handler gives this
                                    // message
                }
            });
            */

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error starting agent", e);
        }
    }
    public void informAgsEnvironmentChanged() {
        try {
            // TODO:
            //saci.Message m = new saci.Message("(tell :content environmentChanged)");
            //mbox.broadcast(m);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error sending notifyEvents ", e);
        }
    }

    public void informAgsEnvironmentChanged(Collection agentsToNotify) {
        try {
            if (agentsToNotify == null) {
                informAgsEnvironmentChanged();
            } else {
                // TODO:
                //saci.Message m = new saci.Message("(tell :content environmentChanged)");
                //Iterator i = agentsToNotify.iterator();
                //while (i.hasNext()) {
                //    m.put("receiver", i.next().toString());
                //    mbox.sendMsg(m);
                //}
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error sending notifyEvents ", e);
        }
    }


    /**
     * TODO:
     */
    public void stopAg() {
        //userEnv.stop();
        //super.stopAg();
    }

    /**
     * TODO:
     */
    public RuntimeServicesInfraTier getRuntimeServices() {
        return null; //new SaciRuntimeServices(getSociety());
    }
}
