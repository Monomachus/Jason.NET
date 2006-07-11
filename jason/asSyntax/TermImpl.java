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

import jason.asSemantics.Agent;
import jason.asSemantics.Unifier;
import jason.asSyntax.parser.as2j;

import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Represents a Term (a predicate parameter), e.g.: val(10,x(3)).
 */
public class TermImpl implements Term, Serializable {

    private String functor = null;
    private List<Term> terms;

    static private Logger logger = Logger.getLogger(Term.class.getName());
    
    public TermImpl() {
    }

    public TermImpl(String fs) {
        if (fs != null && Character.isUpperCase(fs.charAt(0))) {
            logger.warning("Are you sure you want to create a term that begins with uppercase ("+fs+")? Should it be a VarTerm instead?");
        }
        setFunctor(fs);
    }

    public TermImpl(TermImpl t) {
        setFunctor(t.getFunctor());
        setTerms(t.getDeepCopyOfTerms());
    }

    public static Term parse(String sTerm) {
        as2j parser = new as2j(new StringReader(sTerm));
        try {
            return parser.t(); // parse.t() may returns a Pred/List...
        } catch (Exception e) {
            logger.log(Level.SEVERE,"Error parsing term " + sTerm,e);
            return null;
        }
    }

    
    public void setFunctor(String fs) {
        functor = fs;
        predicateIndicatorCache = null;
    }

    public String getFunctor() {
        return functor;
    }

    protected PredicateIndicator predicateIndicatorCache = null; // to not compute it all the time (is is called many many times)
    
    /** returns functor symbol "/" arity */
    public PredicateIndicator getPredicateIndicator() {
        if (predicateIndicatorCache == null) {
            predicateIndicatorCache = new PredicateIndicator(getFunctor(),getTermsSize());
        }
        return predicateIndicatorCache;
    }

    public int hashCode() {
        return getPredicateIndicator().hashCode();
    }
    
    /** 
     * logCons checks whether one particular predicate
     * is a log(ical)Cons(equence) of the belief base.
     * 
     * Returns an iterator for all unifiers that are logCons.
     */
    public Iterator<Unifier> logCons(Agent ag, Unifier un) {
        return null;
    }   

    public static final List<Unifier> EMPTY_UNIF_LIST = Collections.emptyList();

    /** create an iterator for a list of unifiers */
    protected Iterator<Unifier> createUnifIterator(Unifier... unifs) {
        List<Unifier> r = new ArrayList<Unifier>(unifs.length);
        for (int i=0; i<unifs.length; i++) {
            r.add(unifs[i]);
        }
        return r.iterator();
    }
    
    
    public void addTerm(Term t) {
        if (terms == null)
            terms = new ArrayList<Term>();
        terms.add(t);
        predicateIndicatorCache = null;
    }
    public void addTerms(List<Term> l) {
        for (Term t: l) {
            addTerm( t);
        }
    }

    
    public void setTerms(List<Term> l) {
        terms = l;
    }
    public void setTerm(int i, Term t) {
        terms.set(i,t);
    }
     
    /** returns the i-th term (first term is 0) */
    public Term getTerm(int i) {
        if (terms != null && terms.size() > i) {
            return terms.get(i);
        } else {
            return null;
        }
    }


    public int getTermsSize() {
        if (terms != null) {
            return terms.size();
        } else {
            return 0;
        }
    }
    public List<Term> getTerms() {
        return terms;
    }
    
    public Term[] getTermsArray() {
        Term ts[] = null;
        if (getTermsSize() == 0) {
            ts = new Term[0];
        } else {
            ts = new Term[getTermsSize()];
            for (int i=0; i<getTermsSize(); i++) { // use "for" instead of iterator for ListTerm compatibility
                ts[i] = getTerm(i);
            }
        }
        return ts;
    }

    public boolean isVar() {
        return false;
    }
    public boolean isLiteral() {
        return false;
    }
    public boolean isRule() {
        return false;
    }
    public boolean isList() {
        return false;
    }
    public boolean isString() {
        return false;
    }
    public boolean isInternalAction() {
        return false;
    }
    public boolean isArithExpr() {
        return false;
    }
    public boolean isNumeric() {
        return false;
    }
    public boolean isPred() {
        return false;
    }
    

    public boolean isGround() {
        for (int i=0; i<getTermsSize(); i++) {
            if (!getTerm(i).isGround()) {
                return false;
            }
        }
        return true;
    }

