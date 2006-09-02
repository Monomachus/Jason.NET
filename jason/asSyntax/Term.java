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

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Term Interface
 */
public interface Term extends Cloneable, Comparable<Term>, Serializable {

    public String getFunctor();

    /** returns functor symbol "/" arity */
    public PredicateIndicator getPredicateIndicator();

    /**
     * logCons checks whether one particular predicate is a
     * log(ical)Cons(equence) of the belief base.
     * 
     * Returns an iterator for all unifiers that are logCons.
     */
    public Iterator<Unifier> logCons(Agent ag, Unifier un);

    public void addTerm(Term t);

    public void addTerms(List<Term> l);

    public void setTerm(int i, Term t);

    public void setTerms(List<Term> l);

    public int getTermsSize();

    /** returns the i-th term (first term is 0) */
    public Term getTerm(int i);

    public List<Term> getTerms();

    public Term[] getTermsArray();

    public boolean isVar();

    public boolean isLiteral();
    
    public boolean isRule();

    public boolean isList();

    public boolean isString();

    public boolean isInternalAction();

    public boolean isArithExpr();

    public boolean isNumeric();

    public boolean isPred();

    public boolean isGround();

    public boolean hasVar(Term t);

    public void makeVarsAnnon();

    public void makeTermsAnnon();

    public Object clone();

    public boolean equals(Object o);

    /** remove the valued cached for hashCode */
    public void resetHashCodeCache();

    public Element getAsDOM(Document document);
}
