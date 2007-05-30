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
//
//----------------------------------------------------------------------------

package jason.asSyntax;

import jason.asSyntax.parser.as2j;

import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Trigger implements Cloneable {


    public enum TEOperator { 
        add { public String toString() { return "+"; } }, 
        del { public String toString() { return "-"; } }
    };
    
    public enum TEType { 
        belief  { public String toString() { return ""; } }, 
        achieve { public String toString() { return "!"; } }, 
        test    { public String toString() { return "?"; } }
    };
    
	static private Logger logger = Logger.getLogger(Trigger.class.getName());
  
	TEOperator operator = TEOperator.add;
    TEType     type     = TEType.belief;

	Literal literal;
	
	public Trigger(TEOperator op, TEType t, Literal l) {
		literal = l;
        type    = t;
		setTrigOp(op);
	}

	public static Trigger parseTrigger(String sTe) {
		as2j parser = new as2j(new StringReader(sTe));
		try {
			return parser.trigger(); 
		} catch (Exception e) {
			logger.log(Level.SEVERE,"Error parsing trigger" + sTe,e);
			return null;
		}
	}

	public void setTrigOp(TEOperator op) {
		operator = op;
        piCache  = null;
	}

	public boolean sameType(Trigger e) {
		return operator == e.operator && type == e.type;
	}

    @Override
	public boolean equals(Object o) {
        if (o != null && o instanceof Trigger) {
            Trigger t = (Trigger) o;
            return (operator == t.operator && type == t.type && literal.equals(t.getLiteral()));
        }
        return false;
	}

    @Override
    public int hashCode() {
        return getPredicateIndicator().hashCode();
    }


	public boolean isAchvGoal() {
		return type == TEType.achieve;
	}

	public boolean isGoal() {
		return type == TEType.achieve || type == TEType.test;
	}

	public TEType getType() {
		return type;
	}

	public boolean isAddition() {
		return (operator == TEOperator.add);
	}

	public Object clone() {
        Trigger c = new Trigger(operator, type, (Literal)literal.clone());
        c.piCache = this.piCache;
        return c; 
	}

    
	PredicateIndicator piCache = null;
	
	/** return [+|-][!|?] super.getFucntorArity */
	public PredicateIndicator getPredicateIndicator() {
        if (piCache == null) {
            piCache = new PredicateIndicator(operator.toString() + type + literal.getFunctor(), literal.getTermsSize());
        }
        return piCache;
    }

	public Literal getLiteral() {
		return literal;
	}
	
	public String toString() {
		return operator.toString() + type + literal;
	}
    
    
    /** get as XML */
    public Element getAsDOM(Document document) {
        Element e = (Element) document.createElement("trigger");
        e.setAttribute("add", operator.toString());
        e.setAttribute("type", type.toString());
        e.appendChild(literal.getAsDOM(document));
        return e;
    }

}
