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

public class DefaultLiteral extends Literal implements Cloneable {

	// TODO: not extends Literal (because of the VarTerm), same solution than BodyLiteral

	boolean defType;

    public DefaultLiteral(boolean t, Literal l) {
        super(l);
        defType = t;
    }
    public DefaultLiteral(boolean t, VarTerm v) {
        super(v);
        defType = t;
    }

    public boolean isDefaultNegated() {
        return(defType==D.LDefNeg);
    }

    public boolean equals(Object o) {
        try {
            DefaultLiteral d = (DefaultLiteral) o;
            return (defType==d.defType && super.equals(d));
        }
        catch (Exception e) {
            return super.equals(o);
        }
    }

    public Object clone() {
        return new DefaultLiteral(defType, (Literal)super.clone());
    }
    
    public String toString() {
        if(defType==D.LDefPos)
            return super.toString();
        else
            return "not " + super.toString();
    }

}
