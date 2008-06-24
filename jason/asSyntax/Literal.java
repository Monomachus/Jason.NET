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
import jason.architecture.AgArch;
import jason.asSemantics.Agent;
import jason.asSemantics.Unifier;
import jason.asSyntax.parser.ParseException;
import jason.asSyntax.parser.as2j;

import java.io.StringReader;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * A Literal is a Pred with strong negation (~).
 */
public class Literal extends Pred implements LogicalFormula {

	private static final long serialVersionUID = 1L;
    private static Logger logger = Logger.getLogger(Literal.class.getName());
    
	public static final boolean LPos   = true;
    public static final boolean LNeg   = false;
    public static final Literal LTrue  = new TrueLiteral();
    public static final Literal LFalse = new FalseLiteral();

	private boolean type = LPos;

	/** creates a positive literal */
	public Literal(String functor) {
		super(functor);
	}

	/** to be used by atom */
    protected Literal(String functor, int termsSize) {
        super(functor, termsSize);
    }

	/** if pos == true, the literal is positive, otherwise it is negative */
	public Literal(boolean pos, String functor) {
		super(functor);
		type = pos;
	}

	/** if pos == true, the literal is positive, otherwise it is negative */
	public Literal(boolean pos, Pred p) {
		super(p);
		type = pos;
	}

	public Literal(Literal l) {
	    super((Pred) l);
	    type = l.type;
	}


	public static Literal parseLiteral(String sLiteral) {
		try {
			as2j parser = new as2j(new StringReader(sLiteral));
			return parser.literal();
		} catch (Exception e) {
			logger.log(Level.SEVERE,"Error parsing literal " + sLiteral,e);
			return null;
		}
    }
	
    public static Literal tryParsingLiteral(String sLiteral) throws ParseException {
        return new as2j(new StringReader(sLiteral)).literal();
    }

    @Override
	public boolean isLiteral() {
		return true;
	}

	@Override
	public boolean isAtom() {
		return super.isAtom() && !negated();
	}
	
	/** to be overridden by subclasses (as internal action) */
	public boolean canBeAddedInBB() {
		return true;
	}
	
	public boolean negated() {
		return type == LNeg;
	}
    
    public void setNegated(boolean b) {
        type = b;
        resetHashCodeCache();
    }

    /** 
     * logicalConsequence checks whether one particular predicate
     * is a logical consequence of the belief base.
     * 
     * Returns an iterator for all unifiers that are logCons.
     */
    public Iterator<Unifier> logicalConsequence(final Agent ag, final Unifier un) {
        final Iterator<Literal> il = ag.getBB().getCandidateBeliefs(this, un);
        if (il == null) // no relevant bels
            return LogExpr.EMPTY_UNIF_LIST.iterator();
        
        return new Iterator<Unifier>() {
            Unifier           current = null;
            Iterator<Unifier> ruleIt = null; // current rule solutions iterator
            Rule              rule; // current rule
            Literal           cloneAnnon = null; // a copy of the literal with makeVarsAnnon
            AgArch            arch = ag.getTS().getUserAgArch();
            boolean           needsUpdate = true;
            
            public boolean hasNext() {
                if (needsUpdate)
                    get();
                return current != null;
            }

            public Unifier next() {
                if (needsUpdate)
                    get();
                Unifier a = current;
                if (current != null)
                    needsUpdate = true;
                return a;
            }

            private void get() {
                needsUpdate = false;
                current     = null;
                if (!arch.isRunning()) return;
                
                // try rule iterator
                while (ruleIt != null && ruleIt.hasNext()) {
                    // unifies the rule head with the result of rule evaluation
                    Unifier ruleUn = ruleIt.next(); // evaluation result
                    Literal rhead  = rule.headClone();
                    rhead.apply(ruleUn);
                    rhead.makeVarsAnnon(ruleUn);
                    
                    Unifier unC = un.copy();
                    if (unC.unifiesNoUndo(Literal.this, rhead)) {
                        current = unC;
                        return;
                    }
                }
                
                // try literal iterator
                while (il.hasNext()) {
                    Literal b = il.next(); // b is the relevant entry in BB
                    if (b.isRule()) {
                        rule = (Rule)b;
                        
                        // create a copy of this literal, ground it and 
                        // make its vars anonymous, 
                        // it is used to define what will be the unifier used
                        // inside the rule.
                        if (cloneAnnon == null) {
                            cloneAnnon = (Literal)Literal.this.clone();
                            cloneAnnon.apply(un);
                            cloneAnnon.makeVarsAnnon(null);
                        }
                        Unifier ruleUn = new Unifier();
                        if (ruleUn.unifiesNoUndo(cloneAnnon, rule)) { // the rule head unifies with the literal
                            ruleIt = rule.getBody().logicalConsequence(ag,ruleUn);
                            get();
                            if (current != null) { // if it get a value
                                return;
                            }
                        }
                    } else {
                        Unifier u = (Unifier) un.clone();
                        if (u.unifiesNoUndo(Literal.this, b)) {
                            current = u;
                            return;
                        }
                    }
                }
            }

            public void remove() {}
        };
    }   

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;

