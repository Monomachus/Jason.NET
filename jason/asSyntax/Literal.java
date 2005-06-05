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
// http://www.csc.liv.ac.uk/~bordini
// http://www.inf.furb.br/~jomi
//----------------------------------------------------------------------------

package jason.asSyntax;

import jason.D;
import jason.asSyntax.parser.as2j;

import java.io.StringReader;

public class Literal extends Pred implements Cloneable {

	boolean type = D.LPos;

	/** if pos == true, the literal is positive, else it is negative */
	public Literal(boolean pos, Pred p) {
		super(p);
		type = pos;
	}

	public Literal(Literal l) {
		super((Pred) l);
		type = l.type;
	}
	
	public Literal(Term t) {
		super(t);
		type = D.LPos;
	}

	public static Literal parseLiteral(String sLiteral) {
		as2j parser = new as2j(new StringReader(sLiteral));
		try {
			return parser.l();
		} catch (Exception e) {
			System.err.println("Error parsing literal " + sLiteral);
			e.printStackTrace();
			return null;
		}
	}

	public void set(Object o) {
		super.set(o);
		try {
			Literal l = (Literal)o;
			type = l.type;
		} catch (Exception e) {
			// no problem, o is not a literal, may be it is a pred and the
			// super.set handle it
		}
	}
	
	
	public boolean isInternalAction() {
		return (getFunctor().indexOf('.') >= 0);
	}

	public boolean negated() {
		return (type == D.LNeg);
	}

	public boolean equals(Object o) {
		try {
			Literal l = (Literal) o;
			return (type == l.type && super.equals(l));
		} catch (Exception e) {
			return super.equals(o);
		}
	}

	public Object clone() {
		return new Literal(type, (Pred) super.clone());
	}

	
	/** return [~] super.getFucntorArity */
	public String getFunctorArity() {
		if (functorArityBak == null) {
			functorArityBak = (type == D.LPos) ? super.getFunctorArity() : "~" + super.getFunctorArity(); 
		}
		return functorArityBak;
	}
	
	public int hashCode() {
		return getFunctorArity().hashCode();
	}

	public String toString() {
		if (type == D.LPos)
			return super.toString();
		else
			return "~" + super.toString();
	}

}