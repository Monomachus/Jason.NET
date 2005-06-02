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
// http://www.csc.liv.ac.uk/~bordini
// http://www.inf.furb.br/~jomi
//----------------------------------------------------------------------------


package jason.asSyntax;

import jason.D;

public class BodyLiteral extends Literal implements Cloneable {
    
    byte formType;

    public BodyLiteral(byte t, Literal l) {
        super(l);
        formType = t;
    }

    public byte getType() {
        return(formType);
    }
    
    public boolean equals(Object o) {
    	try {
    		BodyLiteral b = (BodyLiteral) o;
    		return (formType==b.formType && super.equals(b));
    	} catch (Exception e) {
    		return false;
    	}
    }

    public boolean equalsAsLiteral(Object o) {
    	return super.equals(o);
    }
    
    
    public boolean isAsk() {
    	if (! getFunctorArity().equals(".send/4")) {
    		return false;
    	}
    	if (getTerm(1).toString().startsWith("ask")) {
    		return true;
    	} else {
    		return false;
    	}
    }
    
    public Object clone() {
        return new BodyLiteral(formType, (Literal)super.clone());
    }
    
    public String toString() {
        switch(formType) {
            case D.HAction :
                return super.toString();
            case D.HAchieve :
                return "!" + super.toString();
            case D.HTest :
                return "?" + super.toString();
            case D.HAddBel :
                return "+" + super.toString();
            case D.HDelBel :
                return "-" + super.toString();
        }
        // What to do here???
        return("ERROR in Literal to String");
    }
    
}
