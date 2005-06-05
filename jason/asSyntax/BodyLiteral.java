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

public class BodyLiteral implements Cloneable {
    
	Term body;
    byte formType;

    public BodyLiteral(byte t, Literal l) {
        body = (Term)l.clone();
        formType = t;
    }
    public BodyLiteral(byte t, VarTerm v) {
        //super((Term)v);
		body = (Term)v.clone();
		formType = t;
    }

    public byte getType() {
        return formType;
    }
	
	public Term getBody() {
		return body;
	}
    
    public boolean equals(Object o) {
    	try {
    		BodyLiteral b = (BodyLiteral) o;
    		return formType==b.formType && body.equals(b.body);
    	} catch (Exception e) {
    		return false;
    	}
    }

    public boolean equalsAsLiteral(Object o) {
    	return body.equals(o);
    }
    
    
    public boolean isAsk() {
    	if (! body.getFunctorArity().equals(".send/4")) {
    		return false;
    	}
    	if (body.getTerm(1).toString().startsWith("ask")) {
    		return true;
    	} else {
    		return false;
    	}
    }
    
    public Object clone() {
		if (body.isVar()) {
			return new BodyLiteral(formType, (VarTerm)body);			
		} else {
			return new BodyLiteral(formType, (Literal)body);
		}
    }
    
    public String toString() {
        switch(formType) {
            case D.HAction :
                return body.toString();
            case D.HAchieve :
                return "!" + body.toString();
            case D.HTest :
                return "?" + body.toString();
            case D.HAddBel :
                return "+" + body.toString();
            case D.HDelBel :
                return "-" + body.toString();
        }
        // What to do here???
        return("ERROR in Literal to String");
    }
    
}
