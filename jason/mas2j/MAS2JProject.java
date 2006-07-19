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
//   Revision 1.14  2006/03/02 13:33:41  jomifred
//   changes in MASLauncher interface
//
//   Revision 1.13  2006/03/02 03:30:36  jomifred
//   no message
//
//   Revision 1.12  2006/03/02 02:52:15  jomifred
//   some changes in MASLauncher
//
//   Revision 1.11  2006/03/02 01:42:14  jomifred
//   the jIDE package was remove, the writeScriptInterface's methods was moved to MASLauncher
//
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
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a MAS2J project (usually created from a .mas2j file)
 */
public class MAS2JProject {

	public static final String EXT       = "mas2j";
	public static final String AS_EXT    = "asl";
	
	private static Logger logger = Logger.getLogger(MAS2JProject.class.getName());
		
	String soc;

	ClassParameters envClass = null; 
	
    ClassParameters controlClass = null;
	
	String infrastructure = "Centralised";

	String projectDir = "." + File.separator;
	File   projectFile = null;
	
	List<AgentParameters> agents = new ArrayList<AgentParameters>();
	
    
    public static MAS2JProject parse(String file) {
        try {
            jason.mas2j.parser.mas2j parser = new jason.mas2j.parser.mas2j(new FileReader(file));
            return parser.mas();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error parsing mas2j file.", e);
            return null;
        }
    }
    
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
	
	public void setInfrastructure(String a) {
		infrastructure = a;
	}
	public String getInfrastructure() {
		return infrastructure;
	}

	public void setEnvClass(ClassParameters e) {
		envClass = e;
	}
	public ClassParameters getEnvClass() {
		return envClass;
	}
	
	public void setSocName(String s) {
		soc = s;
	}

	public String getSocName() {
		return soc;
	}

	public void setControlClass(ClassParameters sControl) {
		controlClass = sControl;
	}
	public ClassParameters getControlClass() {
		return controlClass;
	}

	public void initAgMap() {
		agents = new ArrayList<AgentParameters>();
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
	
	public List<AgentParameters> getAgents() {
		return agents;
	}
	
	public Set<File> getAllASFiles() {
		Set<File> files = new HashSet<File>();
		for (AgentParameters agp: agents) {
			if (agp.asSource != null) {
				String dir = projectDir + File.separator;
				if (agp.asSource.toString().startsWith(File.separator)) {
					dir = "";
				}
				files.add(new File(dir + agp.asSource));
			}
		}
		return files;
	}
	
	public String toString() {
		StringBuffer s = new StringBuffer("MAS " + getSocName() + " {\n");
		s.append("   infrastructure: "+getInfrastructure()+"\n");
		s.append("   environment: "+getEnvClass());
		if (envClass.host != null) {
			s.append(" at "+envClass.host);
		}
		s.append("\n");
		
		if (getControlClass() != null) {
			s.append("   executionControl: "+getControlClass());
			if (getControlClass().host != null) {
				s.append(" at "+getControlClass().host);
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
	
}
