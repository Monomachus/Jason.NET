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
 *  represents a logical expression like [ <le> ] < & | "|" | not > <le>.
 */
public class LogExprTerm extends TermImpl {

	private static final long serialVersionUID = 1L;

	public enum LogicalOp { 
		none   { public String toString() { return ""; } }, 
		not    { public String toString() { return "not "; } }, 
		and    { public String toString() { return " & "; } },
		or     { public String toString() { return " | "; } };
	}

	private  Term lhs, rhs;
	private  LogicalOp op = LogicalOp.none;

	static private Logger logger = Logger.getLogger(LogExprTerm.class.getName());
	
	public LogExprTerm() {
		super();
	}
	
	public LogExprTerm(Term t1, LogicalOp oper, Term t2) {
		lhs = t1;
		op = oper;
		rhs = t2;
	}

	public LogExprTerm(LogicalOp oper, Term t1) {
		op = oper;
		rhs = t1;
	}
    
    /** 
     * logCons checks whether one particular predicate
     * is a log(ical)Cons(equence) of the belief base.
     * 
     * Returns an iterator for all unifiers that are logCons.
     */
    @Override
    public Iterator<Unifier> logCons(final Agent ag, Unifier un) {
        try {
	        final Iterator<Unifier> ileft;
	        switch (op) {
	        
	        case not:
	            if (!rhs.logCons(ag,un).hasNext()) {
	                return createUnifIterator(un);
	            }
	            break;
	        
	        case and:
	            ileft = lhs.logCons(ag,un);
	            return new Iterator<Unifier>() {
	                Unifier current = null;
	                Iterator<Unifier> iright = null;
	                public boolean hasNext() {
	                    if (current == null) get();
	                    return current != null;
	                }
	                public Unifier next() {
	                    if (current == null) get();
	                    Unifier a = current;
	                    get();
	                    return a;
	                }
	                private void get() {
	                    current = null;
	                    while ((iright == null || !iright.hasNext()) && ileft.hasNext()) {
	                        iright = rhs.logCons(ag, ileft.next());
	                    }
	                    if (iright != null && iright.hasNext()) {
	                        current = iright.next();
	                    }
	                }
	                public void remove() {}
	            };
	            
	        case or:
	            ileft = lhs.logCons(ag,un);
	            final Iterator<Unifier> iright = rhs.logCons(ag,un);
	            return new Iterator<Unifier>() {
	                Unifier current = null;
	                public boolean hasNext() {
	                    if (current == null) get();
	                    return current != null;
	                }
	                public Unifier next() {
	                    if (current == null) get();
	                    Unifier a = current;
	                    get();
	                    return a;
	                }
	                private void get() {
	                    current = null;
	                    if (ileft.hasNext()) {
	                        current = ileft.next();
	                    } else if (iright.hasNext()) {
	                        current = iright.next();
	                    }
	                }
	                public void remove() {}
	            };
	        }
    	    } catch (Exception e) {
        		String slhs = "is null";
        		if (lhs != null) {
        			Iterator<Unifier> i = lhs.logCons(ag,un);
        			if (i != null) {
        				slhs = "";
        				while (i.hasNext()) {
        					slhs += i.next().toString()+", ";
        				}
        			} else {
        				slhs = "iterator is null";
        			}
        		} 
        		String srhs = "is null";
        		if (lhs != null) {
        			Iterator<Unifier> i = rhs.logCons(ag,un);
        			if (i != null) {
        				srhs = "";
        				while (i.hasNext()) {
        					srhs += i.next().toString()+", ";
        				}
        			} else {
        				srhs = "iterator is null";
        			}
        		} 
        		
        		logger.log(Level.SEVERE, "Error evaluating expression "+this+". \nlhs elements="+slhs+". \nrhs elements="+srhs,e);
        	}
        return EMPTY_UNIF_LIST.iterator();  // empty iterator for unifier
    }   

    /** returns some Term that can be evaluated */
    public static Term parseExpr(String sExpr) {
        as2j parser = new as2j(new StringReader(sExpr));
        try {
            return (Term)parser.le();
        } catch (Exception e) {
            logger.log(Level.SEVERE,"Error parsing expression "+sExpr,e);
        }
        return null;
    }
	
	/** make a hard copy of the terms */
	public Object clone() {
		// do not call constructor with term parameter!
		LogExprTerm t = new LogExprTerm();
		if (lhs != null) {
			t.lhs = (Term) lhs.clone();
		}

		t.op = this.op;
		
		if (rhs != null) {
			t.rhs = (Term) rhs.clone();
		}
		return t;
	}
	

    @Override
	public boolean equals(Object t) {
		if (t != null && t instanceof LogExprTerm) {
			LogExprTerm eprt = (LogExprTerm)t;
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
		} 
		return false;
	}

    @Override
    public int hashCode() {
        int code = op.hashCode();
        if (lhs != null)
            code += lhs.hashCode();
        if (rhs != null)
            code += rhs.hashCode();
        return code;
    }	
	/** gets the Operation of this Expression */
	public LogicalOp getOp() {
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

	public boolean isUnary() {
		return lhs == null;
	}

	public boolean isGround() {
		return lhs.isGround() && rhs.isGround();
	}
	
    @Override
    public String toString() {
		if (lhs == null) {
			return op+"("+rhs+")";
		} else {
			return "("+lhs+op+rhs+")";
		}
	}

    /** get as XML */
    @Override
    public Element getAsDOM(Document document) {
        Element u = (Element) document.createElement("expression");
        u.setAttribute("type","logical");
        u.setAttribute("operator", op.toString());
        if (lhs != null) {
            Element l = (Element) document.createElement("left");
            l.appendChild(lhs.getAsDOM(document));
            u.appendChild(l);
        }
        if (rhs != null) {
            Element r = (Element) document.createElement("right");
            r.appendChild(rhs.getAsDOM(document));
            u.appendChild(r);
	}
        return u;
    }
}
