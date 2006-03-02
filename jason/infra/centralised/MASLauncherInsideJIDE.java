package jason.infra.centralised;

import jason.jeditplugin.Config;
import jason.jeditplugin.JasonID;
import jason.jeditplugin.MASLauncher;
import jason.mas2j.MAS2JProject;
import jason.runtime.MASConsoleGUI;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class MASLauncherInsideJIDE extends MASLauncher {
	Method getLauncherMethod = null;
	Method finishMethod = null;
	
	MASLauncherInsideJIDE(MAS2JProject project) {
		super(project);
	}
	
	public void stopMAS() {
		try {
			if (getLauncherMethod != null && finishMethod != null) {
				finishMethod.invoke(getLauncherMethod.invoke(null,null),null);
			}
		} catch (Exception e) {
			System.err.println("Execution error: " + e);
			e.printStackTrace();
		}

		super.stopMAS();
	}

	public void run() {
		try {
			File fXML = new File(project.getDirectory() + File.separator + project.getSocName()+".xml");
			System.out.println("Running MAS with "+fXML.getAbsolutePath());
			// create a new RunCentralisedMAS (using my class loader to not cache user classes and to find user project directory)
			Class rmas = new MASClassLoader(project.getDirectory()).loadClass(RunCentralisedMAS.class.getName());
			Class[] classParameters = { (new String[2]).getClass() };
			Method executeMethod = rmas.getDeclaredMethod("main", classParameters);
			classParameters = new Class[0];
			getLauncherMethod = rmas.getDeclaredMethod("getRunner", classParameters);
			finishMethod = rmas.getDeclaredMethod("finish", classParameters);
			
			Object objectParameters[] = { new String[] {fXML.getAbsolutePath(), "insideJIDE"} };
			// Static method, no instance needed
			executeMethod.invoke(null, objectParameters);
			//((RunCentralisedMAS)rmas.newInstance()).main(new String[] {fXML.getAbsolutePath(), "insideJIDE"});

			stop = false;
			while (!stop) {
				sleep(250); // to not consume cpu
				if (getLauncherMethod.invoke(null, null) == null) {
					stop = true;
				}
			}
		} catch (Exception e) {
			System.err.println("Execution error: " + e);
			e.printStackTrace();
		} finally {
			if (listener != null) {
				listener.masFinished();
			}
			getLauncherMethod = null;
			finishMethod = null;
		}
	}

	public void writeScripts(boolean debug) {
	}	


	
	class MASClassLoader extends ClassLoader {
		
		String MASDirectory;
		JarFile jf;
		List libDirJars = new ArrayList();
		
		MASClassLoader(String masDir) {
			MASDirectory = masDir;
			try {
				jf = new JarFile(Config.get().getJasonJar());
				
				// create the jars from application lib directory
				if (MASDirectory != null) {
					File lib = new File(MASDirectory + File.separator + "lib");
					// add all jar files in lib dir
					if (lib.exists()) {
						File[] fs = lib.listFiles();
						for (int i=0; i<fs.length; i++) {
							if (fs[i].getName().endsWith(".jar")) {
								libDirJars.add(new JarFile(fs[i].getAbsolutePath()));
							}
						}
					}
					
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		public Class loadClass(String name) throws ClassNotFoundException {
			//System.out.println("loadClass " + name);
			if (! name.equals(MASConsoleGUI.class.getName())) { // let super loader to get MASConsoleGUI, since it must be shared between RunCentMAS and jIDE
				if (! name.equals(JasonID.class.getName())) { // let super loader to get MASConsoleGUI, since it must be shared between RunCentMAS and jIDE
					if (name.startsWith("jason.") || name.startsWith("jIDE.")) {
						//System.out.println("loading new ");
						Class c = findClassInJar(jf, name);
						if (c == null) {
							System.out.println("does not find class "+name+" in jason.jar");
						} else {
							return c;
						}
					}
				}
			}
			return super.loadClass(name);
		}

		public Class findClass(String name) {
			byte[] b = findClassBytes(MASDirectory, name);
			if (b != null) {
				//System.out.println(name + " found ");
				return defineClass(name, b, 0, b.length);
			} else {
				// try in lib dir
				Iterator i = libDirJars.iterator();
				while (i.hasNext()) {
					JarFile j = (JarFile)i.next();
					Class c = findClassInJar(j, name);
					if (c != null) {
						return c;
					}
				}
				System.err.println("Error loading class "+name);
				return null;
			}
		}

		public byte[] findClassBytes(String dir, String className) {
			try {
				String pathName = dir + File.separatorChar	+ className.replace('.', File.separatorChar) + ".class";
				FileInputStream inFile = new FileInputStream(pathName);
				byte[] classBytes = new byte[inFile.available()];
				inFile.read(classBytes);
				return classBytes;
			} catch (java.io.IOException ioEx) {
				//ioEx.printStackTrace();
				return null;
			}
		}

		public Class findClassInJar(JarFile jf, String className) {
			try {
				if (jf == null) 
					return null;
				JarEntry je = jf.getJarEntry(className.replace('.', '/') + ".class"); // must be '/' since inside jar / is used not \
				if (je == null) {
					return null;
				}
				InputStream in = new BufferedInputStream(jf.getInputStream(je));
				//System.out.println(c.getResource("/"+className.replace('.', File.separatorChar) + ".class"));
				//InputStream in = new BufferedInputStream(c.getResource("/"+className.replace('.', File.separatorChar) + ".class").openStream());
				byte[] classBytes = new byte[in.available()];
				in.read(classBytes);
				//System.out.println(" found "+className+ " size="+classBytes.length);
				return defineClass(className, classBytes, 0, classBytes.length);
			} catch (Exception e) {
				System.err.println("Error loading class "+className);
				e.printStackTrace();
				return null;
			}
		}
	}


}
