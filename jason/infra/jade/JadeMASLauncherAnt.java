package jason.infra.jade;

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
import java.util.Iterator;

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

        return super.replaceMarks(script, debug);
    }
}
