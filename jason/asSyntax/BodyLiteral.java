package jason.asSyntax;

import java.util.Iterator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class BodyLiteral extends Structure implements Iterable<BodyLiteral> {

    public enum BodyType {
        none {            public String toString() { return ""; }},
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

    public static final String BODY_PLAN_FUNCTOR = ";";

    private Term        term     = null; 
    private BodyLiteral next     = null;
    private BodyType    formType = BodyType.none;
    
    /** constructor for empty plan body */
    public BodyLiteral() {
        super(BODY_PLAN_FUNCTOR, 0);
    }
    
    public BodyLiteral(BodyType t, Term b) {
        super(BODY_PLAN_FUNCTOR, 0);
        term     = b;
        formType = t;
        setSrc(b);
    }

    public void setNext(BodyLiteral next) {
        this.next = next;
    }
    public BodyLiteral getNext() {
        return next;
    }

    public boolean isEmpty() {
        return term == null;
    }
    
    public BodyType getType() {
        return formType;
    }
    
    public Term getTerm() {
        return term;
    }
    
    public Literal getLiteralFormula() {
        if (term instanceof Literal)
            return (Literal)term;
        else 
            return null;
    }

    public Iterator<BodyLiteral> iterator() {
        return new Iterator<BodyLiteral>() {
            BodyLiteral current = BodyLiteral.this;
            public boolean hasNext() {
                return current != null && current.term != null && current.next != null; 
            }
            public BodyLiteral next() {
                BodyLiteral r = current;
                if (current != null)
                    current = current.next;
                return r;
            }
            public void remove() { }
        };
    }

    // Override some structure methods to work with unification/equals
    @Override
    public int getArity() {
        if (term == null)
            return 0;
        else if (next == null)
            return 1;
        else
            return 2;
    }

    @Override
    public Term getTerm(int i) {
        if (i == 0) return term;
        if (i == 1) {
            if (next != null && next.term.isVar() && next.next == null) 
                // if next is the last VAR, return that var
                return next.term;
            else
                return next;
        }
        return null;
    }

    @Override
    public void setTerm(int i, Term t) {
        if (i == 0) term = t;
        if (i == 1) System.out.println("Should not set next of body literal!");
    }

    @Override
    public boolean isPlanBody() {
        return true;
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
    public int calcHashCode() {
        return formType.hashCode() + super.calcHashCode();
    }

    public boolean add(BodyLiteral bl) {
        if (term == null)
            swap(bl);
        else if (next == null)
            next = bl;
        else 
            next.add(bl);
        return true;
    }
    
    public boolean add(int index, BodyLiteral bl) {
        if (index == 0) {
            swap(bl);
            this.next = bl;
        } else { 
            next.add(index - 1, bl);
        }
        return true;
    }

    public Term remove(int index) {
        if (index == 0) {
            if (next == null) {
                term = null; // becomes an empty
            } else {
                Term oldvalue = term;
                swap(next); // get values of text
                next = next.next;
                return oldvalue;
            }
            return this;
        } else { 
            return next.remove(index - 1);
        }
    }

    public int size() {
        if (term == null) 
            return 0;
        else if (next == null)
            return 1;
        else
            return next.size() + 1;
    }

    private void swap(BodyLiteral bl) {
        BodyType bt = this.formType;
        this.formType = bl.formType;
        bl.formType   = bt;

        Term l = this.term;
        this.term = bl.term;
        bl.term   = l;
    }

    public Object clone() {
        if (term == null) // empty
            return new BodyLiteral();

        BodyLiteral c = new BodyLiteral(formType, (Term)term.clone());
        if (next != null)
            c.setNext((BodyLiteral)getNext().clone());
        return c;
    }
    
    public String toString() {
        if (term == null)
            return "";
        else if (next == null)
            return formType.toString() + term;
        else
            return formType.toString() + term + "; " + next;
    }

    /** get as XML */
    public Element getAsDOM(Document document) {
        Element u = (Element) document.createElement("body-literal");
        if (formType.toString().length() > 0) {
            u.setAttribute("type", formType.toString());
        }
        u.appendChild( ((Structure)term).getAsDOM(document));
        return u;
    }
}