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
        StringBuffer source = new StringBuffer("MAS auctionCent { ");
        source.append("infrastructure: Centralised ");
        source.append("environment: myEnv at \"x.edu\" ");
        source.append("executionControl: myController ");
        source.append("agents: ag1 [events=discard,intBels=newFocus,osfile=\"a/x.xml\"]; ag2 /home/agTest.asl agentClass mypkg.MyAgent #2; ag3 at \"x.edu\"; auctionner agentArchClass AuctionnerGUI;");
        source.append("directives: md1=mypkg.DebugDirective; md2=mypkg.LogDirective;");
        source.append("classpath: \"x.jar\"; \"../../\";");
        source.append("aslsourcepath: \"kk\"; \".\";");
        source.append("}");
    	parser = new mas2j(new StringReader(source.toString()));
	}

    public void testToString() {
        boolean ok = true;
        try {
            MAS2JProject project = parser.mas();
            System.out.println(project);
            parser = new mas2j(new StringReader(project.toString()));
            parser.mas();
        } catch (Exception e) {
            System.err.println("Error:"+e);
            e.printStackTrace();
            ok = false;
        }
        assertTrue(ok);
    }

	@SuppressWarnings("unchecked")
	public void testParser() {
		try {
			MAS2JProject project = parser.mas();
	    	project.setDirectory("/tmp");
			
			//project.writeXMLScript(System.out);
			//project.writeScripts(true);
			Map ag1Opt = project.getAg("ag1").options;
			assertEquals(ag1Opt.size(),3);
            
            assertEquals(project.getDirectiveClasses().size(),2);
            assertEquals(project.getDirectiveClasses().get("md2").toString(), "mypkg.LogDirective");
            
		} catch (Exception e) {
			System.err.println("Error:"+e);
			e.printStackTrace();
		}
	}

}
