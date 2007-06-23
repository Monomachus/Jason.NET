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
 * Represents a relational expression like 10 > 20.
 * 
 * When the operator is <b>=..</b>, the first argument is a literal and the 
 * second as list, e.g.:
 * <code>
 * Literal =.. [functor, list of terms, list of annots]
 * </code>
 * Examples:
 * <ul>
 * <li> X =.. [~p, [t1, t2], [a1,a2]]<br>
 *      X is ~p(t1,t2)[a1,a2]
 * <li> ~p(t1,t2)[a1,a2] =.. X<br>
 *      X is [~p, [t1, t2], [a1,a2]]
 * </ul>
 * 
 * @author jomi
 */
public class RelExpr implements LogicalFormula {

	private static final long serialVersionUID = 1L;

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

    private Term lhs, rhs;
	private RelationalOp op = RelationalOp.none;

	static private Logger logger = Logger.getLogger(RelExpr.class.getName());
	
	public RelExpr() {
		super();
	}
	
	public RelExpr(Term t1, RelationalOp oper, Term t2) {
		lhs = t1;
		op = oper;
		rhs = t2;
	}
    
	public boolean apply(Unifier u) {
		boolean r1 = true, r2 = true;
		if (lhs != null) r1 = lhs.apply(u);
		if (rhs != null) r2 = rhs.apply(u);
		return r1 && r2;
	}

	public int getSrcLine() {
		int l = -1;
		if (lhs != null)          l = lhs.getSrcLine();
		if (rhs != null && l < 0) l = rhs.getSrcLine();
		return l;
	}
	
    public Iterator<Unifier> logicalConsequence(final Agent ag, Unifier un) {
        Term xp = (Term)lhs.clone();
        Term yp = (Term)rhs.clone();
        xp.apply(un);
        yp.apply(un);

        switch (op) {
        
        case gt : if (xp.compareTo(yp)  >  0) return LogExpr.createUnifIterator(un);  break;
        case gte: if (xp.compareTo(yp)  >= 0) return LogExpr.createUnifIterator(un);  break;
        case lt : if (xp.compareTo(yp)  <  0) return LogExpr.createUnifIterator(un);  break;
        case lte: if (xp.compareTo(yp)  <= 0) return LogExpr.createUnifIterator(un);  break;
        case eq : if (xp.equals(yp))          return LogExpr.createUnifIterator(un);  break;
        case dif: if (!xp.equals(yp))         return LogExpr.createUnifIterator(un);  break;
        case unify: if (un.unifies(xp,yp))    return LogExpr.createUnifIterator(un);  break;

        case literalBuilder: 
            try {
                Literal  p = (Literal)xp;  // lhs clone
                ListTerm l = (ListTerm)yp; // rhs clone
                //logger.info(p+" test "+l+" un="+un); 

                // both are not vars, using normal unification
                if (!p.isVar() && !l.isVar()) {
                	if (un.unifies(p.getAsListOfTerms(), l)) {
                		return LogExpr.createUnifIterator(un);
                	}
                } else {
                
	                // first is var, second is list, var is assigned to l tranformed in literal
	                if (p.isVar() && l.isList() && un.unifies(p, Literal.newFromListOfTerms(l))) {
	                    return LogExpr.createUnifIterator(un);
	                }
	                
	                // first is literal, second is var, var is assigned to l tranformed in list
	                if (p.isLiteral() && l.isVar() && un.unifies(p.getAsListOfTerms(), l)) {
	                    return LogExpr.createUnifIterator(un);
	                }

	                // both are vars, error
	                logger.log(Level.SEVERE, "Both arguments of "+lhs+" =.. "+rhs+" are variables!");
                }

            } catch (Exception e) {
                logger.log(Level.SEVERE, "The arguments of operator =.. are not Literal and List.", e);
            }
            break;
        }
        
        return LogExpr.EMPTY_UNIF_LIST.iterator();  // empty iterator for unifier
    }   

    /** returns some LogicalFormula that can be evaluated */
    public static LogicalFormula parseExpr(String sExpr) {
        as2j parser = new as2j(new StringReader(sExpr));
        try {
            return (LogicalFormula)parser.rel_expr();
        } catch (Exception e) {
            logger.log(Level.SEVERE,"Error parsing expression "+sExpr,e);
        }
        return null;
    }
	
	/** make a hard copy of the terms */
	public Object clone() {
		// do not call constructor with term parameter!
		RelExpr t = new RelExpr();
        t.op = this.op;
		if (lhs != null) t.lhs = (Term) lhs.clone();
		if (rhs != null) t.rhs = (Term) rhs.clone();
		return t;
	}
	

    @Override
	public boolean equals(Object t) {
		if (t != null && t instanceof RelExpr) {
			RelExpr eprt = (RelExpr)t;
			if (lhs == null && eprt.lhs != null) return false;
			if (lhs != null && !lhs.equals(eprt.lhs)) return false;
			if (op != eprt.op) return false;
			if (rhs == null && eprt.rhs != null) return false;
			if (rhs != null && !rhs.equals(eprt.rhs)) return false;
			return true;
		}
        return false;
	}

    @Override
    public int hashCode() {
        int code = op.hashCode();
        if (lhs != null) code += lhs.hashCode();
        if (rhs != null) code += rhs.hashCode();
        return code;
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
