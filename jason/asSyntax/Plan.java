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
//   Revision 1.11  2006/01/04 02:54:41  jomifred
//   using java log API instead of apache log
//
//   Revision 1.10  2005/12/31 16:29:58  jomifred
//   add operator =..
//
//   Revision 1.9  2005/12/17 19:28:44  jomifred
//   no message
//
//   Revision 1.8  2005/08/12 22:26:08  jomifred
//   add cvs keywords
//
//
//----------------------------------------------------------------------------

package jason.asSyntax;

import jason.asSemantics.Unifier;
import jason.asSyntax.parser.as2j;

import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Plan implements Cloneable, Serializable {

	public static final Term TAtomic     = Term.parse("atomic");	
	public static final Term TBreakPoint = Term.parse("breakpoint");	
	
	protected Pred label = null;
	protected Trigger tevent = null;
	protected Term context;
	protected ArrayList<BodyLiteral>    body;

	Boolean isAtomic = null; // if the label has atomic annotation, used to cache the value, so we do not need to seach all label annotations each isAtomic()
	
	private int startSourceLine = 0; // the line number in the AS source
	private int endSourceLine = 0; // the line number in the AS source
	
	static private Logger logger = Logger.getLogger(Plan.class.getName());

	
	public Plan() {
	}

	public Plan(Trigger te, Term ct, ArrayList<BodyLiteral> bd) {
	    tevent = te;
        setContext(ct);
        setBody(bd);
	}

	public Plan(Pred lb, Trigger te, Term ct, ArrayList<BodyLiteral> bd) {
		label = lb;
		tevent = te;
		setContext(ct);
		setBody(bd);
	}

    public void setContext(Term le) {
        context = le;
        if (le != null && le.isLiteral() && ((Literal)le).equals(Literal.LTrue)) {
            context = null;
        }
    }
    
    public void setBody(ArrayList<BodyLiteral> bd) {
        if (bd == null) bd = new ArrayList<BodyLiteral>(0);
        body = bd;
    }
    
	public static Plan parse(String sPlan) {
		as2j parser = new as2j(new StringReader(sPlan));
		try {
			return parser.p();
		} catch (Exception e) {
			logger.log(Level.SEVERE,"Error parsing plan " + sPlan,e);
			return null;
		}
	}

	public void setSourceLines(int b, int e) {
		startSourceLine = b;
		endSourceLine = e;
	}
	public int getStartSourceLine() {
		return startSourceLine;
	}
	public int getEndSourceLine() {
		return endSourceLine;
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

	public Term getContext() {
		return context;
	}

	public List<BodyLiteral> getBody() {
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
        if (context == null && p.context != null) return false;
        if (context != null && p.context != null && !context.equals(p.context)) return false;
		return tevent.equals(p.tevent) && body.equals(p.body);
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
			p.context = (Term)context.clone();
            /*new ArrayList<DefaultLiteral>(context.size());
			for (DefaultLiteral l: context) {
				p.context.add( (DefaultLiteral)l.clone());
			}*/
		}
		
		if (body == null)
			p.body = null;
		else {
			p.body = new ArrayList<BodyLiteral>(body.size());
		    for (BodyLiteral l: body) {
				p.body.add((BodyLiteral)l.clone());
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
		return toASString();
		/*
		return ("<<" + ((label == null) ? "" : label.toString())
				+ tevent.toString() + " : " + listToString(context, " & ") + " <- "
				+ listToString(body, "; ") + ">>");
		*/
	}

	/** returns this plan in a string compliant with AS syntax */
	public String toASString() {
		return  ((label == null) ? "" : "@" + label.toString() + " ")
				+ tevent  
				//((context.size() == 0) ? "true" : listToString(context, " & "))
				+ ((context == null) ? "" : " : " + context)
				+ " <- " +
				((body.size() == 0) ? "true" : listToString(body, "; ")) 
				+ ".";
	}

    
    /** get as XML */
	public Element getAsDOM(Document document) {
		Element u = (Element) document.createElement("plan");
		if (label != null) {
            Element l = (Element) document.createElement("label");
            l.appendChild(new Literal(Literal.LPos, label).getAsDOM(document));
            u.appendChild(l);
		}
        u.appendChild(tevent.getAsDOM(document));
        
		//u.setAttribute("context", listToString(context, " & "));
		//u.setAttribute("body", listToString(body, "; "));
        if (context != null) {
            Element ec = (Element) document.createElement("context");
            ec.appendChild(context.getAsDOM(document));
            u.appendChild(ec);
        }
        
        if (body.size() > 0) {
            Element eb = (Element) document.createElement("body");
            for (BodyLiteral bl: body) {
                eb.appendChild(bl.getAsDOM(document));
            }
            u.appendChild(eb);
        }
        
		return u;
	}

}