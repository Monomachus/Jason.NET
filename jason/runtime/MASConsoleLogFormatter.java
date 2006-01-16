//----------------------------------------------------------------------------
// Copyright (C) 2003  Rafael H. Bordini and Jomi F. Hubner
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
//   Revision 1.2  2006/01/16 16:47:35  jomifred
//   added a new kind of console with one tab for agent
//
//   Revision 1.1  2006/01/04 02:55:57  jomifred
//   using java log API instead of apache log
//
//   Revision 1.1  2005/12/08 20:14:28  jomifred
//   changes for JasonIDE plugin
//
//   Revision 1.19  2005/11/16 18:35:25  jomifred
//   fixed the print(int) on console bug
//
//   Revision 1.18  2005/11/07 12:41:31  jomifred
//   no message
//
//   Revision 1.17  2005/10/30 18:39:48  jomifred
//   change in the AgArch customisation  support (the same customisation is used both to Cent and Saci infrastructures0
//
//   Revision 1.16  2005/10/19 21:41:51  jomifred
//   fixed the bug  continue/stop when running the MAS
//
//   Revision 1.15  2005/09/20 16:59:14  jomifred
//   do not use MASConsole when the logger in Console (and so, do not need an X11)
//
//   Revision 1.14  2005/08/12 21:08:23  jomifred
//   add cvs keywords
//
//----------------------------------------------------------------------------


package jason.runtime;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.LogRecord;

/**
 * Default formatter for Jason output.
 */
public class MASConsoleLogFormatter extends java.util.logging.Formatter {
	public String format(LogRecord l) {
		StringBuffer s = new StringBuffer("[");
		s.append(getAgName(l));
		s.append("] ");
		s.append(l.getMessage());
		if (l.getThrown() != null) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			l.getThrown().printStackTrace(pw);
			s.append("\n"+sw);
		}
		s.append("\n");
		return s.toString();
	}
	
	public static String getAgName(LogRecord l) {
		String lname = l.getLoggerName();
		int posd = lname.lastIndexOf(".");
		if (posd > 0) {
			return lname.substring(posd+1);
		}
		return null;
	}
}
