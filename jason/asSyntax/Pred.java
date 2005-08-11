// ----------------------------------------------------------------------------
// Copyright (C) 2003 Rafael H. Bordini, Jomi F. Hubner, et al.
// 
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
// 
// To contact the authors:
// http://www.dur.ac.uk/r.bordini
// http://www.inf.furb.br/~jomi
//----------------------------------------------------------------------------

package jason.asSyntax;


import jason.asSemantics.Unifier;
import jason.asSyntax.parser.as2j;

import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

/** 
 * A Pred is a Term with annotations, eg a(1)[an1,an2].
 */
public class Pred extends Term implements Cloneable, Comparable, Serializable {

	private ArrayList annots;

	static private Logger logger = Logger.getLogger(Pred.class.getName());

	public Pred() {
	}

	public Pred(String ps) {
		super(ps);
	}

	public Pred(Term t) {
		super(t);
	}

	public Pred(Pred p) {
		set(p);
	}

	public static Pred parsePred(String sPred) {
		as2j parser = new as2j(new StringReader(sPred));
		try {
			return parser.at();
		} catch (Exception e) {
			logger.error("Error parsing predicate " + sPred,e);
			return null;
		}
	}


	/** copy all attributes of Pred <i>p</i> */
	public void set(Pred p) {
		super.set((Term)p);
		copyAnnot(p);
	}

	
	public void addAnnot(Term t) {
		if (annots == null)
			annots = new ArrayList();
		if (!annots.contains(t))
			annots.add(t);
	}
	
	public void addAnnot(int index, Term t) {
		if (annots == null)
			annots = new ArrayList();
		if (!annots.contains(t))
			annots.add(index, t);
	}

	public void delAnnot(Term t) {
		if (annots != null)
			annots.remove(t);
	}

	public void clearAnnot() {
		if (annots != null)
			annots.clear();
	}
	
	public List getAnnots() {
		return annots;
	}

	public boolean hasAnnot(Term t) {
		if (annots == null)
			return false;
		return annots.contains(t);
	}

	public boolean emptyAnnot() {
		if (annots == null)
			return true;
		else
			return annots.isEmpty();
	}
	
	/**
	 * Add a source annotation like "source(<s>)". 
	 */
	public void addSource(String s) {
		Term ts = new Term("source");
		ts.addTerm(new Term(s));
		addAnnot(ts);
	}

	/**
	 * Add a source annotation like "source(<t>)". 
	 */
	public void addSource(Term t) {
		Term ts = new Term("source");
		ts.addTerm(t);
		addAnnot(ts);
	}

	/** del source(<s>) */
	public boolean delSource(Term s) {
		if (annots != null) {	
			Iterator i = annots.iterator();
			while (i.hasNext()) {
				Term t = (Term)i.next();
				if (t.getFunctor().equals("source") && t.getTerm(0).equals(s)) {
					i.remove();
					return true;
				}
			}
		}
		return false;
	}
	
	/** 
	 * return the sources of this Pred as a list.
	 * from annots [souce(a), source(b)]
	 * it returns [a,b] 
	 */
	public ListTerm getSources() {
		ListTerm ls = new ListTerm();
		if (annots != null) {
			Iterator i = annots.iterator();
			while (i.hasNext()) {
				Term t = (Term)i.next();
				if (t.getFunctor().equals("source")) {
					ls.add( t.getTerm(0) );
				}
			}
		}
		return ls;
	}
	
	
	/** del all sources annotations */
	public void delSources() {
		if (annots != null) {
			Iterator i = annots.iterator();
			while (i.hasNext()) {
				Term t = (Term)i.next();
				if (t.getFunctor().equals("source")) {
					i.remove();
				}
			}
		}
	}
	
	public boolean hasSource() {
		if (annots != null) {
			Iterator i = annots.iterator();
			while (i.hasNext()) {
				Term t = (Term)i.next();
				if (t.getFunctor().equals("sources")) {
					return true;
				}
			}
		}
		return false;		
	}