    public void makeVarsAnnon() {
        for (int i=0; i<getTermsSize(); i++) {
            if (getTerm(i).isVar()) {
                setTerm(i,new UnnamedVar());
            } else if (getTerm(i).getTermsSize()>0) {
                getTerm(i).makeVarsAnnon();
            }
        }
    }

    public void makeTermsAnnon() {
        for (int i=0; i<getTermsSize(); i++) {
            setTerm(i,new UnnamedVar());
        }
    }

    public boolean hasVar(Term t) {
        if (this.equals(t))
            return true;
        for (int i=0; i<getTermsSize(); i++) {
            if (getTerm(i).hasVar(t)) {
                return true;
            }
        }
        return false;
    }

    public boolean equals(Object t) {
        if (t == null)  return false;
        
        // if t is a VarTerm, uses var's equals
        try {
            VarTerm vt = (VarTerm)t;
            //System.out.println(this.functor+" equals1 "+vt.getFunctor());
            return vt.equals(this);
        } catch (Exception e) {}

        try {
            Term tAsTerm = (Term)t;
            //System.out.println(this+" equals2 "+tAsTerm);
            if (getFunctor() == null && tAsTerm.getFunctor() != null) {
                return false;
            }
            if (getFunctor() != null && !getFunctor().equals(tAsTerm.getFunctor())) {
                return false;
            }
            if (getTerms() == null && tAsTerm.getTerms() == null) {
                return true;
            }
            if (getTerms() == null || tAsTerm.getTerms() == null) {
                return false;
            }
            if (getTermsSize() != tAsTerm.getTermsSize()) {
                return false;
            }

            for (int i=0; i<getTermsSize(); i++) {
                //System.out.println(" *term "+i+" "+getTerm(i)+getTerm(i).getClass().getName()
                //      +"="+tAsTerm.getTerm(i)+tAsTerm.getTerm(i).getClass().getName()+" deu "+getTerm(i).equals(tAsTerm.getTerm(i)));             
                if (!getTerm(i).equals(tAsTerm.getTerm(i))) {
                    return false;
                }
            }
            return true;
        } catch (ClassCastException e) {
            return false;
        }
    }

    public int compareTo(Term tAsTerm) {
        try {
            // TODO: why overriding in ArithExprTerm is not working and we need this if?
            return ((ArithExprTerm)this).compareTo(tAsTerm);
        } catch (Exception e) {}

        int c;
        if (getFunctor() != null && tAsTerm.getFunctor() != null) {
            c = getFunctor().compareTo(tAsTerm.getFunctor());
            if (c != 0)
                return c;
        }
        if (getTerms() == null && tAsTerm.getTerms() == null)
            return 0;
        if (getTerms() == null)
            return -1;
        if (tAsTerm.getTerms() == null)
            return 1;
        if (getTerms().size() < tAsTerm.getTerms().size())
            return -1;
        else if (getTerms().size() > tAsTerm.getTerms().size())
            return 1;

        // same number of terms
        for (int i=0; i<getTermsSize(); i++) {
            c = getTerm(i).compareTo(tAsTerm.getTerm(i));
            if (c != 0)
                return c;
        }
        return 0;
    }

    /** make a deep copy of the terms */
    public Object clone() {
        TermImpl c = new TermImpl(this);
        c.predicateIndicatorCache = this.predicateIndicatorCache;
        return c;
    }

    protected List<Term> getDeepCopyOfTerms() {
        if (terms == null) {
            return null;
        }
        List<Term> l = new ArrayList<Term>(getTerms().size());
        for (Term ti: getTerms()) {
            l.add((Term)ti.clone());
        }
        return l;
    }
    
    public String toString() {
        StringBuffer s = new StringBuffer();
        if (functor != null) {
            s.append(functor);
        }
        if (terms != null) {
            s.append("(");
            Iterator<Term> i = terms.iterator();
            while (i.hasNext()) {
                s.append(i.next());
                if (i.hasNext())
                    s.append(",");
            }
            s.append(")");
        }
        return s.toString();
    }
   
    /** get as XML */
    public Element getAsDOM(Document document) {
        Element u = (Element) document.createElement("term");
        u.setAttribute("functor",getFunctor());
        //u.appendChild(document.createTextNode(toString()));
        if (getTerms() != null && !getTerms().isEmpty()) {
            Element ea = document.createElement("arguments");
            for (Term t: getTerms()) {
                ea.appendChild(t.getAsDOM(document));
            }
            u.appendChild(ea);
        }
        return u;
    }
}
