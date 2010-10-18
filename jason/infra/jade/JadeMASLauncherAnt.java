package jason.infra.jade;

import jason.asSyntax.ASSyntax;
import jason.asSyntax.StringTerm;
import jason.asSyntax.parser.ParseException;
import jason.infra.centralised.CentralisedMASLauncherAnt;
import jason.jeditplugin.Config;
import jason.jeditplugin.MASLauncherInfraTier;
import jason.mas2j.AgentParameters;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Creates the script build.xml to launch the MAS using JADE.
 */
public class JadeMASLauncherAnt extends CentralisedMASLauncherAnt implements MASLauncherInfraTier {

    public static String snifferConfFile       = "sniffer.properties";
    public static String customSnifferConfFile = "c-sniffer.properties";
    //private static Logger logger = Logger.getLogger(JadeMASLauncherAnt.class.getName());

    protected String replaceMarks(String script, boolean debug) {
        // create sniffer file
        File sFile  = new File(project.getDirectory()+File.separator+snifferConfFile);
        File csFile = new File(project.getDirectory()+File.separator+customSnifferConfFile);
        try {
            if (csFile.exists()) {
                BufferedReader in = new BufferedReader(new FileReader(csFile));
                BufferedWriter out = new BufferedWriter(new FileWriter(sFile));
                String line;
                while ( (line=in.readLine()) != null) {
                    out.write(line+"\n");
                }
                out.close();
            } else {
                sFile.delete();
                if (Config.get().getBoolean(Config.JADE_SNIFFER)) {
                    PrintWriter out = new PrintWriter(new FileWriter(sFile));
                    out.print("preload=");
                    Iterator<AgentParameters> i = project.getAgents().iterator();
                    while (i.hasNext()) {
                        AgentParameters ap = i.next();
                        out.print(ap.name);
                        if (i.hasNext()) out.print(";");
                    }
                    out.println();
                    out.close();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        // replace build.xml tags
        String jadeJar = Config.get().getJadeJar();
        if (!Config.checkJar(jadeJar)) {
            System.err.println("The path to the jade.jar file (" + jadeJar + ") was not correctly set. Go to menu Plugin->Options->Jason to configure the path.");
        }

        script = replace(script, "<PROJECT-RUNNER-CLASS>", RunJadeMAS.class.getName());

        String jadepath = "\t<pathelement location=\"" + Config.get().getJadeJar() + "\"/>";
        try {
            String http = new File(Config.get().getJadeJar()).getAbsoluteFile().getParent() + "/http.jar";
            jadepath += "\n\t<pathelement location=\"" + http + "\"/>";
        } catch (Exception _) {}
        try {
            String tools = new File(Config.get().getJadeJar()).getAbsoluteFile().getParent() + "/jadeTools.jar";
            jadepath += "\n\t<pathelement location=\"" + tools + "\"/>";
        } catch (Exception _) {}
        try {
            String jar = new File(Config.get().getJadeJar()).getAbsoluteFile().getParent() + "/commons-codec-1.3.jar";
            jadepath += "\n\t<pathelement location=\"" + jar + "\"/>";
        } catch (Exception _) {}
        
        
        script = replace(script, "<PATH-LIB>", jadepath + "\n\t<PATH-LIB>");
        
        String startContainers = 
            "    <target name=\"main-container\" depends=\"compile\" >\n" 
          + "        <echo message=\"Starting JADE Main-Container\" />\n"
          + "        <java classname=\"jason.infra.jade.RunJadeMAS\" failonerror=\"true\" fork=\"yes\" dir=\"${basedir}\" >\n"
          + "            <classpath refid=\"project.classpath\"/>\n"
          + "            <arg line=\"${mas2j.project.file} -container-name Main-Container "+Config.get().getJadeArgs()+"\"/>\n"
          + "          <jvmarg line=\"-Xmx500M -Xss8M\"/>\n"
          + "        </java>\n"
          + "    </target>\n\n";
        
        // collect containers
        Set<String> containers = new HashSet<String>();
        for (AgentParameters ap: project.getAgents()) {
            if (ap.getHost() != null && !ap.getHost().isEmpty() && !ap.getHost().equals("Main-Container"))
                containers.add(ap.getHost());
        }

        String mainHost;
        mainHost = project.getInfrastructure().getParameter("main_container_host");
        if (mainHost == null) {
            mainHost = "localhost";
        } else {
            try {
                mainHost = ((StringTerm)ASSyntax.parseLiteral(mainHost).getTerm(0)).getString();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        for (String container: containers) {
            StringBuilder agents = new StringBuilder();
            String sep = " ";
            for (AgentParameters ap: project.getAgents()) {
                if (ap.getHost() != null && ap.getHost().equals(container)) {
                    for (int cAg = 0; cAg < ap.qty; cAg++) {
                        String numberedAg = ap.getAgName();
                        if (ap.qty > 1)
                            numberedAg += (cAg + 1); //String.format("%0"+String.valueOf(ap.qty).length()+"d", cAg + 1);
                        agents.append(sep+numberedAg+":jason.infra.jade.JadeAgArch(j-project,"+project.getProjectFile().getName()+","+ap.getAgName()+")");
                        sep = ";";
                    }                    
                }
            }
            startContainers += 
                "    <target name=\""+container+"\" depends=\"compile\" >\n" +
                "        <echo message=\"Starting JADE Container "+container+"\" />\n"+
                "        <java classname=\"jade.Boot\" failonerror=\"true\" fork=\"yes\" dir=\"${basedir}\" >\n"+
                "            <classpath refid=\"project.classpath\"/>\n"+
                "            <arg line=\"-container -host "+mainHost+" -container-name "+container+" "+agents+"\"/>\n"+
                "            <jvmarg line=\"-Xmx500M -Xss8M\"/>\n"+    
                "        </java>\n"+
                "    </target>\n\n";
        }  

        script = replace(script, "<OTHER-TASK>", startContainers);

        return super.replaceMarks(script, debug);
    }
}
