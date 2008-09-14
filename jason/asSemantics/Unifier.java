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
//----------------------------------------------------------------------------

package jason.asSemantics;

import jason.asSyntax.Literal;
import jason.asSyntax.Pred;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;
import jason.asSyntax.Trigger;
import jason.asSyntax.VarTerm;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Unifier implements Cloneable {

    private static Logger logger = Logger.getLogger(Unifier.class.getName());

    protected HashMap<VarTerm, Term> function = new HashMap<VarTerm, Term>();

    /** 
     * @deprecated use t.apply(un) instead.
     */
    public void apply(Term t) {
    	t.apply(this);
    }

    /** 
     * @deprecated use p.apply(un) instead.
     */
    public void apply(Pred p) {
    	p.apply(this);
    }

    /**
     * gets the value for a Var, if it is unified with another var, gets this
     * other's value
     */
    public Term get(String var) {
        return get(new VarTerm(var));
    }

    /**
     * gets the value for a Var, if it is unified with another var, gets this
     * other's value
     */
    public Term get(VarTerm vtp) {
        Term vl = function.get(vtp);
        if (vl != null && vl.isVar()) { // optimised deref
            return get((VarTerm)vl);
        }
        return vl;
    }

    /*
    public Term get(Term t) {
        if (t.isVar()) {
            return function.get( deref((VarTerm) t));
        } else {
            return null;
        }
    }
    */

    public boolean unifies(Trigger te1, Trigger te2) {
        return te1.sameType(te2) && unifies(te1.getLiteral(), te2.getLiteral());
    }

    public boolean unifiesNoUndo(Trigger te1, Trigger te2) {
        return te1.sameType(te2) && unifiesNoUndo(te1.getLiteral(), te2.getLiteral());
    }

    // ----- Unify for Predicates/Literals
    
    /** this version of unifies undo the variables' mapping 
        if the unification fails. 
        E.g. 
          u.unifier( a(X,10), a(1,1) );
        does not change u, i.e., u = {}
     */
    @SuppressWarnings("unchecked")
    public boolean unifies(Term t1, Term t2) {
        HashMap cfunction = (HashMap)function.clone();
        if (unifiesNoUndo(t1,t2)) {
            return true;
        } else {
            function = cfunction;
            return false;
        }
    }

    /** this version of unifies does not undo the variables' mapping 
        in case of failure. It is however faster than the version with
        undo.
        E.g. 
          u.unifier( a(X,10), a(1,1) );
        fails, but changes u to {X = 10} 
    */
    public boolean unifiesNoUndo(Term t1g, Term t2g) {

		Pred np1 = null;
		Pred np2 = null;
		
    	if (t1g instanceof Pred && t2g instanceof Pred) {
    		np1 = (Pred)t1g;
    		np2 = (Pred)t2g;
        
    		// tests when np1 or np2 are Vars with annots
	        if ((np1.isVar() && np1.hasAnnot()) || np2.isVar() && np2.hasAnnot()) {
	            if (!np1.hasSubsetAnnot(np2, this)) {
	                return false;
	            }
	        }
    	}
    	
        // unify as Term
        boolean ok = unifyTerms(t1g, t2g);

        // if np1 is a var that was unified, clear its annots, as in
        //      X[An] = p(1)[a,b]
        // X is mapped to p(1) and not p(1)[a,b]
        // (if the user wants the "remaining" annots, s/he should write
        //      X[An|R] = p(1)[a,b]
        // X = p(1), An = a, R=[b]
        if (ok && np1 != null) { // they are predicates
	        if (np1.isVar() && np1.hasAnnot()) {
	            np1 = deref( (VarTerm)np1);
	        	Term np1vl = function.get( (VarTerm) np1);
	        	if (np1vl != null && np1vl.isPred()) {
	        	    Pred pvl = (Pred)np1vl.clone();
	        		pvl.clearAnnots();
	        		bind((VarTerm) np1, pvl);
	        	}
	        }
	        if (np2.isVar() && np2.hasAnnot()) {
	            np2 = deref( (VarTerm)np2);
	        	Term np2vl = function.get((VarTerm) np2);
	        	if (np2vl != null && np2vl.isPred()) {
                    Pred pvl = (Pred)np2vl.clone(); 
                    pvl.clearAnnots();
                    bind((VarTerm) np2, pvl);
	        	}
	        }
        }
        return ok;
    }

    
    // ----- Unify for Terms

    protected boolean unifyTerms(Term t1g, Term t2g) {
        // if args are expressions, apply them and use their values
        if (t1g.isArithExpr()) {
            t1g = t1g.clone();
            t1g.apply(this);
        }
        if (t2g.isArithExpr()) {
            t2g = t2g.clone();
            t2g.apply(this);
        }

        final boolean t1gisvar = t1g.isVar();
        final boolean t2gisvar = t2g.isVar();
        
        // one of the args is a var
        if (t1gisvar || t2gisvar) { 

            // deref vars
            final VarTerm t1gv = t1gisvar ? deref((VarTerm)t1g) : null;
            final VarTerm t2gv = t2gisvar ? deref((VarTerm)t2g) : null;

            // get their values
            final Term t1vl = t1gisvar ? function.get(t1gv) : t1g;
            final Term t2vl = t2gisvar ? function.get(t2gv) : t2g;
            
            if (t1vl != null && t2vl != null) {
                // unifies the two values of the vars
                return unifiesNoUndo(t1vl, t2vl); 
            } else if (t1vl != null) {
                bind(t2gv, t1vl);
            } else if (t2vl != null) {
                bind(t1gv, t2vl);
            } else { //if (t1gv != null && t2gv != null) {
                // unify two vars
                bind(t1gv, t2gv);
            }
            return true;
        }        
        
        // both terms are not vars
        
        // if any of the terms is not a structure (is a number or a
        // string), they must be equal
        if (!t1g.isStructure() || !t2g.isStructure())
        	return t1g.equals(t2g);

        // both terms are structures

        Structure t1s = (Structure)t1g;
        Structure t2s = (Structure)t2g;

        // different arities
        final int ts = t1s.getArity();
        if (ts != t2s.getArity())
            return false;
        
        final boolean t1islit = t1g.isLiteral();
        final boolean t2islit = t2g.isLiteral();
        final boolean t1isneg = t1islit && ((Literal)t1g).negated();
        final boolean t2isneg = t2islit && ((Literal)t2g).negated();

        // if both are literal, they must have the same negated
        if (t1islit && t2islit && t1isneg != t2isneg)
        	return false;
        	
        // if one term is literal and the other not, the literal should not be negated
        if (t1islit && !t2islit && t1isneg)
        	return false;
        if (t2islit && !t1islit && t2isneg)
        	return false;
        
        // if the first term is a predicate and the second not, the first should not have annots 
        if (t1g.isPred() && !t2g.isPred() && ((Pred)t1g).hasAnnot())
        	return false;
        
        // different functor
        if (t1s.getFunctor() != null && !t1s.getFunctor().equals(t2s.getFunctor()))
            return false;
        
        // unify inner terms
        // do not use iterator! (see ListTermImpl class)
        for (int i = 0; i < ts; i++)
            if (!unifiesNoUndo(t1s.getTerm(i), t2s.getTerm(i)))
                return false;

        // if both are predicates, the first's annots must be subset of the second's annots
        if (t1g.isPred() && t2g.isPred())
            if ( ! ((Pred)t1g).hasSubsetAnnot((Pred)t2g, this))
                return false;
        
        return true;
    }
    
    public VarTerm deref(VarTerm v) {
        Term vl = function.get(v);
        // original def (before optimise)
        //   if (vl != null && vl.isVar())
        //      return deref(vl);
        //   return v;
        
        VarTerm first = v;
        while (vl != null && vl.isVar()) {
            v  = (VarTerm)vl;
            vl = function.get(v);
        }
        if (first != v) {
            function.put(first, v); // optimise map
        }            
        return v;
    }
    
    
    
    public void bind(VarTerm vt1, VarTerm vt2) {
        final int comp = vt1.compareTo(vt2); 
        if (comp < 0) {
            function.put((VarTerm)vt1.clone(), vt2);
        } else if (comp > 0){
            function.put((VarTerm)vt2.clone(), vt1);
        } // if they are the same (comp == 0), do not bind
    }
    
    private void bind(VarTerm vt, Term vl) {
        function.put((VarTerm) vt.clone(), vl);
    }
        
    public void clear() {
        function.clear();
    }

    public String toString() {
        return function.toString();
    }

    public int size() {
        return function.size();
    }

    /** add all unifications from u */
    public void compose(Unifier u) {
        for (VarTerm k: u.function.keySet())
            function.put( (VarTerm)k.clone(), u.function.get(k).clone());
    }

    @SuppressWarnings("unchecked")
    public Unifier clone() {
        try {
            Unifier newUn = new Unifier();
            newUn.function = (HashMap)function.clone();
            //newUn.compose(this);
            return newUn;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error cloning unifier.",e);
            return null;
        }
    }
    
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        if (o instanceof Unifier) return function.equals( ((Unifier)o).function);
        return false;
    }
    
    /** get as XML */
    public Element getAsDOM(Document document) {
        Element u = (Element) document.createElement("unifier");
        for (VarTerm v: function.keySet()) {
            Element ev = v.getAsDOM(document);
            Element vl = (Element) document.createElement("value");
            vl.appendChild( function.get(v).getAsDOM(document));
            Element map = (Element) document.createElement("map");
            map.appendChild(ev);
            map.appendChild(vl);
            u.appendChild(map);
        }
        return u;
    }
}
