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
//   Revision 1.1  2005/12/08 20:14:28  jomifred
//   changes for JasonIDE plugin
//
//   Revision 1.3  2005/08/12 21:08:23  jomifred
//   add cvs keywords
//
//----------------------------------------------------------------------------

package jason.runtime;


import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Layout;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.LoggingEvent;

/** runs a MAS */
public class MASConsoleAppender extends AppenderSkeleton  {
    
	private MASConsoleGUI fGUI;

	public MASConsoleAppender() {
		fGUI = MASConsoleGUI.get();
	}

	public boolean requiresLayout() {
		return true;
	}

	public void append(LoggingEvent event) {
    	// Reminder: the nesting of calls is:
        //
        //    doAppend()
        //      - check threshold
        //      - filter
        //      - append();
        //        - checkEntryConditions();
        //        - subAppend();

        if (!checkEntryConditions()) {
			return;
		}
		subAppend(event);
    }
  
    /**
	 * This method determines if there is a sense in attempting to append.
	 * <p>
	 * It checks whether there is a set output target and also if there is a set
	 * layout. If these checks fail, then the boolean value <code>false</code>
	 * is returned.
	 */
    protected boolean checkEntryConditions() {
		if (this.closed) {
			LogLog.warn("Not allowed to write to a closed appender.");
			return false;
		}
		if (this.layout == null) {
			errorHandler.error("No layout set for the appender named [" + name + "].");
			return false;
		}
		return true;
	}
    
    /**
	 * Actual writing occurs here.
	 */
	protected void subAppend(LoggingEvent event) {

		fGUI.append(this.layout.format(event));

		if (layout.ignoresThrowable()) {
			String[] s = event.getThrowableStrRep();
			if (s != null) {
				int len = s.length;
				for (int i = 0; i < len; i++) {
					fGUI.append(s[i]);
					fGUI.append(Layout.LINE_SEP);
				}
			}
		}
	}

    
    public void close() {
    	super.closed = true;
    }
}
