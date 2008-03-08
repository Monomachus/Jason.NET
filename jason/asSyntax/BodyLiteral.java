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

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/** Represents an item of a plan body (achieve, test, action, ...) */
public class BodyLiteral extends SourceInfo implements Cloneable {

    public enum BodyType {
        action {          public String toString() { return ""; }},
        internalAction {  public String toString() { return ""; }},
        achieve {         public String toString() { return "!"; }},
        test {            public String toString() { return "?"; }},
        addBel {          public String toString() { return "+"; }},
        delBel {          public String toString() { return "-"; }},
        delAddBel {       public String toString() { return "-+"; }},
        achieveNF {       public String toString() { return "!!"; }},
        constraint {      public String toString() { return ""; }}
    }

    private LogicalFormula  formula;
    private BodyType        formType;
    
    /** used of actions, internal actions, test goals, achieve goals, adds, removes */
    public BodyLiteral(BodyType t, Literal l) {
        formula = (Literal) l.clone();
        formType = t;
        if (l.isInternalAction())
            formType = BodyType.internalAction;
        setSrc(l);
    }

    /** used for test goals and constraints (the argument is a logical formula) */
    public BodyLiteral(BodyType t, LogicalFormula lf) {
        formula = (LogicalFormula) lf.clone();
        formType = t;
        if (lf instanceof SourceInfo)
            setSrc((SourceInfo)lf);
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
        if (formType == BodyType.test || formType == BodyType.constraint) {
            return new BodyLiteral(formType, formula);
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
