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
//   Revision 1.10  2006/01/04 02:54:41  jomifred
//   using java log API instead of apache log
//
//   Revision 1.9  2005/12/31 16:29:58  jomifred
//   add operator =..
//
//   Revision 1.8  2005/12/30 20:40:16  jomifred
//   new features: unnamed var, var with annots, TE as var
//
//   Revision 1.7  2005/08/12 22:26:08  jomifred
//   add cvs keywords
//
//
//----------------------------------------------------------------------------

package jason.asSyntax;

import jason.asSyntax.parser.as2j;

import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Trigger implements Cloneable {


    public static final byte      TEBel      = 0;
    public static final byte      TEAchvG    = 1;
    public static final byte      TETestG    = 2;
    public static final boolean   TEAdd      = false;
    public static final boolean   TEDel      = true;
	
	static private Logger logger = Logger.getLogger(Trigger.class.getName());
    
    
	boolean trigType = TEAdd;
	byte goal = TEBel;
	Literal literal;
	
	public Trigger(boolean t, byte g, Literal l) {
		literal = (Literal)l.clone();
		trigType = t;
		goal = g;
	}

	public static Trigger parseTrigger(String sTe) {
		as2j parser = new as2j(new StringReader(sTe));
		try {
			return parser.te(); 
		} catch (Exception e) {
			logger.log(Level.SEVERE,"Error parsing trigger" + sTe,e);
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
		return (trigType == t.trigType && goal == t.goal && literal.equals(t.getLiteral()));
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
		return new Trigger(trigType, goal, literal); 
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
		return s + literal.getFunctorArity();
	}

	public Literal getLiteral() {
		return literal;
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
		s += literal.toString();
		return s;
	}

}