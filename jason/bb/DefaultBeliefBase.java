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

public class DefaultBeliefBase implements BeliefBase {

    static private Logger                  logger   = Logger.getLogger(DefaultBeliefBase.class.getName());

    /**
     * belsMap is a table where the key is the bel.getFunctorArity and the value
     * is a list of literals with the same functorArity.
     */
    Map<PredicateIndicator, List<Literal>> belsMap  = new HashMap<PredicateIndicator, List<Literal>>();

    private int                            size     = 0;

    /** set of beliefs with percept annot, used to improve performance of buf */
    Set<Literal>                          percepts = new HashSet<Literal>();

    public void init(Agent ag, String[] args) {
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
        
        /*
        if (!l.isGround() && !l.isRule()) {
            logger.log(Level.SEVERE, "Error: Literal must be ground!");
            return false;
        }
        */
        
        Literal bl = containsAsTerm(l);
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

            List<Literal> listFunctor = belsMap.get(l.getPredicateIndicator());
            if (listFunctor == null) {
                listFunctor = new ArrayList<Literal>();
                belsMap.put(l.getPredicateIndicator(), listFunctor);
            }
            listFunctor.add(l);
            // add it in the percepts list
            if (l.hasAnnot(TPercept)) {
                percepts.add(l);
            }
            size++;
            return true;
        }
    }

    public Iterator<Literal> getAll() {
        List<Literal> all = new ArrayList<Literal>(size());
        for (List<Literal> l : belsMap.values()) {
            all.addAll(l);
        }
        return all.iterator();
    }

    public boolean remove(Literal l) {
        Literal bl = containsAsTerm(l);
        if (bl != null) {
            if (l.hasSubsetAnnot(bl)) {
                if (l.hasAnnot(TPercept)) {
                    percepts.remove(bl);
                }
                bl.delAnnot((Pred) l);
                if (!bl.hasAnnot()) {
                    PredicateIndicator key = l.getPredicateIndicator();
                    List<Literal> listFunctor = belsMap.get(key);
                    listFunctor.remove(bl);
                    if (listFunctor.isEmpty()) {
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

    /** remove all believes with some functor/arity */
    public boolean abolish(PredicateIndicator pi) {
        return belsMap.remove(pi) != null;
    }

    /**
     * returns the literal l as it is in BB, this method does not consider
     * annots in the search. e.g. if BB={a(10)[a,b]}, contains(a(10)[d]) returns
     * a(10)[a,b]
     */
    public Literal containsAsTerm(Literal l) {
        List<Literal> listFunctor = belsMap.get(l.getPredicateIndicator());
        if (listFunctor == null) {
            return null;
        }
        for (Literal bl : listFunctor) {
            if (l.equalsAsTerm(bl)) {
                return bl;
            }
        }
        return null;
    }

    public Iterator<Literal> getRelevant(Literal l) {
        if (l.isVar()) {
            // all bels are relevant
            return getAll();
        } else {
            List<Literal> relList = belsMap.get(l.getPredicateIndicator());
            if (relList != null) {
                return relList.iterator();
            } else {
                return null;
            }
        }
    }

    public String toString() {
        return belsMap.toString();
    }

    /** get as XML */
    public Element getAsDOM(Document document) {
        Element ebels = (Element) document.createElement("beliefs");
        for (List<Literal> ll : belsMap.values()) {
            for (Literal l : ll) {
                ebels.appendChild(l.getAsDOM(document));
            }
        }
        return ebels;
    }
}
