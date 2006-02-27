package jason.runtime;

import java.util.Set;

public interface RuntimeServicesInfraTier {
	public boolean createAgent(String agName, String agSource, String agClass, String archClass, Settings stts) throws Exception;
	public boolean killAgent(String agName);
	public Set     getAgentsName();
	public int     getAgentsQty();
	public void    stopMAS() throws Exception;
}
