package jason.asSyntax;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ObjectTermImpl extends DefaultTerm implements ObjectTerm {

    private final Object o;
    
    /** Creates a new Term Wrapper for java object */
    public ObjectTermImpl(Object o) {
        this.o = o;
    }
    
    public Object getObject() {
        return o;
    }
    
    @Override
    protected int calcHashCode() {
        return o.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return this.o.equals(o);
    }
    
    @Override
    public Object clone() {
        try {
            return new ObjectTermImpl(o.getClass().getMethod("clone", (Class[])null).invoke(o, (Object[])null));
        } catch (Exception e) {
            System.err.println("The object inside ObjectTerm should be clonable!");
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String toString() {
        return o.toString();
    }
    
    public Element getAsDOM(Document document) {
        Element u = (Element) document.createElement("object-term");
        u.appendChild(document.createTextNode(o.toString()));
        return u;
    }
}
