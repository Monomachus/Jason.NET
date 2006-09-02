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

import java.io.StringReader;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Represents a variable Term: like X (starts with upper case). It may have a
 * value, afert {@link Unifier}.apply.
 * 
 * @author jomi
 */
public class VarTerm extends Literal implements NumberTerm, ListTerm, StringTerm {
	private static final long serialVersionUID = 1L;

	static private Logger logger = Logger.getLogger(VarTerm.class.getName());

    private Term          value  = null;

    public VarTerm() {
        super();
    }

    public VarTerm(String s) {
        if (s != null && Character.isLowerCase(s.charAt(0))) {
            logger.warning("Are you sure you want to create a VarTerm that begins with lowercase (" + s + ")? Should it be a Term instead?");
        }
        setFunctor(s);
    }

    public static VarTerm parseVar(String sVar) {
        as2j parser = new as2j(new StringReader(sVar));
        try {
            return parser.var();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error parsing var " + sVar, e);
            return null;
        }
    }

    public Object clone() {
        if (value != null) {
            return (Term) value.clone();
        } else {
            // do not call constructor with term parameter!
            VarTerm t = new VarTerm();
            t.setFunctor(super.getFunctor());
            if (getAnnots() != null && !getAnnots().isEmpty()) {
                t.setAnnots((ListTerm) getAnnots().clone());
            }
            return t;
        }
    }

    @Override
    public boolean isVar() {
        return value == null;
    }

    public boolean isUnnamedVar() {
        return false;
    }

    @Override
    public boolean isGround() {
        return value != null && value.isGround();
    }

    /**
     * grounds a variable, set a value for this var (e.g. X = 10; Y = a(b,c);
     * ...)
     */
    public boolean setValue(Term vl) {
        if (vl.isVar()) {
            logger.warning("Attempted set a variable as a value for a variable, in " + this.getFunctor());
            return false;
        }
        value = vl;
        return true;
    }

    /** returns true if this var has a value */
    public boolean hasValue() {
        return value != null;
    }

    /**
     * returns the value of this var. 
     */
    public Term getValue() {
        return value;
    }

    @Override
    public boolean equals(Object t) {
        if (t == null) return false;
        if (t == this) return true;
        if (t instanceof Term) {
            Term tAsTerm = (Term) t;
            Term vl = getValue();
            // System.out.println("cheking equals form "+tAsTerm.getFunctor()+"
            // and this "+this.getFunctor()+" my value "+vl);
            if (vl != null) {
                // campare the values
                return vl.equals(t);
            }

            // is t also a var? (its value must also be null)
            if (t instanceof VarTerm) {
                final VarTerm tAsVT = (VarTerm) t;
                if (tAsVT.getValue() == null) {
                    return getFunctor().equals(tAsTerm.getFunctor());
                }
            }
        }
        return false;
    }

    public int compareTo(Term t) {
        if (value != null)
            return value.compareTo(t);
        else
            return super.compareTo(t);
    }

    // ----------
    // Term methods overridden
    // 
    // in case this VarTerm has a value, use value's methods
    // ----------

    @Override
    public String getFunctor() {
        if (value == null) {
            return super.getFunctor();
        } else {
            return getValue().getFunctor();
        }
    }

    @Override
    public PredicateIndicator getPredicateIndicator() {
        if (value != null) {
            return value.getPredicateIndicator();
        } else if (predicateIndicatorCache == null) {
            predicateIndicatorCache = new PredicateIndicator(getFunctor(), 0);
        }
        return predicateIndicatorCache;
    }

    @Override
    public int hashCode() {
        if (value != null)
            return value.hashCode();
        else
            return getFunctor().hashCode();
    }

    @Override
    public Iterator<Unifier> logCons(Agent ag, Unifier un) {
        if (value != null)
            return value.logCons(ag, un);
        else {
            // try to apply
            VarTerm c = (VarTerm) this.clone();
            un.apply(c);
            if (c.hasValue()) {
                return c.getValue().logCons(ag, un);
            }
            return null;
        }

    }

    @Override
    public Term getTerm(int i) {
        if (value == null) {
            return null;
        } else {
            return getValue().getTerm(i);
        }
    }

