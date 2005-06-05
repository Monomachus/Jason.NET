package jason.asSyntax;


/**
 * Represents a variable Term: like X (starts with upper case). 
 * It may have a value, afert Unifier.apply.
 * 
 * @author jomi
 */
public class VarTerm extends Term {
	
	private Term value = null;
	
	public VarTerm() {
		super();
	}

	public VarTerm(String s) {
		if (s != null && Character.isLowerCase(s.charAt(0))) {
			System.err.println("Warning: are you sure to create a VarTerm that begins with lower case ("+s+")? Should it be a Term?");			
		}
		setFunctor(s);
	}
	
	public Object clone() {
		// do not call constructor with term parameter!
		VarTerm t = new VarTerm();
		t.funcSymb = funcSymb;
		if (value != null) {
			t.value = (Term)value.clone();
		}
		return t;
	}
	
	public boolean isVar() {
		return !isGround();
	}

	public boolean isGround() {
		return getValue() != null;
	}
	
	public boolean setValue(Term vl) {
		try {
			// If vl is a Var, find out a possible loop
			VarTerm vlvl = (VarTerm)((VarTerm)vl).value; // not getValue! (use the "real" value)
			while (vlvl != null) {
				if (vlvl == this) {
					System.err.println("Error: trying to make a loop in VarTerm values of "+this.funcSymb);
					return false;
				}
				vlvl = (VarTerm)vlvl.value;
			}
		} catch (Exception e) {}
		value = vl;
		return true;
	}
	
	/** returns true if this var has a value */
	public boolean hasValue() {
		return value != null;
	}
	
	/** 
	 * When a var has a var as value, returns the last var in the chain.
	 * Otherwise, return this. 
	 * The returned VarTerm is normally used to set/get a value for the var. 
	 */
	public VarTerm getLastVarChain() {
		try {
			// if value is a var, return it
			return ((VarTerm)value).getLastVarChain();
		} catch (Exception e) {}
		return this;
	}
	
	/** 
	 * returns the value of this var. 
	 * if value is a var, returns the value of these var
	 * 
	 * @return
	 */
	public Term getValue() {
		/*
		try {
			// if value is a var, return its value
			return ((VarTerm)value).getValue();
		} catch (StackOverflowError e) {
			System.err.println("Stack overflow in VarTerm.getValue!\n\t"+this);
			return null;
		} catch (Exception e) {}
	
		return value;
		*/
		return getLastVarChain().value;
	}

	public boolean equals(Object t) {
		if (t == null)
			return false;
		try {
			Term tAsTerm = (Term)t;
			Term vl = getValue();
			//System.out.println("cheking equals form "+tAsTerm.funcSymb+"="+this.funcSymb+" my value "+vl);
			if (vl == null) {
				// is other also a var? (its value must also be null)
				try {
					VarTerm tAsVT = (VarTerm)t;
					if (tAsVT.getValue() != null) {
						//System.out.println("returning false*");
						return false;
					}

					// no value, the var names must be equal
					//System.out.println("will return "+funcSymb.equals(tAsTerm.funcSymb));
					return funcSymb.equals(tAsTerm.funcSymb);
				} catch (Exception e) {
					return false;
				}
				
			} else {
				// campare the values
				return vl.equals(t);
			}
		} catch (ClassCastException e) {
		}
		return false;
	}
	
	public String toString() {
		Term vl = getValue();
		if (vl == null) {
			// no value, the var name must be equal
			return funcSymb;
		} else {
			// campare the values
			return vl.toString();
		}
	}
}
