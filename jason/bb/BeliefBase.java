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
import jason.asSyntax.TermImpl;

import java.util.Iterator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public interface BeliefBase {

    public static final Term TPercept = TermImpl.parse("source(percept)");
    public static final Term TSelf    = TermImpl.parse("source(self)");

    /** 
     * Called before the MAS execution with the agent that uses this BB 
     * and the args informed in .mas2j 
     * E.g. in .mas2j:
     *     agent BeliefBaseClass(1,bla);
     * the init args will be ["1", "bla"].
     */
    public void init(Agent ag, String[] args);
    
    /** Called before the end of MAS execution */
    public void stop();
    
    public boolean add(Literal l);

    /** returns an iterator for all beliefs. */
    public Iterator<Literal> getAll();

    /** 
     * returns an iterator for all literals relevant for l's predicate indicator,
     * if l is a var, return all beliefs.
     * E.g.: 
     * BB={a(10),a(20),a(2,1),b(f)}, getRelecant(a(5)) = {{a(10),a(20)}.
     * BB={a(10),a(20)}, getRelecant(X) = {{a(10),a(20)}.
     */
    public Iterator<Literal> getRelevant(Literal l);

    public int size();

    /** returns all beliefs that have percept as source */
    public Iterator<Literal> getPercepts();

    public boolean remove(Literal l);

    /** remove all believes with some functor/arity */
    public boolean abolish(PredicateIndicator pi);

    /** get as XML */
    public Element getAsDOM(Document document);
}
