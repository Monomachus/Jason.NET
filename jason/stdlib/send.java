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
import jason.asSemantics.Circumstance;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.Intention;
import jason.asSemantics.Message;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.BodyLiteral;
import jason.asSyntax.ListTerm;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Pred;
import jason.asSyntax.StringTerm;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;

/**
  <p>Internal action: <b><code>.send</code></b>.
  
  <p>Description: sends a message to an agent.
  
  <p>Parameters:<ul>
  
  <li>+ arg[0] (atom, string, or list): the receiver of the
  message. It is the unique name of the agent that will receive the
  message (or list of names).<br/>

  <li>+ arg[1] (atom): the illocutionary force of the message (tell,
  achieve, ...).<br/>
  
  <li>+ arg[2] (literal): the content of the message.<br/>
  
  <li><i>+ arg[3]</i> (any term - optional): the answer of an ask
  message (performatives askOne and askAll).<br/> 
  
  <li><i>+ arg[4]</i> (number - optional): time out (in mili-seconds)
  for an ask answer.<br/> 

  </ul>

  <p>In <b>ask</b> messages, the arguments 3 and 4 are optional. In
  case they are informed, .send suspends the intention until an answer
  is received and unified with the arg[3]. Otherwise, the intention is
  not suspended and the answer (that is a tell message) produces a
  belief addition event as usual.
  
  <p>Examples (suppose that agent jomi is sending the messages):<ul>

  <li> <code>.send(rafael,tell,value(10))</code>: sends
  <code>value(10)</code> to the agent named rafael. The literal
  <code>value(10)[source(jomi)]</code> will be added as a belief in
  the rafael's belief base.</li>

  <li> <code>.send(rafael,achieve,go(10,30)</code>: sends
  <code>go(10,30)</code> to the agent named rafael. When rafael
  receives this message, an event like
  <code>!go(10,30)[source(jomi)]</code> will be added in his event
  queue.</li>

  <li> <code>.send(rafael,askOne,value(beer,X))</code>: sends
  <code>value(beer,X)</code> to the agent named rafael. This .send
  does not suspend the jomi's intention. An event like
  <code>+value(beer,10)[source(rafael)]</code> is generated in jomi's
  side when rafael answer the ask.</li>

  <li> <code>.send(rafael,askOne,value(beer,X),A)</code>: sends
  <code>value(beer,X)</code> to the agent named rafael. This send
  suspend the jomi's intention until he receives the rafael
  answer. The answer (something like <code>value(beer,10)</code>)
  unifies with <code>A</code>.</li>

  <li> <code>.send(rafael,askOne,value(beer,X),A,2000)</code>: same as
  previous example, but agent jomi waits for 2 seconds. If no message
  is received in this time, <code>A</code> unifies with
  <code>timeout</code>.</li>

  </ul>

  See the Jason manual for more details about agent communication.

  @see jason.stdlib.broadcast
  @see jason.stdlib.my_name

*/
public class send extends DefaultInternalAction {
    
    private boolean lastSendWasSynAsk = false; 
    
    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        Term to   = null;
        Term ilf  = null;
        Term pcnt = null;
		// check parameters
        try {
            to   = (Term)args[0].clone();
            ilf  = (Term)args[1].clone();
            pcnt = (Term)args[2].clone();
	        
            un.apply(to);
            
            if (!to.isAtom() && !to.isList() && !to.isString()) {
                throw new JasonException("The TO parameter ('"+to+"') of the internal action 'send' is not an atom or list of atoms!");
            }

            un.apply(ilf);
            if (! ilf.isAtom()) {
                throw new JasonException("The Ilf Force parameter ('"+ilf+"') of the internal action 'send' is not an atom!");
            }
            un.apply(pcnt);
	        
            // remove source annots in the content (in case it is a pred)
            try {
                ((Pred)pcnt).delSources();
            } catch (Exception e) {}
            
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new JasonException("The internal action 'send' has not received three arguments");
        } 
        Message m = new Message(ilf.toString(), null, null, pcnt.toString());

        // assync ask have a fourth argument and should suspend the intention
        lastSendWasSynAsk = m.isAsk() && args.length > 3;
        if (lastSendWasSynAsk) {
        	ts.getC().getPendingIntentions().put(m.getMsgId(), ts.getC().getSelectedIntention());
        }

        // tell with 4 args is a reply to
        if (m.isTell() && args.length > 3) {
            Term mid = (Term)args[3].clone();
            if (mid.isVar()) {
            	un.apply(mid);
            }
            if (! mid.isAtom()) {
                throw new JasonException("The Message ID parameter of the internal action 'send' is not an atom!");            	            	
            }
            m.setInReplyTo(mid.toString());
        }
        
        // send the message
        try {
            if (to.isList()) {
                if (m.isAsk()) {
                    throw new JasonException("Can not send ask to a list of receivers!");                                                   
                } else {
                    for (Term t: (ListTerm)to) {
                        if (t.isAtom() || t.isString()) {
                            String rec = t.toString();
                            if (t.isString()) {
                                rec = ((StringTerm)t).getString();
                            }
                            m.setReceiver(rec);
                            ts.getUserAgArch().sendMsg(m);
                        } else {
                            throw new JasonException("The TO parameter ('"+t+"') of the internal action 'send' is not an atom!");
                        }
                    }
                }
            } else {
                String rec = to.toString();
                if (to.isString()) {
                    rec = ((StringTerm)to).getString();
                }
                m.setReceiver(rec);
                ts.getUserAgArch().sendMsg(m);
            }
            
            if (lastSendWasSynAsk && args.length == 5) {
                // get the timout
                NumberTerm tto = (NumberTerm)args[4].clone();
                un.apply(tto);
                new CheckTimeout((long)tto.solve(), m.getMsgId(), ts.getC()).start(); 
            }
            
            return true;
        } catch (Exception e) {
            throw new JasonException("Error sending message " + m + "\nError="+e);
        }
    }

    @Override
    public boolean suspendIntention() {
        return lastSendWasSynAsk;
    }
    
    
    private static Structure timeoutTerm = new Structure("timeout");
    
    class CheckTimeout extends Thread {
        
        private long timeout = 0;
        private String idInPending;
        private Circumstance c;
        
        public CheckTimeout(long to, String rw, Circumstance c) {
            this.timeout = to;
            this.idInPending = rw;
            this.c = c;
        }
        
        public void run() {
            try {
                sleep(timeout);

                // if the intention is still in PI, brings it back to C.I
                Intention intention = c.getPendingIntentions().remove(idInPending);
                if (intention != null) {
                    // unify "timeout" with the .send fourth parameter
                    BodyLiteral send = intention.peek().removeCurrentStep();
                    intention.peek().getUnif().unifies(send.getLiteralFormula().getTerm(3), timeoutTerm);
                    // add the intention back in the C.I
                    c.addIntention(intention);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
