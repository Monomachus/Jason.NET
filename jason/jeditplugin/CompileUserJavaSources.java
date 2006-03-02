package jason.jeditplugin;

import jason.mas2j.MAS2JProject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Set;

public class CompileUserJavaSources  extends Thread {
	private boolean ok = true;
	private boolean finished = false;
	private Set files;
	MAS2JProject project;

	CompileUserJavaSources(Set files, MAS2JProject project) {
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
