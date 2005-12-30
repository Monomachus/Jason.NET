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
//   Revision 1.11  2005/12/30 20:40:16  jomifred
//   new features: unnamed var, var with annots, TE as var
//
//   Revision 1.10  2005/08/12 22:26:08  jomifred
//   add cvs keywords
//
//
//----------------------------------------------------------------------------


package jason.asSyntax;


public class DefaultLiteral implements Cloneable {

	public static final boolean   LDefPos    = true;
    public static final boolean   LDefNeg    = false;
	
	Literal literal;
	boolean defType;

    public DefaultLiteral(boolean t, Literal l) {
        literal = (Literal)l.clone();
        defType = t;
    }

    public boolean isDefaultNegated() {
        return defType==LDefNeg;
    }
	
	public Term getLiteral() {
		return literal;
	}

	public void addTerm(Term t) {
		literal.addTerm(t);
	}
	
    public boolean equals(Object o) {
        try {
            DefaultLiteral d = (DefaultLiteral) o;
            return (defType==d.defType && literal.equals(d.literal));
        }
        catch (Exception e) {
            return super.equals(o);
        }
    }

    public Object clone() {
		return new DefaultLiteral(defType, literal);
    }
	    
    public String toString() {
        if (defType == LDefPos)
            return literal.toString();
        else
            return "not " + literal.toString();
    }
}