	/** 
	 * "import" Annotations from another Predicate
	 */
	public void addAnnot(Pred p) {
		if (p.annots == null) {
			return;
		}
		if (annots == null && !p.emptyAnnot()) {
			annots = new ArrayList(p.annots.size());
		}
		for (int i=0; i < p.annots.size(); i++) {
			Term t = (Term) p.annots.get(i);
			// p will only contain the annots actually added (for Event)
			if (!annots.contains(t)) {
				annots.add(t.clone());
			} else {
				// TODO: why del? (jomi has removed it since it causes concurrent problems): 
				// JOMI: o addBel vai precisar so da lista de anotacoes
				// que foram ADICIONADAS pra gerar somente os eventos relativos
				// aaquelas anotacoesque sao NOVAS. Eu acho que vai dar pau este teu comentario,
				// so que nao temos teste pra estas coisas ainda -- ou temos teste
				// e eu que nao entendo mais como funciona isto! :)))
				
				// Remove what is not new from l 
				//p.delAnnot(t);
				//i--;
			}
		}
	}

	public void delAnnot(Pred p) {
		if (p.annots == null) {
			return;
		}
		if (emptyAnnot()) {
			p.clearAnnot();
		} else {
			for (int i=0; i < p.annots.size(); i++) {
				Term t = (Term) p.annots.get(i);
				// p will only contain the annots actually deleted (for Event)
				if (annots.contains(t)) {
					annots.remove(t);
				} else {
					p.delAnnot(t);
					i--;
				}
			}
		}
	}

	public void copyAnnot(Pred p) {
		if (p.annots != null) {
			annots = new ArrayList(p.annots.size());
			for (int i=0; i < p.annots.size(); i++) {
				annots.add( ((Term)p.annots.get(i)).clone());
			}
		} else {
			annots = null;
		}
	}

	/** returns true if all this predicate annots are in p's annots */ 
	public boolean hasSubsetAnnot(Pred p) {
		if (annots == null)
			return true;
		if (annots != null && p.annots == null)
			return false;
		for (int i=0; i<annots.size(); i++) {
			Term myAnnot = (Term)annots.get(i);
			if (!p.annots.contains(myAnnot)) {
				return false;
			}
		}
		return true;
	}

	/** this version unifies the annot list */
	public boolean hasSubsetAnnot(Pred p, Unifier u) {
		if (annots == null)
			return true;
		if (annots != null && p.annots == null)
			return false;
		for (int i=0; i<annots.size(); i++) {
			Term annot = (Term)annots.get(i);
			if (!p.annots.contains(annot)) {
				if (p.annots.size() <= i) {
					return false;
				}
				Term pAnnot = (Term)p.annots.get(i);
				if (! u.unifies(annot, pAnnot)) {
					return false;
				}
			}
		}
		return true;
	}
	
	
	public boolean equals(Object o) {
		if (!super.equals(o))
			return false;
		try {
			Pred p = (Pred) o;
			if (this.hasSubsetAnnot(p) && p.hasSubsetAnnot(this))
				return true;
			return false;
		} catch (Exception e) {
			return true;
		}
	}

	public boolean equalsAsTerm(Object p) {
		return super.equals((Term) p);
	}

	public int compareTo(Object p) {
		int c;
		c = super.compareTo(p);
		if (c != 0)
			return c;
		if (annots.size() < ((Pred) p).annots.size())
			return -1;
		if (annots.size() > ((Pred) p).annots.size())
			return 1;
		return 0;
	}

	public Object clone() {
		return new Pred(this);
	}


	public String toString() {
		String s;
		s = super.toString();
		if (annots != null && !annots.isEmpty()) {
			s += "[";
			Iterator i = annots.iterator();
			while (i.hasNext()) {
				s += i.next();
				if (i.hasNext())
					s += ",";
			}
			s += "]";
		}
		return s;
	}
}