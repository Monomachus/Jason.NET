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

package jason.asSyntax;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class BeliefBase {

	
	public static final Term TPercept = Term.parse("source(percept)");
	public static final Term TSelf = Term.parse("source(self)");

	static private Logger logger = Logger.getLogger(BeliefBase.class.getName());
	
	
	
	/** 
	 * belsMap is a table where the key is the bel.getFunctorArity and the
	 * value is a list of literals with the same functorArity.
	 */
	Map<String,List<Literal>> belsMap = new HashMap<String,List<Literal>>();

	
	/** list of beliefs with percept annot, used to improve performance of brf */
	List<Literal> percepts = new ArrayList<Literal>();

	public BeliefBase() {
	}

	public int size() {
		int c = 0;
		for (List<Literal> l: belsMap.values()) {
			c += l.size();
		}
		return c;
	}

	private void addPercept(Literal l) {
		if (l.hasAnnot(TPercept)) {
			if (! percepts.contains(l)) {
				percepts.add(l);
			}
		}
	}
	
	private void removePercept(Literal l) {
		percepts.remove(l);
	}
	
	public List<Literal> getPercepts() {
		return percepts;
	}
	
	public boolean add(Literal l) {
		Literal bl = contains(l);
		if (bl != null) {
			if (l.hasSubsetAnnot(bl)) // the current bel bl already has l's annots
				return false;
			else {
				bl.importAnnots((Pred) l); // "import" annots from the new bel 
				addPercept(bl); // check if it needs to be added in the percepts list
				return true;
			}
		} else {
			try {
				// minimize the allocation space of terms
				if (l.getTerms() != null) 
					((ArrayList)l.getTerms()).trimToSize();
			} catch (Exception e) {
				logger.log(Level.SEVERE,"Error trimming literal's terms/annotations!",e);
			}

			String key = l.getFunctorArity();
			List<Literal> listFunctor = belsMap.get(key);
			if (listFunctor == null) {
				listFunctor = new ArrayList<Literal>();
				belsMap.put(key, listFunctor);
			}
			listFunctor.add(l);
			addPercept(l); // add it in the percepts list
			return true;
		}
	}
	

	/** returns a list with all beliefs. */
	public List<Literal> getAllBeliefs() {
		List<Literal> all = new ArrayList<Literal>();
        for (List<Literal> l: belsMap.values()) {
			all.addAll(l);
		}
		return all;
	}

	public boolean remove(Literal l) {
		Literal bl = contains(l);
		if (bl != null) {
			if (l.hasSubsetAnnot(bl)) {
				if (bl.hasAnnot(TPercept)) {
					removePercept(bl);
				}
				bl.delAnnot((Pred) l);
				if (!bl.hasAnnot()) {
					String key = l.getFunctorArity();
					List<Literal> listFunctor = belsMap.get(key);
					listFunctor.remove(bl);
					if (listFunctor.isEmpty()) {
						belsMap.remove(key);
					}
				}
				return true;
			} else {
				return false;
			}
		} else {
			if (logger.isLoggable(Level.FINE)) {
				logger.fine("Does not contain "+l+" in "+getAllBeliefs());
			}
			return false;
		}
	}
	
	/** remove all believes with the same functor/arity than <i>l</i> */
	public boolean removeAll(Literal l) {
		boolean result = false;
		List<Literal> all = getRelevant(l);
		if (all != null) {
			Iterator i = all.iterator();
			while (i.hasNext()) {
				if (((Literal)i.next()).getFunctor().equals(l.getFunctor())) {
					i.remove();
					result = true;
				}
			}
		}		
		return result;
	}

	public Literal contains(Literal l) {
		String key = l.getFunctorArity();
		List<Literal> listFunctor = belsMap.get(key);
		if (listFunctor == null) {
			return null;
		}
		for (int i=0; i<listFunctor.size(); i++) {
			Literal bl = (Literal)listFunctor.get(i);
			if (l.equalsAsTerm(bl)) {
				return bl;
			}
		}
		return null;
	}

	public List<Literal> getRelevant(Literal l) {
		if (l.isVar()) {
			// all bels are relevant
			return getAllBeliefs();
		} else {
			String key = l.getFunctorArity();
			return belsMap.get(key);
		}
	}

	public String toString() {
		return belsMap.toString();
	}

	/** get as XML */
	public Element getAsDOM(Document document) {
		Element ebels = (Element) document.createElement("beliefs");
		for (List<Literal> ll: belsMap.values()) {
            for (Literal l: ll) {
				ebels.appendChild(l.getAsDOM(document));
			}
		}
		return ebels;
	}	
}