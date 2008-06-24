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
import jason.asSemantics.Unifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents an atom (a structure with no arguments, e.g. "tell", "a"), it is an
 * immutable object.  It extends Literal, so can be used in place of a
 * Literal, but does not allow operations on terms/atoms and can not be negated.
 */
public final class Atom extends Literal {

    private static final long serialVersionUID = 1L;
    private static Logger logger = Logger.getLogger(Atom.class.getName());

    public Atom(String functor) {
        super(functor, 0);
    }
    
    public Object clone() {
		return this; // since this object is immutable
    }

    @Override
    public boolean apply(Unifier u) {
    	return false;
    }
    
	//
	// override structure methods
	//
    
	@Override
    public void addTerm(Term t) {
		logger.log(Level.SEVERE, "atom error!",new JasonException("atom has no terms!"));
    }
    
	@Override
    public void addTerms(List<Term> l) {
		logger.log(Level.SEVERE, "atom error!",new JasonException("atom has no terms!"));
    }
    
	@Override
    public void setTerms(List<Term> l) {
		logger.log(Level.SEVERE, "atom error!",new JasonException("atom has no terms!"));
    }
    
	@Override
    public void setTerm(int i, Term t) {
		logger.log(Level.SEVERE, "atom error!",new JasonException("atom has no terms!"));
    }
     
	@Override
    public int getArity() {
		return 0;
    }
    
	@Override
	public boolean isAtom() {
		return true;
	}

	@Override
    public boolean isGround() {
        return true;
    }
	
	@Override
	public boolean hasTerm() {
	    return false;
	}
	
	@Override
	public boolean hasVar(VarTerm t) {
	    return false;
	}
	
	@Override
	public void countVars(Map<VarTerm, Integer> c) {}
	
	@Override
	protected List<Term> getDeepCopyOfTerms() {
		// this method exists to make the Structure(Structure) constructor to work with 
		// an Atom as parameter
		return new ArrayList<Term>(2);
	}

	@Override
	public List<Term> getTerms() {
		return emptyTermList;
	}
	
	@Override
	public Term[] getTermsArray() {
	    return emptyTermArray;
	}
	
	@Override
	public void setNegated(boolean b) {
    	logger.log(Level.SEVERE, "You should not negate the atom "+this+"\n",new Exception());
		super.setNegated(b);
	}
	
    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        if (o instanceof Structure) {
        	Structure s = (Structure)o;
        	return s.isAtom() && getFunctor().equals(s.getFunctor());
        }
        return false;
    }
    
    @Override
    public boolean addAnnot(Term t) {
    	logger.log(Level.SEVERE, "You should not add annot '"+t+"' in atom "+this+"\n",new Exception());
    	return super.addAnnot(t);
    }
    
    @Override
    public void addAnnots(List<Term> l) {
        logger.log(Level.SEVERE, "You should not add annots '"+l+"' in atom "+this+"\n",new Exception());
        super.addAnnots(l);
    }
    
    @Override public void makeTermsAnnon() { }
    @Override public void makeVarsAnnon() { }
    @Override public void makeVarsAnnon(Unifier un) { }

    @Override
    protected int calcHashCode() {
        return getFunctor().hashCode();
    }
}
