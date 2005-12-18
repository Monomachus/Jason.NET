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
//   Revision 1.7  2005/12/18 15:31:02  jomifred
//   no message
//
//   Revision 1.6  2005/12/17 19:51:58  jomifred
//   no message
//
//   Revision 1.5  2005/12/17 19:28:47  jomifred
//   no message
//
//   Revision 1.4  2005/12/16 22:09:20  jomifred
//   no message
//
//   Revision 1.3  2005/12/09 14:47:40  jomifred
//   no message
//
//   Revision 1.2  2005/12/08 20:06:59  jomifred
//   changes for JasonIDE plugin
//
//
//----------------------------------------------------------------------------

package jason.jeditplugin;


import jason.mas2j.MAS2JProject;

import javax.swing.SwingUtilities;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.gjt.sp.jedit.EBMessage;
import org.gjt.sp.jedit.EBPlugin;
import org.gjt.sp.jedit.gui.DockableWindowManager;
import org.gjt.sp.jedit.msg.BufferUpdate;
import org.gjt.sp.util.Log;

import sidekick.SideKickPlugin;

public class JasonIDPlugin extends EBPlugin {
	public static final String NAME = "jason";
    public static final String MENU = "jason.menu";
    public static final String PROPERTY_PREFIX = "plugin.jason.";
    public static final String OPTION_PREFIX   = "options.jason.";

    static {
		Logger.getRootLogger().addAppender(new ConsoleAppender(new PatternLayout("[%c{1}] %m%n")));
		Logger.getRootLogger().setLevel(Level.INFO);    	
    }
    
    public void handleMessage(EBMessage msg) {
    	if (org.gjt.sp.jedit.jEdit.getViews().length > 0) {
	    	final DockableWindowManager d = org.gjt.sp.jedit.jEdit.getViews()[0].getDockableWindowManager();
	    	if (d.getDockableWindow(NAME) == null) {
	    		/*
	    		if (!d.isDockableWindowVisible(NAME)) {
	    			d.addDockableWindow(NAME);
	    			//d.floatDockableWindow(NAME);
	    	    	//Log.log(Log.DEBUG,this,"Add to dock");
	    			//JasonID jid = (JasonID)d.getDockableWindow(NAME);
	    			//jid.start();
	    		}
	        } else {
	        */
    			SwingUtilities.invokeLater(new Thread() {
    				public void run() {
    					d.addDockableWindow(NAME);
    				}
    			});
	        }
    	}
    	
    	if (msg != null && msg instanceof BufferUpdate) {
        	BufferUpdate bu = (BufferUpdate)msg;
        	if ((bu.getWhat() == BufferUpdate.LOADED || bu.getWhat() == BufferUpdate.CREATED)) {
        		if (bu.getBuffer().getPath().endsWith(MAS2JProject.EXT)) {
        			bu.getBuffer().setProperty("sidekick.parser", JasonProjectSideKickParser.ID);
        		}
    			if (bu.getBuffer().getPath().endsWith(MAS2JProject.AS_EXT)) {
    				bu.getBuffer().setProperty("sidekick.parser", AgentSpeakSideKickParser.ID);
    			}
            }
    	}
	}
    
    AgentSpeakSideKickParser asskp = new AgentSpeakSideKickParser(); 
    JasonProjectSideKickParser jpskp = new JasonProjectSideKickParser();
    
	public void start() {
    	SideKickPlugin.registerParser(asskp);
    	SideKickPlugin.registerParser(jpskp);
    	Log.log(Log.DEBUG,this,"Registered "+asskp);
    	Log.log(Log.DEBUG,this,"Registered "+jpskp);
    	
    	handleMessage(null);
    } 
	
	public void stop() {
		SideKickPlugin.unregisterParser(asskp);
		SideKickPlugin.unregisterParser(jpskp);
	}
}
