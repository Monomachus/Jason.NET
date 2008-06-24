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
//
//----------------------------------------------------------------------------

package jason.asSyntax;

import jason.asSemantics.Unifier;
import jason.asSyntax.parser.as2j;

import java.io.StringReader;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A Pred is a Structure with annotations, e.g.: a(1)[an1,an2].
 */
public class Pred extends Structure {

	private static final long serialVersionUID = 1L;
    private static Logger logger = Logger.getLogger(Pred.class.getName());

	private ListTerm      annots;

    public Pred(String ps) {
        super(ps);
    }

    public Pred(Structure t) {
        super(t);
    }

    public Pred(Pred p) {
        super(p);

        if (p.annots != null) {
            annots = (ListTerm) p.getAnnots().clone();
        } else {
            annots = null;
        }
    }

    /** to be used by atom */
    public Pred(String functor, int termsSize) {
        super(functor, termsSize);
    }

    public static Pred parsePred(String sPred) {
        as2j parser = new as2j(new StringReader(sPred));
        try {
            return parser.pred();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error parsing predicate " + sPred, e);
            return null;
        }
    }

    @Override
    public boolean isPred() {
        return true;
    }

	@Override
	public boolean isAtom() {
		return super.isAtom() && !hasAnnot();
	}
    
    @Override
    public boolean isGround() {
        if (annots == null) {
            return super.isGround();
        } else {
            return super.isGround() && annots.isGround();
        }
    }

    @Override    	
    public boolean apply(Unifier u) {
    	boolean r = super.apply(u);
        if (annots != null) {
            boolean ra = annots.apply(u);
            r = r || ra;
        }
        return r;
    }
    
    public void setAnnots(ListTerm l) {
        annots = l;
        if (annots != null && annots.isEmpty()) annots = null;
    }

    public boolean addAnnot(Term t) {
        if (annots == null) annots = new ListTermImpl();
        if (!annots.contains(t)) {
            annots.add(t);
            return true;
        }
        return false;
    }

    public void addAnnots(List<Term> l) {
        if (l == null || l.isEmpty()) return;
        ListTerm tail;
        if (annots == null) {
            annots = new ListTermImpl();
            tail = annots;
        } else {
            tail= annots.getLast();
        }
        for (Term t : l) {
            if (!annots.contains(t)) 
            	tail = tail.append(t);
        }
    }

    public void addAnnot(int index, Term t) {
        if (annots == null) annots = new ListTermImpl();
        if (!annots.contains(t)) annots.add(index, t);
    }

    public void delAnnot(Term t) {
        if (annots != null) annots.remove(t);
    }

    public void clearAnnots() {
		annots = null;
    }

    public ListTerm getAnnots() {
        return annots;
    }

    public boolean hasAnnot(Term t) {
        if (annots == null) return false;
        return annots.contains(t);
    }

    /** returns true if the pred has at least one annot */
    public boolean hasAnnot() {
        return annots != null && !annots.isEmpty();
    }

    @Override
    public boolean hasVar(VarTerm t) {
        if (super.hasVar(t)) return true;
        if (annots != null)
            for (Term v: annots)
                if (v.hasVar(t)) 
                    return true;
        return false;
    }
    
    @Override
    public void countVars(Map<VarTerm, Integer> c) {
        super.countVars(c);
        if (annots != null)
            for (Term t: annots)
                t.countVars(c);
    }

    /**
     * "import" annots from another predicate <i>p</i>. p will be changed
     * to contain only the annots actually imported (for Event), 
     * for example:
     *     p    = b[a,b] 
     *     this = b[b,c] 
     *     after import, p = b[a] 
     * It is used to generate event <+b[a]>.
     * 
     * @return true if some annot was imported.
     */
    public boolean importAnnots(Pred p) {
    	boolean imported = false;
        if (p.hasAnnot()) {
            if (annots == null) annots = new ListTermImpl();
            ListTerm tail = annots.getLast();
            
	        Iterator<Term> i = p.getAnnots().iterator();
	        while (i.hasNext()) {
	            Term t = i.next();
	            // p will only contain the annots actually added (for Event)
	            if (!annots.contains(t)) {
	            	tail = tail.append((Term) t.clone());
	                imported = true;
	            } else {
	                // Remove what is not new from p
	                i.remove();
	            }
	        }
        }
        return imported;
    }

