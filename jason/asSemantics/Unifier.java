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


package jason.asSemantics;

import jason.asSyntax.DefaultLiteral;
import jason.asSyntax.Literal;
import jason.asSyntax.Pred;
import jason.asSyntax.Term;
import jason.asSyntax.Trigger;

import java.util.HashMap;
import java.util.Iterator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Unifier implements Cloneable {
    
    private HashMap function = new HashMap();
    
    
    public void apply(Term t) {
        if (t.isVar()) {
            if (function.containsKey(t.getFunctor())) {
                t.set(function.get(t.getFunctor()));
                apply(t); // in case t has var args
            }
            return;
        }
        if (t.getTerms() != null) {
        	for (int i = 0; i < t.getTerms().size(); i++) {
        		apply((Term) t.getTerms().get(i));
        	}
        }
    }

    public void apply(Pred p) {
    	apply((Term)p);
        if (p.getAnnots() != null) {
        	for (int i = 0; i < p.getAnnots().size(); i++) {
        		apply((Term) p.getAnnots().get(i));
        	}
        }
    }

    public Term get(String var) {
        return (Term)function.get(var);
    }
    
    public void compose(Term t, Unifier u) {
        if (t.isVar()) {
            if (u.function.containsKey(t.getFunctor())) {
                function.put(t.getFunctor(),u.function.get(t.getFunctor()));
            }
            else {
                // TODO: WHAT TO DO THEN?
            }
            return;
        }
        if (t.getTerms()==null)
            return;
        for (Iterator i = t.getTerms().iterator(); i.hasNext(); ) {
            compose((Term) i.next(), u);
        }
    }
    
    public boolean unifies(Term t1, Term t2) {
        Term t1g, t2g;
        t1g = (Term) t1.clone();
        apply(t1g);
        t2g = (Term) t2.clone();
        apply(t2g);
        
        // identical variables or constants
        if (t1g.equals(t2g)) {
            return true;
        }
        
        // if two atoms or structures
        else if (!t1g.isVar() && !t2g.isVar()) {
            // different funcSymb in atoms or structures
            if (!t1g.getFunctor().equals(t2g.getFunctor())) {
                return false;
            }
            // different arities
            else if ( (t1.getTerms()!=null && t2.getTerms()!=null &&
                       t1.getTerms().size() != t2.getTerms().size()) ||
                      (t1.getTerms()==null && t2.getTerms()!=null)   ||
                      (t1.getTerms()!=null && t2.getTerms()==null) ) {
                return false;
            }
        }
        
        // t1 is var that doesn't occur in t2
        if (t1g.isVar() && !t2g.hasVar(t1g)) {
            function.put(t1g.getFunctor(), t2g);
            return true;
        }
        // t2 is var that doesn't occur in t1
        else if (t2g.isVar() && !t1g.hasVar(t2g)) {
            function.put(t2g.getFunctor(), t1g);
            return true;
        }
        // both are structures, same funcSymb, same arity
        else {
            if (t1g.getTerms() == null && t2g.getTerms() == null) {
                return true;
            } else if (t1g.getTerms() == null || t2g.getTerms() == null) {
                return false;
            } else {
            	for (int i=0; i < t1g.getTerms().size(); i++) {
                    if (!unifies((Term)t1g.getTerms().get(i),(Term)t2g.getTerms().get(i)))
                        return false;
                }
                return true;
            }
        }
    }
    
    public boolean unifies(Pred p1, Pred p2) {
        if (!unifies((Term)p1, (Term)p2))
            return false;
        // unification with annotation:
        // terms unify and annotations are subset
        return p1.hasSubsetAnnot(p2, this);        
    }
    
    public boolean unifies(Literal l1, Literal l2) {
        if (l1.negated()!=l2.negated())
            return false;
        return unifies((Pred)l1,(Pred)l2);
    }
    
    public boolean unifies(DefaultLiteral d1, DefaultLiteral d2) {
        if (d1.isDefaultNegated()!=d2.isDefaultNegated())
            return false;
        return unifies((Literal)d1,(Literal)d2);
    }
    
    public boolean unifies(Trigger te1, Trigger te2) {
        return (te1.sameType(te2) && unifies((Literal)te1,(Literal)te2));
    }
    
    public void clear() {
        function.clear();
    }
    
    public String toString() {
        return function.toString();
    }
    
    public Object clone() {
        try {
        	Unifier newUn = new Unifier();
        	newUn.function = (HashMap)this.function.clone();
            return newUn;
        } catch (Exception e) {
        	e.printStackTrace();
            return null;
        }
    }

    /** get as XML */
	public Element getAsDOM(Document document) {
		Element u = (Element) document.createElement("unifier");
		u.appendChild(document.createTextNode(this.toString()));
		return u;
	}
    
}
