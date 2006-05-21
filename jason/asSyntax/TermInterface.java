package jason.asSyntax;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public interface TermInterface extends Cloneable {
	public Object clone();
	public boolean isGround();
	public boolean equals(Object o);
	public String getFunctor();
    public Element getAsDOM(Document document);
}
