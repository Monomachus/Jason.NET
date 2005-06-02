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
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.ParseList;

import java.util.Iterator;

public class addPlan {
    
    public static boolean execute(TransitionSystem ts, Unifier un, String[] args) throws Exception {
        try {
        	if (args[0].startsWith("[")) { // if arg[0] is a list
        		ParseList pl = new ParseList(args[0]);
        		Iterator i = pl.getAsList().iterator();
        		while (i.hasNext()) {
            		ts.getAg().addPlan(i.next().toString(), args[1]);
        		}
        	} else { // args[0] is a plan
        		ts.getAg().addPlan(args[0], args[1]);
        	}
            return true;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new JasonException("The internal action 'addPlan' has not received two arguments (plan's string and source)");
        } catch (Exception e) {
            throw new JasonException("Error in internal action 'addPlan': "+e);
        }
    }
    
}
