package jason.asSyntax;

import jason.asSemantics.Unifier;

import java.util.Iterator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/** 
 *  Represents a plan body item (achieve, test, action, ...) and its successors.
 * 
 *  A plan body like <code>a1; ?t; !g</code> is represented by the following structure
 *  <code>(a1, (?t, (!g)))</code>.
 *  
 * @author Jomi  
 */
public class BodyLiteralImpl extends Structure implements BodyLiteral, Iterable<BodyLiteral> {

    public static final String BODY_PLAN_FUNCTOR = ";";

    private Term        term     = null; 
    private BodyLiteral next     = null;
    private BodyType    formType = BodyType.none;
    
    /** constructor for empty plan body */
    public BodyLiteralImpl() {
        super(BODY_PLAN_FUNCTOR, 0);
    }
    
    public BodyLiteralImpl(BodyType t, Term b) {
        super(BODY_PLAN_FUNCTOR, 0);
        term     = b;
        formType = t;
        setSrc(b);
    }

    public void setBodyNext(BodyLiteral next) {
        this.next = next;
    }
    public BodyLiteral getBodyNext() {
        return next;
    }

    public boolean isEmptyBody() {
        return term == null;
    }
    
    public BodyType getBodyType() {
        return formType;
    }
    public void setBodyType(BodyType bt) {
        formType = bt;
    }
    
    public Term getBodyTerm() {
        return term;
    }
    public void setBodyTerm(Term t) {
        term = t;
    }
    
    @Override
    public boolean isPlanBody() {
        return true;
    }
    
    public Iterator<BodyLiteral> iterator() {
        return new Iterator<BodyLiteral>() {
            BodyLiteral current = BodyLiteralImpl.this;
            public boolean hasNext() {
                return current != null && current.getBodyTerm() != null; 
            }
            public BodyLiteral next() {
                BodyLiteral r = current;
                if (current != null)
                    current = current.getBodyNext();
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
        if (i == 0) 
            return term;
        if (i == 1) {
            if (next != null && next.getBodyTerm().isVar() && next.getBodyNext() == null) 
                // if next is the last VAR, return that var
                return next.getBodyTerm();
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
    public boolean apply(Unifier u) {
        // do not apply in next!
        resetHashCodeCache();
        if (term != null && term.apply(u)) {
            if (term.isPlanBody()) { // we can not have "inner" body literals
                formType = ((BodyLiteral)term).getBodyType();
                term     = ((BodyLiteral)term).getBodyTerm();
            }
            return true;
        }
        return false;
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;

        if (o instanceof BodyLiteral) {
            BodyLiteral b = (BodyLiteral)o;
            return formType == b.getBodyType() && super.equals(o);
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

    public Term removeBody(int index) {
        if (index == 0) {
            if (next == null) {
                term = null; // becomes an empty
            } else {
                Term oldvalue = term;
                swap(next); // get values of text
                next = next.getBodyNext();
                return oldvalue;
            }
            return this;
        } else { 
            return next.removeBody(index - 1);
        }
    }

    public int getPlanSize() {
        if (term == null) 
            return 0;
        else if (next == null)
            return 1;
        else
            return next.getPlanSize() + 1;
    }

    private void swap(BodyLiteral bl) {
        BodyType bt = this.formType;
        this.formType = bl.getBodyType();
        bl.setBodyType(bt);

        Term l = this.term;
        this.term = bl.getBodyTerm();
        bl.setBodyTerm(l);
    }

    public Object clone() {
        if (term == null) // empty
            return new BodyLiteralImpl();

        BodyLiteralImpl c = new BodyLiteralImpl(formType, (Term)term.clone());
        if (next != null)
            c.setBodyNext((BodyLiteral)getBodyNext().clone());
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
        Element eb = (Element) document.createElement("body");
        BodyLiteral bl = this;
        while (bl != null && !bl.isEmptyBody()) {
            Element u = (Element) document.createElement("body-literal");
            if (bl.getBodyType().toString().length() > 0) {
                u.setAttribute("type", bl.getBodyType().toString());
            }
            u.appendChild( ((Structure)bl.getBodyTerm()).getAsDOM(document));
            eb.appendChild(u);
            
            bl = bl.getBodyNext();
        }
        return eb;
    }
}
