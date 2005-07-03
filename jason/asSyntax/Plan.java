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

import jason.asSemantics.Unifier;
import jason.asSyntax.parser.as2j;

import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Plan implements Cloneable, Serializable {

	public static final Term TAtomic = Term.parse("atomic");	
	
	Pred label = null;
	protected Trigger tevent = null;
	protected ArrayList context;
	protected ArrayList body;
	Boolean isAtomic = null; // if the label has atomic annotation, used to cache the value, so we do not need to seach all label annotations each isAtomic()
	
	
	public Plan() {
	}

	public Plan(Trigger te, ArrayList ct, ArrayList bd) {
		tevent = te;
		context = ct;
		body = bd;
	}

	public Plan(Pred lb, Trigger te, ArrayList ct, ArrayList bd) {
		label = lb;
		tevent = te;
		context = ct;
		body = bd;
	}

	public static Plan parse(String sPlan) {
		as2j parser = new as2j(new StringReader(sPlan));
		try {
			return parser.p();
		} catch (Exception e) {
			System.err.println("Error parsing plan " + sPlan);
			e.printStackTrace();
			return null;
		}
	}

	public Pred getLabel() {
		return label;
	}

	public void setLabel(String l) {
		label = new Pred(l);
	}
	
	public Trigger getTriggerEvent() {
		return tevent;
	}

	public List getContext() {
		return context;
	}

	public List getBody() {
		return body;
	}

	public boolean isAtomic() {
		if (isAtomic == null) {
			if (label != null) {
				isAtomic = new Boolean(label.hasAnnot(TAtomic));
			} else {
				isAtomic = new Boolean(false);
			}
		}
		return isAtomic.booleanValue();
	}
	
	public Unifier relevant(Trigger te) {
		// annots in plan's TE must be a subset of the ones in the event!
		// (see definition of Unifier.unifies for 2 Preds)
		Unifier u = new Unifier();
		if (u.unifies(tevent, te))
			return u;
		else
			return null;
	}


	public boolean equals(Object o) {
		Plan p = (Plan) o;
		return (tevent.equals(p.tevent) && context.equals(p.context) && body.equals(p.body));
	}

	public Object clone() {
		Plan p = new Plan();
		if (label == null)
			p.label = null;
		else
			p.label = (Pred)label.clone();
		// tevent shouldn't be null!!!
		p.tevent = (Trigger) tevent.clone();
		
		if (context == null)
			p.context = null;
		else {
			p.context = new ArrayList(context.size());
			Iterator i = context.iterator();
			while (i.hasNext()) {
				Literal l = (Literal) i.next();
				p.context.add(l.clone());
			}
		}
		
		if (body == null)
			p.body = null;
		else {
			p.body = new ArrayList(body.size());
			Iterator i = body.iterator();
			while (i.hasNext()) {
				BodyLiteral l = (BodyLiteral) i.next();
				p.body.add(l.clone());
			}
		}
		return p;
	}

	private String listToString(List l, String separator) {
		StringBuffer s = new StringBuffer();
		Iterator i = l.iterator();
		while (i.hasNext()) {
			s.append(i.next().toString());
			if (i.hasNext()) {
				s.append(separator);
			}
		}
		return s.toString();
	}

	public String toString() {
		return ("<<" + ((label == null) ? "" : label.toString() + " -> ")
				+ tevent.toString() + " : " + listToString(context, " & ") + " <- "
				+ listToString(body, "; ") + ">>");
	}

	/** returns this plan in a string compliant with AS syntax */
	public String toASString() {
		return  ((label == null) ? "" : "@" + label.toString() + " ")
				+ tevent.toString() + " : " + 
				((context.size() == 0) ? "true" : listToString(context, " & "))
				+ " <- " +
				((body.size() == 0) ? "true" : listToString(body, "; ")) 
				+ ".";
	}
	
    /** get as XML */
	public Element getAsDOM(Document document) {
		Element u = (Element) document.createElement("plan");
		if (label != null) {
			u.setAttribute("label", label.toString());
		}
		u.setAttribute("trigger", tevent.toString());
		u.setAttribute("context", listToString(context, " & "));
		u.setAttribute("body", listToString(body, "; "));
		return u;
	}

}