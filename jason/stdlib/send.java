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
import jason.asSemantics.InternalAction;
import jason.asSemantics.Message;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.ListTerm;
import jason.asSyntax.Pred;
import jason.asSyntax.Term;

import java.util.Iterator;

public class send implements InternalAction {
    
	/**
	 * arg[0] is receiver (an agent name or a list of names)
	 * arg[1] is illocucionary force
	 * arg[2] is content
	 *  
	 */
    public boolean execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
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
        if (m.isAsk()) {
            try {
                if (args[3] == null); // just to test 3rd argument
                //if (ans == null) {
                //    throw new JasonException("The VAR parameter of the internal action 'send' is not a term!");            	
                //}
                //m.setAskArg(ans.toString());
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new JasonException("The internal action 'send-ask' has not received four arguments");
            } 
        }

        // tell with 4 args is a reply to
        if (m.isTell() && args.length > 3) {
            try {
                Term mid = (Term)args[3].clone();
                //if (mid == null) {
                //    throw new JasonException("The Message ID parameter of the internal action 'send' is not a term!");            	
                //}
                if (mid.isVar()) {
                	un.apply(mid);
                }
                if (! mid.isGround()) {
                    throw new JasonException("The Message ID parameter of the internal action 'send' is not a ground term!");            	            	
                }
                m.setInReplyTo(mid.toString());
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new JasonException("The internal action 'send-ask' has not received four arguments");
            }
        }
        
        try {
            	if (to.isList()) {
            		Iterator i = ((ListTerm)to).iterator();
            		while (i.hasNext()) {
            			Term t = (Term)i.next();
                		m.setReceiver(t.toString());
                		ts.getUserAgArch().sendMsg(m);        			
            		}
            	} else {
            		m.setReceiver(to.toString());
            		ts.getUserAgArch().sendMsg(m);
            	}
            return true;
        } catch (Exception e) {
            throw new JasonException("Error sending message " + m + "\nError="+e);
        }
    }
    
}
