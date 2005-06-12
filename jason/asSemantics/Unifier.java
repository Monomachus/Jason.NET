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
import jason.asSyntax.VarTerm;

import java.util.HashMap;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Unifier implements Cloneable {
    
    private HashMap function = new HashMap();
    
    // TODO IMPORTANT: remove unnecessary tail symbols
    // and accompanying extra list when grounding variables
    // that are internal to a list
    public void apply(Term t) {
        if (t.isVar()) {
			VarTerm vt = (VarTerm) t;
			if (! vt.hasValue()) { 
				Term vl = get(vt.getFunctor());
				//System.out.println("appling="+t+"="+vl+" un="+this);
				if (vl != null) {
					vt.setValue(vl);
					apply(vt); // in case t has var args
				}
			}
			return;
		}
		for (int i = 0; i < t.getTermsSize(); i++) {
			apply(t.getTerm(i));
		}
    }

    public void apply(Pred p) {
    	apply((Term) p);
		if (p.getAnnots() != null) {
			for (int i = 0; i < p.getAnnots().size(); i++) {
				apply((Term) p.getAnnots().get(i));
			}
		}
    }

	/**
	 * gets the value for a Var, if it is unified with another var, gets this
	 * other's value
	 */
    public Term get(String var) {
		if (var == null) return null;
		
		Term vl = (Term)function.get(var);
		if (vl == null) return null;
		
		// if vl is also a var, get this var value
		try {
			//System.out.println("*"+var+"*"+vl+" - "+this);
			VarTerm vt = (VarTerm)vl;
			Term vtvl = vt.getValue();
			if (vtvl != null) { // the variable has value, is ground
				return vtvl;
			} else { // the variable is not ground, but could be unified
				vtvl = get( vt.getFunctor() );
				if (vtvl != null) {
					return vtvl;
				}
			}
	
			return null; // no value!
		} catch (StackOverflowError e) {
			System.err.println("Stack overflow in unifier.get!\n\t"+this);
			return null;
		} catch (ClassCastException e) {
			return vl;
		}
    }
    
    public void compose(Term t, Unifier u) {
        if (t.isVar()) {
            if (u.function.containsKey(t.getFunctor())) {
                function.put(t.getFunctor(),u.function.get(t.getFunctor()));
            } else {
                // TODO: WHAT TO DO THEN?
            }
            return;
        }
        if (t.getTerms()==null)
            return;
        for (int i=0; i < t.getTermsSize(); i++) {
            compose(t.getTerm(i), u);
        }
    }
    
    public boolean unifies(Term t1, Term t2) {
        Term t1g = (Term)t1.clone();
        apply(t1g);
        Term t2g = (Term)t2.clone();
        apply(t2g);
        //System.out.println(t1+"="+t2);
        
		List t1gl = t1g.getTerms();
		List t2gl = t2g.getTerms();
		
        // identical variables or constants
		if (t1g.equals(t2g)) {
			//System.out.println("Equals." + t1 + "=" + t2 + "...." + this);
			return true;
		}
        
        // if two atoms or structures
		if (!t1g.isVar() && !t2g.isVar()) {
			// different funcSymb in atoms or structures
        	if (t1g.getFunctor() != null && !t1g.getFunctor().equals(t2g.getFunctor())) {
				return false;
        	}
            
			// different arities
        	if ( (t1gl==null && t2gl!=null)   ||
					(t1gl!=null && t2gl==null) ) {
				return false;
			}
			if (t1g.getTermsSize() != t2g.getTermsSize()) {
				return false;
			}
        }
		
        // t1 is var that doesn't occur in t2
		if (t1g.isVar() && !t2g.hasVar(t1g)) {
			
			// if t1g is unified with another var, also unify another
			try {
				VarTerm t1gvl = (VarTerm)function.get(t1g.getFunctor());
				if (t1gvl != null) {
					unifies(t1gvl,t2g);
				}
			} catch (Exception e) {}
			
			function.put(t1g.getFunctor(), t2g);
			return true;
		}

		// t2 is var that doesn't occur in t1
		if (t2g.isVar() && !t1g.hasVar(t2g)) {
			// if t2g is unified with another var, also unify another
			try {
				VarTerm t2gvl = (VarTerm)function.get(t2g.getFunctor());
				if (t2gvl != null) {
					unifies(t2gvl,t1g);
				}
			} catch (Exception e) {}
			
			function.put(t2g.getFunctor(), t1g);
			//System.out.println("Unified." + t1 + "=" + t2);
			return true;
		}
		
        // both are structures, same funcSymb, same arity
		if (!t1g.isList() && !t2g.isList()) { // lists have always terms == null
            if (t1gl == null &&  t2gl == null) {
                return true;
            }
		} 
					    
		for (int i=0; i < t1g.getTermsSize(); i++) {
            if (!unifies(t1g.getTerm(i),t2g.getTerm(i)))
                return false;
		}
		return true;
    }
    
   	public boolean unifies(Pred p1, Pred p2) {
        // unification with annotation:
        // terms unify and annotations are subset
        return unifies((Term)p1, (Term)p2) && p1.hasSubsetAnnot(p2, this);        
    }
    
    public boolean unifies(Literal l1, Literal l2) {
        return l1.negated()==l2.negated() && unifies((Pred)l1,(Pred)l2);
    }
    
    public boolean unifies(DefaultLiteral d1, DefaultLiteral d2) {
        return d1.isDefaultNegated()==d2.isDefaultNegated() && unifies((Literal)d1,(Literal)d2);
    }
    
    public boolean unifies(Trigger te1, Trigger te2) {
        return te1.sameType(te2) && unifies((Literal)te1,(Literal)te2);
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
