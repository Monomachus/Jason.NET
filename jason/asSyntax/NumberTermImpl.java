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
// CVS information:
//   $Date$
//   $Revision$
//   $Log$
//   Revision 1.4  2006/01/04 02:54:41  jomifred
//   using java log API instead of apache log
//
//   Revision 1.3  2006/01/02 13:49:00  jomifred
//   add plan unique id, fix some bugs
//
//   Revision 1.2  2005/08/12 22:26:08  jomifred
//   add cvs keywords
//
//
//----------------------------------------------------------------------------

package jason.asSyntax;

import java.util.logging.Level;
import java.util.logging.Logger;

/** implements a term that represents a number */
public class NumberTermImpl extends Term implements NumberTerm {

	static private Logger logger = Logger.getLogger(NumberTermImpl.class.getName());

	private double fValue;
	
	public NumberTermImpl() {
		super();
	}
	
	public NumberTermImpl(String fs) {
		setValue(fs);
	}
	
	public NumberTermImpl(double vl) {
		setValue(vl);
	}
	
	public NumberTermImpl(NumberTermImpl t) {
		setValue(t.solve());
	}

	public void setValue(String s) {
		try {
			setValue(Double.parseDouble(s));
		} catch (Exception e) {
			logger.log(Level.SEVERE,"Error setting number term value from "+s,e);
		}
	}
	public void setValue(double d) {
		fValue = d;
		setFunctor(d+"");
	}
	
	public double solve() {
		return fValue;
	}

	public Object clone() {
		return new NumberTermImpl(solve());
	}
	

	/*
	public static NumberTermImpl parseString(String sTerm) {
		as2j parser = new as2j(new StringReader(sTerm));
		try {
			return (NumberTermImpl)parser.value();
		} catch (Exception e) {
			logger.error("Error parsing number term " + sTerm,e);
			return null;
		}
	}
	*/

	public boolean isNumber() {
		return true;
	}
	
	public boolean equals(Object o) {
		try {
			Term t = (Term)o;
			if (t.isVar()) return false;
			NumberTerm st = (NumberTerm)t;
			return solve() == st.solve();
		} catch (Exception e) {}
		return false;
	}

	public String toString() {
		long r = Math.round(fValue);
		if (fValue == (double)r) {
			return ""+r;
		} else {
			return ""+fValue;
		}
	}
}
