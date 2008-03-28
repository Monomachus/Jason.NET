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

/** 
 * Represents a plan body item (achieve, test, action, ...) and its successors, 
 * it is thus like a list term.
 *  
 * @author Jomi  
 */
public class BodyLiteral extends ListTermImpl implements Cloneable {

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

    private BodyType        formType;
    
    public BodyLiteral() {
    }
    
    public BodyLiteral(BodyType t, Term b) {
        setTerm(b);
        setSrc(b);
        formType = t;
    }

    public BodyType getType() {
        return formType;
    }

    public Literal getLiteralFormula() {
        Term t = getTerm();
        if (t instanceof Literal)
            return (Literal)t;
        else 
            return null;
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        if (o instanceof BodyLiteral) {
            BodyLiteral b = (BodyLiteral)o;
            return formType == b.formType && super.equals(o);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return formType.hashCode() + super.hashCode();
    }

    public Object clone() {
        BodyLiteral c;
        Term t = getTerm();
        if (t == null) { // empty body
            c = new BodyLiteral();
        } else {
            c = new BodyLiteral(formType, (Term)t.clone());
            c.setNext((Term)getNext().clone());
        }
        return c;
    }

    @Override
    protected void setValuesFrom(ListTerm lt) {
        super.setValuesFrom(lt);
        formType = ((BodyLiteral)lt).formType;
    }
    
    
    public String toString() {
        if (isEmpty()) 
            return "";
        else if (getNext().isEmpty())
            return formType.toString() + getTerm() + ".";
        else
            return formType.toString() + getTerm() + "; " + getNext();
    }

    /** get as XML */
    public Element getAsDOM(Document document) {
        Element u = (Element) document.createElement("body-literal");
        if (formType.toString().length() > 0) {
            u.setAttribute("type", formType.toString());
        }
        u.appendChild( ((Structure)getTerm()).getAsDOM(document));
        return u;
    }
}
