package jason.infra.jade;

import jade.wrapper.PlatformController;
import jason.mas2j.ClassParameters;
import jason.runtime.RuntimeServicesInfraTier;
import jason.runtime.Settings;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JadeRuntimeServices implements RuntimeServicesInfraTier {

    private static Logger logger  = Logger.getLogger(JadeRuntimeServices.class.getName());
    
    private PlatformController pc;
    
    JadeRuntimeServices(PlatformController pc) {
        this.pc = pc;
    }
    
    public boolean createAgent(String agName, String agSource, String agClass, String archClass, ClassParameters bbPars, Settings stts) throws Exception {
        // TODO: implement
        logger.warning("not implemented yet!");
        return false;
    }

    @SuppressWarnings("unchecked")
    public Set getAgentsName() {
        // TODO: implement
        logger.warning("not implemented yet!");
        return null;
    }

    public int getAgentsQty() {
        try {
            // TODO: implement
            logger.warning("not implemented yet!");
            return 0;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting agents qty", e);
            return 0;
        }
    }

    public boolean killAgent(String agName) {
        try {
            // TODO: implement
            logger.warning("not implemented yet!");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error killing agent", e);
        }
        return false;
    }

    public void stopMAS() throws Exception {
        if (pc != null) pc.kill();
    }
}
