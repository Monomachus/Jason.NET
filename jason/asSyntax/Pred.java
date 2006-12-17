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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A Pred is a Structure with annotations, eg a(1)[an1,an2].
 */
public class Pred extends Structure {

	private static final long serialVersionUID = 1L;

	private ListTerm      annots;

    static private Logger logger = Logger.getLogger(Pred.class.getName());

    public Pred() {
    }

    public Pred(String ps) {
        super(ps);
    }

    public Pred(Structure t) {
        super(t);
    }

    public Pred(Pred p) {
        this((Structure) p);
        copyAnnot(p);
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
    public boolean isGround() {
        if (annots == null) {
            return super.isGround();
        } else {
            return super.isGround() && annots.isGround();
        }
    }

    public void setAnnots(ListTerm l) {
        annots = l;
        if (annots != null && annots.isEmpty()) {
            annots = null;
        }
    }

    public boolean addAnnot(Term t) {
        if (annots == null) {
            annots = new ListTermImpl();
        }
        if (!annots.contains(t)) {
            annots.add(t);
            return true;
        }
        return false;
    }

    public void addAnnots(List<Term> l) {
        if (l == null)
            return;
        for (Term t : l) {
            addAnnot(t);
        }
    }

    public void addAnnot(int index, Term t) {
        if (annots == null)
            annots = new ListTermImpl();
        if (!annots.contains(t))
            annots.add(index, t);
    }

    public void delAnnot(Term t) {
        if (annots != null)
            annots.remove(t);
    }

    public void clearAnnots() {
        if (annots != null)
            annots = null;
    }

    public ListTerm getAnnots() {
        return annots;
    }

    public boolean hasAnnot(Term t) {
        if (annots == null)
            return false;
        return annots.contains(t);
    }

    /** returns true if the pred has at leat one annot */
    public boolean hasAnnot() {
        return annots != null && !annots.isEmpty();
    }

    /**
     * Adds a source annotation like "source(<i>agName</i>)".
     */
    public void addSource(Structure agName) {
        if (agName != null) {
            Structure ts = new Structure("source");
            ts.addTerm(agName);
            addAnnot(ts);
        }
    }

    /** deletes "source(<i>agName</i>)" */
    public boolean delSource(Structure agName) {
        if (annots != null) {
            Structure ts = new Structure("source");
            ts.addTerm(agName);
            return annots.remove(ts);
        }
        return false;
    }

    /**
     * returns the sources of this Pred as a new list. e.g.: from annots
     * [source(a), source(b)], it returns [a,b]
     */
    public ListTerm getSources() {
        ListTerm ls = new ListTermImpl();
        if (annots != null) {
            for (Term ta : annots) {
                if (ta.isStructure()) {
                    Structure tas = (Structure)ta;
                    if (tas.getFunctor().equals("source")) {
                        ls.add(tas.getTerm(0));
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
     * "import" Annotations from another Predicate <i>p</i>. p will only
     * contain the annots actually imported (for Event)
     */
    public void importAnnots(Pred p) {
        if (!p.hasAnnot()) {
            return;
        }
        Iterator<Term> i = p.getAnnots().iterator();
        while (i.hasNext()) {
            Term t = i.next();
            // p will only contain the annots actually added (for Event)
            if (!hasAnnot(t)) {
                addAnnot((Term) t.clone());
            } else {
                // Remove what is not new from p
                i.remove();
            }
        }
    }

    /**
     * removes all annots in this pred that are in <i>p</i> p will only contain
     * the annots actually deleted (for Event)
     */
    public void delAnnot(Pred p) {
        if (p.getAnnots() == null) {
            return;
        }
        if (!hasAnnot()) {
            p.clearAnnots();
        } else {
            Iterator<Term> i = p.getAnnots().iterator();
            while (i.hasNext()) {
                Term t = i.next();
                if (hasAnnot(t)) {
                    annots.remove(t);
                } else {
                    i.remove();
                }
            }
        }
    }

    public void copyAnnot(Pred p) {
        if (p.annots != null) {
            annots = (ListTerm) p.getAnnots().clone();
        } else {
            annots = null;
        }
    }

    /** returns true if all this predicate annots are in p's annots */
    public boolean hasSubsetAnnot(Pred p) {
        if (annots == null)
            return true;
        if (annots != null && p.getAnnots() == null)
            return false;
        for (Term myAnnot : annots) {
            if (!p.hasAnnot(myAnnot)) {
                return false;
            }
        }
        return true;
    }

    /**
     * returns true if all this predicate annots are in p's annots (this version
     * unifies the annot list and remove p's annots)
     */
    public boolean hasSubsetAnnot(Pred p, Unifier u) {
        if (annots == null)
            return true;
        if (annots != null && p.getAnnots() == null)
            return false;

        p = (Pred) p.clone(); // clone p to change its annots, the remaining
                              // annots will unify this annots Tail

        // if p annots has a Tail, p annots's Tail will receive this annots
        // this[a,b,c] = p[x,y|T]
        // T will be [a,b,c]
        VarTerm pTail = null;
        Term tail = p.getAnnots().getTail();
        if (tail instanceof VarTerm) {
            pTail = (VarTerm)tail;
        }
        
        for (Term annot : annots) {
            // search annot in p's annots
            boolean ok = false;
            Iterator<Term> j = p.getAnnots().iterator();
            while (j.hasNext() && !ok) {
                Term pAnnot = j.next();
                if (u.unifies(annot, pAnnot)) {
                    ok = true;
                    j.remove();
                }
            }
            // if p has a tail, add annot in p's tail
            if (!ok && pTail != null) {
                ListTerm pTailAnnots = (ListTerm) u.get(pTail);
                if (pTailAnnots == null) {
                    pTailAnnots = new ListTermImpl();
                    u.unifies(pTail, pTailAnnots);
                    pTailAnnots = (ListTerm)u.get(pTail);
                }
                pTailAnnots.add(annot);
                ok = true;
            }
            if (!ok)
                return false;
        }

        // if this Pred has a Tail, unify it with p remaining annots
        tail = annots.getTail();
        if (tail instanceof VarTerm) {
            // System.out.println("tail="+tail+"/"+p.getAnnots());
            u.unifies((VarTerm)tail, p.getAnnots());
        }

        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        if (o instanceof Pred) {
            final Pred p = (Pred) o;
            return super.equals(o) && this.hasSubsetAnnot(p) && p.hasSubsetAnnot(this);
        } else if (o instanceof Term) { 
            return super.equals(o);
        }
        return false;
    }

    /*
     // BB does not work with this hashvalue, annots are removed while the bel is in BB
    @Override
    protected int calcHashCode() {
        int result = super.calcHashCode();
        if (getAnnots() != null) {
            for (Term annot : getAnnots()) {
                result = 31 * result + annot.hashCode();
            }
        }
        return result;
    }
    */
    
    public boolean equalsAsTerm(Object p) {
        return super.equals((Term) p);
    }

    @Override
    public int compareTo(Term t) {
        int c = super.compareTo(t);
        if (c != 0)
            return c;
        
        if (t.isPred()) {
            Pred tAsPred = (Pred)t;
            if (getAnnots() == null && tAsPred.getAnnots() == null) {
                return 0;
            }
            if (getAnnots() == null) {
                return -1;
            }
            if (tAsPred.getAnnots() == null) {
                return 1;
            }
    
            Iterator<Term> pai = tAsPred.getAnnots().iterator();
            for (Term a : getAnnots()) {
                c = a.compareTo(pai.next());
                if (c != 0) {
                    return c;
                }
            }

            if (getAnnots().size() < tAsPred.getAnnots().size())
                return -1;
            if (getAnnots().size() > tAsPred.getAnnots().size())
                return 1;
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
        String s;
        s = super.toString();
        if (annots != null && !annots.isEmpty()) {
            s += annots.toString();
        }
        return s;
    }

    /** get as XML */
    @Override
    public Element getAsDOM(Document document) {
        Element u = super.getAsDOM(document);
        if (getAnnots() != null && !getAnnots().isEmpty()) {
            Element ea = document.createElement("annotations");
            ea.appendChild(getAnnots().getAsDOM(document));
            u.appendChild(ea);
        }
        return u;
    }
}
