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
//   Revision 1.1  2005/12/23 00:49:57  jomifred
//   StringTerm is now an interface implemented by StringTermImpl
//
//   Revision 1.6  2005/08/12 22:26:08  jomifred
//   add cvs keywords
//
//
//----------------------------------------------------------------------------

package jason.asSyntax;

import jason.asSyntax.parser.as2j;

import java.io.StringReader;

import org.apache.log4j.Logger;

public class StringTermImpl extends Term implements StringTerm {

	static private Logger logger = Logger.getLogger(StringTermImpl.class.getName());

	public StringTermImpl() {
		super();
	}
	
	public StringTermImpl(String fs) {
		setString(fs);
	}
	
	public StringTermImpl(StringTermImpl t) {
		setString(t.getString());
	}

	/*
	public void setFunctor(String fs) {
		if (fs.startsWith("\"")) {
			fs = fs.substring(1,fs.length()-1);
		}
		super.setFunctor(fs);
	}
	*/

	public void setString(String s) {
		if (s.startsWith("\"")) {
			s = s.substring(1,s.length()-1);
		}
		setFunctor(s);
	}
	
	public String getString() {
		return getFunctor();
	}
	
	public Object clone() {
		return new StringTermImpl(getString());
	}
	
	
	public static StringTerm parseString(String sTerm) {
		as2j parser = new as2j(new StringReader(sTerm));
		try {
			return (StringTerm)parser.t();
		} catch (Exception e) {
			logger.error("Error parsing string term " + sTerm,e);
			return null;
		}
	}

	public boolean isString() {
		return true;
	}
	
	public int length() {
		return getString().length();
	}

	public boolean equals(Object t) {
		try {
			StringTerm st = (StringTerm)t;
			return this.getString().equals(st.getString());
		} catch (Exception e) {}
		return false;
	}
	
	public String toString() {
		return "\""+getString()+"\"";
	}
}
