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

import java.util.Iterator;
import java.util.List;

/**
 * List of Terms Interface
 * 
 * @author jomi
 */
public interface ListTerm extends java.util.List<Term>, Term {
	
	public void setTerm(Term t);
	public Term getTerm();
	public void setNext(Term l);
	public ListTerm getNext();
	
	public int size();
	public boolean isEmpty();
	public boolean isEnd();
	
	public boolean isTail();
	public VarTerm getTail();
	public void setTail(VarTerm v);
	public ListTerm getLast();
	public ListTerm append(Term t);
	public ListTerm concat(ListTerm lt);
	public Iterator<ListTerm> listTermIterator();
    public List<Term> getAsList();
}
