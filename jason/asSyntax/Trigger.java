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

import jason.asSyntax.parser.as2j;

import java.io.StringReader;

import org.apache.log4j.Logger;

public class Trigger extends Literal implements Cloneable {


    public static final byte      TEBel      = 0;
    public static final byte      TEAchvG    = 1;
    public static final byte      TETestG    = 2;
    public static final boolean   TEAdd      = false;
    public static final boolean   TEDel      = true;
	
	static private Logger logger = Logger.getLogger(Trigger.class.getName());
    
    
	boolean trigType = TEAdd;

	byte goal = TEBel;

	public Trigger(boolean t, byte g, Literal l) {
		super(l);
		trigType = t;
		goal = g;
	}

	public static Trigger parseTrigger(String sTe) {
		as2j parser = new as2j(new StringReader(sTe));
		try {
			return parser.te(); 
		} catch (Exception e) {
			logger.error("Error parsing trigger " + sTe,e);
			return null;
		}
	}

	public void setTrigType(boolean t) {
		trigType = t;
	}

	public boolean sameType(Trigger e) {
		return (trigType == e.trigType && goal == e.goal);
	}

	public boolean equals(Object obj) {
		Trigger t = (Trigger) obj;
		return (trigType == t.trigType && goal == t.goal && super.equals(t));
	}

	public boolean isAchvGoal() {
		return (goal == TEAchvG);
	}

	public boolean isGoal() {
		return (goal == TEAchvG || goal == TETestG);
	}

	public byte getGoal() {
		return goal;
	}

	public boolean isAddition() {
		return (trigType == TEAdd);
	}

	public Object clone() {
		return new Trigger(trigType, goal, (Literal) super.clone());
	}

	/** return [+|-][!|?] super.getFucntorArity */
	public String getFunctorArity() {
		String s;
		if (trigType == TEAdd)
			s = "+";
		else
			s = "-";
		if (goal == TEAchvG)
			s += "!";
		else if (goal == TETestG)
			s += "?";
		return s + super.getFunctorArity();
	}

	public int hashCode() {
		return getFunctorArity().hashCode();
	}

	public String toString() {
		String s;
		if (trigType == TEAdd)
			s = "+";
		else
			s = "-";
		if (goal == TEAchvG)
			s += "!";
		else if (goal == TETestG)
			s += "?";
		s += super.toString();
		return s;
	}

}