    @Override
    public void addTerm(Term t) {
        if (value != null) {
            getValue().addTerm(t);
        }
    }

    @Override
    public int getTermsSize() {
        if (value == null) {
            return 0;
        } else {
            return getValue().getTermsSize();
        }
    }

    @Override
    public List<Term> getTerms() {
        if (value == null) {
            return null;
        } else {
            return getValue().getTerms();
        }
    }

    @Override
    public void setTerms(List<Term> l) {
        if (value != null) {
            value.setTerms(l);
        }
    }

    @Override
    public void setTerm(int i, Term t) {
        if (value != null) {
            value.setTerm(i,t);
        }
    }

    @Override
    public void addTerms(List<Term> l) {
        if (value != null) {
            value.addTerms(l);
        }
    }

    @Override
    public Term[] getTermsArray() {
        if (value == null) {
            return null;
        } else {
            return value.getTermsArray();
        }
    }

    @Override
    public boolean isInternalAction() {
        return value != null && getValue().isInternalAction();
    }

    @Override
    public boolean isList() {
        return value != null && getValue().isList();
    }

    @Override
    public boolean isString() {
        return value != null && getValue().isString();
    }

    @Override
    public boolean isNumeric() {
        return value != null && value.isNumeric();
    }

    @Override
    public boolean isPred() {
        return value != null && getValue().isPred();
    }

    @Override
    public boolean isLiteral() {
        return value != null && getValue().isLiteral();
    }

    @Override
    public boolean isRule() {
        return value != null && getValue().isRule();
    }

    @Override
    public boolean isArithExpr() {
        return value != null && value.isArithExpr();
    }

    @Override
    public boolean hasVar(Term t) {
        if (value == null) {
            return super.hasVar(t);
        } else {
            return value.hasVar(t);
        }
    }

    @Override
    public String toString() {
        if (value == null) {
            // no value, the var name must be equal
            String s = getFunctor();
            if (getAnnots() != null && !getAnnots().isEmpty()) {
                s += getAnnots();
            }
            return s;
        } else {
            // campare the values
            return value.toString();
        }
    }

    // ----------
    // Pred methods overridden
    // 
    // in case this VarTerm has a value, use value's methods
    // ----------

    @Override
    public void setAnnots(ListTerm l) {
        if (value != null && value.isPred())
            ((Pred) value).setAnnots(l);
        else
            super.setAnnots(l);
    }

    @Override
    public void addAnnot(int index, Term t) {
        if (value != null && getValue().isPred())
            ((Pred) getValue()).addAnnot(index, t);
        else
            super.addAnnot(index, t);
    }

    @Override
    public void importAnnots(Pred p) {
        if (value != null && getValue().isPred())
            ((Pred) getValue()).importAnnots(p);
        else
            super.importAnnots(p);
    }

    @Override
    public boolean addAnnot(Term t) {
        if (value != null && getValue().isPred())
            return ((Pred) getValue()).addAnnot(t);
        else
            return super.addAnnot(t);
    }

    @Override
    public void addAnnots(List<Term> l) {
        if (value != null && getValue().isPred())
            ((Pred) getValue()).addAnnots(l);
        else
            super.addAnnots(l);
    }

    @Override
    public void clearAnnots() {
        if (value != null && getValue().isPred())
            ((Pred) getValue()).clearAnnots();
        else
            super.clearAnnots();
    }

    @Override
    public void delAnnot(Pred p) {
        if (value != null && getValue().isPred())
            ((Pred) getValue()).delAnnot(p);
        else
            super.delAnnot(p);
    }

    @Override
    public void delAnnot(Term t) {
        if (value != null && getValue().isPred())
            ((Pred) getValue()).delAnnot(t);
        else
            super.delAnnot(t);
    }

    @Override
    public boolean hasAnnot(Term t) {
        if (value != null && getValue().isPred())
            return ((Pred) getValue()).hasAnnot(t);
        else
            return super.hasAnnot(t);
    }

