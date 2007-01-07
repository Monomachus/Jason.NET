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
import jason.asSyntax.PredicateIndicator;
import jason.asSyntax.Term;
import jason.asSyntax.DefaultTerm;

import java.util.Iterator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * Common interface for all kinds of Jason Belief bases, even those
 * customised by the user.
 */
public interface BeliefBase extends Iterable<Literal> {

    public static final Term TPercept = DefaultTerm.parse("source(percept)");
    public static final Term TSelf    = DefaultTerm.parse("source(self)");

    /** 
     * Called before the MAS execution with the agent that uses this
     * BB and the args informed in .mas2j project.<br>
     * Example in .mas2j:<br>
     *     <code>agent BeliefBaseClass(1,bla);</code><br>
     * the init args will be ["1", "bla"].
     */
    public void init(Agent ag, String[] args);
    
    /** Called just before the end of MAS execution */
    public void stop();
    
    /** Adds a belief in the BB, returns true if succeed */
    public boolean add(Literal l);

    /** Returns an iterator for all beliefs. */
    public Iterator<Literal> iterator();

    /** @deprecated use iterator() instead of getAll */
    public Iterator<Literal> getAll();
    
    /** 
     * Returns an iterator for all literals relevant for l's predicate
     * indicator, if l is a var, return all beliefs.<br>
     *
     * Example, if BB={a(10),a(20),a(2,1),b(f)}, then
     * <code>getRelevant(a(5))</code> = {{a(10),a(20)}.<br> if
     * BB={a(10),a(20)}, then <code>getRelevant(X)</code> =
     * {{a(10),a(20)}.
     */
    public Iterator<Literal> getRelevant(Literal l);

    /**
     * Returns the literal l as it is in BB, this method does not
     * consider annotations in the search. <br> Example, if
     * BB={a(10)[a,b]}, <code>contains(a(10)[d])</code> returns
     * a(10)[a,b].
     */
    public Literal contains(Literal l);
    
    /** Returns the number of beliefs in BB */
    public int size();

    /** Returns all beliefs that have percept as source */
    public Iterator<Literal> getPercepts();

    /** Removes a literal from BB, returns true if succeed */
    public boolean remove(Literal l);

    /** Removes all believes with some functor/arity */
    public boolean abolish(PredicateIndicator pi);

    /** Gets the BB as XML */
    public Element getAsDOM(Document document);
}
