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

  <p>Internal action: <b><code>.prefix(<i>P</i>,<i>L</i>)</code></b>.
  
  <p>Description: checks if some list <i>P</i> is a prefix of list <i>L</i>. If
  <i>P</i> has free variables, this internal action backtracks all
  possible values for <i>P</i>.

  <p>Parameters:<ul>
  
  <li>+/- prefix (list): the prefix to be checked.</li>
  <li>+ list (list): the list where the prefix is from.</li>
  
  </ul>

  <p>Examples:<ul> 

  <li> <code>.prefix([a],[a,b,c])</code>: true.</li>
  <li> <code>.prefix([b,c],[a,b,c])</code>: false.</li>
  <li> <code>.prefix(X,[a,b,c])</code>: unifies X with any suffix of the list, i.e., [a,b,c], [a,b], and [a], in this order;
                                        note that this is different from what its usual implementation in logic programming would result;
                                        also, the empty list is not generated as a possibly prefix. </li>

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
public class prefix extends DefaultInternalAction {
    
    private static InternalAction singleton = null;
    public static InternalAction create() {
        if (singleton == null) 
            singleton = new prefix();
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
        final ListTerm list = ((ListTerm)args[1]).cloneLT();
		
        return new Iterator<Unifier>() {
            Unifier c = null; // the current response (which is an unifier)
            
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
                while (!list.isEmpty()) {
                    c = un.clone();
                    if (c.unifiesNoUndo(sublist, list.clone())) {
    					((List)list).remove(list.size()-1);
                        return; // found another sublist, c is the current response
					}
					((List)list).remove(list.size()-1);
                }
                c = null; // no more sublists found 
            }

            public void remove() {}
        };
    }
}
