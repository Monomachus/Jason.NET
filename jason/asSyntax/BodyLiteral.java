//----------------------------------------------------------------------------
// Copyright (C) 2003  Rafael H. Bordini, Jomi F. Hubner, et al.
// 
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
// 
// To contact the authors:
// http://www.dur.ac.uk/r.bordini
// http://www.inf.furb.br/~jomi
//
// CVS information:
//   $Date$
//   $Revision$
//   $Log$
//   Revision 1.10  2005/12/30 20:40:16  jomifred
//   new features: unnamed var, var with annots, TE as var
//
//   Revision 1.9  2005/08/12 22:26:08  jomifred
//   add cvs keywords
//
//
//----------------------------------------------------------------------------


package jason.asSyntax;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class BodyLiteral implements Cloneable {
    
    public static final byte      HAction    = 0;
    public static final byte      HAchieve   = 1;
    public static final byte      HTest      = 2;
    public static final byte      HAddBel    = 3;
    public static final byte      HDelBel    = 4;	
    public static final byte      HAchieveNF = 5;
	
	Literal literal;
    byte    formType;

    public BodyLiteral(byte t, Literal l) {
        literal = (Literal)l.clone();
        formType = t;
    }

    public byte getType() {
        return formType;
    }
	
	public Term getLiteral() {
		return literal;
	}
    
	// used with arithmetic expressions
	public void addTerm(Term t) {
		literal.addTerm(t);
	}
	
    public boolean equals(Object o) {
    	try {
    		BodyLiteral b = (BodyLiteral) o;
    		return formType==b.formType && literal.equals(b.literal);
    	} catch (Exception e) {
    		return false;
    	}
    }

    
    public boolean isAsk() {
    	if (! literal.getFunctorArity().equals(".send/4")) {
    		return false;
    	}
    	if (literal.getTerm(1).toString().startsWith("ask")) {
    		return true;
    	} else {
    		return false;
    	}
    }
    
    public Object clone() {
		return new BodyLiteral(formType, literal);
    }

    public String getTypeStr() {
        switch(formType) {
            case HAction :
                return "";
            case HAchieve :
                return "!";
            case HAchieveNF :
                return "!!";
            case HTest :
                return "?";
            case HAddBel :
                return "+";
            case HDelBel :
                return "-";
        }
        // What to do here???
        return("ERROR in Literal to String");
    }

    public String toString() {
        return getTypeStr() + literal.toString();
    }

    /** get as XML */
    public Element getAsDOM(Document document) {
        Element u = (Element) document.createElement("body-literal");
        if (getTypeStr().length() > 0) {
            u.setAttribute("type", getTypeStr());
        }
        u.appendChild(literal.getAsDOM(document));
        return u;
    }    
}
