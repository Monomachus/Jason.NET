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

package jason.bb;

import jason.asSemantics.Agent;
import jason.asSyntax.Literal;
import jason.asSyntax.Pred;
import jason.asSyntax.PredicateIndicator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Default implementation of Jason BB.
 */
public class DefaultBeliefBase implements BeliefBase {

    static private Logger logger;

    /**
     * belsMap is a table where the key is the bel.getFunctorArity and the value
     * is a list of literals with the same functorArity.
     */
    private Map<PredicateIndicator, BelEntry> belsMap  = new HashMap<PredicateIndicator, BelEntry>();

    private int size = 0;

    /** set of beliefs with percept annot, used to improve performance of buf */
    Set<Literal> percepts = new HashSet<Literal>();

    public void init(Agent ag, String[] args) {
    	logger = Logger.getLogger(ag.getTS().getUserAgArch().getAgName() + "-"+DefaultBeliefBase.class.getSimpleName());
    }

    public void stop() {
    }

    public int size() {
        return size;
    }

    public Iterator<Literal> getPercepts() {
        return percepts.iterator();
    }

    public boolean add(Literal l) {
        if (l.equals(Literal.LTrue) || l.equals(Literal.LFalse)) {
            logger.log(Level.SEVERE, "Error: <true> or <false> can not be added as beliefs.");
            return false;
        }
        
        Literal bl = contains(l);
        if (bl != null && !bl.isRule()) {
            // add only annots
            if (l.hasSubsetAnnot(bl))
                // the current bel bl already has l's annots
                return false;
            else {
                // "import" annots from the new bel
                bl.importAnnots((Pred) l);
                // check if it needs to be added in the percepts list
                if (l.hasAnnot(TPercept)) {
                    percepts.add(bl);
                }
                return true;
            }
        } else {
            try {
                // minimize the allocation space of terms
                if (l.getTerms() != null)
                    ((ArrayList) l.getTerms()).trimToSize();
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error trimming literal's terms!", e);
            }

            BelEntry entry = belsMap.get(l.getPredicateIndicator());
            if (entry == null) {
                entry = new BelEntry();
                belsMap.put(l.getPredicateIndicator(), entry);
            }
            entry.add(l);
            // add it in the percepts list
            if (l.hasAnnot(TPercept)) {
                percepts.add(l);
            }
            size++;
            return true;
        }
    }

    public Iterator<Literal> iterator() {
        List<Literal> all = new ArrayList<Literal>(size());
        for (BelEntry be : belsMap.values()) {
            all.addAll(be.list);
        }
        return all.iterator();
	}
    
    /** @deprecated use iterator() instead of getAll */
    public Iterator<Literal> getAll() {
    	return iterator();
    }
    

    public boolean remove(Literal l) {
        Literal bl = contains(l);
        if (bl != null) {
            if (l.hasSubsetAnnot(bl)) {
                if (l.hasAnnot(TPercept)) {
                    percepts.remove(bl);
                }
                bl.delAnnot((Pred) l);
                if (!bl.hasAnnot()) {
                    PredicateIndicator key = l.getPredicateIndicator();
                    BelEntry entry = belsMap.get(key);
                    entry.remove(bl);
                    if (entry.isEmpty()) {
                        belsMap.remove(key);
                    }
                    size--;
                }
                return true;
            } else {
                return false;
            }
        } else {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Does not contain " + l + " in " + belsMap);
            }
            return false;
        }
    }

    public boolean abolish(PredicateIndicator pi) {
        return belsMap.remove(pi) != null;
    }

    public Literal contains(Literal l) {
        BelEntry entry = belsMap.get(l.getPredicateIndicator());
        if (entry == null) {
            return null;
        } else {
            //logger.info("*"+l+":"+l.hashCode()+" = "+entry.contains(l)+" in "+this);//+" entry="+entry);
            return entry.contains(l);
        }
    }

    public Iterator<Literal> getRelevant(Literal l) {
        if (l.isVar()) {
            // all bels are relevant
            return iterator();
        } else {
            BelEntry entry = belsMap.get(l.getPredicateIndicator());
            if (entry != null) {
                return entry.list.iterator();
            } else {
                return null;
            }
        }
    }

    public String toString() {
        return belsMap.toString();
    }

    public Element getAsDOM(Document document) {
        Element ebels = (Element) document.createElement("beliefs");
        for (Literal l: this) {
            ebels.appendChild(l.getAsDOM(document));
        }
        return ebels;
    }
    
    /** each predicate indicator has one BelEntry assigned to it */
    class BelEntry {
        
        private List<Literal> list = new ArrayList<Literal>(); // maintains the order of the bels
        private Map<LiteralWrapper,Literal> map = new HashMap<LiteralWrapper,Literal>(); // to fastly find contents, from literal do list index
        
        public void add(Literal l) {
            map.put(new LiteralWrapper(l), l);
            list.add(l);
        }
        
        public void remove(Literal l) {
            Literal linmap = map.remove(new LiteralWrapper(l)); 
            if (linmap != null) {
                list.remove(linmap);
            }
        }
        
        public boolean isEmpty() {
            return list.isEmpty();
        }
        
        public Literal contains(Literal l) {
            return map.get(new LiteralWrapper(l));
        }
        
        public String toString() {
            StringBuilder s = new StringBuilder();
            for (Literal l: list) {
                s.append(l+":"+l.hashCode()+",");
            }
            return s.toString();
            //return list.toString();
        }
        
        /** a literal that uses equalsAsTerm for equals */
        class LiteralWrapper {
            private Literal l;
            public LiteralWrapper(Literal l) { this.l = l; }
            public int hashCode() { return l.hashCode(); }
            public boolean equals(Object o) { return o instanceof LiteralWrapper && l.equalsAsTerm(((LiteralWrapper)o).l); }
            public String toString() { return l.toString(); }
        }
    }
}
