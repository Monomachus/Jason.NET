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


package jason.asSyntax;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class PlanLibrary {

    // the order of insertion is relevant
    // need various plans for the same hashCode
	// List codes = new ArrayList();
	
	// a MAP from TE to a list of relevant plans
    Map relPlans = new HashMap();

	/**
	 * All plans as defined in the AS code
	 */
	List plans = new ArrayList();

    
    public void add(Plan p) {
    	// trim the plan
    	if (p.body != null) {
    		p.body.trimToSize();
    	}
    	if (p.context != null) {
    		p.context.trimToSize();
    	}
    	List codesList = (List)relPlans.get(p.tevent.getFunctorArity());
    	if (codesList == null) {
    		codesList = new ArrayList();
        	relPlans.put(p.tevent.getFunctorArity(), codesList);
    	}
    	codesList.add(p);
    	
    	
        //codes.add(new Integer(p.tevent.hashCode()));
        plans.add(p);
    }

	public void addAll(PlanLibrary pl) {
		Iterator i = pl.getPlans().iterator();
		while (i.hasNext()) {
			Plan p = (Plan)i.next(); 
			add(p);
		}
	}
    
    public Plan get(int i) {
        return (Plan)plans.get(i);
    }
    
    public List getPlans() {
    	return plans;
    }

    public int indexOf(Plan p) {
        return plans.indexOf(p);
    }

    public Plan remove(int i) {
        //codes.remove(i);
        Plan p = (Plan)plans.remove(i);
    	List codesList = (List)relPlans.get(p.tevent.getFunctorArity());
        codesList.remove(p);
        if (codesList.isEmpty()) {
        	// no more plans for this TE
        	relPlans.remove(p.tevent.getFunctorArity());
        }
        return p;
    }

    public boolean isRelevant(Trigger t) {
        //return codes.contains(new Integer(t.hashCode()));
    	return getAllRelevant(t) != null;
    }

    public List getAllRelevant(Trigger t) {
    	return (List)relPlans.get(t.getFunctorArity()); 
    	/*
        if (!isRelevant(t))
            return null;
        // IMPORTANT: avoid creating a new list here???
        List lr = new ArrayList();
        for (int i=0; i<codes.size(); i++) {
            if (t.hashCode()==((Integer)codes.get(i)).intValue()) {
                lr.add(plans.get(i));
            }
        }
        return lr;
        */
    }

    public String toString() {
        return plans.toString();
    }
    
	/** get as XML */
	public Element getAsDOM(Document document) {
		Element eplans = (Element) document.createElement("plans");
		Iterator i = plans.iterator();
		while (i.hasNext()) {
			Plan p = (Plan)i.next(); 
			eplans.appendChild(p.getAsDOM(document));
		}
		return eplans;
	}

}
