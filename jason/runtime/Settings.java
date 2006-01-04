//----------------------------------------------------------------------------
// Copyright (C) 2003  Rafael H. Bordini, Jomi F. Hubner, et al.
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
// To contact the authors:
// http://www.dur.ac.uk/r.bordini
// http://www.inf.furb.br/~jomi
//
// CVS information:
//   $Date$
//   $Revision$
//   $Log$
//   Revision 1.1  2006/01/04 02:55:57  jomifred
//   using java log API instead of apache log
//
//   Revision 1.11  2005/12/08 20:06:59  jomifred
//   changes for JasonIDE plugin
//
//   Revision 1.10  2005/10/30 18:39:48  jomifred
//   change in the AgArch customisation  support (the same customisation is used both to Cent and Saci infrastructures0
//
//   Revision 1.9  2005/10/20 21:40:09  jomifred
//   add some comments
//
//   Revision 1.8  2005/08/15 13:05:25  jomifred
//   using infrastructure instead of architecture in mas2j
//
//   Revision 1.7  2005/08/12 22:26:08  jomifred
//   add cvs keywords
//
//
//----------------------------------------------------------------------------


package jason.runtime;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


/** MAS Runtime Settings (from mas2j file) */
public class Settings {
	
    public static final byte      ODiscard        = 1;
    public static final byte      ORequeue        = 2;
    public static final byte      ORetrieve       = 3;
    public static final boolean   OSameFocus      = true;
    public static final boolean   ONewFocus       = false;
    public static final int       ODefaultNRC     = 1;
    public static final int       ODefaultVerbose = 1;
    public static final boolean   ODefaultSync    = false;
	
	
    byte    events    = ODiscard;
    boolean intBels   = OSameFocus;
    int     nrcbp     = ODefaultNRC;
    int     verbose   = ODefaultVerbose;
    boolean sync      = ODefaultSync; 

    Map userParameters = new HashMap();
    
    Logger logger = Logger.getLogger(Settings.class.getName());			
    
    public Settings() {
    }
    
    public Settings(String options) {
        setOptions(options);
    }
    
    public void setOptions(String options) {
        logger.fine("Setting options from "+options);
        jason.mas2j.parser.mas2j parser = new jason.mas2j.parser.mas2j( new StringReader(options));
        try {
            setOptions(parser.ASoptions());
            logger.fine("Settings are "+userParameters);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error parsing options "+options,e);
        }
    }
    
    public void setOptions(Map options) {
        if (options == null) return;
        userParameters = options;
        
        Iterator i = options.keySet().iterator();
        while (i.hasNext()) {
        	String key = (String)i.next();
        	
        	if (key.equals("events")) {
		        String events = (String)options.get("events");
	            if (events.equals("discard")) {
	                setEvents(ODiscard);
	            } else if (events.equals("requeue")) {
	                setEvents(ORequeue);
	            } else if (events.equals("retrieve")) {
	                setEvents(ORetrieve);
	            }

        	} else if (key.equals("intBels")) {
		        String intBels = (String)options.get("intBels");
	            if (intBels.equals("sameFocus")) {
	                setIntBels(OSameFocus);
	            } else if (intBels.equals("newFocus")) {
	            	setIntBels(ONewFocus);
	            }

        	} else if (key.equals("nrcbp")) {
		        String nrc = (String)options.get("nrcbp");
	            setNRCBP(nrc);
	
        	} else if (key.equals("verbose")) {
        		String verbose = (String)options.get("verbose");
	            setVerbose(verbose);
	
        	} else if (key.equals("synchronised")) {
        		String sSync = (String)options.get("synchronised");
	        	if (sSync.equals("true")) {
	        		setSync(true);
	        	} else {
	        		setSync(false);
	        	}
        	} else {
        		//userParameters.put(key, options.get(key));
	        }
        }
    }

	public void setEvents(byte opt) {
		events = opt;
	}

	public void setIntBels(boolean opt) {
		intBels = opt;
	}

    public void setNRCBP(String opt) {
        try {
            setNRCBP( Integer.parseInt(opt));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

	public void setNRCBP(int opt) {
		nrcbp = opt;
	}

    public void setVerbose(String opt) {
        try {
            setVerbose( Integer.parseInt(opt));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

	public void setVerbose(int opt) {
		verbose = opt;
	}
    
    public boolean discard() {
        return events == ODiscard;
    }

    public boolean requeue() {
        return events == ORequeue;
    }
    
    public boolean retrieve() {
        return events == ORetrieve;
    }

    public boolean sameFocus() {
        return(intBels);
    }
    public boolean newFocus() {
        return(!intBels);
    }

    public int nrcbp() {
    	return nrcbp;
    }
    
    public int verbose() {
        return verbose;
    }
    
    /*
    public Level log4JLevel() {
    	 switch(verbose) {
    	 case 0 : return Level.WARN;
    	 case 1 : return Level.INFO;
    	 case 2 : return Level.DEBUG;
    	 }
    	 return Level.INFO;
    }
    */
    
    public java.util.logging.Level logLevel() {
	   	 switch(verbose) {
	   	 case 0 : return java.util.logging.Level.WARNING;
	   	 case 1 : return java.util.logging.Level.INFO;
	   	 case 2 : return java.util.logging.Level.FINE;
	   	 }
	   	 return java.util.logging.Level.INFO;
   }
    
    /** returns true if the execution is synchronized */
    public boolean isSync() {
    	return sync;
    }
    
    public void setSync(boolean pSync) {
    	sync = pSync;
    }
    
    public String getUserParameter(String key) {
    	return (String)userParameters.get(key);
    }
}
