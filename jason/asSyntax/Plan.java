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
//----------------------------------------------------------------------------

package jason.asSyntax;

import jason.asSemantics.Unifier;
import jason.asSyntax.parser.as2j;

import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Plan implements Cloneable, Serializable {
    
	private static final long serialVersionUID = 1L;

	protected Pred              label  = null;
    protected Trigger           tevent = null;
    protected LogicalFormula    context;
    protected List<BodyLiteral> body;
    
    private boolean isAtomic = false;
    private boolean isAllUnifs = false;
    private boolean hasBreakpoint = false;
    
    private static final Term TAtomic         = TermImpl.parse("atomic");
    private static final Term TBreakPoint     = TermImpl.parse("breakpoint");
    private static final Term TAllUnifs       = TermImpl.parse("all_unifs");
    
    /*
    private enum Annots {
        atomic, breakpoint
    }
    // enum set of special annots, to improve performance in isAtomic, isBreak,
    // ...
    private EnumSet<Annots>   properties      = EnumSet.noneOf(Annots.class);
     */
    
    private int               startSourceLine = 0;
    private int               endSourceLine   = 0;
    
    static private Logger     logger          = Logger.getLogger(Plan.class.getName());
    
    public Plan() {
    }
    
    public Plan(Trigger te, LogicalFormula ct, ArrayList<BodyLiteral> bd) {
        tevent = te;
        setContext(ct);
        setBody(bd);
    }
    
    public Plan(Pred lb, Trigger te, LogicalFormula ct, ArrayList<BodyLiteral> bd) {
        tevent = te;
        setLabel(lb);
        setContext(ct);
        setBody(bd);
    }
    
    public void setLabel(Pred p) {
        label = p;
        if (p != null) {
            // isAtomic = label.hasAnnot(TAtomic);
            if (label.hasAnnot(TAtomic)) {
                //properties.add(Annots.atomic);
                isAtomic = true;
            }
            if (label.hasAnnot(TBreakPoint)) {
                //properties.add(Annots.breakpoint);
                hasBreakpoint = true;
            }
            if (label.hasAnnot(TAllUnifs)) {
                isAllUnifs = true;
            }
        }
    }
    
    public Pred getLabel() {
        return label;
    }
    
    public void setContext(LogicalFormula le) {
        context = le;
        if (le != null && le instanceof Literal && ((Literal) le).equals(Literal.LTrue)) {
            context = null;
        }
    }
    
    public void setBody(List<BodyLiteral> bd) {
        if (bd == null)
            body = Collections.emptyList();
        else
            body = bd;
    }
    
    public static Plan parse(String sPlan) {
        as2j parser = new as2j(new StringReader(sPlan));
        try {
            return parser.plan();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error parsing plan " + sPlan, e);
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
    
    public Trigger getTriggerEvent() {
        return tevent;
    }
    
    public LogicalFormula getContext() {
        return context;
    }
    
    public List<BodyLiteral> getBody() {
        return body;
    }
    
    public boolean isAtomic() {
        return isAtomic; //properties.contains(Annots.atomic);
    }
    
    public boolean hasBreakpoint() {
        return hasBreakpoint;//properties.contains(Annots.breakpoint);
    }

    public boolean isAllUnifs() {
        return isAllUnifs; 
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
    
    @Override
    public boolean equals(Object o) {
        if (o == this) return true;

        if (o != null && o instanceof Plan) {
            Plan p = (Plan) o;
            if (context == null && p.context != null)
                return false;
            if (context != null && p.context != null && !context.equals(p.context))
                return false;
            return tevent.equals(p.tevent) && body.equals(p.body);
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        int code = 37;
        if (context != null)
            code += context.hashCode();
        if (tevent != null)
            code += tevent.hashCode();
        if (body != null) {
            code += body.hashCode();
        }
        return code;
    }
    
    public Object clone() {
        Plan p = new Plan();
        if (label != null) {
            p.setLabel((Pred) label.clone());
        }
        
        // tevent shouldn't be null!!!
        p.tevent = (Trigger) tevent.clone();
        
        if (context != null) {
            p.setContext((LogicalFormula) context.clone());
        }
        
        List<BodyLiteral> copy = new LinkedList<BodyLiteral>(); // the plan will be "consumed" by remove(0), so linkedlist
        for (BodyLiteral l : body) {
            copy.add((BodyLiteral) l.clone());
        }
        p.setBody(copy);
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
    }
    
    /** returns this plan in a string compliant with AS syntax */
    public String toASString() {
        return (((label == null) ? "" : "@" + label + " ") + 
               tevent + ((context == null) ? "" : " : " + context) + 
               ((body.size() == 0) ? "" : " <- " + listToString(body, "; ")) + 
               ".");
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
        
        if (context != null) {
            Element ec = (Element) document.createElement("context");
            ec.appendChild(context.getAsDOM(document));
            u.appendChild(ec);
        }
        
        if (body.size() > 0) {
            Element eb = (Element) document.createElement("body");
            for (BodyLiteral bl : body) {
                eb.appendChild(bl.getAsDOM(document));
            }
            u.appendChild(eb);
        }
        
        return u;
    }
}