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
//   Revision 1.3  2006/02/28 15:11:29  jomifred
//   improve javadoc
//
//   Revision 1.2  2006/02/27 18:46:26  jomifred
//   creation of the RuntimeServices interface
//
//   Revision 1.1  2006/02/18 15:24:30  jomifred
//   changes in many files to detach jason kernel from any infrastructure implementation
//
//   Revision 1.11  2006/02/17 13:13:16  jomifred
//   change a lot of method/classes names and improve some comments
//
//   Revision 1.10  2006/01/04 03:00:46  jomifred
//   using java log API instead of apache log
//
//   Revision 1.9  2005/10/30 16:07:33  jomifred
//   add comments
//
//   Revision 1.8  2005/08/12 22:26:08  jomifred
//   add cvs keywords
//
//
//----------------------------------------------------------------------------


package jason.infra.saci;

import jason.JasonException;
import jason.asSyntax.Term;
import jason.environment.Environment;
import jason.environment.EnvironmentInfraTier;
import jason.runtime.RuntimeServicesInfraTier;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import saci.MessageHandler;

/**
 * This class implements the saci version of the environment infrastructure tier.
 */
public class SaciEnvironment extends saci.Agent implements EnvironmentInfraTier {

	private Environment fUserEnv;

	static Logger logger = Logger.getLogger(SaciEnvironment.class.getName());

    public SaciEnvironment() {
    }
    
    public void informAgsEnvironmentChanged() {
        try {
            saci.Message m = new saci.Message("(tell :content environmentChanged)");
            mbox.broadcast(m);
        } catch (Exception e) {
            logger.log(Level.SEVERE,"Error sending notifyEvents ",e);
        }
    }

    public void informAgsEnvironmentChanged(Collection agentsToNotify) {
        try {
            if (agentsToNotify == null) {
                informAgsEnvironmentChanged();
            } else {
                saci.Message m = new saci.Message("(tell :content environmentChanged)");
                Iterator i = agentsToNotify.iterator();
                while (i.hasNext()) {
                    m.put("receiver", i.next().toString());
                    mbox.sendMsg(m);
                }
            }
        } catch (Exception e) {
        	logger.log(Level.SEVERE,"Error sending notifyEvents ",e);
        }
    }
    
    public void initAg(String[] args) throws JasonException {
        // create the user environment
        try {
            fUserEnv = (Environment)Class.forName(args[0]).newInstance();
            fUserEnv.setEnvironmentInfraTier(this);
			fUserEnv.init(null);
        } catch (Exception e) {
        	logger.log(Level.SEVERE,"Error in Saci Environment initAg",e);
            throw new JasonException("The user environment class instantiation '"+args[0]+"' fail!"+e.getMessage());
        }
        
        try {
            
            // add a message handler to answer perception asks
            // this handler filter is
            //  . content: getPercepts
            //  . performative: ask-all
            //  . language: all
            //  . ontology: AS-Perception
            mbox.addMessageHandler("getPercepts", "ask-all", null, "AS-Perception", new MessageHandler() {
                public boolean processMessage(saci.Message m) {
                	saci.Message r = null;
                	try {
						r = new saci.Message("(tell)");
						r.put("receiver", m.get("sender"));
						r.put("in-reply-to", m.get("reply-with"));
						r.put("ontology", m.get("ontology"));
                        
						List percepts = fUserEnv.getPercepts(m.get("sender").toString());
						if (percepts != null) { 
							synchronized(percepts) {
								r.put("content", percepts.toString());
							}
						}
						mbox.sendMsg(r);
                        
                    } catch (Exception e) {
                    	logger.log(Level.SEVERE,"Error sending message "+r,e);
                    }
                    return true; // no other message handler gives this message
                }
            });
            
			
            // add a message handler to answer action asks
            // this handler filter is
            //  . content: execute
            //  . performative: ask
            //  . language: all
            //  . ontology: AS-Action
            mbox.addMessageHandler("execute", "ask", null, "AS-Action", new MessageHandler() {
                public boolean processMessage(saci.Message m) {
                	saci.Message r = null;
                	try {
                        r = new saci.Message("(tell)");
                        r.put("receiver", m.get("sender"));
                        r.put("in-reply-to", m.get("reply-with"));
                        r.put("ontology", m.get("ontology"));
                        String sender = m.get("sender").toString();
                        Term action   = Term.parse((String)m.get("action"));
                        
                        //logger.info("doing: "+action);
                        
                        if (fUserEnv.executeAction(sender, action)) {
                            r.put("content", "ok");
                        } else {
                            r.put("content", "error");
                        }
                        
                        if (mbox != null) { // the agent could be out meanwhile
                        	mbox.sendMsg(r);
                        }
                    } catch (Exception e) {
                    	logger.log(Level.SEVERE,"Error sending message "+e,e);
                    }
                    return true; // no other message handler gives this message
                }
            });
            
            
        } catch (Exception e) {
        	logger.log(Level.SEVERE,"Error starting agent",e);
        }
    }

	public void stopAg() {
		fUserEnv.stop();
		super.stopAg();
	}

    public RuntimeServicesInfraTier getRuntimeServices() {
    	return new SaciRuntimeServices(getSociety());
    }
}
