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
//   Revision 1.4  2006/03/01 19:22:05  jomifred
//   no message
//
//   Revision 1.3  2006/02/21 14:24:37  jomifred
//   add TODO
//
//   Revision 1.2  2006/02/18 15:24:50  jomifred
//   changes in many files to detach jason kernel from any infrastructure implementation
//
//   Revision 1.1  2006/01/14 15:18:58  jomifred
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

package jason.jeditplugin;

import jason.mas2j.MAS2JProject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Set;

// TODO: when using jdk 1.5, change RunProcess to ProcessBuilder as
// explained at http://java.sun.com/developer/JDCTechTips/2005/tt0727.html#1

// TODO: when using jdk 1.6 use API suport to compile.

/** runs an MAS */
public class RunProject {

	MASLauncher masLauncher;

	public void run(MAS2JProject project, RunProjectListener listener) {
		try {
			String jasonJar = Config.get().getJasonJar();
			if (!Config.checkJar(jasonJar)) {
				System.err.println("The path to the jason.jar file ("+jasonJar+") was not correctly set, the MAS may not run. Go to menu Plugins->Plugins Options->Jason to configure the path.");
			}
			String javaHome = Config.get().getJavaHome();
			if (!Config.checkJavaHomePath(javaHome)) {
				System.err.println("The Java home directory ("+javaHome+") was not correctly set, the MAS may not run. Go to the Plugins->Options->Jason menu to configure the path.");
			}
			
			// compile some files
			CompileThread compT = new CompileThread(project.getAllUserJavaFiles(), project);
			compT.start();
			if (masLauncher != null) {
				masLauncher.stopMAS();
			}
			if (!compT.waitCompilation()) {
				return;
			}

			masLauncher = project.getInfrastructureFactory().createMASLauncher();
			masLauncher.setProject(project);
			masLauncher.setListener(listener);
			masLauncher.start();
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void stopMAS() {
		if (masLauncher != null) {
			masLauncher.stopMAS();
		}
		masLauncher = null;
	}


	class CompileThread extends Thread {
		private boolean ok = true;
		private boolean finished = false;
		private Set files;
		MAS2JProject project;

		CompileThread(Set files, MAS2JProject project) {
			super("CompileThread");
			this.files = files;
			this.project = project;
		}

		synchronized boolean waitCompilation() {
			while (!finished) {
				try {
					wait(2000); // waits 2 seconds
				} catch (Exception e) {
				}
			}
			return ok;
		}

		synchronized void stopWaiting() {
			notifyAll();
		}

		public void run() {
			try {
				if (needsComp()) {
					String command = MASLauncher.getAsScriptCommand("compile-" + project.getSocName());
					System.out.println("Compiling user class with " + command);
					Process p = Runtime.getRuntime().exec(command, null, new File(project.getDirectory()));
					p.waitFor();
					ok = !needsComp();
					if (!ok) {
							BufferedReader in = new BufferedReader(new InputStreamReader(p.getErrorStream()));
							while (in.ready()) {
								System.out.println(in.readLine());
								//jasonID.output.append(in.readLine() + "\n");
							}
					}
				}
			} catch (Exception e) {
				System.err.println("Compilation error: " + e);
			} finally {
				finished = true;
				stopWaiting();
			}
		}
		
		boolean needsComp() {
			Iterator ifiles = files.iterator();
			while (ifiles.hasNext()) {
				String file = (String) ifiles.next();

				File javaF = new File(project.getDirectory()
						+ File.separatorChar + file + ".java");
				File classF = new File(project.getDirectory()
						+ File.separatorChar + file + ".class");
				//System.out.println(classF.lastModified()+" > "+javaF.lastModified());
				if (javaF.exists() && !classF.exists()) {
					return true;
				} else if (javaF.exists() && classF.exists()) {
					if (classF.lastModified() < javaF.lastModified()) {
						return true;
					}
				}
			}
			return false;
		}
	}

}