    @Override
    public boolean hasAnnot() {
        if (value != null && getValue().isPred())
            return ((Pred) getValue()).hasAnnot();
        else
            return super.hasAnnot();
    }

    @Override
    public ListTerm getAnnots() {
        if (value != null && getValue().isPred())
            return ((Pred) getValue()).getAnnots();
        else
            return super.getAnnots();
    }

    @Override
    public void addSource(Term t) {
        if (value != null && getValue().isPred())
            ((Pred) getValue()).addSource(t);
        else
            super.addSource(t);

    }

    @Override
    public boolean delSource(Term s) {
        if (value != null && getValue().isPred())
            return ((Pred) getValue()).delSource(s);
        else
            return super.delSource(s);
    }

    @Override
    public void delSources() {
        if (value != null && getValue().isPred())
            ((Pred) getValue()).delSources();
        else
            super.delSources();
    }

    @Override
    public ListTerm getSources() {
        if (value != null && getValue().isPred())
            return ((Pred) getValue()).getSources();
        else
            return super.getSources();
    }

    @Override
    public boolean hasSource() {
        if (value != null && getValue().isPred())
            return ((Pred) getValue()).hasSource();
        else
            return super.hasSource();
    }

    @Override
    public boolean hasSource(Term s) {
        if (value != null && getValue().isPred())
            return ((Pred) getValue()).hasSource(s);
        else
            return super.hasSource(s);
    }

    // ----------
    // Literal methods overridden
    // 
    // in case this VarTerm has a value, use value's methods
    // ----------

    @Override
    public boolean negated() {
        return value != null && getValue().isLiteral() && ((Literal) getValue()).negated();
    }

    // ----------
    // ArithmeticExpression methods overridden
    // Interface NumberTerm
    // ----------

    public double solve() {
        if (hasValue() && value.isNumeric()) {
            return ((NumberTerm) value).solve();
        } else {
            logger.warning("Error getting numerical value of VarTerm " + this);
        }
        return 0;
    }

    // ----------
    //
    // ListTerm methods overridden
    // 
    // ----------

    public void add(int index, Term o) {
        if (value != null && getValue().isList())
            ((ListTerm) getValue()).add(index, o);
    }

    public boolean add(Term o) {
        return value != null && getValue().isList() && ((ListTerm) getValue()).add(o);
    }

    @SuppressWarnings("unchecked")
    public boolean addAll(Collection c) {
        return value != null && getValue().isList() && ((ListTerm) getValue()).addAll(c);
    }

    @SuppressWarnings("unchecked")
    public boolean addAll(int index, Collection c) {
        return value != null && getValue().isList() && ((ListTerm) getValue()).addAll(index, c);
    }

    public void clear() {
        if (value != null && getValue().isList())
            ((ListTerm) getValue()).clear();
    }

    public boolean contains(Object o) {
        return value != null && getValue().isList() && ((ListTerm) getValue()).contains(o);
    }

    public boolean containsAll(Collection c) {
        return value != null && getValue().isList() && ((ListTerm) getValue()).containsAll(c);
    }

    public Term get(int index) {
        if (value != null && getValue().isList())
            return ((ListTerm) getValue()).get(index);
        else
            return null;
    }

    public int indexOf(Object o) {
        if (value != null && getValue().isList())
            return ((ListTerm) getValue()).indexOf(o);
        else
            return -1;
    }

    public int lastIndexOf(Object o) {
        if (value != null && getValue().isList())
            return ((ListTerm) getValue()).lastIndexOf(o);
        else
            return -1;
    }

    public Iterator<Term> iterator() {
        if (value != null && getValue().isList())
            return ((ListTerm) getValue()).iterator();
        else
            return null;
    }

    public ListIterator<Term> listIterator() {
        if (value != null && getValue().isList())
            return ((ListTerm) getValue()).listIterator();
        else
            return null;
    }

    public ListIterator<Term> listIterator(int index) {
        if (value != null && getValue().isList())
            return ((ListTerm) getValue()).listIterator(index);
        else
            return null;
    }

    public Term remove(int index) {
        if (value != null && getValue().isList())
            return ((ListTerm) getValue()).remove(index);
        else
            return null;
    }

