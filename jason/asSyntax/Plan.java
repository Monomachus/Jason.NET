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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/** Represents an AgentSpack plan */
public class Plan extends SourceInfo implements Cloneable, Serializable {
    
	private static final long serialVersionUID = 1L;
    private static final Term TAtomic         = DefaultTerm.parse("atomic");
    private static final Term TBreakPoint     = DefaultTerm.parse("breakpoint");
    private static final Term TAllUnifs       = DefaultTerm.parse("all_unifs");
    
    private static Logger     logger          = Logger.getLogger(Plan.class.getName());
    
	private Pred              label  = null;
    private Trigger           tevent = null;
    private LogicalFormula    context;
    private PlanBody          body;
    
    
    private boolean isAtomic = false;
    private boolean isAllUnifs = false;
    private boolean hasBreakpoint = false;
    
    // used by clone
    public Plan() {
    }
    
    // used by parser
    public Plan(Pred label, Trigger te, LogicalFormula ct, PlanBody bd) {
        tevent = te;
        setLabel(label);
        setContext(ct);
        if (bd == null)
            body = new PlanBodyImpl();
        else
            body = bd;
    }
    
    public void setLabel(Pred p) {
        label = p;
        if (p != null && p.hasAnnot()) {
            for (Term t: label.getAnnots()) {
                if (t.equals(TAtomic))
                    isAtomic = true;
                if (t.equals(TBreakPoint))
                    hasBreakpoint = true;
                if (t.equals(TAllUnifs))
                    isAllUnifs = true;
                // if change here, also change the clone()!
            }
        }
    }
    
    public Pred getLabel() {
        return label;
    }
    
    public void setContext(LogicalFormula le) {
        context = le;
        if (Literal.LTrue.equals(le))
            context = null;
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
    
    /** @deprecated use getTrigger */
    public Trigger getTriggerEvent() {
        return tevent;
    }

    public Trigger getTrigger() {
        return tevent;
    }
    
    public LogicalFormula getContext() {
        return context;
    }
    
    public PlanBody getBody() {
        return body;
    }
    
    public boolean isAtomic() {
        return isAtomic;
    }
    
    public boolean hasBreakpoint() {
        return hasBreakpoint;
    }

    public boolean isAllUnifs() {
        return isAllUnifs; 
    }
    
    /** returns an unifier if this plan is relevant for the event <i>te</i>, 
        returns null otherwise.
    */
    public Unifier isRelevant(Trigger te) {
        // annots in plan's TE must be a subset of the ones in the event!
        // (see definition of Unifier.unifies for 2 Preds)
        Unifier u = new Unifier();
        if (u.unifiesNoUndo(tevent, te))
            return u;
        else
            return null;
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == this) return true;

        if (o != null && o instanceof Plan) {
            Plan p = (Plan) o;
            if (context == null && p.context != null) return false;
            if (context != null && p.context != null && !context.equals(p.context)) return false;
            return tevent.equals(p.tevent) && body.equals(p.body);
        }
        return false;
    }
    
    public List<VarTerm> getSingletonVars() {
        Map<VarTerm, Integer> all  = new HashMap<VarTerm, Integer>();
        tevent.getLiteral().countVars(all);
        if (context != null) 
            context.countVars(all);
        body.countVars(all);

        List<VarTerm> r = new ArrayList<VarTerm>();
        for (VarTerm k: all.keySet()) {
            if (all.get(k) == 1 && !k.isUnnamedVar())
                r.add(k);
        }
        return r;
    }
    
    @Override
    public int hashCode() {
        int code = 37;
        if (context != null) code += context.hashCode();
        if (tevent != null)  code += tevent.hashCode();
        code += body.hashCode();
        return code;
    }
    
    public Object clone() {
        Plan p = new Plan();
        if (label != null) { 
            p.label         = (Pred) label.clone();
            p.isAtomic      = isAtomic;
            p.hasBreakpoint = hasBreakpoint;
            p.isAllUnifs    = isAllUnifs;
        }
        
        p.tevent = (Trigger)tevent.clone();
        
        if (context != null) 
            p.context = (LogicalFormula)context.clone();
        
        p.body = (PlanBody)body.clone();
        
        p.setSrc(this);

        return p;
    }

    /** used to create a plan clone in a new IM */
    public Plan cloneOnlyBody() {
        Plan p = new Plan();
        if (label != null) { 
            p.label         = label;
            p.isAtomic      = isAtomic;
            p.hasBreakpoint = hasBreakpoint;
            p.isAllUnifs    = isAllUnifs;
        }
        
        p.tevent  = (Trigger)tevent.clone();
        p.context = context;
        p.body    = (PlanBody)body.clone();
        
        p.setSrc(this);

        return p;
    }
    
    public String toString() {
        return toASString();
    }
    
    /** returns this plan in a string complaint with AS syntax */
    public String toASString() {
        return ((label == null) ? "" : "@" + label + " ") + 
               tevent + ((context == null) ? "" : " : " + context) +
               (body.isEmptyBody() ? "" : " <- " + body) +
               ".";
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
        
        if (!body.isEmptyBody()) {
            u.appendChild(body.getAsDOM(document));
        }
        
        return u;
    }
}
