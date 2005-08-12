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
//   Revision 1.6  2005/08/12 22:26:08  jomifred
//   add cvs keywords
//
//
//----------------------------------------------------------------------------

package jason.asSyntax;

import jason.asSyntax.parser.as2j;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * Each nth-ListTerm has a term and the next ListTerm.
 * The last ListTem is a emptyListTerm (term==null).
 * In lists with tail ([a|X]), next is the Tail (next=X).

 * @author jomi
 */
public class ListTerm extends Term {
	
	boolean isTail = false;
	
	private Term term;
	private Term next;

	static private Logger logger = Logger.getLogger(ListTerm.class.getName());
	
	public ListTerm() {
		super();
	}

    public static ListTerm parseList(String sList) {
        as2j parser = new as2j(new StringReader(sList));
        try {
            return (ListTerm)parser.list();
        } catch (Exception e) {
            logger.error("Error parsing list "+sList,e);
			return null;
        }
    }
	
	/** make a hard copy of the terms */
	public Object clone() {
		// do not call constructor with term parameter!
		ListTerm t = new ListTerm();
		if (term != null) {
			t.term = (Term)this.term.clone();
		}
		if (next != null) {
			t.next = (Term)this.next.clone();
		}
		return t;
	}
	

	public boolean equals(Object t) {
		try {
			ListTerm tAsTerm = (ListTerm)t;
			if (term == null && tAsTerm.term != null) {
				return false;
			}
			if (term != null && !term.equals(tAsTerm.term)) {
				return false;
			}
			if (next != null) {
				return next.equals(tAsTerm.next);
			}
			return true;
		} catch (ClassCastException e) {
			return false;
		}
	}
	
	
	/** gets the term of this ListTerm */
	public Term getTerm() {
		return term;
	}
	
	public ListTerm getNext() {
		try {
			return (ListTerm)next;
		} catch (Exception e){}
		return null;
	}
	
	
	// for unifier compatibility
	public int getTermsSize() {
		if (isEmpty()) {
			return 0;
		} else {
			return 2; // term and next
		}
	}
	// for unifier compatibility
	public Term getTerm(int i) {
		if (i == 0) {
			return term;
		}
		if (i == 1) {
			return next;
		}
		return null;
	}
	
	/** return the this ListTerm elements (0=Term, 1=ListTerm) */
	public List getTerms() {
		List l = new ArrayList(2);
		if (term != null) {
			l.add(term);
		}
		if (next != null) {
			l.add(next);
		}
		return l;
	}
	
	public void addTerm(Term t) {
		logger.warn("Do not use addTerm in lists!");
	}

	public int size() {
		if (isEmpty()) {
			return 0;
		} else if (isTail()) {
			return 1;
		} else {
			return getNext().size() + 1;
		}
	}
	
	public boolean isList() {
		return true;
	}
	public boolean isEmpty() {
		return term == null;
	}
	public boolean isEnd() {
		return isEmpty() || isTail();
	}

	public boolean isGround() {
		Iterator i = termsIterator();
		while (i.hasNext()) {
			Term t = (Term)i.next();
			if (!t.isGround()) {
				return false;
			}
		}
		return true;
	}
	
	public void setTail(Term t) {
		isTail = true;
		next = t;
	}
	public boolean isTail() {
		return isTail;
	}
	
	/** returns this ListTerm's tail element in case this ListTerm has the Tail, otherwise, returns null */
	public Term getTail() {
		if (isTail) {
			return next;
		} else {
			return null;
		}
	}
	
	/** get the last ListTerm of this List */
	public ListTerm getLast() {
		if (isEnd()) {
			return this;
		} else if (next != null) {
			return getNext().getLast();
		} 
		return null; // !!! no last!!!!
	}
	
	
	/** add a term in the end of the list
	 * @return the ListTerm where the term was added
	 */
	public ListTerm add(Term t) {
		if (isEmpty()) {
			term = t;
			next = new ListTerm();
			return this;
		} else if (isTail()) {
			// What to do?
			return null;
		} else {
			return getNext().add(t);
		}
	}

	
	/** Add a list in the end of this list.
	 * This method do not clone <i>lt</i>.
	 * @return the last ListTerm of the new list
	 */
	public ListTerm concat(ListTerm lt) {
		if ( ((ListTerm)next).isEmpty() ) {
			next = lt;
		} else {
			((ListTerm)next).concat(lt);
		}
		return lt.getLast();
	}

	
	/** returns an iterator where each element is a ListTerm */
	public Iterator listTermIterator() {
		final ListTerm lt = this;
		return new Iterator() {
			ListTerm current = lt;
			public boolean hasNext() {
				return current != null && !current.isEmpty();
			}
			public Object next() {
				Object o = current;
				current = current.getNext();
				return o;
			}
			public void remove() {	
			}
		};
	}

	/** returns an iterator where each element is a Term of this list */
	public Iterator termsIterator() {
		final Iterator i = this.listTermIterator();
		return new Iterator() {
			public boolean hasNext() {
				return i.hasNext();
			}
			public Object next() {
				ListTerm lt = (ListTerm)i.next();
				return lt.getTerm();
			}
			public void remove() {	
			}
		};
	}
	
	
	/** 
	 * Returns this ListTerm as a Java List. 
	 * Note: the list Tail is considered just the last element of the list!
	 */
    public List getAsList() {
        List l = new ArrayList();
		Iterator i = termsIterator();
		while (i.hasNext()) {
			l.add( i.next() );
		}
		return l;
    }

	
	public String toString() {
		StringBuffer s = new StringBuffer("[");
		Iterator i = listTermIterator();
		while (i.hasNext()) {
			ListTerm lt = (ListTerm)i.next();
			s.append( lt.getTerm() );
			if (lt.isTail()) {
				s.append("|");
				s.append(lt.getTail());
			} else if (i.hasNext()) {
				s.append(",");
			}
		}
		s.append("]");
		return s.toString();
	}
		
}
