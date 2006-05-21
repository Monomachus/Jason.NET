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
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


/** 
 * Represents a relational expression like 10 < 20.
 * 
 * Notes about =.. operator
 * 
 * Literal =.. [functor, list of terms, list of annots]
 * 
 * Example: X =.. [~p, [t1, t2], [a1,a2]]
 *          X is ~p(t1,t2)[a1,a2]
 *          
 *          ~p(t1,t2)[a1,a2] =.. X
 *          X is [~p, [t1, t2], [a1,a2]]
 * 
 * @author jomi
 */
public class RelExprTerm extends Term {

    public enum RelationalOp { 
		none   { public String toString() { return ""; } }, 
		gt     { public String toString() { return " > "; } }, 
		gte    { public String toString() { return " >= "; } },
		lt     { public String toString() { return " < "; } }, 
		lte    { public String toString() { return " <= "; } },
		eq     { public String toString() { return " == "; } },
		dif    { public String toString() { return " \\== "; } },
		unify          { public String toString() { return " = "; } },
		literalBuilder { public String toString() { return " =.. "; } };
	}

    private  Term lhs, rhs;
	private RelationalOp op = RelationalOp.none;

	static private Logger logger = Logger.getLogger(RelExprTerm.class.getName());
	
	public RelExprTerm() {
		super();
	}
	
	public RelExprTerm(Term t1, RelationalOp oper, Term t2) {
		lhs = t1;
		op = oper;
		rhs = t2;
	}
    
    /** 
     * logCons checks whether one particular predicate
     * is a log(ical)Cons(equence) of the belief base.
     * 
     * Returns an iterator for all unifiers that are logCons.
     */
    @Override
    public Iterator<Unifier> logCons(final Agent ag, Unifier un) {
        Term xp = null;
        Term yp = null;
        if (op != RelationalOp.literalBuilder) {
            xp = (Term)lhs.clone();
            yp = (Term)rhs.clone();
            un.apply(xp);
            un.apply(yp);
        }
        switch (op) {
        
        case gt : if (xp.compareTo(yp)  >  0) return createUnifIterator(un);  break;
        case gte: if (xp.compareTo(yp)  >= 0) return createUnifIterator(un);  break;
        case lt : if (xp.compareTo(yp)  <  0) return createUnifIterator(un);  break;
        case lte: if (xp.compareTo(yp)  <= 0) return createUnifIterator(un);  break;
        case eq : if (xp.equals(yp))          return createUnifIterator(un);  break;
        case dif: if (!xp.equals(yp))         return createUnifIterator(un);  break;
        case unify: if (un.unifies(xp,yp))    return createUnifIterator(un);  break;

        case literalBuilder: 
            try {
                Literal p = (Literal)lhs;
                ListTerm l = (ListTerm)rhs;
                
                // both are not vars, using normal unification
                if (!lhs.isVar() && !rhs.isVar() && un.unifies((Term)p.getAsListOfTerms(), (Term)l)) {
                    return createUnifIterator(un);
                }
                
                // first is var, second is list, var is assigned to l tranformed in literal
                if (lhs.isVar() && rhs.isList() && un.unifies(p, Literal.newFromListOfTerms(l))) {
                    return createUnifIterator(un);
                }
                
                // first is literal, second is var, var is assigned to l tranformed in list
                if (lhs.isLiteral() && rhs.isVar() && un.unifies((Term)p.getAsListOfTerms(), (Term)l)) {
                    return createUnifIterator(un);
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "The arguments of operator =.. are not Literal and List.", e);
            }
            break;
        }
        
        return LogExprTerm.EMPTY_UNIF_LIST.iterator();  // empty iterator for unifier
    }   

    /** returns some Term that can be evaluated */
    public static Term parseExpr(String sExpr) {
        as2j parser = new as2j(new StringReader(sExpr));
        try {
            return (Term)parser.re();
        } catch (Exception e) {
            logger.log(Level.SEVERE,"Error parsing expression "+sExpr,e);
        }
        return null;
    }
	
	/** make a hard copy of the terms */
	public Object clone() {
		// do not call constructor with term parameter!
		RelExprTerm t = new RelExprTerm();
		if (lhs != null) {
			t.lhs = (Term) lhs.clone();
		}

		t.op = this.op;
		
		if (rhs != null) {
			t.rhs = (Term) rhs.clone();
		}
		return t;
	}
	

	public boolean equals(Object t) {
		try {
			RelExprTerm eprt = (RelExprTerm)t;
			if (lhs == null && eprt.lhs != null) {
				return false;
			}
			if (lhs != null && !lhs.equals(eprt.lhs)) {
				return false;
			}
			
			if (op != eprt.op) {
				return false;
			}

			if (rhs == null && eprt.rhs != null) {
				return false;
			}
			if (rhs != null && !rhs.equals(eprt.rhs)) {
				return false;
			}
			return true;
		} catch (ClassCastException e) {
			return false;
		}
	}
	
	/** gets the Operation of this Expression */
	public RelationalOp getOp() {
		return op;
	}
	
	/** gets the LHS of this Expression */
	public Term getLHS() {
		return lhs;
	}
	
	/** gets the RHS of this Expression */
	public Term getRHS() {
		return rhs;
	}
	
	
	public void addTerm(Term t) {
		logger.warning("Do not use addTerm in expressions!");
	}

	public boolean isGround() {
		return lhs.isGround() && rhs.isGround();
	}
	
	public String toString() {
		return "("+lhs+op+rhs+")";
	}
    
    
    /** get as XML */
    public Element getAsDOM(Document document) {
        Element u = (Element) document.createElement("expression");
        u.setAttribute("type","relational");
        u.setAttribute("operator", op.toString());
        if (rhs!=null) {
            Element l = (Element) document.createElement("left");
            l.appendChild(lhs.getAsDOM(document));
            u.appendChild(l);
        }
        Element r = (Element) document.createElement("right");
        r.appendChild(rhs.getAsDOM(document));
        u.appendChild(r);
        return u;
    }
}
