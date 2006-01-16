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
//   Revision 1.3  2006/01/16 16:47:35  jomifred
//   added a new kind of console with one tab for agent
//
//   Revision 1.2  2006/01/11 14:52:38  jomifred
//   no message
//
//   Revision 1.1  2006/01/04 02:55:57  jomifred
//   using java log API instead of apache log
//
//   Revision 1.1  2005/12/08 20:14:28  jomifred
//   changes for JasonIDE plugin
//
//   Revision 1.3  2005/08/12 21:08:23  jomifred
//   add cvs keywords
//
//----------------------------------------------------------------------------

package jason.runtime;


import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;

/** Logger handler (redirect output to MASConsoleGUI) */
public class MASConsoleLogHandler extends StreamHandler  {
    
	public static String formaterField = MASConsoleLogHandler.class.getName()+".formatter";
	public static String levelField = MASConsoleLogHandler.class.getName()+".level";
	
	private MASConsoleGUI fGUI;

	public MASConsoleLogHandler() {
		fGUI = MASConsoleGUI.get();
		//setFormatter(new MASConsoleLogFormatter());
		String formatter = LogManager.getLogManager().getProperty(formaterField);
		if (formatter != null) {
			try {
				setFormatter((Formatter) Class.forName(formatter).newInstance());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		String level = LogManager.getLogManager().getProperty(levelField);
		if (level != null) {
			try {
				setLevel(Level.parse(level));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void publish(LogRecord l) {
		if (l.getLoggerName().startsWith("jason") || l.getLoggerName().startsWith("jIDE")) {
			fGUI.append(MASConsoleLogFormatter.getAgName(l), getFormatter().format(l));
		}
	}
}
