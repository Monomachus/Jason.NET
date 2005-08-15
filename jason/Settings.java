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
//   Revision 1.8  2005/08/15 13:05:25  jomifred
//   using infrastructure instead of architecture in mas2j
//
//   Revision 1.7  2005/08/12 22:26:08  jomifred
//   add cvs keywords
//
//
//----------------------------------------------------------------------------


package jason;

import jIDE.parser.mas2j;

import java.io.StringReader;
import java.util.Map;

import org.apache.log4j.Level;

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
    
    public Settings() {
    }
    
    public Settings(String options) {
        setOptions(options);
    }
    
    public void setOptions(String options) {
        mas2j parser = new mas2j( new StringReader(options));
        try {
            setOptions(parser.ASoptions());
        } catch (Exception e) {
            System.err.println("Error parsing options "+options);
            e.printStackTrace();
        }
    }
    
    public void setOptions(Map options) {
        if (options == null) return;
        
        //System.out.println("setting option from "+options);
        
        String events = (String)options.get("events");
        if (events != null) {
            if (events.equals("discard")) {
                setEvents(ODiscard);
            } else if (events.equals("requeue")) {
                setEvents(ORequeue);
            } else if (events.equals("retrieve")) {
                setEvents(ORetrieve);
            }
        }
        
        String intBels = (String)options.get("intBels");
        if (intBels != null) {
            if (intBels.equals("sameFocus")) {
                setIntBels(OSameFocus);
            } else if (intBels.equals("newFocus")) {
            	setIntBels(ONewFocus);
            }
        }

        String nrc = (String)options.get("nrcbp");
        if (nrc != null) {
            setNRCBP(nrc);
        }

        String verbose = (String)options.get("verbose");
        if (verbose != null) {
            setVerbose(verbose);
        }

        String sSync = (String)options.get("synchronised");
        if (sSync != null) {
        	if (sSync.equals("true")) {
        		setSync(true);
        	} else {
        		setSync(false);
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
    
    public Level log4JLevel() {
    	 switch(verbose) {
    	 case 0 : return Level.WARN;
    	 case 1 : return Level.INFO;
    	 case 2 : return Level.DEBUG;
    	 }
    	 return Level.INFO;
    }
    
    /** returns true if the execution is synchronized */
    public boolean isSync() {
    	return sync;
    }
    
    public void setSync(boolean pSync) {
    	sync = pSync;
    }
}
