package jason.stdlib;

import jason.JasonException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.ListTerm;
import jason.asSyntax.Term;

import java.util.Iterator;

/**
<p>Internal action: <b><code>.min</code></b>.

<p>Description: gets the minimal value of a list of terms. 
The "natural" order for each type of terms is used. Between 
different types of terms, the following order is
used:<br>

numbers &lt; atoms &lt; structures &lt; lists 

<p>Parameters:<ul>
<li>+   list (list): the list where to find the minimal term.<br/>
<li>+/- minimal (term). 
</ul>

<p>Examples:<ul>

<li> <code>.min([c,a,b],X)</code>: <code>X</code> unifies with
<code>a</code>.

<li>
<code>.min([b,c,10,g,f(10),[3,4],5,[3,10],f(4)],X)</code>:
<code>X</code> unifies with <code>5</code>.

<li>
<code>.min([3,2,5],2])</code>: true.

<li>
<code>.min([3,2,5],5)</code>: false.

<li>
<code>.min([],X)</code>: false.

</ul>

@see jason.stdlib.concat
@see jason.stdlib.length
@see jason.stdlib.member
@see jason.stdlib.nth
@see jason.stdlib.sort
@see jason.stdlib.max

*/
public class min extends DefaultInternalAction {

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        try {
        	args[0].apply(un);
        	args[1].apply(un);

        	if (!args[0].isList()) {
            	throw new JasonException("The first argument of .min should be a list.");
            }
        	ListTerm list = (ListTerm)args[0];
        	if (list.isEmpty()) {
            	return false;        		
        	}

        	Iterator<Term> i = list.iterator();
        	Term min = i.next();
        	while (i.hasNext()) {
        		Term t = i.next();
        		if (compare(min,t)) {
        			min = t;
        		}
        	}
            return un.unifies(args[1], min);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new JasonException("The internal action 'max/min' has not received two arguments.");
        } catch (JasonException e) {
        	throw e;
        } catch (Exception e) {
            throw new JasonException("Error in internal action 'max/min': " + e, e);
        }    
    }
    
    protected boolean compare(Term a, Term t) {
    	return a.compareTo(t) > 0;
    }
}
