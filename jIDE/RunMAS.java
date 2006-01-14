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
//   Revision 1.29  2006/01/14 15:20:22  jomifred
//   Config and some code of RunMAS was moved to package plugin
//
//   Revision 1.28  2006/01/06 12:05:37  jomifred
//   operator - removes bel from BB and changes the current unifier.
//
//   Revision 1.27  2006/01/04 02:54:41  jomifred
//   using java log API instead of apache log
//
//   Revision 1.26  2005/12/30 20:40:16  jomifred
//   new features: unnamed var, var with annots, TE as var
//
//   Revision 1.23  2005/12/08 20:05:01  jomifred
//   changes for JasonIDE plugin
//
//   Revision 1.20  2005/11/17 20:11:50  jomifred
//   fix a bug in openning a project
//
//   Revision 1.17  2005/10/29 21:46:22  jomifred
//   add a new class (MAS2JProject) to store information parsed by the mas2j parser. This new class also create the project scripts
//
//   Revision 1.16  2005/09/20 17:00:26  jomifred
//   load classes from the project lib directory
//
//   Revision 1.15  2005/08/14 23:29:40  jomifred
//   use new java class to run process
//
//   Revision 1.14  2005/08/12 23:29:11  jomifred
//   support for saci arch in IA createAgent
//
//   Revision 1.13  2005/08/12 21:08:23  jomifred
//   add cvs keywords
//
//----------------------------------------------------------------------------

package jIDE;

import jason.jeditplugin.RunProject;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

/** runs an MAS */
public class RunMAS extends AbstractAction {

	JasonID jasonID = null;
	RunProject runProject;
	
	public RunMAS(JasonID jID) {
		super("Run MAS...", new ImageIcon(JasonID.class.getResource("/images/execute.gif")));
		jasonID = jID;
		runProject = new RunProject(jID);
	}

	public void actionPerformed(ActionEvent e) {
		try {
			jasonID.output.setText("");
			jasonID.saveAllAct.actionPerformed(null);
			if (jasonID.fMAS2jThread.foregroundCompile() && jasonID.fASParser.foregroundCompile()) {
				// the foregroun do it. jasonID.openAllASFiles(jasonID.fMAS2jThread.fCurrentProject.getAllASFiles());
				jasonID.runMASButton.setEnabled(false);
				jasonID.debugMASButton.setEnabled(false);
				jasonID.stopMASButton.setEnabled(true);
				runProject.run(jasonID.fMAS2jThread.fCurrentProject);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
