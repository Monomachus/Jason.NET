package jason.mas2j;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** 
 * Used to store class parameters in .mas2j file, e.g. 
 *      environment: Mars(a,b,c); 
 * this class stores "Mars(a,b,c)"
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
    
    public String[] getParametersArray() {        
        String[] p = new String[parameters.size()];
        int i=0;
        for (String s: parameters) {
            p[i++] = removeQuotes(s);
        }
        return p;
    }
    
    /** returns parameters with space as separator */
    public String getParametersStr() {
        StringBuffer out = new StringBuffer();
        if (parameters.size() > 0) {
            for (String s: parameters) {
                out.append(s+" ");
            }
        }
        return out.toString();
        
    }
    
    public String toString() {
        StringBuffer out = new StringBuffer(className);
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
