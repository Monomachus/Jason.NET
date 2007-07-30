package jason.mas2j;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import jason.asSyntax.*;

/** 
 * Used to store class parameters in .mas2j file, e.g. 
 *      environment: Mars(a,b,c); 
 * this class stores 
 *   className  = Mars,
 *   parameters = {a,b,c}
 * 
 * @author jomi
 */
public class ClassParameters {
    public String className;
    public List<String> parameters = new ArrayList<String>();
    public String host;
    
    public ClassParameters() {}
    public ClassParameters(String className) {
        this.className = className;
    }
    public ClassParameters(Structure s) {
        className = s.getFunctor();
        if (s.getArity() > 0) {
            for (Term t: s.getTerms()) {
                parameters.add(t.toString());
            }
        }
    }
    
    public boolean hasParameters() {
        return !parameters.isEmpty();
    }
    
    public String[] getParametersArray() {        
        String[] p = new String[parameters.size()];
        int i=0;
        for (String s: parameters) {
            p[i++] = removeQuotes(s);
        }
        return p;
    }
    
    /** returns parameters with space as separator */
    public String getParametersStr(String sep) {
        StringBuilder out = new StringBuilder();
        if (parameters.size() > 0) {
            Iterator<String> i = parameters.iterator();
            while (i.hasNext()) {
                out.append(i.next());
                if (i.hasNext()) out.append(sep);
            }
        }
        return out.toString();
        
    }
    
    public String toString() {
        StringBuilder out = new StringBuilder(className);
        if (parameters.size() > 0) {
            out.append("(");
            Iterator<String> i = parameters.iterator();
            while (i.hasNext()) {
                out.append(i.next());
                if (i.hasNext()) {
                    out.append(",");
                }
            }
            out.append(")");
        }
        return out.toString();
    }

    String removeQuotes(String s) {
        if (s.startsWith("\"") && s.endsWith("\"")) {
            return s.substring(1, s.length() - 1);
        } else {
            return s;
        }
    }
}
