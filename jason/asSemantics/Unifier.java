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

import jason.asSyntax.ArithExprTerm;
import jason.asSyntax.ListTerm;
import jason.asSyntax.Literal;
import jason.asSyntax.NumberTermImpl;
import jason.asSyntax.Pred;
import jason.asSyntax.Term;
import jason.asSyntax.TermImpl;
import jason.asSyntax.Trigger;
import jason.asSyntax.VarTerm;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Unifier implements Cloneable {

    static Logger logger = Logger.getLogger(Unifier.class.getName());

    private HashMap<VarTerm, Term> function = new HashMap<VarTerm, Term>();

    public void apply(Term t) {
        if (t.isArithExpr()) {
            ArithExprTerm et = (ArithExprTerm) t;
            // apply values to expression variables
            apply(et.getLHS());
            if (!et.isUnary()) {
                apply(et.getRHS());
            }
            et.setValue(new NumberTermImpl(et.solve()));
        } else if (t.isVar()) {
            VarTerm vt = (VarTerm) t;
            if (!vt.hasValue()) {
                Term vl = get(vt);
                // System.out.println("appling="+t+"="+vl+" un="+this);
                if (vl != null && !(vl instanceof VarsCluster)) {
                    vt.setValue(vl);
                    apply(vt); // in case t has var args
                }
            }
            return;
        }
        // do not use iterator! (see ListTermImpl class)
        final int ts = t.getTermsSize();
        for (int i = 0; i < ts; i++) {
            apply(t.getTerm(i));
        }
        t.resetHashCodeCache();
    }

    public void apply(Pred p) {
        apply((Term) p);
        if (p.getAnnots() != null) {
            Iterator<ListTerm> i = p.getAnnots().listTermIterator();
            while (i.hasNext()) {
                ListTerm lt = i.next();
                apply(lt.getTerm());
                if (lt.isTail()) {
                    apply((Term) lt.getNext());
                }
            }
        }
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
        return function.get(vtp);
    }

    public Term get(Term t) {
        if (t.isVar()) {
            return function.get((VarTerm) t);
        } else {
            return null;
        }
    }

    // ----- Unify for Terms

    public boolean unifies(Term t1g, Term t2g) {
        List t1gts = t1g.getTerms();
        List t2gts = t2g.getTerms();

        // if args are expressions, apply them to use the values
        if (t1g.isArithExpr()) {
            t1g = (Term) t1g.clone();
            apply(t1g);
        }
        if (t2g.isArithExpr()) {
            t2g = (Term) t2g.clone();
            apply(t2g);
        }

        // identical variables or constants
        if (t1g.equals(t2g)) {
            // System.out.println("Equals." + t1 + "=" + t2 + "...." + this);
            return true;
        }

        // if two atoms or structures
        if (!t1g.isVar() && !t2g.isVar()) {
            // different arities
            if ((t1gts == null && t2gts != null)
                    || (t1gts != null && t2gts == null)) {
                return false;
            }
            if (t1g.getTermsSize() != t2g.getTermsSize()) {
                return false;
            }
            // different funcSymb in atoms or structures
            if (t1g.getFunctor() != null
                    && !t1g.getFunctor().equals(t2g.getFunctor())) {
                return false;
            }
        }

        // both are vars
        if (t1g.isVar() && t2g.isVar()) {
            VarTerm t1gv = (VarTerm) t1g;
            VarTerm t2gv = (VarTerm) t2g;
            Term t1vl = function.get(t1gv);
            Term t2vl = function.get(t2gv);

            // if the variable value is a var cluster, it means it has no value
            if (t1vl instanceof VarsCluster)
                t1vl = null;
            if (t2vl instanceof VarsCluster)
                t2vl = null;

            // both has value, their values should unify
            if (t1vl != null && t2vl != null) {
                return unifies(t1vl, t2vl);
            }
            // only t1 has value, t1's value should unify with var t2
            if (t1vl != null) {
                return unifies(t2gv, t1vl);
            }
            // only t2 has value, t2's value should unify with var t2
            if (t2vl != null) {
                return unifies(t1gv, t2vl);
            }

            // both are var (not unnamedvar) with no value, like X=Y
            // we must ensure that these vars will form a cluster
            if (! t1gv.isUnnamedVar() && ! t2gv.isUnnamedVar()) {
                VarTerm t1c = (VarTerm) t1gv.clone();
                VarTerm t2c = (VarTerm) t2gv.clone();
                VarsCluster cluster = new VarsCluster(t1c, t2c);
                if (cluster.hasValue()) {
                    // all vars of the cluster should have the same value
                    for (VarTerm vtc : cluster.get()) {
                        function.put(vtc, cluster);
                    }
                }
            }
            return true;
        }

        // t1 is var that doesn't occur in t2
        if (t1g.isVar() && !t2g.hasVar(t1g)) {
            return setVarValue((VarTerm) t1g, t2g);
        }

        // t2 is var that doesn't occur in t1
        if (t2g.isVar() && !t1g.hasVar(t2g)) {
            return setVarValue((VarTerm) t2g, t1g);
        }

        // both are structures, same funcSymb, same arity
        if (t1gts == null && t2gts == null && !t1g.isList() && !t2g.isList()) {
            // lists always have terms == null
            return true;
        }

        // do not use iterator! (see ListTermImpl class)
        final int ts = t1g.getTermsSize();
        for (int i = 0; i < ts; i++) {
            Term t1 = t1g.getTerm(i);
            Term t2 = t2g.getTerm(i);
            // if t1 or t2 are var with value, use the value
            Term t1vl = get(t1);
            if (t1vl != null && !(t1vl instanceof VarsCluster))
                t1 = t1vl;
            Term t2vl = get(t2);
            if (t2vl != null && !(t2vl instanceof VarsCluster))
                t2 = t2vl;
            if (!unifies2(t1, t2)) {
                return false;
            }
        }
        return true;
    }

    private boolean setVarValue(VarTerm vt, Term value) {
        //if (vt.isUnnamedVar())
        //    return true;

        value = (Term) value.clone();

        // if the var has a cluster, set value for all cluster
        Term currentVl = function.get(vt);
        if (currentVl != null && currentVl instanceof VarsCluster) {
            VarsCluster cluster = (VarsCluster) currentVl;
            for (VarTerm cvt : cluster.get()) {
                function.put(cvt, value);
            }
        } else {
            // no value in cluster
            function.put((VarTerm) vt.clone(), value);
        }
        return true;
    }

    /**
     * this version of unify tries to call the appropriate unify method
     * (Literal, Pred, or Term versions)
     */
    public boolean unifies2(Term t1g, Term t2g) {
        // try to cast both to Literal
        if (t1g instanceof Literal && t2g instanceof Literal) {
            return unifies((Literal) t1g, (Literal) t2g);
        } else if (t1g instanceof Pred && t2g instanceof Pred) {
            // try to cast both to Pred
            return unifies((Pred) t1g, (Pred) t2g);
        } else {
            // use args as Terms
            return unifies(t1g, t2g);
        }
    }

    // ----- Pred

    public boolean unifies(Pred np1, Pred np2) {
        // unification with annotation:
        // terms unify and annotations are subset

        // test sub set annots
        if (!np1.isVar() && !np2.isVar() && !np1.hasSubsetAnnot(np2, this)) {
            return false;
        }

        // tests when np1 or np2 are Vars with annots
        if ((np1.isVar() && np1.hasAnnot()) || np2.isVar() && np2.hasAnnot()) {
            if (!np1.hasSubsetAnnot(np2, this)) {
                return false;
            }
        }

        // unify as Term
        boolean ok = unifies((Term) np1, (Term) np2);

        // if np1 is a var that unified, clear its annots
        if (ok && np1.isVar() && np1.hasAnnot()) {
            ((Pred) function.get((VarTerm) np1)).setAnnots(null);
        }
        if (ok && np2.isVar() && np2.hasAnnot()) {
            ((Pred) function.get((VarTerm) np2)).setAnnots(null);
        }
        return ok;
    }

    // ----- Literal

    public boolean unifies(Literal l1, Literal l2) {

        // if l1 and l2 are vars with values, compare using their values
        Term l1vl = get(l1);
        if (l1vl != null && l1vl.isLiteral())
            l1 = (Literal) l1vl;
        Term l2vl = get(l2);
        if (l2vl != null && l2vl.isLiteral())
            l2 = (Literal) l2vl;

        if (!l1.isVar() && !l2.isVar() && l1.negated() != l2.negated()) {
            return false;
        }
        //System.out.println(l1+"="+l2);
        return unifies((Pred) l1, (Pred) l2);
    }

    public boolean unifies(Trigger te1, Trigger te2) {
        return te1.sameType(te2) && unifies(te1.getLiteral(), te2.getLiteral());
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
            for (VarTerm k: function.keySet()) {
                newUn.function.put( (VarTerm)k.clone(), (Term)function.get(k).clone());
            }
            return newUn;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error cloning unifier.",e);
            return null;
        }
    }
    
    /*
    public void removeUngroundVars() {
        Iterator<VarTerm> ik = function.keySet().iterator();
        while (ik.hasNext()) {
            VarTerm k = ik.next();
            Term vl = function.get(k); 
            if (!vl.isGround() || vl instanceof VarsCluster) {
                ik.remove();
            }
        }        
    }
    */

    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        if (o instanceof Unifier) {
            return function.equals( ((Unifier)o).function);
        }
        return false;
    }
    
    
    private static int idCount = 0;
    /**
     * used to group a set of vars. E.g.: when X = Y = W = Z the function map
     * has X -> { X, Y, W, Z } Y -> { X, Y, W, Z } W -> { X, Y, W, Z } Z -> { X,
     * Y, W, Z } So when one var is assigned to a value, all var gives this
     * value.
     * 
     * @author jomi
     * 
     */
    class VarsCluster extends TermImpl {
		private static final long serialVersionUID = 1L;

        int id = 0;
		Set<VarTerm> vars = null;

        // used in clone
        private VarsCluster() { }
        
        VarsCluster(VarTerm v1, VarTerm v2) {
            id = ++idCount;
            add(v1);
            add(v2);
        }

        void add(VarTerm vt) {
            Term vl = function.get(vt);
            if (vl == null) {
                // v1 has no value
                if (vars == null) {
                    vars = new HashSet<VarTerm>();
                }
                vars.add(vt);
            } else if (vl instanceof VarsCluster) {
                if (vars == null) {
                    vars = ((VarsCluster) vl).get();
                } else {
                    vars.addAll(((VarsCluster) vl).get());
                }
            } else {
                logger.warning("joining var that has value!");
            }
        }

        Set<VarTerm> get() {
            return vars;
        }
        
        public boolean equals(Object o) {
            if (o == null) return false;
            if (o == this) return true;
            if (o instanceof VarsCluster) {
                return vars.equals(((VarsCluster)o).vars);
            }
            return false;
        }
        
        boolean hasValue() {
            return vars != null && !vars.isEmpty();
        }

        public Object clone() {
            VarsCluster c = new VarsCluster();
            c.vars = new HashSet<VarTerm>();
            for (VarTerm vt: this.vars) {
                c.vars.add((VarTerm)vt.clone());
            }
            return c;
        }
        
        public String toString() {
            return "_VC"+id;
        }
    }

    /** get as XML */
    public Element getAsDOM(Document document) {
        Element u = (Element) document.createElement("unifier");
        u.appendChild(document.createTextNode(this.toString()));
        return u;
    }

}
