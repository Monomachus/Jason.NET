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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 This class represents an abstract literal (an Atom, Structure, Predicate, ...), is is mainly
 the interface of a literal. 
 
 To create a Literal, one of the following classes may be used:
 Atom -- the most simple literal, is composed by a functor (no term, no annots);
 Structure -- has functor and terms;
 Pred -- has functor, terms, and annotations;
 LiteralImpl -- Pred + negation. This latter class supports all the operations of 
 the Literal interface.
 
 There are useful static methods in this class to create Literals.
 
 @author jomi
 
 @see Atom
 @see Structure
 @see Pred
 @see LiteralImpl
 
 */
public abstract class Literal extends DefaultTerm implements LogicalFormula {

    private static final long serialVersionUID = 1L;
    private static Logger logger = Logger.getLogger(Literal.class.getName());
    
    public static final boolean LPos   = true;
    public static final boolean LNeg   = false;
    public static final Literal LTrue  = new TrueLiteral();
    public static final Literal LFalse = new FalseLiteral();

    protected PredicateIndicator predicateIndicatorCache = null; // to not compute it all the time (is is called many many times)
    
    /** create a literal with only a functor, but where terms and annotations may be added */
    public static Literal create(String functor) {
        return new LiteralImpl(functor);
    }

    /** create a literal copying data from other literal */
    public static Literal create(Literal l) {
        return new LiteralImpl(l);
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

    public Literal copy() {
        return (Literal)clone(); // should call the clone, that is overridden in subclasses
    }

    public abstract String getFunctor();
    
    @Override
    public boolean isLiteral() {
        return true;
    }
    
    /** returns functor symbol "/" arity */
    public PredicateIndicator getPredicateIndicator() {
        if (predicateIndicatorCache == null) {
            predicateIndicatorCache = new PredicateIndicator(getFunctor(),getArity());
        }
        return predicateIndicatorCache;
    }

    /* default implementation of some methods */

    public int getArity()         { return 0;  }
    public boolean hasTerm()      { return false; } // should use getArity to work for list/atom
    public List<Term> getTerms()  { return Structure.emptyTermList;   }
    public Term[] getTermsArray() { return getTerms().toArray(Structure.emptyTermArray);  }
    public List<VarTerm> getSingletonVars() { return new ArrayList<VarTerm>(); }


    public void makeTermsAnnon()  {}
    public void makeVarsAnnon()   {}
    public void makeVarsAnnon(Unifier un) {}

    public ListTerm getAnnots()     { return null; }
    public boolean hasAnnot(Term t) { return false; }
    public boolean hasAnnot()       { return false; }
    public boolean hasSubsetAnnot(Literal p)            { return true; }
    public boolean hasSubsetAnnot(Literal p, Unifier u) { return true; }
    public void clearAnnots()       { }
    public ListTerm getAnnots(String functor) { return new ListTermImpl(); }
    public ListTerm getSources()    { return new ListTermImpl(); }
    public boolean hasSource()      { return false; }
    public boolean hasSource(Term agName) { return false; }

    public boolean canBeAddedInBB() { return false; }
    public boolean negated()        { return false; }

    public boolean equalsAsStructure(Object p) { return false;  }
    
	/* Not implemented methods */
	
    // structure
    public void addTerm(Term t) {  logger.log(Level.SEVERE, "addTerm is not implemented in the class "+this.getClass().getSimpleName(), new Exception());  }
    public void delTerm(int index) { logger.log(Level.SEVERE, "delTerm is not implemented in the class "+this.getClass().getSimpleName(), new Exception());  }
    public void addTerms(Term ... ts ) { logger.log(Level.SEVERE, "addTerms is not implemented in the class "+this.getClass().getSimpleName(), new Exception());  }
    public void addTerms(List<Term> l) { logger.log(Level.SEVERE, "addTerms is not implemented in the class "+this.getClass().getSimpleName(), new Exception());  }
    public Term getTerm(int i)    { logger.log(Level.SEVERE, "getTerm(i) is not implemented in the class "+this.getClass().getSimpleName(), new Exception()); return null; }
    public void setTerms(List<Term> l) { logger.log(Level.SEVERE, "setTerms is not implemented in the class "+this.getClass().getSimpleName(), new Exception());  }
    public void setTerm(int i, Term t) { logger.log(Level.SEVERE, "setTerm is not implemented in the class "+this.getClass().getSimpleName(), new Exception());  }
    
    // pred
    public void setAnnots(ListTerm l) { logger.log(Level.SEVERE, "setAnnots is not implemented in the class "+this.getClass().getSimpleName(), new Exception());  }
    public boolean addAnnot(Term t) { logger.log(Level.SEVERE, "addAnnot is not implemented in the class "+this.getClass().getSimpleName(), new Exception()); return false; }
    public void addAnnots(List<Term> l) { logger.log(Level.SEVERE, "addAnnots is not implemented in the class "+this.getClass().getSimpleName(), new Exception());  }
    public void addAnnot(int index, Term t) { logger.log(Level.SEVERE, "addAnnot is not implemented in the class "+this.getClass().getSimpleName(), new Exception());  }
    public void delAnnot(Term t) { logger.log(Level.SEVERE, "delAnnot is not implemented in the class "+this.getClass().getSimpleName(), new Exception());  }
    public boolean delAnnots(List<Term> l) { logger.log(Level.SEVERE, "delAnnots is not implemented in the class "+this.getClass().getSimpleName(), new Exception()); return false; }
    public boolean importAnnots(Literal p) { logger.log(Level.SEVERE, "importAnnots is not implemented in the class "+this.getClass().getSimpleName(), new Exception());  return false; }

    public void addSource(Term agName) { logger.log(Level.SEVERE, "addSource is not implemented in the class "+this.getClass().getSimpleName(), new Exception());  }
    public boolean delSource(Term agName) { logger.log(Level.SEVERE, "delSource is not implemented in the class "+this.getClass().getSimpleName(), new Exception());  return false; }
    public void delSources() { logger.log(Level.SEVERE, "delSources is not implemented in the class "+this.getClass().getSimpleName(), new Exception());  }

    // literal    
    public void setNegated(boolean b) {  logger.log(Level.SEVERE, "setNegated is not implemented in the class "+this.getClass().getSimpleName(), new Exception());  }
    
    
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
                    
                    Unifier unC = un.clone();
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
                            cloneAnnon = Literal.this.copy();
                            cloneAnnon.apply(un);
                            cloneAnnon.makeVarsAnnon(un);
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
                        Unifier u = un.clone();
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

    
	/** returns this literal as a list with three elements: [functor, list of terms, list of annots] */
	public ListTerm getAsListOfTerms() {
		ListTerm l = new ListTermImpl();
		l.add(new LiteralImpl(!negated(), getFunctor()));
		ListTerm lt = new ListTermImpl();
		lt.addAll(getTerms());
		l.add(lt);
		if (hasAnnot()) {
		    l.add(getAnnots().cloneLT());
		} else {
		    l.add(new ListTermImpl());
		}
		return l;
	}

	/** creates a literal from a list with three elements: [functor, list of terms, list of annots] */
	public static Literal newFromListOfTerms(ListTerm lt) throws JasonException {
		try {
			Iterator<Term> i = lt.iterator();
			
			Term tfunctor = i.next();

			boolean pos = Literal.LPos;
			if (tfunctor.isLiteral() && ((Literal)tfunctor).negated()) {
				pos = Literal.LNeg;
			}

			Literal l = new LiteralImpl(pos,((Atom)tfunctor).getFunctor());

			if (i.hasNext()) {
				l.setTerms(((ListTerm)i.next()).cloneLT());
			}
			if (i.hasNext()) {
				l.setAnnots(((ListTerm)i.next()).cloneLT());
			}
			return l;
		} catch (Exception e) {
			throw new JasonException("Error creating literal from "+lt);
		}
	}
	
    @SuppressWarnings("serial")
    static final class TrueLiteral extends LiteralImpl {
    	public TrueLiteral() {
    		super("true", 0);
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
	static final class FalseLiteral extends LiteralImpl {
    	public FalseLiteral() {
    		super("false", 0);
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
