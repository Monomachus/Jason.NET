package jason.mas2j;

import jason.architecture.AgArch;
import jason.bb.DefaultBeliefBase;
import jason.runtime.Settings;

import java.io.File;
import java.util.Iterator;
import java.util.Map;

/** 
 * represents the agent declaration in the MAS2J project file. 
 * The project parser creates this object while parsing.
 * 
 * @author jomi
 */
public class AgentParameters {
	public String              name      = null;
    public File                asSource  = null;
    public ClassParameters     agClass   = null;
    public ClassParameters     bbClass   = null;
    public ClassParameters     archClass = null;
    public int                 qty       = 1;
    public String              host      = null;
    public Map<String, String> options   = null;
	
	public String toString() {
		return getAsInMASProject();
	}
	
    public void setupDefault() {
        if (agClass == null) {
            agClass = new ClassParameters(jason.asSemantics.Agent.class.getName());
        }
        if (archClass == null) {
            archClass = new ClassParameters(AgArch.class.getName());
        }
        if (bbClass == null) {
            bbClass = new ClassParameters(DefaultBeliefBase.class.getName());
        }        
    }
    
    public void setAgClass(String c) {
        if (c != null) agClass = new ClassParameters(c);
    }
    public void setArchClass(String c) {
        if (c != null) archClass = new ClassParameters(c);        
    }
    public void setBB(ClassParameters c) {
        if (c != null) bbClass = c;        
    }
    
	public String getAsInMASProject() {
        StringBuilder s = new StringBuilder(name+" ");
		if (asSource != null && !asSource.getName().startsWith(name)) {
			s.append(asSource+" ");
		}
		if (options != null && options.size() > 0) {
			s.append("[");
			Iterator i = options.keySet().iterator();
			while (i.hasNext()) {
				String k = (String)i.next();
				s.append(k+"="+options.get(k));
				if (i.hasNext()) {
					s.append(", ");
				}
			}
			s.append("] ");
		}
		if (archClass != null && archClass.className.length() > 0) {
			s.append("agentArchClass "+archClass+" ");
		}
		if (agClass != null && agClass.className.length() > 0) {
			s.append("agentClass "+agClass+" ");
		}
        if (bbClass != null && bbClass.className.length() > 0) {
            s.append("beliefBaseClass "+bbClass+" ");
        }
		if (qty > 1) {
			s.append("#"+qty+" ");
		}
		if (host != null && host.length() > 0) {
			s.append("at "+host);
		}
		return s.toString().trim() + ";";
	}

	public Settings getAsSetts(boolean debug, boolean forceSync) {
		Settings stts = new Settings();
		if (options != null) {
			Iterator i = options.keySet().iterator();
			String s = ""; String v = "";
			while (i.hasNext()) {
				String key = (String) i.next();
				s += v + key + "=" + options.get(key);
				v = ",";
			}
			if (s.length() > 0) {
				stts.setOptions("["+s+"]");
			}
		}
	    if (debug) {
	    	stts.setVerbose(2);
	    }
		
	    if (forceSync || debug) {
	    	stts.setSync(true);
	    }
        
        return stts;
	}	
}
