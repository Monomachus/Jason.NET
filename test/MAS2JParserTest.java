package test;

import jIDE.RunCentralisedMAS;
import jIDE.mas2j.MAS2JProject;
import jIDE.parser.mas2j;

import java.io.StringReader;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

/** JUnit test case for syntax package */
public class MAS2JParserTest extends TestCase {

	mas2j parser;
	 
	protected void setUp() throws Exception {
		super.setUp();
		Logger.getRootLogger().addAppender(new ConsoleAppender(new PatternLayout("[%c{1}] %m%n")));
    	Logger.getRootLogger().setLevel(Level.DEBUG);
    	parser = new mas2j(new StringReader("MAS auctionCent {  infrastructure: Centralised environment: myEnv at \"x.edu\" executionControl: myController agents: ag1 [events=discard,intBels=newFocus,osfile=\"a/x.xml\"]; ag2 /home/agTest.asl agentClass mypkg.MyAgent #2; ag3 at \"x.edu\"; auctionner agentArchClass AuctionnerGUI;}"));
	}

	public void testParser() {
		try {
			MAS2JProject project = parser.mas();
	    	project.setProjectDir("/tmp");
			//System.out.println(project);
			
			project.debugOn();
			project.writeXMLScript(System.out);
			//project.writeScripts();
			Map ag1Opt = project.getAg("ag1").options;
			assertEquals(ag1Opt.size(),3);
		} catch (Exception e) {
			System.err.println("Error:"+e);
			e.printStackTrace();
		}
	}

	public void testParseArrayFromString() {
		String[] args = RunCentralisedMAS.getArrayFromString("jason.asSemantics.Agent '/Users/jomi/programming/cvs/Jason/examples/Simple/./agCount.asl' options y='a a a',verbose=1,x=1,bla='blas/x1 y/t.txt'");
		System.out.println("*"+args[1]);		
		System.out.println("*"+args[2]+" "+args[3]);
		assertEquals(args.length, 4);
	}	

}
