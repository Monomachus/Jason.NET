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
//----------------------------------------------------------------------------


package jason.asSyntax;


public class BodyLiteral implements Cloneable {
    
    public static final byte      HAction    = 0;
    public static final byte      HAchieve   = 1;
    public static final byte      HTest      = 2;
    public static final byte      HAddBel    = 3;
    public static final byte      HDelBel    = 4;	
	
	Term literal;
    byte formType;

    public BodyLiteral(byte t, Literal l) {
        literal = (Term)l.clone();
        formType = t;
    }
    public BodyLiteral(byte t, VarTerm v) {
        //super((Term)v);
		literal = (Term)v.clone();
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
		if (literal.isVar()) {
			return new BodyLiteral(formType, (VarTerm)literal);			
		} else {
			return new BodyLiteral(formType, (Literal)literal);
		}
    }
    
    public String toString() {
        switch(formType) {
            case HAction :
                return literal.toString();
            case HAchieve :
                return "!" + literal.toString();
            case HTest :
                return "?" + literal.toString();
            case HAddBel :
                return "+" + literal.toString();
            case HDelBel :
                return "-" + literal.toString();
        }
        // What to do here???
        return("ERROR in Literal to String");
    }
    
}
