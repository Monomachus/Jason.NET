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
    public static final Literal LTrue  = new Literal("true");
    public static final Literal LFalse = new Literal("false");

	private boolean type = LPos;

	/** creates a positive literal */
	public Literal(String functor) {
		super(functor);
	}

	/** to be used by atom */
    protected Literal(String functor, boolean createTerms) {
        super(functor, createTerms);
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
		as2j parser = new as2j(new StringReader(sLiteral));
		try {
			return parser.literal();
		} catch (Exception e) {
			logger.log(Level.SEVERE,"Error parsing literal " + sLiteral,e);
			return null;
		}
	}

	public boolean isInternalAction() {
		return getFunctor() != null && getFunctor().indexOf('.') >= 0;
	}
	
    @Override
	public boolean isLiteral() {
		return true;
	}

	@Override
	public boolean isAtom() {
		return super.isAtom() && !negated();
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
    @SuppressWarnings("unchecked")
    public Iterator<Unifier> logicalConsequence(final Agent ag, final Unifier un) {
        if (isInternalAction()) {
            try {
            	// clone terms array
                Term[] current = getTermsArray();
                Term[] clone = new Term[current.length];
                for (int i=0; i<clone.length; i++) {
                    clone[i] = (Term)current[i].clone();
                    clone[i].apply(un);
                }

            	// calls execute
                Object oresult = ag.getIA(this).execute(ag.getTS(), un, clone);
                if (oresult instanceof Boolean && (Boolean)oresult) {
                    return LogExpr.createUnifIterator(un);
                } else if (oresult instanceof Iterator) {
                    return ((Iterator<Unifier>)oresult);
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, getErrorMsg() + ": " +	e.getMessage(), e);
            }
            return LogExpr.EMPTY_UNIF_LIST.iterator();  // empty iterator for unifier
        } else if (this.equals(LTrue)) {
            return LogExpr.createUnifIterator(un);            
        } else if (this.equals(LFalse)) {
            return LogExpr.EMPTY_UNIF_LIST.iterator();            
        } else {
            final Literal lclone = (Literal)this.clone();
            lclone.apply(un);
            final Iterator<Literal> il = ag.getBB().getRelevant(lclone);

            if (il == null) // no relevant bels
                return LogExpr.EMPTY_UNIF_LIST.iterator();

            return new Iterator<Unifier>() {
                Unifier           current = null;
                Iterator<Unifier> ruleIt = null; // current rule solutions iterator
                Rule              rule; // current rule
                Literal           lcloneAnnon = null; // a copy of lclone with makeVarsAnnon
                
                public boolean hasNext() {
                    if (current == null) get();
                    return current != null;
                }

                public Unifier next() {
                    if (current == null) get();
                    Unifier a = current;
                    current = null;
                    return a;
                }

                private void get() {
                    current = null;
                    
                    // try rule iterator
                    while (ruleIt != null && ruleIt.hasNext()) {
                        // unifies the rule head with the result of rule evaluation
                        Unifier ruleUn = ruleIt.next(); // evaluation result
                        Literal rhead  = rule.headClone();
                        rhead.apply(ruleUn);
                        
                        Unifier unC = (Unifier) un.clone();
                        if (unC.unifiesNoUndo(lclone, rhead)) {
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
                            if (lcloneAnnon == null) {
                                lcloneAnnon = (Literal)lclone.clone();
                                lcloneAnnon.makeVarsAnnon();
                            }
                            Unifier ruleUn = new Unifier();
                            if (ruleUn.unifiesNoUndo(lcloneAnnon, rule)) { // the rule head unifies with the literal
                                ruleIt = rule.getBody().logicalConsequence(ag,ruleUn);
                                get();
                                if (current != null) { // if it get a value
                                    return;
                                }
                            }
                        } else {
                            /* -- the problem below was solved by translating test_rule(A,A) to test_rule(A,A):-true.
                            if (!b.isGround()) {
                                // All the code below is necessary for situations like
                                //    test_rule(A,A).
                                //    !test(test_wrong_value).
                                //    +!test(A) : test_rule(Test,test_right_value) <- .println("Test = ",Test).
                                // where a local variable has the same name as a variable in the belief.
                                //
                                // So, the solution is
                                // 1. create a new empy unifier to unify lclone with b 
                                //                                           ; lclone is test_rule(Test,test_right_value)
                                //                                           ; b is test_rule(A,A)
                                //                                           ; u is {A=test_right_value, Test=test_right_value}
                                //                                           ; note the value of A in this unifier
                                // 2. create another clone b of lclone to apply this
                                //    unifier u                              ; c after apply is test_rule(test_right_value,test_right_value)
                                // 3. create the result unifier as a clone of the current (un)
                                // 4. using the new unifier, unify lclone and
                                //    b to get only the value of Test and not the A
                                //    in the result unifier
                                Unifier u = new Unifier();
                                if (u.unifies(lclone, b)) {
                                    b = (Literal)lclone.clone();
                                    b.apply(u);
                                }
                            }
                            */
                            Unifier u = (Unifier) un.clone();
                            if (u.unifiesNoUndo(lclone, b)) {
                                current = u;
                                return;
                            }
                        }
                    }
                }

                public void remove() {}
            };
        }
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

    public String getErrorMsg() {
    	String line = (getSrcLine() >= 0 ? ":"+getSrcLine() : "");
    	String ia   = (isInternalAction() ? " internal action" : "");
        return "Error in "+ia+" '"+this+"' ("+ getSrc() + line + ")";    	
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
		l.add((ListTerm)getAnnots().clone());
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
        if (isInternalAction()) {
            u.setAttribute("ia", isInternalAction()+"");
        }
        if (negated()) {
            u.setAttribute("negated", negated()+"");
        }
        u.appendChild(super.getAsDOM(document));
        return u;
    }    
}
