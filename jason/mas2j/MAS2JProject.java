// ----------------------------------------------------------------------------
// Copyright (C) 2003 Rafael H. Bordini, Jomi F. Hubner, et al.
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//
// To contact the authors:
// http://www.dur.ac.uk/r.bordini
// http://www.inf.furb.br/~jomi
//
// CVS information:
//   $Date$
//   $Revision$
//   $Log$
//   Revision 1.10  2006/02/18 15:25:16  jomifred
//   changes in many files to detach jason kernel from any infrastructure implementation
//
//   Revision 1.9  2006/01/16 16:47:35  jomifred
//   added a new kind of console with one tab for agent
//
//   Revision 1.8  2006/01/14 18:23:22  jomifred
//   centralised infra does not use xml script file anymore
//
//   Revision 1.7  2006/01/14 15:25:52  jomifred
//   Config and some code of RunMAS was moved to package plugin
//
//   Revision 1.6  2006/01/04 03:00:46  jomifred
//   using java log API instead of apache log
//
//   Revision 1.1  2005/12/08 20:13:53  jomifred
//   changes for JasonIDE plugin
//
//   Revision 1.2  2005/10/30 18:39:48  jomifred
//   change in the AgArch customisation  support (the same customisation is used both to Cent and Saci infrastructures0
//
//   Revision 1.1  2005/10/29 21:46:22  jomifred
//   add a new class (MAS2JProject) to store information parsed by the mas2j parser. This new class also create the project scripts
//
//
//----------------------------------------------------------------------------

package jason.mas2j;

import jason.JasonException;
import jason.infra.InfrastructureFactory;
import jason.jeditplugin.Config;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Represents a MAS2J project (usually created from a .mas2j file)
 */
public class MAS2JProject {

	public static final String EXT       = "mas2j";
	public static final String AS_EXT    = "asl";
	
	private Logger logger = Logger.getLogger(MAS2JProject.class.getName());
		
	String soc;

	String envClass = null; 
	String envHost = null;
	
	String controlClass = null;
	String controlHost = null;
	
	String infrastructure = "Centralised";

	String projectDir = "." + File.separator;
	File   projectFile = null;
	
	List agents = new ArrayList();
	
	public void setDirectory(String d) {
		if (d != null) {
			projectDir = d;
			if (projectDir.length() > 0) {
				if (!projectDir.endsWith(File.separator)) {
					projectDir += File.separator;
				}
			}
		}
	}
	public String getDirectory() {
		return projectDir;
	}
	
	public void setProjectFile(File f) {
		projectFile = f;
	}
	
	public File getProjectFile() {
		return projectFile;
	}
	
	public String getXmlScriptFile() {
		return projectDir + soc + ".xml";
	}

	public void setInfrastructure(String a) {
		infrastructure = a;
	}
	public String getInfrastructure() {
		return infrastructure;
	}

	public void setEnvClass(String e) {
		envClass = e;
	}
	public String getEnvClass() {
		return envClass;
	}
	
	public void setEnvHost(String h) {
		envHost = h;
	}
	
	public String getEnvHost() {
		return envHost;
	}

	public void setSocName(String s) {
		soc = s;
	}

	public String getSocName() {
		return soc;
	}

	public void setControlClass(String sControl) {
		controlClass = sControl;
	}
	public String getControlClass() {
		return controlClass;
	}
	public void setControlHost(String h) {
		controlHost = h;
	}
	public String getControlHost() {
		return controlHost;
	}

	public void initAgMaps() {
		agents = new ArrayList();
	}
	public void addAgent(AgentParameters a) {
		agents.add(a);
	}
	public AgentParameters getAg(String name) {
		Iterator i = agents.iterator();
		while (i.hasNext()) {
			AgentParameters a = (AgentParameters)i.next();
			if (a.name.equals(name)) {
				return a;
			}
		}
		return null;
	}
	
	public List getAgents() {
		return agents;
	}
	
