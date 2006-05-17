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
// CVS information:
//   $Date$
//   $Revision$
//   $Log$
//   Revision 1.19  2006/01/04 02:54:41  jomifred
//   using java log API instead of apache log
//
//   Revision 1.18  2006/01/02 13:49:00  jomifred
//   add plan unique id, fix some bugs
//
//   Revision 1.17  2005/12/31 16:29:58  jomifred
//   add operator =..
//
//   Revision 1.16  2005/12/30 20:40:16  jomifred
//   new features: unnamed var, var with annots, TE as var
//
//   Revision 1.15  2005/12/22 00:03:30  jomifred
//   ListTerm is now an interface implemented by ListTermImpl
//
//   Revision 1.14  2005/12/20 19:52:05  jomifred
//   no message
//
//   Revision 1.13  2005/08/16 21:03:42  jomifred
//   add some comments on TODOs
//
//   Revision 1.12  2005/08/12 22:18:37  jomifred
//   add cvs keywords
//
//
//----------------------------------------------------------------------------


package jason.asSemantics;

import jason.asSyntax.DefaultLiteral;
import jason.asSyntax.ExprTerm;
import jason.asSyntax.ListTerm;
import jason.asSyntax.Literal;
import jason.asSyntax.NumberTermImpl;
import jason.asSyntax.Pred;
import jason.asSyntax.Term;
import jason.asSyntax.Trigger;
import jason.asSyntax.VarTerm;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Unifier implements Cloneable {
    
	static Logger logger = Logger.getLogger(Unifier.class.getName());

	private HashMap function = new HashMap();
    
    public void apply(Term t) {
    	if (t.isExpr()) {
    		ExprTerm et = (ExprTerm)t;
    		// apply values to expression variables
    		apply( (Term)et.getLHS());
    		if (!et.isUnary()) {
    			apply( (Term)et.getRHS());
    		}
    		et.setValue(new NumberTermImpl(et.solve()));
    	} else if (t.isVar()) {
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
		for (int i = 0; i < t.getTermsSize(); i++) { // do not use iterator! (see ListTermImpl class)
			apply(t.getTerm(i));
		}
    }

    public void apply(Pred p) {
    	apply((Term) p);
		if (p.getAnnots() != null) {
			Iterator i = p.getAnnots().listTermIterator();
			while (i.hasNext()) {
				ListTerm lt = (ListTerm)i.next();
				apply(lt.getTerm());
				if (lt.isTail()) {
					apply((Term)lt.getNext());
				}
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
			logger.severe("Stack overflow in unifier.get!\n\t"+this);
			return null;
		} catch (ClassCastException e) {
			return vl;
		}
    }
    
    // TODO: compose is no longer used in TS. Delete?
    public void compose(Term t, Unifier u) {
        if (t.isVar()) {
            if (u.function.containsKey(t.getFunctor())) {
            	// Note we are losing any previous maping of that variable,
            	// presumably this was either left unchanged or updated
            	// by the plan execution
                function.put(t.getFunctor(),u.function.get(t.getFunctor()));
            } // else {
                // Uninstantiated variables remain when the plan for a
            	// goal has finished. Normally this shouldn't happend, but
                // nothing necessarily wrong with it. If it was a programming
                // mistake, an error will eventually occur (e.g., in an action
            	// with an uninstantiated variable).
            // }
            return;
        }
        if (t.getTerms()==null)
            return;
        for (int i=0; i < t.getTermsSize(); i++) {
            compose(t.getTerm(i), u);
        }
    }
    
    // ----- Unify for Terms
    
    public boolean unifiesNoClone(Term t1g, Term t2g) {
    	List t1gts = t1g.getTerms();
		List t2gts = t2g.getTerms();

		/*
		// check if an expression needs solving, before anything else
		// version with expression unification (X+3 = (2+1)+3) unifies X with (2+1) 
		try {
			ExprTerm t1ge = (ExprTerm)t1g;
			try {
				ExprTerm t2ge = (ExprTerm)t2g;
			} catch (ClassCastException e) {
				// t1 is expr but t2 is not
				double t1gd = t1ge.solve();
				String t1gs = Double.toString(t1gd);
				if (t1gs.endsWith(".0")) {
					t1g = new Term(Long.toString(Math.round(t1gd)));
				}
				else {
					t1g = new Term(t1gs);
				}
			}
		} catch (ClassCastException e) {
			try {
				ExprTerm t2ge = (ExprTerm)t2g;
				// t1 is not expr but t2 is
				double t2gd = t2ge.solve();
				String t2gs = Double.toString(t2gd);
				if (t2gs.endsWith(".0")) {
					t2g = new Term(Long.toString(Math.round(t2gd)));
				}
				else {
					t2g = new Term(t2gs);
				}
			} catch (ClassCastException e2) {
			}
		}
		*/
		
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
        	if ( (t1gts==null && t2gts!=null) || (t1gts!=null && t2gts==null) ) {
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
			
			if (! ((VarTerm)t1g).isUnnamedVar())
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
			
			if (! ((VarTerm)t2g).isUnnamedVar())
				function.put(t2g.getFunctor(), t1g);
			//System.out.println("Unified." + t1 + "=" + t2);
			return true;
		}
		
        // both are structures, same funcSymb, same arity
        if (t1gts == null && t2gts == null && !t1g.isList() && !t2g.isList()) { // lists have always terms == null
        	return true;
		} 
					    
		for (int i=0; i < t1g.getTermsSize(); i++) { // do not use iterator! (see ListTermImpl class)
			Term t1 = t1g.getTerm(i);
			Term t2 = t2g.getTerm(i);
			apply(t1);
			apply(t2);
            if (!unifies2NoClone(t1,t2)) {
                return false;
            }
		}
		return true;
    }

    public boolean unifies(Term t1, Term t2) {
        Term t1g = (Term)t1.clone();
        Term t2g = (Term)t2.clone();
        apply(t1g);
        apply(t2g);
        //System.out.println("TermUn: "+t1+"="+t2+" : "+t1g+"="+t2g);
        return unifiesNoClone(t1g, t2g);
    }

    /** this version of unify tries to call the appropriate unify method (Literal, Pred, or Term versions) */
    public boolean unifies2(Term t1, Term t2) {
        Term t1g = (Term)t1.clone();
        Term t2g = (Term)t2.clone();
        apply(t1g);
        apply(t2g);
        return unifies2NoClone(t1g,t2g);
    }

    public boolean unifies2NoClone(Term t1g, Term t2g) {
    	// try to cast both to Literal
    	try {
            return unifiesNoClone((Literal)t1g, (Literal)t2g);    		
    	} catch (Exception e1) {
    		// try to cast both to Pred
    		try {
    			return unifiesNoClone((Pred)t1g, (Pred)t2g);
    		} catch (Exception e2) {
    			// use args as Terms
    			return unifiesNoClone(t1g, t2g);
    		}
    	}
        //System.out.println("TermUn: "+t1+"="+t2+" : "+t1g+"="+t2g);
        //return unifiesNoClone(t1g, t2g);
    }

   	// ----- Pred

    public boolean unifies(Pred p1, Pred p2) {
   		Pred np1 = (Pred)p1.clone();
   		Pred np2 = (Pred)p2.clone();
   		apply(np1);
   		apply(np2);
   		//System.out.println("PredUn: "+p1+"="+p2+" : "+np1+"="+np2);
        return unifiesNoClone((Pred)np1, (Pred)np2); 
    }
   	private boolean unifiesNoClone(Pred np1, Pred np2) {
        // unification with annotation:
        // terms unify and annotations are subset
   		
        if (!np1.isVar() && !np2.isVar() && !np1.hasSubsetAnnot(np2, this)) {
        	return false;
        }
        
        // tests when np1 or np2 are Vars with annots
        if (np1.isVar() && np1.hasAnnot() && !np1.hasSubsetAnnot(np2, this)) {
        	return false;
        }
        if (np2.isVar() && np2.hasAnnot() && !np1.hasSubsetAnnot(np2, this)) {
        	return false;
        }
    	/* (code used when remains in X some annots)
    	 ListTerm newAnnots1 = null; // new annots for np1 (e.g. np1 is X[a,b,c] and np2 is p[a,b,c,d], newAnnots1 will be [d])
        if (np1.isVar() && np1.hasAnnot()) {
        	VarTerm tail = np1.getAnnots().getTail(); 
        	if (tail == null) {
        		tail = new VarTerm("Auto___Tail");
        		np1.getAnnots().setTail(tail);
        	}
        	if (!np1.hasSubsetAnnot(np2, this)) {
        		return false;
        	}
        	newAnnots1 = (ListTerm)get(tail.getFunctor());
        	function.remove(tail.getFunctor());
        }
       	*/
        
        // unify as Term
        boolean ok = unifiesNoClone((Term)np1, (Term)np2);

        // clear annots of vars
        if (ok && np1.isVar() && np1.hasAnnot()) { //newAnnots1 != null) {
        	((Pred)function.get(np1.getFunctor())).setAnnots(null);
        	//((Pred)function.get(np1.getFunctor())).setAnnots(newAnnots1);
        	//System.out.println("np1="+np1.getFunctor()+"/"+this+":"+newAnnots1);
        }
        if (ok && np2.isVar() && np2.hasAnnot()) {
        	((Pred)function.get(np2.getFunctor())).setAnnots(null);
        }
        return ok;
    }
    
   	// ----- Literal
   	
    public boolean unifies(Literal l1, Literal l2) {
    	Literal nl1 = (Literal)l1.clone();
   		Literal nl2 = (Literal)l2.clone();
   		apply(nl1);
   		apply(nl2);
        return unifiesNoClone(nl1,nl2);
    }
    private boolean unifiesNoClone(Literal l1, Literal l2) {
        if (!l1.isVar() && !l2.isVar() && l1.negated() != l2.negated()) {
        	return false;
        }
        return unifiesNoClone((Pred)l1,(Pred)l2);
    }
    
    public boolean unifies(DefaultLiteral d1, DefaultLiteral d2) {
        return d1.isDefaultNegated()==d2.isDefaultNegated() && unifies((Literal)d1.getLiteral(),(Literal)d2.getLiteral());
    }
    
    public boolean unifies(Trigger te1, Trigger te2) {
        return te1.sameType(te2) && unifies(te1.getLiteral(),te2.getLiteral());
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
