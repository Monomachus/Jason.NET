package jason.asSyntax;

import jason.asSyntax.parser.as2j;

import java.io.StringReader;

public class StringTerm extends Term {

	// TODO: do not use functor to represent the string
	//private String fValue;
	
	public StringTerm() {
		super();
	}
	
	public StringTerm(String fs) {
		setString(fs);
	}
	
	public StringTerm(StringTerm t) {
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
		return new StringTerm(getString());
	}
	
	
	public static StringTerm parseString(String sTerm) {
		as2j parser = new as2j(new StringReader(sTerm));
		try {
			return (StringTerm)parser.t();
		} catch (Exception e) {
			System.err.println("Error parsing string term " + sTerm);
			e.printStackTrace();
			return null;
		}
	}

	public boolean isString() {
		return true;
	}
	
	public int length() {
		return getString().length();
	}
	
	public String toString() {
		return "\""+getString()+"\"";
	}
}