        if (o instanceof Literal) {
			final Literal l = (Literal) o;
			return type == l.type && hashCode() == l.hashCode() && super.equals(l);
		} else if (o instanceof Structure) {
			return !negated() && super.equals(o);
		}
        return false;
	}

    @Override    
    public String getErrorMsg() {
        return "Error in '"+this+"' ("+ super.getErrorMsg() + ")";       
    }
    
    @Override
    public int compareTo(Term t) {
        if (t.isLiteral()) {
            Literal tl = (Literal)t;
            if (!negated() && tl.negated()) {
                return -1;
            } if (negated() && !tl.negated()) {
                return 1;
            }
        }
        int c = super.compareTo(t);
        if (c != 0) return c;
        return 0;
    }        

	public Object clone() {
        Literal c = new Literal(this);
        c.predicateIndicatorCache = this.predicateIndicatorCache;
        c.hashCodeCache = this.hashCodeCache;
        return c;
	}
	
	public Literal copy() {
	    return (Literal)clone();
	}

    
    @Override
    protected int calcHashCode() {
        int result = super.calcHashCode();
        if (negated()) result += 3271;
        return result;
    }

	/** returns [~] super.getPredicateIndicator */
	@Override 
    public PredicateIndicator getPredicateIndicator() {
		if (predicateIndicatorCache == null)
		    predicateIndicatorCache = new PredicateIndicator(((type == LPos) ? "" : "~")+getFunctor(),getArity());
		return predicateIndicatorCache;
	}
	
	/** returns this literal as a list [<functor>, <list of terms>, <list of annots>] */
	public ListTerm getAsListOfTerms() {
		ListTerm l = new ListTermImpl();
		l.add(new Literal(type, getFunctor()));
		ListTerm lt = new ListTermImpl();
		lt.addAll(getTerms());
		l.add(lt);
		if (hasAnnot()) {
		    l.add((ListTerm)getAnnots().clone());
		} else {
		    l.add(new ListTermImpl());
		}
		return l;
	}

	/** creates a literal from a list [<functor>, <list of terms>, <list of annots>] */
	public static Literal newFromListOfTerms(ListTerm lt) throws JasonException {
		try {
			Iterator<Term> i = lt.iterator();
			
			Term tfunctor = i.next();

			boolean pos = Literal.LPos;
			if (tfunctor.isLiteral() && ((Literal)tfunctor).negated()) {
				pos = Literal.LNeg;
			}

			Literal l = new Literal(pos,((Structure)tfunctor).getFunctor());

			if (i.hasNext()) {
				l.setTerms((ListTerm)((ListTerm)i.next()).clone());
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
    @Override
    public Element getAsDOM(Document document) {
        Element u = (Element) document.createElement("literal");
        if (negated()) {
            u.setAttribute("negated", negated()+"");
        }
        u.appendChild(super.getAsDOM(document));
        return u;
    }

    
    @SuppressWarnings("serial")
    static final class TrueLiteral extends Literal {
    	public TrueLiteral() {
    		super("true",0);
		}

    	@Override
        public boolean canBeAddedInBB() {
    		return false;
    	}
        
    	@Override
        public Iterator<Unifier> logicalConsequence(final Agent ag, final Unifier un) {
        	return LogExpr.createUnifIterator(un);            
        }
    }
    
    @SuppressWarnings("serial")
	static final class FalseLiteral extends Literal {
    	public FalseLiteral() {
    		super("false",0);
		}

    	@Override
        public boolean canBeAddedInBB() {
    		return false;
    	}
    	
        @Override
        public Iterator<Unifier> logicalConsequence(final Agent ag, final Unifier un) {
        	return LogExpr.EMPTY_UNIF_LIST.iterator();            
        }
    }
    
}