    /**
     * removes all annots in this pred that are in <i>p</i>.
     * @return true if some annot was removed.
     */
    public boolean delAnnot(Pred p) {
    	boolean removed = false;
        if (p.hasAnnot() && this.hasAnnot()) {
        	for (Term t: p.getAnnots()) { 
        		boolean r = annots.remove(t);
	            removed = removed || r;
	        }
        }
        return removed;
    }

    /** returns true if all this predicate annots are in p's annots */
    public boolean hasSubsetAnnot(Pred p) {
        if (annots == null) return true;
        if (annots != null && p.getAnnots() == null) return false;
        for (Term myAnnot : annots) {
            if (!p.hasAnnot(myAnnot)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true if all this predicate's annots are in p's annots using the
     * unifier u. 
     *
     * if p annots has a Tail, p annots's Tail will receive this predicate's annots,
     * e.g.: 
     *   this[a,b,c] = p[x,y,b|T]
     * unifies and T is [a,c] (this will be a subset if p has a and c in its annots).
     *
     * if this annots has a tail, the Tail will receive all necessary term
     * to be a subset, e.g.:
     *   this[b|T] = p[x,y,b]
     * unifies and T is [x,y] (this will be a subset if T is [x,y].
     */
    public boolean hasSubsetAnnot(Pred p, Unifier u) {
    	//return getSubsetAnnots(p,u,null);
    	
        if (annots == null) return true;
        if (p.getAnnots() == null) return false;

        // since p's annots will be changed, clone them
        ListTerm pannots = (ListTerm)p.getAnnots().clone();

        VarTerm pTail = null;
        if (pannots.getTail() instanceof VarTerm) pTail = (VarTerm)pannots.getTail();

        for (Term annot : annots) {
            // search annot in p's annots
            boolean ok = false;
            Iterator<Term> j = pannots.iterator();
            while (j.hasNext() && !ok) {
                Term pAnnot = j.next();
                if (u.unifiesNoUndo(annot, pAnnot)) {
                    ok = true;
                    j.remove();
                }
            }
            // if p has a tail, add annot in p's tail
            if (!ok && pTail != null) {
                ListTerm pAnnotsTail = (ListTerm) u.get(pTail);
                if (pAnnotsTail == null) {
                    pAnnotsTail = new ListTermImpl();
                    u.unifies(pTail, pAnnotsTail);
                    pAnnotsTail = (ListTerm)u.get(pTail);
                }
                pAnnotsTail.add(annot);
                ok = true;
            }
            if (!ok) return false;
        }

        // if this Pred has a Tail, unify it with p remaining annots
        Term thisTail = annots.getTail();
        if (thisTail instanceof VarTerm) {
            u.unifies(thisTail, pannots);
        }

        return true;
    }

    
    
    /**
     * Adds a source annotation like "source(<i>agName</i>)".
     */
    public void addSource(Structure agName) {
        if (agName != null)
            addAnnot(createSource(agName));
    }

    /** deletes "source(<i>agName</i>)" */
    public boolean delSource(Structure agName) {
        if (annots != null)
            return annots.remove(createSource(agName));
        else
        	return false;
    }

    public static Term createSource(String source) {
    	Structure s = new Structure("source",1);
    	s.addTerm(new Atom(source));
    	return s;
    }
    public static Term createSource(Structure source) {
    	Structure s = new Structure("source",1);
    	s.addTerm(source);
    	return s;
    }
    
    /**
     * returns the sources of this Pred as a new list. e.g.: from annots
     * [source(a), source(b)], it returns [a,b]
     */
    public ListTerm getSources() {
        ListTerm ls = new ListTermImpl();
        if (annots != null) {
            ListTerm tail = ls;
            for (Term ta : annots) {
                if (ta.isStructure()) {
                    Structure tas = (Structure)ta;
                    if (tas.getFunctor().equals("source")) {
                        tail = tail.append(tas.getTerm(0));
                    }
                }
            }
        }
        return ls;
    }

    /**
     * returns all annots with the specified functor e.g.: from annots
     * [t(a), t(b), source(tom)]
     * and functor "t",
     * it returns [t(a),t(b)]
     */
    public ListTerm getAnnots(String functor) {
        ListTerm ls = new ListTermImpl();
        if (annots != null) {
        	ListTerm tail = ls;
            for (Term ta : annots) {
            	if (ta.isStructure()) {
            		if (((Structure)ta).getFunctor().equals(functor)) {
            			tail = tail.append(ta);
            		}
            	}
            }
        }
        return ls;
    }

    /** deletes all sources annotations */
    public void delSources() {
        if (annots != null) {
            Iterator<Term> i = annots.iterator();
            while (i.hasNext()) {
                Term t = i.next();
                if (t.isStructure()) {
                    if (((Structure)t).getFunctor().equals("source")) {
                        i.remove();
                    }
                }
            }
        }
    }

    public boolean hasSource() {
        if (annots != null) {
            for (Term ta : annots) {
                if (ta.isStructure()) {
                    if (((Structure)ta).getFunctor().equals("source")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /** returns true if this pred has a "source(<i>agName</i>)" */
    public boolean hasSource(Structure agName) {
        if (annots != null) {
            Structure ts = new Structure("source");
            ts.addTerm(agName);
            return annots.contains(ts);
        }
        return false;
    }

    
    /**
     * Replaces all variables of the term for unnamed variables (_).
     * 
     * @param changes is the map of replacements
     */
    @Override
    protected void makeVarsAnnon(Unifier un, Map<VarTerm,UnnamedVar> changes) {
        if (annots != null) {
        	Iterator<ListTerm> i = annots.listTermIterator();
        	while (i.hasNext()) {
        		ListTerm lt = i.next();
        		Term ta = lt.getTerm();
                if (ta.isVar()) {
                	// replace ta to an unnamed var
                	UnnamedVar uv = changes.get(ta);
                	if (uv == null) {
                		VarTerm vt = (VarTerm)ta;
                		uv = vt.preferredUnnamedVar(un);
                		changes.put((VarTerm)ta, uv);
                	}
                	lt.setTerm(uv);
                } else if (ta.isStructure()) {
                	((Structure)ta).makeVarsAnnon(un,changes);
                }
            }
        }
        super.makeVarsAnnon(un, changes);
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        if (o instanceof Pred) {
            final Pred p = (Pred) o;
            return super.equals(o) && this.hasSubsetAnnot(p) && p.hasSubsetAnnot(this);
        } else if (o instanceof Structure) {
            return !hasAnnot() && super.equals(o);
        }
        return false;
    }

    
    public boolean equalsAsStructure(Object p) { // this method must be in this class, do not move (I do not remember why!)
        return super.equals((Term) p);
    }

    @Override
    public int compareTo(Term t) {
        int c = super.compareTo(t);
        if (c != 0) return c;
        
        if (t.isPred()) {
            Pred tAsPred = (Pred)t;
            if (getAnnots() == null && tAsPred.getAnnots() == null) return 0;
            if (getAnnots() == null) return -1;
            if (tAsPred.getAnnots() == null) return 1;
    
            Iterator<Term> pai = tAsPred.getAnnots().iterator();
            for (Term a : getAnnots()) {
                c = a.compareTo(pai.next());
                if (c != 0) return c;
            }

            final int ats = getAnnots().size();
            final int ots = tAsPred.getAnnots().size(); 
            if (ats < ots) return -1;
            if (ats > ots) return 1;
        }
        return 0;
    }

    public Object clone() {
        return new Pred(this);
    }

    public String toStringAsTerm() {
        return super.toString();
    }

    public String toString() {
        String s = super.toString();
        if (hasAnnot())  s += annots.toString();
        return s;
    }

    /** get as XML */
    @Override
    public Element getAsDOM(Document document) {
        Element u = super.getAsDOM(document);
        if (hasAnnot()) {
            Element ea = document.createElement("annotations");
            ea.appendChild(getAnnots().getAsDOM(document));
            u.appendChild(ea);
        }
        return u;
    }
}
