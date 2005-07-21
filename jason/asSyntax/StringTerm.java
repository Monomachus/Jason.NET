package jason.asSyntax;

import jason.asSyntax.parser.as2j;

import java.io.StringReader;

import org.apache.log4j.Logger;

public class StringTerm extends Term {

	static private Logger logger = Logger.getLogger(StringTerm.class.getName());

	// TODO: do not use functor to represent the string
	//private String fValue;
	
	public StringTerm() {
		super();
	}
	
	public StringTerm(String fs) {
		setValue(fs);
	}
	
	public StringTerm(StringTerm t) {
		setValue(t.getValue());
	}

	/*
	public void setFunctor(String fs) {
		if (fs.startsWith("\"")) {
			fs = fs.substring(1,fs.length()-1);
		}
		super.setFunctor(fs);
	}
	*/

	public void setValue(String s) {
		if (s.startsWith("\"")) {
			s = s.substring(1,s.length()-1);
		}
		setFunctor(s);
	}
	
	public String getValue() {
		return getFunctor();
	}
	
	public Object clone() {
		return new StringTerm(getValue());
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
		return getValue().length();
	}

	public boolean equals(Object t) {
		try {
			StringTerm st = (StringTerm)t;
			return this.getValue().equals(st.getValue());
		} catch (Exception e) {}
		return false;
	}
	
	
	public String toString() {
		return "\""+getValue()+"\"";
	}
}
