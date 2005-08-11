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
//----------------------------------------------------------------------------


package jason.asSemantics;

import jason.asSyntax.Plan;

import java.io.Serializable;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


/** 
 * An Option is a Plan and the Unifier that has made it
*  relevant and applicable
*/

public class Option implements Serializable {

	Plan plan;

	Unifier unif;

    public Option(Plan p, Unifier u) {
        plan = p;
        unif = u;
    }
	
	public Object clone() {
		return new Option( (Plan)plan.clone(), (Unifier)unif.clone());
	}

    public String toString() {
        return "("+plan+","+unif+")";
    }
    
    public Plan getPlan() {
    	return plan;
    }
    
	/** get as XML */
	public Element getAsDOM(Document document) {
		Element op = (Element) document.createElement("option");
		if (plan != null) {
			op.appendChild(plan.getAsDOM(document));
		}
		if (unif != null) {
			op.appendChild(unif.getAsDOM(document));
		}
		return op;
	}
    
}
