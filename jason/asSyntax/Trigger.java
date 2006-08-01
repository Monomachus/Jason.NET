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


    public static final byte      TEBel      = 0;
    public static final byte      TEAchvG    = 1;
    public static final byte      TETestG    = 2;
    public static final boolean   TEAdd      = false;
    public static final boolean   TEDel      = true;
	
	static private Logger logger = Logger.getLogger(Trigger.class.getName());
    
  
	boolean trigType = TEAdd;
	byte goal = TEBel;
	Literal literal;
	
	public Trigger(boolean t, byte g, Literal l) {
		literal = l;
		setTrigType(t);
		goal = g;
	}

	public static Trigger parseTrigger(String sTe) {
		as2j parser = new as2j(new StringReader(sTe));
		try {
			return parser.te(); 
		} catch (Exception e) {
			logger.log(Level.SEVERE,"Error parsing trigger" + sTe,e);
			return null;
		}
	}

	public void setTrigType(boolean t) {
		trigType = t;
                piCache = null;
	}

	public boolean sameType(Trigger e) {
		return (trigType == e.trigType && goal == e.goal);
	}

    @Override
	public boolean equals(Object o) {
        if (o != null && o instanceof Trigger) {
            Trigger t = (Trigger) o;
            return (trigType == t.trigType && goal == t.goal && literal.equals(t.getLiteral()));
        }
        return false;
	}

    @Override
    public int hashCode() {
        return getPredicateIndicator().hashCode();
    }


	public boolean isAchvGoal() {
		return (goal == TEAchvG);
	}

	public boolean isGoal() {
		return (goal == TEAchvG || goal == TETestG);
	}

	public byte getGoal() {
		return goal;
	}

	public boolean isAddition() {
		return (trigType == TEAdd);
	}

	public Object clone() {
        Trigger c = new Trigger(trigType, goal, (Literal)literal.clone());
        c.piCache = this.piCache;
        return c; 
	}

    
	PredicateIndicator piCache = null;
	
	/** return [+|-][!|?] super.getFucntorArity */
	public PredicateIndicator getPredicateIndicator() {
        if (piCache == null) {
            String s;
            if (trigType == TEAdd)
                s = "+";
            else
                s = "-";
            if (goal == TEAchvG)
                s += "!";
            else if (goal == TETestG)
                s += "?";
            piCache = new PredicateIndicator(s + literal.getFunctor(), literal.getTermsSize());
        }
        return piCache;
    }

	public Literal getLiteral() {
		return literal;
	}
	
	public String toString() {
		String s;
		if (trigType == TEAdd)
			s = "+";
		else
			s = "-";
		if (goal == TEAchvG)
			s += "!";
		else if (goal == TETestG)
			s += "?";
		s += literal.toString();
		return s;
	}
    
    
    /** get as XML */
    public Element getAsDOM(Document document) {
        Element e = (Element) document.createElement("trigger");
        String s;
        if (trigType == TEAdd)
            s = "+";
        else
            s = "-";
        e.setAttribute("add", s);
        
        s = null;
        if (goal == TEAchvG)
            s = "!";
        else if (goal == TETestG)
            s = "?";
        if (s != null) {
            e.setAttribute("type", s);
        }
        
        e.appendChild(literal.getAsDOM(document));
        return e;
    }

}