	public Set getAllASFiles() {
		Set files = new HashSet();
		Iterator iag = agents.iterator();
		while (iag.hasNext()) {
			AgentParameters agp = (AgentParameters)iag.next();
			if (agp.asSource != null) {
				files.add(new File(projectDir + File.separator + agp.asSource));
			}
		}
		return files;
	}
	
	public Set getAllUserJavaFiles() {
		Set files = new HashSet();
		Iterator iag = agents.iterator();
		while (iag.hasNext()) { 
			AgentParameters agp = (AgentParameters)iag.next();
			if (agp.agClass != null) files.add(agp.agClass.replace('.', '/'));
			if (agp.archClass != null) files.add(agp.archClass.replace('.', '/'));
		}
		if (getEnvClass() != null) {
			files.add(getEnvClass().replace('.', '/'));
		}
		return files;
	}

	public Set getAllUserJavaDirectories() {
		Set directories = new HashSet();
		Iterator ifiles = getAllUserJavaFiles().iterator();
		while (ifiles.hasNext()) {
			String dir = new File(ifiles.next() + ".java").getParent();
			if (dir == null) { // no parent
				dir = ".";
			}
			directories.add(dir);
		}
		return directories;
	}

	public String getFullClassPath() {
		String clPath = "\"$CLASSPATH\"";
		String indelim = "\"";
		String outdelim = "";
		if (System.getProperty("os.name").indexOf("indows") > 0) {
			clPath = "%CLASSPATH%";
			indelim = "";
			outdelim = "\"";
		}

		String dDir = projectDir;
		if (dDir.endsWith(File.separator)) {
			dDir = dDir.substring(0, dDir.length() - 1);
		}

		String sLib = "";
		File lib = new File(dDir + File.separator + "lib");
		// add all jar files in lib dir
		if (lib.exists()) {
			File[] fs = lib.listFiles();
			for (int i = 0; i < fs.length; i++) {
				if (fs[i].getName().endsWith(".jar")) {
					sLib += indelim + fs[i].getAbsolutePath() + indelim
							+ File.pathSeparator;
				}
			}
		}

		return outdelim + "." + File.pathSeparator + indelim + Config.get().getJasonJar() + indelim
				//+ saciJar
				//+ File.pathSeparator + indelim + Config.get().getProperty(Config.LOG4J_JAR) + indelim
				+ File.pathSeparator + indelim + dDir + indelim
				+ File.pathSeparator + sLib + clPath + outdelim;
	}


	public String toString() {
		StringBuffer s = new StringBuffer("MAS " + getSocName() + " {\n");
		s.append("   infrastructure: "+getInfrastructure()+"\n");
		s.append("   environment: "+getEnvClass());
		if (envHost != null) {
			s.append(" at "+envHost);
		}
		s.append("\n");
		
		if (getControlClass() != null) {
			s.append("   executionControl: "+getControlClass());
			if (controlHost != null) {
				s.append(" at "+controlHost);
			}
			s.append("\n");
		}
		
		// agents
		s.append("   agents:\n");
		Iterator i = agents.iterator();
		while (i.hasNext()) {
			s.append("       "+i.next());
			s.append(";\n");
		}
		s.append("}");
		return s.toString();
	}

	private InfrastructureFactory infraFac = null; // backup 
	public InfrastructureFactory getInfrastructureFactory() throws JasonException {
		if (infraFac == null) {
			try {
				String facClass = Config.get().getInfrastructureFactoryClass(infrastructure);
				infraFac = (InfrastructureFactory)Class.forName(facClass).newInstance();
			} catch (Exception e) { 
				throw new JasonException("The project's infrastructure ('"+infrastructure+"') is unknown!");
			}
		}
		return infraFac;
	}
	
	/** write the scrits that run the MAS in the chosen infrastructure */
	public void writeScripts(boolean debug)  throws JasonException {
		getInfrastructureFactory().createWriteScripts().writeScripts(this, debug);
	}
}
