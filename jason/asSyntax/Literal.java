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

import jason.JasonException;
import jason.asSemantics.Agent;
import jason.asSemantics.Unifier;
import jason.asSyntax.parser.as2j;

import java.io.StringReader;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * A Literal is a Pred with strong negation (~).
 */
public class Literal extends Pred implements Cloneable {

	public static final boolean   LPos       = true;
    public static final boolean   LNeg       = false;
    public static final Literal   LTrue      = new Literal(LPos, new Pred("true"));
    public static final Literal   LFalse     = new Literal(LPos, new Pred("false"));

    static private Logger logger = Logger.getLogger(Literal.class.getName());

	boolean type = LPos;

	public Literal() {
	}

	/** if pos == true, the literal is positive, else it is negative */
	public Literal(boolean pos, Pred p) {
		super(p);
		type = pos;
	}

	public Literal(Literal l) {
		super((Pred) l);
		type = l.type;
	}
	
	public Literal(Term t) {
		super(t);
		type = LPos;
	}

	public static Literal parseLiteral(String sLiteral) {
		as2j parser = new as2j(new StringReader(sLiteral));
		try {
			return parser.l();
		} catch (Exception e) {
			logger.log(Level.SEVERE,"Error parsing literal " + sLiteral,e);
			return null;
		}
	}

	public boolean isInternalAction() {
		return getFunctor() != null && getFunctor().indexOf('.') >= 0;
	}
	
	public boolean isLiteral() {
		return true;
	}

	public boolean negated() {
		return (type == LNeg);
	}

    /** 
     * logCons checks whether one particular predicate
     * is a log(ical)Cons(equence) of the belief base.
     * 
     * Returns an iterator for all unifiers that are logCons.
     */
    @Override
    public Iterator<Unifier> logCons(final Agent ag, final Unifier un) {
        if (isInternalAction()) {
            try {
                // calls execute
                if (ag.getIA(this).execute(ag.getTS(), un, getTermsArray())) {
                     return createUnifIterator(un);
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE,"Error in IA ",e);
            }
        } else if (this == LTrue) {
            return createUnifIterator(un);            
        } else if (this == LFalse) {
            return EMPTY_UNIF_LIST.iterator();            
        } else {
            List<Literal> relList = ag.getBS().getRelevant(this);
            if (relList != null) {
                final Iterator<Literal> il = relList.iterator();
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
                        while (il.hasNext()) {
                            Literal b = il.next();
                            Unifier unC = (Unifier) un.clone();
                            if (unC.unifies(Literal.this,b)) {
                                current = unC;
                                return;
                            }
                        }
                    }
                    public void remove() {}
                };
            }
        }
        
        return EMPTY_UNIF_LIST.iterator();  // empty iterator for unifier
    }   

    public boolean equals(Object o) {
		try {
			Literal l = (Literal) o;
			return (type == l.type && super.equals(l));
		} catch (Exception e) {
			return super.equals(o);
		}
	}

	public Object clone() {
		return new Literal(type, (Pred)this);
	}

	
	/** return [~] super.getFucntorArity */
	public String getFunctorArity() {
		if (functorArityBak == null) {
			functorArityBak = (type == LPos) ? super.getFunctorArity() : "~" + super.getFunctorArity(); 
		}
		return functorArityBak;
	}
	
	public int hashCode() {
		return getFunctorArity().hashCode();
	}

	/** returns this literal as a list [<functor>, <list of terms>, <list of annots>] */
	public ListTerm getAsListOfTerms() {
		ListTerm l = new ListTermImpl();
		l.add(new Literal(type, new Pred(getFunctor())));
		ListTerm lt = new ListTermImpl();
		if (getTerms() != null) {
			lt.addAll(getTerms());
		}
		l.add(lt);
		if (getAnnots() != null && !getAnnots().isEmpty()) {
			l.add(getAnnots().clone());
		} else {
			l.add(new ListTermImpl());
		}
		return l;
	}

	/** creates a literal from a list [<functor>, <list of terms>, <list of annots>] */
	public static Literal newFromListOfTerms(ListTerm lt) throws JasonException {
		try {
			Iterator i = lt.iterator();
			Literal l = (Literal)((Literal)i.next()).clone();
			if (i.hasNext()) {
				l.addTerms((ListTerm)((ListTerm)i.next()).clone());
			}
			if (i.hasNext()) {
				l.setAnnots((ListTerm)((ListTerm)i.next()).clone());
			}
			return l;
		} catch (Exception e) {
			throw new JasonException("Error creating literal from "+lt);
		}
	}
	
	public String toString() {
		if (type == LPos)
			return super.toString();
		else
			return "~" + super.toString();
	}

    /** get as XML */
    public Element getAsDOM(Document document) {
        Element u = (Element) document.createElement("literal");
        if (isInternalAction()) {
            u.setAttribute("ia", isInternalAction()+"");
        }
        u.appendChild(super.getAsDOM(document));
        /*u.setAttribute("term",super.toStringAsTerm());
        if (getAnnots() != null && !getAnnots().isEmpty()) {
            u.setAttribute("annots", getAnnots().toString());
        }
        */
        return u;
    }    
    
}