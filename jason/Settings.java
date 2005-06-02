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
// http://www.csc.liv.ac.uk/~bordini
// http://www.inf.furb.br/~jomi
//----------------------------------------------------------------------------


package jason;

import jIDE.parser.mas2j;
import java.io.StringReader;
import java.util.Map;

public class Settings {
    byte    events    = D.ODiscard;
    boolean intBels   = D.OSameFocus;
    int     verbose   = D.ODefaultVerbose;
    boolean sync      = false; 
    
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
                setEvents(D.ODiscard);
            } else if (events.equals("requeue")) {
                setEvents(D.ORequeue);
            } else if (events.equals("retrieve")) {
                setEvents(D.ORetrieve);
            }
        }
        
        String intBels = (String)options.get("intBels");
        if (intBels != null) {
            if (intBels.equals("sameFocus")) {
                setIntBels(D.OSameFocus);
            } else if (intBels.equals("newFocus")) {
            	setIntBels(D.ONewFocus);
            }
        }
        
        String verbose = (String)options.get("verbose");
        if (verbose != null) {
            setVerbose(verbose);
        }

        String sSync = (String)options.get("synchronized");
        if (sSync != null) {
        	if (sSync.equals("true")) {
        		setSync(true);
        	} else {
        		setSync(false);
        	}
        }
    }

	/**
	 * 
	 * @uml.property name="events"
	 */
	public void setEvents(byte opt) {
		events = opt;
	}

	/**
	 * 
	 * @uml.property name="intBels"
	 */
	public void setIntBels(boolean opt) {
		intBels = opt;
	}

    
    public void setVerbose(String opt) {
        try {
            setVerbose( Integer.parseInt(opt));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

	/**
	 * 
	 * @uml.property name="verbose"
	 */
	public void setVerbose(int opt) {
		verbose = opt;
	}
    
    public boolean discard() {
        return events == D.ODiscard;
    }

    public boolean requeue() {
        return events == D.ORequeue;
    }
    
    public boolean retrieve() {
        return events == D.ORetrieve;
    }

    public boolean sameFocus() {
        return(intBels);
    }
    public boolean newFocus() {
        return(!intBels);
    }
    
    public int verbose() {
        return(verbose);
    }
    
    /** returns true if the execution is synchronized */
    public boolean isSync() {
    	return sync;
    }
    
    public void setSync(boolean pSync) {
    	sync = pSync;
    }
}
