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
//   Revision 1.6  2005/12/30 20:40:16  jomifred
//   new features: unnamed var, var with annots, TE as var
//
//   Revision 1.5  2005/08/12 22:18:37  jomifred
//   add cvs keywords
//
//
//----------------------------------------------------------------------------


package jason.asSemantics;

import jason.asSyntax.Trigger;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Stack;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Intention implements Serializable {

	public static final Intention EmptyInt   = null;

	private static int idCount = 1;
	private int id;
	
    Stack fIntendedMeans = new Stack();
    
    public Intention() {
    	id = idCount++;
    }
    
    public int getId() {
    	return id;
    }
    
    public void push(IntendedMeans im) {
    	fIntendedMeans.push(im);
    }
    
    public IntendedMeans peek() {
    	return (IntendedMeans)fIntendedMeans.peek();
    }
    
    public  IntendedMeans get(int index) {
    	return (IntendedMeans)fIntendedMeans.get(index);    	
    }

    public IntendedMeans pop() {
    	return (IntendedMeans)fIntendedMeans.pop();
    }
    
    public Iterator iterator() {
    	return fIntendedMeans.iterator();
    }
    
    public int size() {
    	return fIntendedMeans.size();
    }
    
	boolean isAtomic() {
		Iterator i = fIntendedMeans.iterator();
		while (i.hasNext()) {
			IntendedMeans im = (IntendedMeans)i.next();
			if (im.isAtomic()) {
				return true;
			}
		}
		return false;
	}
	
    public boolean hasTrigger(Trigger g) {
        Iterator j = iterator(); 
        while (j.hasNext()) {
            IntendedMeans im = (IntendedMeans)j.next();
            Trigger it = (Trigger) im.getPlan().getTriggerEvent().clone();
            im.unif.apply(it.getLiteral());
            if (new Unifier().unifies(g,it)) {
                return true;
            }
        }
        return false;
    }
    
    
    public String toString() {
    	return fIntendedMeans.toString();
    }

    /** get as XML */
	public Element getAsDOM(Document document) {
		Element eint = (Element) document.createElement("intention");
		eint.setAttribute("id", id+"");
        for (int i=fIntendedMeans.size()-1; i>=0; i--) {
            IntendedMeans im = (IntendedMeans) fIntendedMeans.get(i);
            eint.appendChild(im.getAsDOM(document));
        }
		return eint;
	}
}
