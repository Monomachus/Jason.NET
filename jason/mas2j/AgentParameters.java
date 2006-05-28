package jason.mas2j;

import jason.runtime.Settings;

import java.io.File;
import java.util.Iterator;
import java.util.Map;

public class AgentParameters {
	public String              name      = null;
    public File                asSource  = null;
    public ClassParameters     agClass   = null;
    public ClassParameters     archClass = null;
    public int                 qty       = 1;
    public String              host      = null;
    public Map<String, String> options   = null;
	
	public String toString() {
		return getAsInMASProject();
	}
	
	public String getAsInMASProject() {
		StringBuffer s = new StringBuffer(name+" ");
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
