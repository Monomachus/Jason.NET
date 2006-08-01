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
//----------------------------------------------------------------------------

package jason.asSyntax;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/** implements a term that represents a number */
public class NumberTermImpl extends TermImpl implements NumberTerm {

	private static final long serialVersionUID = 1L;

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
		setFunctor(String.valueOf(d));
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

	@Override
	public boolean isNumeric() {
		return true;
	}
	
    @Override
	public boolean equals(Object o) {
        if (o != null && o instanceof NumberTerm) {
            NumberTerm st = (NumberTerm)o;
			if (st.isVar() || st.isArithExpr()) 
                return false;
            else
                //return Double.doubleToLongBits(solve()) == Double.doubleToLongBits(st.solve());
                return solve() == st.solve();
		} 
		return false;
	}

    @Override
    public int hashCode() {
        return (int)solve();
    }
    
    @Override
    public int compareTo(Term o) {
        try {
            NumberTerm st = (NumberTerm)o;
            if (solve() > st.solve()) return 1;
            if (solve() < st.solve()) return -1;
        } catch (Exception e) {
        }
        return 0;    
    }

	public String toString() {
		long r = Math.round(fValue);
		if (fValue == (double)r) {
			return String.valueOf(r);
		} else {
			return String.valueOf(fValue);
		}
	}
    
    
    /** get as XML */
    @Override
    public Element getAsDOM(Document document) {
        Element u = (Element) document.createElement("number-term");
        u.appendChild(document.createTextNode(toString()));
        return u;
    }    
}
