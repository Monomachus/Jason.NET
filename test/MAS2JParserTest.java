package test;

import jason.mas2j.MAS2JProject;
import jason.mas2j.parser.mas2j;

import java.io.StringReader;
import java.util.Map;

import junit.framework.TestCase;

/** JUnit test case for syntax package */
public class MAS2JParserTest extends TestCase {

	mas2j parser;
	 
	protected void setUp() throws Exception {
		super.setUp();
    	parser = new mas2j(new StringReader("MAS auctionCent {  infrastructure: Centralised environment: myEnv at \"x.edu\" executionControl: myController agents: ag1 [events=discard,intBels=newFocus,osfile=\"a/x.xml\"]; ag2 /home/agTest.asl agentClass mypkg.MyAgent #2; ag3 at \"x.edu\"; auctionner agentArchClass AuctionnerGUI;}"));
	}

	public void testParser() {
		try {
			MAS2JProject project = parser.mas();
	    	project.setDirectory("/tmp");
			//System.out.println(project);
			
			//project.writeXMLScript(System.out);
			//project.writeScripts(true);
			Map ag1Opt = project.getAg("ag1").options;
			assertEquals(ag1Opt.size(),3);
		} catch (Exception e) {
			System.err.println("Error:"+e);
			e.printStackTrace();
		}
	}

}
