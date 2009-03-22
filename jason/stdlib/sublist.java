package jason.stdlib;

import jason.JasonException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.ListTerm;
import jason.asSyntax.Term;

import java.util.Iterator;
import java.util.List;

/**

  <p>Internal action: <b><code>.sublist(<i>S</i>,<i>L</i>)</code></b>.
  
  <p>Description: checks if some list <i>S</i> is a sublist of list <i>L</i>. If
  <i>S</i> has free variables, this internal action backtracks all
  possible values for <i>S</i>. This is based on .prefix and .suffix (try prefixes first then prefixes of each suffix).

  <p>Parameters:<ul>
  
  <li>+/- sublist (list): the sublist to be checked.</li>
  <li>+ list (list): the list where the sublist is from.</li>
  
  </ul>

  <p>Examples:<ul> 

  <li> <code>.sublist([a],[a,b,c])</code>: true.</li>
  <li> <code>.sublist([b],[a,b,c])</code>: true.</li>
  <li> <code>.sublist([c],[a,b,c])</code>: true.</li>
  <li> <code>.sublist([a,b],[a,b,c])</code>: true.</li>
  <li> <code>.sublist([b,c],[a,b,c])</code>: true.</li>
  <li> <code>.sublist([d],[a,b,c])</code>: false.</li>
  <li> <code>.sublist([a,c],[a,b,c])</code>: false.</li>
  <li> <code>.sublist(X,[a,b,c])</code>: unifies X with any suffix of the list, i.e., [a,b,c], [a,b], [a], [b,c], [b], [c] in this order;
                                         note that this is not the order in which its usual implementation would return in logic programming;
                                         also, the empty list is not generated as a possible sublist.</li>

  </ul>

  @see jason.stdlib.concat
  @see jason.stdlib.length
  @see jason.stdlib.sort
  @see jason.stdlib.nth
  @see jason.stdlib.max
  @see jason.stdlib.min
  @see jason.stdlib.reverse

  @see jason.stdlib.difference
  @see jason.stdlib.intersection
  @see jason.stdlib.union

*/
public class sublist extends DefaultInternalAction {
    
    private static InternalAction singleton = null;
    public static InternalAction create() {
        if (singleton == null) 
            singleton = new sublist();
        return singleton;
    }

    // Needs exactly 2 arguments
    @Override public int getMinArgs() { return 2; }
    @Override public int getMaxArgs() { return 2; }	

	// improve the check of the arguments to also check the type of the arguments
    @Override protected void checkArguments(Term[] args) throws JasonException {
        super.checkArguments(args); // check number of arguments
        if (!args[0].isList() && !args[0].isVar())
            throw JasonException.createWrongArgument(this,"first argument must be a list or a variable");
        if (!args[1].isList()) 
            throw JasonException.createWrongArgument(this,"second argument must be a list");
    }

    @Override
    public Object execute(TransitionSystem ts, final Unifier un, Term[] args) throws Exception {

        checkArguments(args);

        // execute the internal action

        final Term sublist = args[0];
//        final Iterator<Term> i = ((ListTerm)args[1]).iterator();
        final ListTerm listOutter = ((ListTerm)args[1]).cloneLT();
		
        return new Iterator<Unifier>() {
            Unifier c = null; // the current response (which is an unifier)
            ListTerm list = listOutter.cloneLT();  // used in the inner loop
            
            public boolean hasNext() {
                if (c == null) // the first call of hasNext should find the first response 
                    find();
                return c != null; 
            }

            public Unifier next() {
                if (c == null) find();
                Unifier b = c;
                find(); // find next response
                return b;
            }
            
            void find() {
                while (!listOutter.isEmpty()) {
                	while (!list.isEmpty()) {
                		c = un.clone();
                		if (c.unifiesNoUndo(sublist, list.clone())) {
                			((List)list).remove(list.size()-1);
                			return; // found another sublist, c is the current response
                		}
                		((List)list).remove(list.size()-1);
                	}
            		((List)listOutter).remove(0);
            		list = listOutter.cloneLT();
                }
                c = null; // no more sublists found 
            }

            public void remove() {}
        };
    }
}
