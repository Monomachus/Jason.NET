package jason.runtime;

import jason.mas2j.ClassParameters;

import java.util.Set;

/**
 * This interface is implemented by the infrastructure tier
 * (Saci/Centralised/...) to provide concrete runtime services.
 */
public interface RuntimeServicesInfraTier {
    /**
     * Creates a new agent with <i>agName</i> from source <i>agSource</i>,
     * using <i>agClass</i> as agent class (default value is
     * jason.asSemantics.Agent), <i>archClass</i> as agent architecture class
     * (default value is jason.architecture.AgArch), <i>bbPars</i> as the belief base
     * class (default value is DefaultBeliefBase), and <i>stts</i> as
     * Settings (default value is new Settings()).
     * 
     * <p>
     * Example: createAgent("bob", "bob.asl", "mypkg.MyAgent", null, null, null);
     */
    public boolean createAgent(String agName, String agSource, String agClass, String archClass, ClassParameters bbPars, Settings stts) throws Exception;

    /**
     * Kills the agent named <i>agName</i>. The stopAg() method, in the agent
     * architecture is called before the agent is removed.
     */
    public boolean killAgent(String agName);

    /** Returns a set of all agents' name */
    public Set<String> getAgentsName();

    /** Gets the number of agents in the MAS. */
    public int getAgentsQty();

    /** Stop all MAS (the agents, the environment, the controller, ...) */
    public void stopMAS() throws Exception;
}
