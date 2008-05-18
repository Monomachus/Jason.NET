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
import jason.asSemantics.InternalAction;
import jason.asSemantics.Unifier;
import jason.stdlib.foreach;
import jason.stdlib.loop;

import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * A particular type of literal used to represent internal actions (has a "." in the functor).
 */
public class InternalActionLiteral extends Literal {

	private static final long serialVersionUID = 1L;
    private static Logger logger = Logger.getLogger(InternalActionLiteral.class.getName());
    
    private InternalAction ia = null; // reference to the object that implements the internal action
    
	public InternalActionLiteral(String functor) {
		super(functor);
	}

	// used by clone
	public InternalActionLiteral(InternalActionLiteral l) {
		super((Literal) l);
		this.ia = l.ia;
	}

	// used by the parser
	public InternalActionLiteral(Pred p, Agent ag) throws Exception {
        super(true,p);
        if (ag != null)
            ia = ag.getIA(getFunctor());
    }
	
    @Override
	public boolean isInternalAction() {
		return true;
	}

    @Override
    public boolean isAtom() {
        return false;
    }
    
    @Override
    public boolean canBeAddedInBB() {
		return false;
	}

    @Override
    public boolean apply(Unifier u) {
    	if (this.ia != null && (this.ia instanceof loop || this.ia instanceof foreach))
    		return false;
		else 
			return super.apply(u);
    }
    
    @Override
    public void countVars(Map<VarTerm, Integer> c) {
        super.countVars(c);
        if (this.ia != null && this.ia instanceof jason.stdlib.wait) {
            // count the vars of first arg
            if (getTerm(0).isString()) {
                try {
                    Trigger te = Trigger.parseTrigger( ((StringTerm)getTerm(0)).getString() );
                    te.getLiteral().countVars(c);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    public Iterator<Unifier> logicalConsequence(Agent ag, Unifier un) {
        if (ag.getTS().getUserAgArch().isRunning()) {
            try {
            	// clone terms array
                Term[] clone = getTermsArray();
                for (int i=0; i<clone.length; i++) {
                    clone[i] = (Term)clone[i].clone();
                    clone[i].apply(un);
                }
    
            	// calls IA's execute method
                Object oresult = getIA(ag).execute(ag.getTS(), un, clone);
                if (oresult instanceof Boolean && (Boolean)oresult) {
                    return LogExpr.createUnifIterator(un);
                } else if (oresult instanceof Iterator) {
                    return ((Iterator<Unifier>)oresult);
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, getErrorMsg() + ": " +	e.getMessage(), e);
            }
        }
        return LogExpr.EMPTY_UNIF_LIST.iterator();  // empty iterator for unifier
    }   

    public InternalAction getIA(Agent ag) throws Exception {
        if (ia == null && ag != null)
            ia = ag.getIA(getFunctor());
        return ia;
    }
    
    @Override
    public String getErrorMsg() {
    	String line = (getSrcLine() >= 0 ? ":"+getSrcLine() : "");
        return "Error in internal action '"+this+"' ("+ getSrc() + line + ")";    	
    }
    
	public Object clone() {
        InternalActionLiteral c = new InternalActionLiteral(this);
        c.predicateIndicatorCache = this.predicateIndicatorCache;
        c.hashCodeCache           = this.hashCodeCache;
        return c;
	}

    
    /** get as XML */
    @Override
    public Element getAsDOM(Document document) {
        Element u = super.getAsDOM(document);
        u.setAttribute("ia", isInternalAction()+"");
        return u;
    }    
}