    public boolean remove(Object o) {
        if (value != null && getValue().isList())
            return ((ListTerm) getValue()).remove(o);
        else
            return false;
    }

    public boolean removeAll(Collection c) {
        if (value != null && getValue().isList())
            return ((ListTerm) getValue()).removeAll(c);
        else
            return false;
    }

    public boolean retainAll(Collection c) {
        if (value != null && getValue().isList())
            return ((ListTerm) getValue()).retainAll(c);
        else
            return false;
    }

    public Term set(int index, Term o) {
        if (value != null && getValue().isList())
            return ((ListTerm) getValue()).set(index, o);
        else
            return null;
    }

    public List<Term> subList(int arg0, int arg1) {
        if (value != null && getValue().isList())
            return ((ListTerm) getValue()).subList(arg0, arg1);
        else
            return null;
    }

    public Object[] toArray() {
        if (value != null && getValue().isList())
            return ((ListTerm) getValue()).toArray();
        else
            return null;
    }

    @SuppressWarnings("unchecked")
    public Object[] toArray(Object[] arg0) {
        if (value != null && getValue().isList())
            return ((ListTerm) getValue()).toArray(arg0);
        else
            return null;
    }

    // from ListTerm

    public void setTerm(Term t) {
        if (value != null && getValue().isList())
            ((ListTerm) getValue()).setTerm(t);
    }

    public void setNext(Term t) {
        if (value != null && getValue().isList())
            ((ListTerm) getValue()).setNext(t);
    }

    public ListTerm append(Term t) {
        if (value != null && getValue().isList())
            return ((ListTerm) getValue()).append(t);
        else
            return null;
    }

    public ListTerm concat(ListTerm lt) {
        if (value != null && getValue().isList())
            return ((ListTerm) getValue()).concat(lt);
        else
            return null;
    }

    public List<Term> getAsList() {
        if (value != null && getValue().isList())
            return ((ListTerm) getValue()).getAsList();
        else
            return null;
    }

    public ListTerm getLast() {
        if (value != null && getValue().isList())
            return ((ListTerm) getValue()).getLast();
        else
            return null;
    }

    public ListTerm getNext() {
        if (value != null && getValue().isList())
            return ((ListTerm) getValue()).getNext();
        else
            return null;
    }

    public Term getTerm() {
        if (value != null && getValue().isList())
            return ((ListTerm) getValue()).getTerm();
        else
            return null;
    }

    public boolean isEmpty() {
        return value != null && getValue().isList() && ((ListTerm) getValue()).isEmpty();
    }

    public boolean isEnd() {
        return value != null && getValue().isList() && ((ListTerm) getValue()).isEnd();
    }

    public boolean isTail() {
        return value != null && getValue().isList() && ((ListTerm) getValue()).isTail();
    }

    public void setTail(VarTerm v) {
        if (value != null && getValue().isList())
            ((ListTerm) getValue()).setTail(v);
    }

    public VarTerm getTail() {
        if (value != null && getValue().isList())
            return ((ListTerm) getValue()).getTail();
        else
            return null;
    }

    public Iterator<ListTerm> listTermIterator() {
        if (value != null && getValue().isList())
            return ((ListTerm) getValue()).listTermIterator();
        else
            return null;
    }

    public int size() {
        if (value != null && getValue().isList())
            return ((ListTerm) getValue()).size();
        else
            return -1;
    }

    // -----------------------
    // StringTerm interface implementation
    // -----------------------

    public void setString(String s) {
        if (value != null && getValue().isString())
            ((StringTerm) getValue()).setString(s);
    }

    public String getString() {
        if (value != null && getValue().isString())
            return ((StringTerm) getValue()).getString();
        else
            return null;
    }

    public int length() {
        if (value != null && getValue().isString())
            return ((StringTerm) getValue()).length();
        else
            return -1;
    }

    /** get as XML */
    public Element getAsDOM(Document document) {
        if (hasValue()) {
            return value.getAsDOM(document);
        } else {
            Element u = (Element) document.createElement("var-term");
            u.appendChild(document.createTextNode(toString()));
            return u;
        }
    }
}
