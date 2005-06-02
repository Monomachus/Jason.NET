package jason.asSyntax;

import jason.asSyntax.parser.as2j;

import java.io.StringReader;

public class StringTerm extends Term {

	public StringTerm() {
		super();
	}
	public StringTerm(String fs) {
		super(fs);
	}
	public StringTerm(StringTerm t) {
		super(t);
	}
	
	public void setFunctor(String fs) {
		if (fs.startsWith("\"")) {
			fs = fs.substring(1,fs.length()-1);
		}
		super.setFunctor(fs);
	}

	public String  getString() {
		return funcSymb;
	}
	
	public Object clone() {
		return new StringTerm(funcSymb);
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
		return funcSymb.length();
	}
	
	public String toString() {
		return "\""+funcSymb+"\"";
	}
}
