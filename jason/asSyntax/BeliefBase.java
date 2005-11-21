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
//   Revision 1.9  2005/11/21 19:09:11  jomifred
//   added method remove-all-with-functor/arity
//
//   Revision 1.8  2005/09/26 11:45:45  jomifred
//   fix bug with source add/remove
//
//   Revision 1.7  2005/08/12 22:26:08  jomifred
//   add cvs keywords
//
//
//----------------------------------------------------------------------------

package jason.asSyntax;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
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
	Map belsMap = new HashMap();

	
	/** list of beliefs with percept annot, used to improve performance of brf */
	List percepts = new ArrayList();

	public BeliefBase() {
	}

	public int size() {
		int c = 0;
		Iterator i = belsMap.values().iterator();
		while (i.hasNext()) {
			List  listFunctor = (List)i.next();
			c += listFunctor.size();
		}
		return c;
	}

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
		if (l.hasAnnot(TPercept)) {
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
				bl.addAnnot((Pred) l); // "import" annots from the new bel 
				addPercept(bl); // check if it needs to be added in the percepts list
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
				logger.error("Error trimming literal's terms/annotations!",e);
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
	
	/*
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
	*/

	/** returns a iterator for all Beliefs. */
	/*
	public Iterator allIterator() {
		List all = new ArrayList();
		Iterator i = belsMap.values().iterator();
		while (i.hasNext()) {
			Iterator j = ((List)i.next()).iterator();
			while (j.hasNext()) {
				all.add(j.next());
			}
		}
		return all.iterator();
	}
	*/

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
			if (l.hasSubsetAnnot(bl)) {
				if (bl.hasSource(TPercept)) {
					removePercept(bl);
				}
				bl.delAnnot((Pred) l);
				if (bl.emptyAnnot()) {
					String key = l.getFunctorArity();
					List listFunctor = (List)belsMap.get(key);
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
			return false;
		}
	}
	
	/** remove all believes with the same functor/arity than <i>l</i> */
	public boolean removeAll(Literal l) {
		boolean result = false;
		List all = getRelevant(l);
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

	public List getRelevant(Literal l) {
		String key = l.getFunctorArity();
		return (List)belsMap.get(key);
	}

	public String toString() {
		return belsMap.toString();
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
		return ebels;
	}	
}