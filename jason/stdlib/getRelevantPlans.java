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
// http://www.csc.liv.ac.uk/~bordini
// http://www.inf.furb.br/~jomi
//----------------------------------------------------------------------------


package jason.stdlib;

import jason.JasonException;
import jason.asSemantics.Option;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;
import jason.asSyntax.Trigger;

import java.util.Iterator;

public class getRelevantPlans {
    
    public static boolean execute(TransitionSystem ts, Unifier un, String[] args) throws Exception {
        try {
        	// first arg is the TE
        	String sTe = args[0];
        	if (sTe.startsWith("\"")) {
        		sTe = sTe.substring(1, sTe.length()-1);
        	}
        	Trigger te = Trigger.parseTrigger(sTe);
        	if (te == null) {
                throw new JasonException("The fist argument is not a TE (getRelevantPlans internal action)");        		
        	}
        	StringBuffer sPlanList = new StringBuffer("["); 
        	Iterator i = ts.relevantPlans(te).iterator();
        	while (i.hasNext()) {
        		Option opt = (Option)i.next();
        		sPlanList.append("\""+opt.getPlan().toASString().replaceAll("\\\"", "\\\\\"")+"\"");
        		if (i.hasNext()) {
        			sPlanList.append(",");
        		}
        	}
        	sPlanList.append("]");
        	
        	// second arg is a var
            Term listVar = Term.parse(args[1]);
            if (listVar == null || ! listVar.isVar()) {
                throw new JasonException("The second argument '"+args[1]+"' is not a VAR (getRelevantPlans internal action)");        		
            }
            
            un.unifies(new Term(sPlanList.toString()), listVar);
            //System.out.println("*** un = "+un);
            
            return true;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new JasonException("The internal action 'getRelevantPlans' has not received two arguments (TE and a VAR)");
        } catch (Exception e) {
            throw new JasonException("Error in internal action 'getRelevantPlans': "+e);
        }
    }
    
}
