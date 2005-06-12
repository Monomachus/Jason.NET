package jason.asSyntax;

import jason.asSyntax.parser.as2j;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
	
	public ListTerm() {
		super();
	}

	/** create a ListTerm with a term and no next ListTerm */
	public ListTerm(Term t) {
		term = t;
	}

    public static ListTerm parseList(String sList) {
        as2j parser = new as2j(new StringReader(sList));
        try {
            return (ListTerm)parser.list();
        } catch (Exception e) {
            System.err.println("Error parsing list "+sList);
            e.printStackTrace();
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
	
	public void addTerm(Term t) {
		System.err.println("Do not use addTerm in lists!");
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

	public void setTail(Term t) {
		isTail = true;
		next = t;
	}
	public boolean isTail() {
		return isTail;
	}
	public Term getTail() {
		if (isTail) {
			return next;
		} else {
			return null;
		}
	}
	
	
	/** creates a ListTerm from Term t, 
	 *  add it in this ListTerm, and
	 *  returns this new ListTerm
	 */
	public ListTerm append(Term t) {
			ListTerm lt = new ListTerm(t);
			next = lt;
			return lt;
	}
	public ListTerm append(ListTerm lt) {
			next = lt;
			return lt;
	}
	
	/** add a list in the end of this list */
	public void concat(ListTerm lt) {
		if ( ((ListTerm)next).isEmpty() ) {
			next = lt;
		} else {
			((ListTerm)next).concat(lt);
		}
	}
	
	/** add a term at the end of the list */
	public void add(Term t) {
		if (isEmpty()) {
			term = t;
			next = new ListTerm();
		} else {
			getNext().add(t);
		}
	}

	public Iterator iterator() {
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
	
	/** 
	 * Returns this ListTerm as a Java List. 
	 * Note: the list Tail is considered just the last element of the list!
	 */
    public List getAsList() {
        List l = new ArrayList();
		Iterator i = iterator();
		while (i.hasNext()) {
			ListTerm lt = (ListTerm)i.next();
			l.add( lt.getTerm() );
		}
		return l;
    }

	
	public String toString() {
		StringBuffer s = new StringBuffer("[");
		Iterator i = iterator();
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
