//----------------------------------------------------------------------------
// Copyright (C) 2003  Rafael H. Bordini, Jomi F. Hubner, et al.
// 
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
// 
// To contact the authors:
// http://www.dur.ac.uk/r.bordini
// http://www.inf.furb.br/~jomi
//
//----------------------------------------------------------------------------

package jason.asSyntax;

import jason.asSemantics.Agent;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class BodyLiteral implements Cloneable {

    public enum BodyType {
        action {
            public String toString() {
                return "";
            }
        },
        internalAction {
            public String toString() {
                return "";
            }
        },
        achieve {
            public String toString() {
                return "!";
            }
        },
        test {
            public String toString() {
                return "?";
            }
        },
        addBel {
            public String toString() {
                return "+";
            }
        },
        delBel {
            public String toString() {
                return "-";
            }
        },
        delAddBel {
            public String toString() {
                return "-+";
            }
        },
        achieveNF {
            public String toString() {
                return "!!";
            }
        },
        constraint {
            public String toString() {
                return "";
            }
        }
    }

    LogicalFormula  formula;
    BodyType        formType;
    int             srcLine = -1;
    
    public BodyLiteral(BodyType t, Literal l) {
        formula = (Literal) l.clone();
        formType = t;
        srcLine = l.getSrcLine();
        if (l.isInternalAction()) {
            formType = BodyType.internalAction;
        }
    }

    public BodyLiteral(RelExpr re) {
        formula = (LogicalFormula) re.clone();
        srcLine = re.getSrcLine();
        formType = BodyType.constraint;
    }

    public BodyLiteral(LogExpr le) {
        formula = (LogicalFormula) le.clone();
        srcLine = le.getSrcLine();
        formType = BodyType.test;
    }

    public BodyType getType() {
        return formType;
    }

    public Literal getLiteralFormula() {
        if (formula instanceof Literal)
            return (Literal)formula;
        else 
            return null;
    }
    
    public LogicalFormula getLogicalFormula() {
        return formula;
    }

    // used with arithmetic expressions
    public void addTerm(Term t) {
        ((Literal)formula).addTerm(t);
    }

    public String getSrcInfo(Agent ag) {
    	String line = "";
    	if (srcLine >= 0) {
    		line = ":"+srcLine;
    	}
    	return " ("+ag.getASLSource()+line+")";
    }
    
    @Override
    public boolean equals(Object o) {
        if (o != null && o instanceof BodyLiteral) {
            BodyLiteral b = (BodyLiteral) o;
            return formType == b.formType && formula.equals(b.formula);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return formType.hashCode() + formula.hashCode();
    }

    public Object clone() {
        if (formType == BodyType.constraint) {
            return new BodyLiteral((RelExpr)formula);
        } else if (formula instanceof LogExpr) {
            return new BodyLiteral((LogExpr)formula);
        } else {
            return new BodyLiteral(formType, (Literal)formula);
        }
    }

    public String toString() {
        return formType + formula.toString();
    }

    /** get as XML */
    public Element getAsDOM(Document document) {
        Element u = (Element) document.createElement("body-literal");
        if (formType.toString().length() > 0) {
            u.setAttribute("type", formType.toString());
        }
        u.appendChild(formula.getAsDOM(document));
        return u;
    }
}
