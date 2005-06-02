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

package jason.asSyntax;


import jason.D;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class BeliefBase {

	/**
	 *  
	 * @uml.property name="bels"
	 * @uml.associationEnd aggregation="aggregate" inverse="beliefBase:jason.asSyntax.Literal" multiplicity="(0 -1)" ordering="ordered"
	 */
	//List bels; // TODO: remove it when map is ok

	/** 
	 * belsMap is a table where the key is the bel.getFunctorArity and the
	 * value is a list of literals with the same functorArity.
	 */
	Map belsMap = new HashMap();

	/**
	 *  
	 * @uml.property name="negBels"
	 * @uml.associationEnd aggregation="aggregate" inverse="beliefBase:jason.asSyntax.Literal" multiplicity="(0 -1)" ordering="ordered"
	 */
	//List negBels;  // TODO: remove it when map is ok
	
	/** list of beliefs with percept annot, used to improve performance of brf */
	List percepts = new ArrayList();

	public BeliefBase() {
	}

	/*
	public List getBels() {
		return bels;
	}
	public List getNegBels() {
		return negBels;
	}
	*/
	
	

	/*
	public boolean addOld(Literal l) {
		List lbs = (l.negated()) ? negBels : bels;
		int i = indexOfTerm(l, lbs);
		if (i >= 0) {
			Literal b = (Literal) lbs.get(i);
			if (l.hasSubsetAnnot(b)) // the current bel b already has l's annots
				return false;
			else {
				b.addAnnot((Pred) l); // "import" annots from the new bel.
				return true;
			}
		} else {
			try {
				// minimize the allocation space of terms/annots
				if (l.getTerms() != null) 
					((ArrayList)l.getTerms()).trimToSize();
				if (l.getAnnots() != null) 
					((ArrayList)l.getAnnots()).trimToSize();
			} catch (Exception e) {
				System.err.println("error trim term's terms/annots!");
				e.printStackTrace();
			}
			lbs.add(l);
			return true;
		}
	}
	*/
	
	private void addPercept(Literal l) {
		if (l.hasAnnot(D.TPercept)) {
			if (! percepts.contains(l)) {
				percepts.add(l);
			}
		}
	}
	
	private void removePercept(Literal l) {
		percepts.remove(l);
	}
	
	public List getPercepts() {
		return percepts;
	}
	
	public boolean add(Literal l) {
		Literal bl = contains(l);
		if (bl != null) {
			if (l.hasSubsetAnnot(bl)) // the current bel bl already has l's annots
				return false;
			else {
				bl.addAnnot((Pred) l); // "import" annots from the new bel and remove what is not new from l 
				addPercept(bl); // add it in the percepts list
				return true;
			}
		} else {
			try {
				// minimize the allocation space of terms/annots
				if (l.getTerms() != null) 
					((ArrayList)l.getTerms()).trimToSize();
				if (l.getAnnots() != null) 
					((ArrayList)l.getAnnots()).trimToSize();
			} catch (Exception e) {
				System.err.println("error trim term's terms/annots!");
				e.printStackTrace();
			}

			String key = l.getFunctorArity();
			List listFunctor = (List)belsMap.get(key);
			if (listFunctor == null) {
				listFunctor = new ArrayList();
				belsMap.put(key, listFunctor);
			}
			listFunctor.add(l);
			addPercept(l); // add it in the percepts list
			return true;
		}
	}
	
	public void addAll(BeliefBase bb) {
		Iterator i = bb.belsMap.values().iterator();
		while (i.hasNext()) {
			Iterator j = ((List)i.next()).iterator();
			while (j.hasNext()) {
				Literal l = (Literal)j.next();
				add(l);
			}
		}
	}

	/*
	public boolean removeOld(Literal l) {
		List lbs = (l.negated()) ? negBels : bels;
		int i = indexOfTerm(l, lbs);
		if (i >= 0) {
			Literal b = (Literal) lbs.get(i);
			if (b.hasSubsetAnnot(l)) {
				b.delAnnot((Pred) l);
				if (b.emptyAnnot())
					lbs.remove(i);
				return true;
			} else
				return false;
		} else
			return false;
	}
	*/

	public boolean remove(Literal l) {
		Literal bl = contains(l);
		if (bl != null) {
			if (bl.hasSubsetAnnot(l)) {
				bl.delAnnot((Pred) l);
				if (bl.emptyAnnot()) {
					String key = l.getFunctorArity();
					List listFunctor = (List)belsMap.get(key);
					listFunctor.remove(bl);
					if (listFunctor.isEmpty()) {
						belsMap.remove(key);
					}
					removePercept(bl);
				}
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	/*
	public int indexOfTerm(Literal l, List lbs) {
		Iterator i = lbs.iterator();
		for (int j = 0; i.hasNext(); j++)
			if (l.equalsAsTerm(i.next()))
				return j;
		return -1;
	}
	*/
	
	public Literal contains(Literal l) {
		String key = l.getFunctorArity();
		List listFunctor = (List)belsMap.get(key);
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


	// TODO: use a hashmap to store beliefs, so the rbb list is not necessary
	/*
	public List getRelevantOld(Literal l) {
		Iterator i = (l.negated()) ? negBels.iterator() : bels.iterator();
		List rbb = new LinkedList();
		while (i.hasNext()) {
			Literal b = (Literal) i.next();
			if (b.hashCode() == l.hashCode())
				rbb.add(b);
		}
		return (rbb);
	}
	*/

	public List getRelevant(Literal l) {
		/*
		if (l.equals(D.LTrue)) {
			List rel = new ArrayList(1);
			rel.add(l);
			return rel;
		}
		*/
		String key = l.getFunctorArity();
		return (List)belsMap.get(key);
	}

	public String toString() {
		return belsMap.toString();
		/*
		if (negBels.isEmpty())
			return (bels.toString());
		else {
			return (bels.toString() + "\n\t" + negBels.toString());
		}
		*/
	}

	/** get as XML */
	public Element getAsDOM(Document document) {
		Element ebels = (Element) document.createElement("beliefs");
		Iterator i = belsMap.values().iterator();
		while (i.hasNext()) {
			Iterator j = ((List)i.next()).iterator();
			while (j.hasNext()) {
				Literal l = (Literal)j.next();
				Element bel = (Element) document.createElement("bel");
				bel.appendChild(document.createTextNode(l.toString()));
				ebels.appendChild(bel);
			}
		}
		/*
		Iterator i = bels.iterator();
		while (i.hasNext()) {
			Literal l = (Literal)i.next();
			Element bel = (Element) document.createElement("bel");
			bel.appendChild(document.createTextNode(l.toString()));
			ebels.appendChild(bel);
		}
		i = negBels.iterator();
		while (i.hasNext()) {
			Literal l = (Literal)i.next();
			Element bel = (Element) document.createElement("bel");
			bel.appendChild(document.createTextNode(l.toString()));
			ebels.appendChild(bel);
		}
		*/		
		return ebels;
	}	
}