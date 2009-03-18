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
 * A Pred extends a Structure with annotations, e.g.: a(1)[an1,an2].
 */
public class Pred extends Structure {

    private static final long serialVersionUID = 1L;
    private static Logger logger = Logger.getLogger(Pred.class.getName());

    private ListTerm      annots;

    public Pred(String functor) {
        super(functor);
    }

    public Pred(Literal l) {
        super(l);

        if (l.hasAnnot()) {
            annots = l.getAnnots().cloneLT();
        } else {
            annots = null;
        }
    }

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
    
    @Override       
    public void setAnnots(ListTerm l) {
        annots = l;
        if (annots != null && annots.isEmpty()) annots = null;
    }

    @Override       
    public boolean addAnnot(Term t) {
        if (annots == null) annots = new ListTermImpl();
        if (!annots.contains(t)) {
            annots.add(t);
            return true;
        }
        return false;
    }

    @Override       
    public Literal addAnnots(List<Term> l) {
        if (l == null || l.isEmpty()) return this;
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
        return this;
    }

    @Override       
    public Literal addAnnots(Term ... l) {
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
        return this;
    }

    /*
    @Override       
    public void addAnnot(int index, Term t) {
        if (annots == null) annots = new ListTermImpl();
        if (!annots.contains(t)) annots.add(index, t);
    }
    */

    @Override       
    public void delAnnot(Term t) {
        if (annots != null) annots.remove(t);
    }

    @Override       
    public void clearAnnots() {
        annots = null;
    }

    @Override       
    public ListTerm getAnnots() {
        return annots;
    }

    @Override       
    public boolean hasAnnot(Term t) {
        if (annots == null) return false;
        return annots.contains(t);
    }

    @Override       
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

    @Override       
    public boolean importAnnots(Literal p) {
        boolean imported = false;
        if (p.hasAnnot()) {
            if (annots == null) annots = new ListTermImpl();
            ListTerm tail = annots.getLast();
            
            Iterator<Term> i = p.getAnnots().iterator();
            while (i.hasNext()) {
                Term t = i.next();
                // p will only contain the annots actually added (for Event)
                if (!annots.contains(t)) {
                    tail = tail.append(t.clone());
                    imported = true;
                } else {
                    // Remove what is not new from p
                    i.remove();
                }
            }
        }
        return imported;
    }

    @Override
    public boolean delAnnots(List<Term> l) {
        boolean removed = false;
        if (l != null && this.hasAnnot()) {
            for (Term t: l) { 
                boolean r = annots.remove(t);
                removed = removed || r;
            }
        }
        return removed;
    }

    @Override
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

    @Override
    public boolean hasSubsetAnnot(Literal p) {
        if (annots == null) return true;
        if (hasAnnot() && !p.hasAnnot()) return false;
        for (Term myAnnot : annots) {
            if (!p.hasAnnot(myAnnot)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean hasSubsetAnnot(Literal p, Unifier u) {
        //return getSubsetAnnots(p,u,null);
        
        if (annots == null) return true;
        if (!p.hasAnnot()) return false;

        // since p's annots will be changed, clone them
        ListTerm pannots = p.getAnnots().cloneLT();

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

    
    
    @Override
    public void addSource(Term agName) {
        if (agName != null)
            addAnnot(createSource(agName));
    }

    @Override
    public boolean delSource(Term agName) {
        if (annots != null)
            return annots.remove(createSource(agName));
        else
            return false;
    }

    public static Term createSource(Term source) {
        Structure s = new Structure("source",1);
        s.addTerm(source);
        return s;
    }
    
    @Override
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

    @Override
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

    @Override
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

    @Override
    public boolean hasSource(Term agName) {
        if (annots != null) {
            return annots.contains(createSource(agName));
        }
        return false;
    }

    
    @Override
    public Literal makeVarsAnnon(Unifier un) {
        if (annots != null) {
            Iterator<ListTerm> i = annots.listTermIterator();
            while (i.hasNext()) {
                ListTerm lt = i.next();
                Term ta = lt.getTerm();

                if (ta.isVar() && !ta.isUnnamedVar()) {
                    // replace ta to an unnamed var
                    VarTerm vt = un.deref((VarTerm)ta);
                    UnnamedVar uv;
                    if (vt.isUnnamedVar()) {
                        uv = (UnnamedVar)vt;
                    } else {
                        uv = new UnnamedVar("_"+UnnamedVar.getUniqueId()+ta);
                        un.bind(vt, uv);
                    }
                    lt.setTerm(uv);
                } else if (ta.isStructure()) {
                    ((Structure)ta).makeVarsAnnon(un);
                }
            }
        }
        return super.makeVarsAnnon(un);
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        if (o instanceof Pred) {
            final Pred p = (Pred) o;
            return super.equals(o) && this.hasSubsetAnnot(p) && p.hasSubsetAnnot(this);
        } else if (o instanceof Atom && !hasAnnot() ) { // if o is some object that extends Atom (e.g. structure), goes to super equals
            return super.equals(o);                     // consider super equals only when this has no annots
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

    public Term clone() {
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
