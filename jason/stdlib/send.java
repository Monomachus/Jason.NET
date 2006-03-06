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
//   Revision 1.8  2006/03/06 17:21:16  jomifred
//   "to"argument may be a list of names
//
//   Revision 1.7  2006/02/17 13:13:16  jomifred
//   change a lot of method/classes names and improve some comments
//
//   Revision 1.6  2005/09/26 11:46:59  jomifred
//   do not send source annots in message content
//
//   Revision 1.5  2005/08/12 22:20:10  jomifred
//   add cvs keywords
//
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
	        
	        
			//System.out.println("Send un="+un);
			//System.out.println("To="+to);
			//System.out.println("Content="+pcnt);
			
            //to  = Term.parse(args[0]);
            //if (to == null) {
            //    throw new JasonException("The TO parameter of the internal action 'send' is not a term!");            	
            //}
	        un.apply(to);

	        if (! to.isGround()) {
                throw new JasonException("The TO parameter ('"+to+"') of the internal action 'send' is not a ground term!");            	
            }

            //ilf = Term.parse(args[1]);
            //if (ilf == null) {
            //    throw new JasonException("The Ilf Force parameter of the internal action 'send' is not a term!");            	
            //}
	        un.apply(ilf);
            if (! ilf.isGround()) {
                throw new JasonException("The Ilf Force parameter ('"+ilf+"') of the internal action 'send' is not a ground term!");            	            	
            }
            /*
            try {
            	as2j parser = new as2j(new StringReader(args[2]));
            	pcnt  = parser.l();
            } catch (Exception pe) {
            	// it could be just a term (like 'true')
            	pcnt = new Pred(Term.parse(args[2]));
            }
            */
            //if (args[2].startsWith("~")) {
            //	pcnt = Literal.parseLiteral(args[2]);
            //} else {
            //pcnt = Term.parse(args[2]);
            //}
	        un.apply(pcnt);
	        
	        // remove source annots in the content (in case it is a pred)
	        try {
	        	((Pred)pcnt).delSources();
	        } catch (Exception e) {}

        } catch (ArrayIndexOutOfBoundsException e) {
            throw new JasonException("The internal action 'send' has not received three arguments");
        } 
        
        /* it may be not ground in ask
        if (!pcnt.isGround()) {
            throw new JasonException("The content of the message '"+pcnt+"' is not ground!");        	
        }
        */
        
        Message m = new Message(ilf.toString(), null, null, pcnt.toString());

        // ask must have a fourth argument
        if (m.isAsk()) {
            try {
                Term ans = (Term)args[3]; // just to test 3rd argument
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
