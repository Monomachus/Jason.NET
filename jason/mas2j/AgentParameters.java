package jason.mas2j;

import jason.architecture.AgArch;
import jason.runtime.Settings;

import java.io.File;
import java.util.Iterator;
import java.util.Map;

public class AgentParameters {
	public String name      = null;
	public File   asSource  = null;
	public String agClass   = null;
	public String archClass = null;
	public Map    options   = null;
	public int    qty       = 1;
	public String host      = null;
	
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
		if (archClass != null && archClass.length() > 0) {
			s.append("agentArchClass "+archClass+" ");
		}
		if (agClass != null && agClass.length() > 0) {
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

	public String getAsSaciXMLScript(String proDir, String soc, String architecture, boolean debug, boolean forceSync) {
		StringBuffer s = new StringBuffer("\t<startAgent "); 
        s.append("\n\t\tname=\""+name+"\" "); 
        s.append("\n\t\tsociety.name=\""+soc+"\" ");
        
        String tmpInfraClass = jason.architecture.CentralisedAgArch.class.getName();
    	if (architecture.equals("Saci")) {
    		tmpInfraClass = jason.architecture.SaciAgArch.class.getName();
    	}
        s.append("\n\t\tclass=\""+tmpInfraClass+"\"");

        String tmpAgClass = agClass;
        if (tmpAgClass == null) {
        	tmpAgClass = jason.asSemantics.Agent.class.getName();
        }
        String tmpAgArchClass = archClass;
        if (tmpAgArchClass == null) {
        	tmpAgArchClass = AgArch.class.getName();
        }
        
        File tmpAsSrc = new File(proDir + File.separator + asSource);
        s.append("\n\t\targs=\""+tmpAgArchClass+" "+tmpAgClass+" '"+tmpAsSrc.getAbsolutePath()+"'"+getSaciOptsStr(debug, forceSync)+"\"");
        if (qty > 1) {
        	s.append("\n\t\tqty=\""+qty+"\" ");
        }
        if (host != null) {
        	s.append("\n\t\thost="+host);
        }
        s.append(" />");
		return s.toString().trim();
	}
	
	String getSaciOptsStr(boolean debug, boolean forceSync) {
		String s = "";
		String v = "";
	    if (debug) {
	    	s += "verbose=2";
	    	v = ",";
	    }
	    if (forceSync || debug) {
	    	s += v+"synchronised=true";
	    	v = ",";
	    }
		Iterator i = options.keySet().iterator();
		while (i.hasNext()) {
			String key = (String) i.next();
			if (!(debug && key.equals("verbose"))) {
				if (!( (forceSync || debug) && key.equals("synchronised"))) {
					s += v + key + "=" + changeQuotes((String)options.get(key));
					v = ",";
				}
			}
		}
		if (s.length() > 0) {
			s = " options " + s;
		}
		return s;
	}
	
	public Settings getAsSetts(boolean debug, boolean forceSync) {
		Settings stts = new Settings();
		
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
	    if (debug) {
	    	stts.setVerbose(2);
	    }
		
	    if (forceSync || debug) {
	    	stts.setSync(true);
	    }
		return stts;
	}
	
	String removeQuotes(String s) {
		if (s.startsWith("\"") && s.endsWith("\"")) {
			return s.substring(1,s.length()-1);
		} else {
			return s;
		}
	}
	
	String changeQuotes(String s) {
		if (s.startsWith("\"") && s.endsWith("\"")) {
			return "'"+s.substring(1,s.length()-1)+"'";
		} else {
			return s;
		}
	}	
}
