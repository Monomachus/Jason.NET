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
//   Revision 1.3  2005/12/09 14:47:40  jomifred
//   no message
//
//   Revision 1.2  2005/12/08 20:06:59  jomifred
//   changes for JasonIDE plugin
//
//
//----------------------------------------------------------------------------

package jason.jeditplugin;


import org.gjt.sp.jedit.EBMessage;
import org.gjt.sp.jedit.EBPlugin;
import org.gjt.sp.jedit.gui.DockableWindowManager;

public class JasonIDPlugin extends EBPlugin {
	public static final String NAME = "jason";
    public static final String MENU = "jason.menu";
    public static final String PROPERTY_PREFIX = "plugin.jason.";
    public static final String OPTION_PREFIX   = "options.jason.";


    public void handleMessage(EBMessage msg) {
    	if (org.gjt.sp.jedit.jEdit.getViews().length > 0) {
	    	DockableWindowManager d = org.gjt.sp.jedit.jEdit.getViews()[0].getDockableWindowManager();
	    	if (!d.isDockableWindowVisible(NAME)) {
	    		d.addDockableWindow(NAME);
	        }
    	}
	}

    /*
    public void createMenuItems(Vector menuItems) {
        menuItems.addElement(GUIUtilities.loadMenu(MENU));
    }
    
    public void createOptionPanes(OptionsDialog od) {
		od.addOptionPane(new JasonOptionPanel());
    }
    */

}
