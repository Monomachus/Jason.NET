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

import jason.asSyntax.parser.as2j;

import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/** 
 * Immutable class fot string terms.
 * 
 * @author jomi
 */
public final class StringTermImpl extends DefaultTerm implements StringTerm {

	private static final long serialVersionUID = 1L;
    
    private final String fValue;

	static private Logger logger = Logger.getLogger(StringTermImpl.class.getName());

	public StringTermImpl() {
		super();
		fValue = null;
	}
	
	public StringTermImpl(String fs) {
		fValue = fs;
	}
	
	public StringTermImpl(StringTermImpl t) {
		fValue = t.getString();
	}

	public String getString() {
		return fValue;
	}
	
	public Object clone() {
		//return new StringTermImpl(getString());
		return this;
	}
	
	public static StringTerm parseString(String sTerm) {
		as2j parser = new as2j(new StringReader(sTerm));
		try {
			return (StringTerm)parser.term();
		} catch (Exception e) {
			logger.log(Level.SEVERE,"Error parsing string term " + sTerm,e);
			return null;
		}
	}

    @Override
	public boolean isString() {
		return true;
	}

 	public int length() {
		return fValue.length();
	}

    @Override
    public boolean equals(Object t) {
        if (t == this) return true;

        if (t != null && t instanceof StringTerm) {
            StringTerm st = (StringTerm)t;
            return fValue.equals(st.getString());
        }
        return false;
    }

    @Override
    protected int calcHashCode() {
        return fValue.hashCode();
    }
	
	public String toString() {
		return "\""+fValue+"\"";
	}

    /** get as XML */
    public Element getAsDOM(Document document) {
        Element u = (Element) document.createElement("string-term");
        u.appendChild(document.createTextNode(toString()));
        return u;
    }    
}
