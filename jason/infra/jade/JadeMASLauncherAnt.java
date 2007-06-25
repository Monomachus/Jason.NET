package jason.infra.jade;

import jason.infra.centralised.CentralisedMASLauncherAnt;
import jason.jeditplugin.Config;
import jason.jeditplugin.MASLauncherInfraTier;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Creates the script build.xml to launch the MAS using JADE.
 */
public class JadeMASLauncherAnt extends CentralisedMASLauncherAnt implements MASLauncherInfraTier {

    private static Logger logger = Logger.getLogger(JadeMASLauncherAnt.class.getName());

    public void stopMAS() {
        new Thread() {
            public void run() {
                try {
                    // TODO:
                    //new SaciRuntimeServices(project.getSocName()).stopMAS();
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Error stoping saci MAS", e);
                }
            }
        }.start();
    }

    protected String replaceMarks(String script, boolean debug) {
        String jadeJar = Config.get().getJadeJar();
        if (!Config.checkJar(jadeJar)) {
            System.err.println("The path to the jade.jar file (" + jadeJar + ") was not correctly set. Go to menu Plugin->Options->Jason to configure the path.");
        }

        script = replace(script, "<PROJECT-RUNNER-CLASS>", RunJadeMAS.class.getName());
        script = replace(script, "<DEBUG>", "");

        String jadepath = "\t<pathelement location=\"" + Config.get().getJadeJar() + "\"/>";
        try {
            String http = new File(Config.get().getJadeJar()).getAbsoluteFile().getParent() + "/http.jar";
            jadepath += "\n\t<pathelement location=\"" + http + "\"/>";
        } catch (Exception _) {}
        
        script = replace(script, "<PATH-LIB>", jadepath + "\n\t<PATH-LIB>");

        return super.replaceMarks(script, debug);
    }
}
