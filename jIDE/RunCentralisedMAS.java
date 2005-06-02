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
// http://www.csc.liv.ac.uk/~bordini
// http://www.inf.furb.br/~jomi
//----------------------------------------------------------------------------


package jIDE;

import jason.architecture.CentralisedAgArch;
import jason.control.CentralisedExecutionControl;
import jason.environment.CentralisedEnvironment;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Runs a SACI script without SACI.
 * It is used to run the MAS without saci.
 */
public class RunCentralisedMAS {
    
    CentralisedEnvironment env = null;
    CentralisedExecutionControl control = null;
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("You must inform only the MAS XML script.");
            System.exit(1);
        }

        Document docDOM = parse(args[0]);
        if (docDOM != null) {
			RunCentralisedMAS r = new RunCentralisedMAS();
			List lags = r.createAg(docDOM);
            r.startAgs(lags);
			
			r.startSyncMode();   
			r.waitEnd();
        }
    }
    
    public static Document parse(String file) {
        try {
            if (file.startsWith("\"")) {
        			file = file.substring(1, file.length()-1);
            }
        	
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true); // se o parser considera o name space
            
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse( new File( file ) );
        } catch (Exception e) {
            System.err.println("Error parsing the script file");
            e.printStackTrace();
            return null;
        }
    }

	List createAg(Document docDOM) {
	    List ags = new ArrayList();
        
        // create the agentes
        NodeList listAg = docDOM.getElementsByTagName("startAgent");
        for (int i=0; i<listAg.getLength(); i++) {
            Element sAg = (Element)listAg.item(i);

            String agName = sAg.getAttribute("name");
            try {
                if (agName.equals("environment")) {
                    System.out.println("Creating environment "+sAg.getAttribute("class"));
                    env = new CentralisedEnvironment(sAg.getAttribute("class"));
                } else if (agName.equals("controller")) {
                	System.out.println("Creating controller "+sAg.getAttribute("class"));
                	control = new CentralisedExecutionControl(env, sAg.getAttribute("class"), sAg.getAttribute("args"));
                } else {
                    // it is an agent
                    int qty = 1;
                    try {
                        qty = Integer.parseInt(sAg.getAttribute("qty"));
                    } catch (Exception e) {}
                    String className = sAg.getAttribute("class");
                    String[] agArgs = saci.launcher.AgentType.getArrayFromString(sAg.getAttribute("args"));
                    for (int cAg=0; cAg<qty; cAg++) {
                        String numberedAg = agName;
                        if (qty > 1) {
                            numberedAg += (cAg+1);
                        }
                        System.out.println("Creating agent "+numberedAg+" ("+(cAg+1)+"/"+qty+") from "+className);
                        CentralisedAgArch agArch = (CentralisedAgArch)Class.forName(className).newInstance();
                        agArch.initAg(agArgs);
                        agArch.setEnv(env);
                        agArch.setName(numberedAg);
                        env.addAgent(agArch);
                        ags.add(agArch);
                    }
                }
                
            } catch (Exception e) {
                System.err.println("Error creating agent "+agName);
                e.printStackTrace();
                System.exit(0);
            }
        } // for
		return ags;
	}

	void startAgs(List ags) {
        // run the agents
        Iterator i = ags.iterator();
        while (i.hasNext()) {
            CentralisedAgArch ag = (CentralisedAgArch)i.next();
            ag.setControl(control);
            ag.start();
        }
	}
	
	void startSyncMode() {
        if (control != null) {
            // start the execution, if it is controlled
    		try {
				Thread.sleep(500); // gives a time to agents enter in wait					
				control.informAllAgToPerformCycle();
    		} catch (Exception e) {
    			e.printStackTrace();
            }	
        }		
	}
	
	void waitEnd() {
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			String line = in.readLine();
			while (! line.equals("quit")) {
				line = in.readLine();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}
}
