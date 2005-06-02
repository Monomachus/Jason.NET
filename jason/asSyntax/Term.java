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

import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Term implements Cloneable, Comparable, Serializable {

	String funcSymb = null;


	/**
	 *  
	 * @uml.property name="terms"
	 * @uml.associationEnd aggregation="aggregate" inverse="term1:jason.asSyntax.Term" multiplicity="(0 -1)" ordering="ordered"
	 */
	List terms;

	public Term() {
	}

	public Term(String fs) {
		setFunctor(fs);
	}

	public Term(Term t) {
		set(t);
	}

	public static Term parse(String sTerm) {
		as2j parser = new as2j(new StringReader(sTerm));
		try {
			return parser.t(); // TODO: parse.t() may returns a Pred!!!!
		} catch (Exception e) {
			System.err.println("Error parsing term " + sTerm);
			e.printStackTrace();
			return null;
		}
	}

	// use Object as parameter to simply the Unifier.apply
	public void set(Object o) {
		try {
			Term t = (Term)o;
			setFunctor(t.funcSymb);
			terms = t.getHardCopyOfTerms();
		} catch (Exception e) {
			System.err.println("Error setting value for term ");
			e.printStackTrace();
		}
	}

	public void setFunctor(String fs) {
		funcSymb = fs;
		functorArityBak = null;
	}

	public String getFunctor() {
		return funcSymb;
	}

	public boolean hasFunctor(String fs) {
		return funcSymb.equals(fs);
	}

	protected String functorArityBak = null; // to not compute it all the time (is is called many many times)
	
	/** return <functor symbol> "/" <arity> */
	public String getFunctorArity() {
		if (functorArityBak == null) {
			if (terms == null) {
				functorArityBak = funcSymb + "/0";
			} else {
				functorArityBak = funcSymb + "/" + terms.size();
			}
		}
		return functorArityBak;
	}

	public int hashCode() {
		return getFunctorArity().hashCode();
	}

	public Term getTerm(int i) {
		if (i == 0)
			return new Term(getFunctor());
		else
			return (Term)terms.get(i - 1);
	}

	public void addTerm(Term t) {
		if (terms == null)
			terms = new ArrayList();
		terms.add(t);
		functorArityBak = null;
	}

	public List getTerms() {
		return terms;
	}

	public boolean isVar() {
		if (funcSymb == null) {
			return false;
		} else {
			return Character.isUpperCase(funcSymb.charAt(0));
		}
	}

	public boolean isList() {
		return funcSymb.equals(D.EmptyList) || funcSymb.equals(D.ListCons);		
	}
	
	public boolean isGround() {
		if (funcSymb == null) // empty predicate
			return true;
		if (isVar()) // variable
			return false;
		if (terms == null) // atom
			return true;
		Iterator i = terms.iterator(); // structure
		while (i.hasNext()) {
			if (!((Term) i.next()).isGround())
				return false;
		}
		return true;
	}

	public boolean hasVar(Term t) {
		if (this.equals(t))
			return true;
		if (terms != null) {
			Iterator i = terms.iterator();
			while (i.hasNext()) {
				if (((Term) i.next()).hasVar(t))
					return true;
			}
		}
		return false;
	}

	public boolean equals(Object t) {
		if (t == null)
			return false;
		try {
			Term tAsTerm = (Term)t;
			if (!funcSymb.equals(tAsTerm.funcSymb))
				return false;
			if (terms == null && tAsTerm.terms == null)
				return true;
			if (terms == null || tAsTerm.terms == null)
				return false;
			if (terms.size() != tAsTerm.terms.size())
				return false;
			for (int i=0; i < terms.size(); i++) {
				if (!( (Term)terms.get(i) ).equals(tAsTerm.terms.get(i))) {
					return false;
				}
			}
			return true;
		} catch (ClassCastException e) {
			return false;
		}
	}

	public int compareTo(Object t) {
		int c;
		if (((Term) t).funcSymb == null)
			return 1;
		if (funcSymb == null)
			return -1;
		c = funcSymb.compareTo(((Term) t).funcSymb);
		if (c != 0)
			return c;
		if (terms == null && ((Term) t).terms == null)
			return 0;
		if (terms == null)
			return -1;
		if (((Term) t).terms == null)
			return 1;
		if (terms.size() < ((Term) t).terms.size())
			return -1;
		else if (terms.size() > ((Term) t).terms.size())
			return 1;
		Iterator i = terms.iterator();
		Iterator j = ((Term) t).terms.iterator();
		while (i.hasNext() && j.hasNext()) {
			c = ((Term) i.next()).compareTo((Term) j.next());
			if (c != 0)
				return c;
		}
		return 0;
	}

	/** make a hard copy of the terms */
	public Object clone() {
		return new Term(this);
	}

	protected List getHardCopyOfTerms() {
		if (terms == null) {
			return null;
		}
		List l = new ArrayList(terms.size());
		Iterator i = terms.iterator();
		while (i.hasNext()) {
			Term ti = (Term)i.next();
			l.add(ti.clone());
		}
		return l;
	}
	
	public String toString() {
		String s;
		if (isList())
			s = "[" + listToString("",this) + "]";
		else {
			s = funcSymb;
			if (terms == null)
				return s;
			s += "(";
			Iterator i = terms.iterator();
			while (i.hasNext()) {
				s += (Term) i.next();
				if (i.hasNext())
					s += ",";
			}
			s += ")";
		}
		return (s);
	}

	public String listToString(String s, Term t) {
		if (t == null) {
			return "";
		}
		if (!t.isList() && !t.isVar()) {
			System.err.println("Error: not a proper list!");
			return "error";
		}
		if (t.funcSymb.equals(D.EmptyList)) {
			return s;
		}
		if (t.isVar()) {
			return s+t;
		}
		s += t.terms.get(0).toString();
		Term u = null;
		if (t.terms.size() > 0) {
			u = (Term)t.terms.get(1);
			if (u.funcSymb.equals(D.EmptyList))
				return s;
			if (!u.isVar())
				s += ",";
			else
				s += "|";
			}
		return listToString(s, u);
	}

}