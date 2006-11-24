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

public class send extends DefaultInternalAction {
    
    private boolean lastSendWasAsk = false; 
    
	/**
	 * arg[0] is receiver (an agent name or a list of names)
	 * arg[1] is illocucionary force
	 * arg[2] is content
	 *  
	 */
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

	        if (! to.isGround()) {
                throw new JasonException("The TO parameter ('"+to+"') of the internal action 'send' is not a ground term!");            	
            }

	        un.apply(ilf);
            if (! ilf.isGround()) {
                throw new JasonException("The Ilf Force parameter ('"+ilf+"') of the internal action 'send' is not a ground term!");            	            	
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

        // ask must have a fourth argument
        lastSendWasAsk = false;
        if (m.isAsk()) {
            if (args.length < 4) {
                throw new JasonException("The internal action 'send-ask' has not received four arguments");
            }
            lastSendWasAsk = true;
        }

        // tell with 4 args is a reply to
        if (m.isTell() && args.length > 3) {
            Term mid = (Term)args[3].clone();
            if (mid.isVar()) {
            	un.apply(mid);
            }
            if (! mid.isGround()) {
                throw new JasonException("The Message ID parameter of the internal action 'send' is not a ground term!");            	            	
            }
            m.setInReplyTo(mid.toString());
        }
        
        try {
        	if (to.isList()) {
        	    if (m.isAsk()) {
                    throw new JasonException("Can not send ask to a list of receivers!");                                                   
                } else {
                    for (Term t: (ListTerm)to) {
                        String rec = t.toString();
                        if (t.isString()) {
                            rec = ((StringTerm)t).getString();
                        }
                		m.setReceiver(rec);
                		ts.getUserAgArch().sendMsg(m);        			
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

            if (args.length == 5 && m.isAsk()) {
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
        return lastSendWasAsk;
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