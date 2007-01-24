
package jason.stdlib;

import jason.asSemantics.*;
import jason.asSyntax.*;
import java.util.logging.*;

/**
  <p>Internal action: <b><code>.substring</code></b>.

  <p>Description: checks if a string is sub-string of another
    string. The arguments can be other kinds of terms, in which case
    the toString() of the term is used. If "position" is a
    free variable, the internal action backtracks all possible values
    for the positions where the sub-string occurs in the string.

  <p>Parameters:<ul>
  <li>+ substring (any term).<br/>
  <li>+ string (any term).<br/>
  <li>+- position (optional -- integer): the position of
  the string where the sub-string occurs. 
  </ul>

  <p>Examples:<ul>
  <li> <code>.substring("b","aaa")</code>: false.
  <li> <code>.substring("b","aaa",X)</code>: false.
  <li> <code>.substring("a","bbacc")</code>: true.
  <li> <code>.substring("a","abbacca",X)</code>: true and <code>X</code> unifies with 0, 3, and 6.
  <li> <code>.substring("a","bbacc",0)</code>: false.
  <li> <code>.substring(a(10),b(t1,a(10)),X)</code>: true and <code>X</code> unifies with 5.
  <li> <code>.substring(a(10),b("t1,a(10)"),X)</code>: true and <code>X</code> unifies with 6.
  </ul>

  @see jason.stdlib.concat
  @see jason.stdlib.length

*/
public class substring extends DefaultInternalAction {

    // TODO: implement backtrack version

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        for (int i = 0; i<args.length; i++) {
            args[i].apply(un);
        }
        
        String s0 = args[0].toString();
        if (args[0].isString()) {
            s0 = ((StringTerm)args[0]).getString();
        }
        
        String s1 = args[1].toString();
        if (args[1].isString()) {
            s1 = ((StringTerm)args[1]).getString();
        }

        //s0 = s0.toUpperCase();
        //s1 = s1.toUpperCase();
        int pos = s1.indexOf(s0);
        if (s1.length() > 0 && pos >= 0) {
            if (args.length == 2) {
                return true;
            } else {
                return un.unifies(args[2], new NumberTermImpl(pos));
            }
        }
        return false;
    }
}

