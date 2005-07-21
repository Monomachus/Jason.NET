package jason.asSyntax;

import org.apache.log4j.Logger;

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
		setValue(t.getValue());
	}

	public void setValue(String s) {
		try {
			setValue(Double.parseDouble(s));
		} catch (Exception e) {
			logger.error("Error setting number term value from "+s,e);
		}
	}
	public void setValue(double d) {
		fValue = d;
		setFunctor(d+"");
	}
	
	public double getValue() {
		return fValue;
	}
	
	public double solve() {
		return fValue;
	}
	
	public Object clone() {
		return new NumberTermImpl(getValue());
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
	
	public boolean equals(Object t) {
		try {
			NumberTermImpl st = (NumberTermImpl)t;
			return this.getValue() == st.getValue();
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
