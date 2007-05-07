package jason.asSyntax.directives;

import jason.asSemantics.Agent;
import jason.asSyntax.Pred;
import jason.asSyntax.StringTerm;
import jason.asSyntax.parser.as2j;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Implementation of the <code>include</code> directive. */
public class Include implements Directive {

    static Logger logger = Logger.getLogger(Include.class.getName());

    public Agent process(Pred directive, Agent outterContent, Agent innerContent) {
    	String file = ((StringTerm)directive.getTerm(0)).getString();
        try {
	    	String outterPrefix = outterContent.getASLSource();
	    	InputStream in;
	    	if (outterContent != null && outterPrefix != null) {
	    		// check if the outter is URL
	    		if (outterPrefix.startsWith("jar")) {
	    			outterPrefix = outterPrefix.substring(0,outterPrefix.indexOf("!")+1) + File.separator;
	    			file = outterPrefix + file;
	    			in = new URL(file).openStream();
	    		} if (outterPrefix.startsWith("ClassResource:")) {
	    			in = Agent.class.getResource("/"+file).openStream();
	    		} else {
	        		file =  new File(outterPrefix).getAbsoluteFile().getParent() + File.separator + file;
	        		in = new FileInputStream(file);
	    		}
	    	} else {
	    		in = new FileInputStream(file);    		
	    	}
    	
        	Agent ag = new Agent();
            as2j parser = new as2j(in); //new FileInputStream(asFileName));
            parser.agent(ag);
            logger.fine("as2j: AgentSpeak program '"+file+"' parsed successfully!");
            return ag;
        } catch (FileNotFoundException e) {
            logger.log(Level.SEVERE,"as2j: the AgentSpeak source file was not found", e);
        } catch (Exception e) {
            logger.log(Level.SEVERE,"as2j: error parsing \"" + file + "\"", e);
        }
        return null;

    	
        //return processInclude(file);
    }

    /*
    Agent processInclude(String asFileName) {
        try {
        	Agent ag = new Agent();
            as2j parser = new as2j(new FileInputStream(asFileName));
            parser.agent(ag);
            logger.fine("as2j: AgentSpeak program '"+asFileName+"' parsed successfully!");
            return ag;
        } catch (FileNotFoundException e) {
            logger.log(Level.SEVERE,"as2j: the AgentSpeak source file was not found", e);
        } catch (Exception e) {
            logger.log(Level.SEVERE,"as2j: error parsing \"" + asFileName + "\"", e);
        }
        return null;
    }
    */
}
