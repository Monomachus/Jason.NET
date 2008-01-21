package jason.asSyntax;

import java.util.Iterator;
import java.util.List;

/**
 * The interface for lists of the AgentSpeak language
 * 
 * @author Jomi
 */
public interface ListTerm extends java.util.List<Term>, Term {
	
    public void setTerm(Term t);
    public Term getTerm();
    public void setNext(Term l);
    public ListTerm getNext();
	
    public boolean isEnd();
	
    public boolean isTail();
    public VarTerm getTail();
    public void setTail(VarTerm v);
    public ListTerm getLast();
    public ListTerm append(Term t);
    public ListTerm concat(ListTerm lt);
    public ListTerm reverse();
    
    public ListTerm union(ListTerm lt);
    public ListTerm intersection(ListTerm lt);
    public ListTerm difference(ListTerm lt);
    
    public Iterator<ListTerm> listTermIterator();
    public List<Term> getAsList();
}
