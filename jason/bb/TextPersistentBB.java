package jason.bb;

import jason.asSemantics.Agent;
import jason.asSyntax.Literal;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of BB that stores the agent BB in text files. This
 * implementation is very simple: when the agent starts, load the
 * beliefs in the file; when the agent stops, save the BB in the file.
 */
public class TextPersistentBB extends DefaultBeliefBase {
    static private Logger logger = Logger.getLogger(TextPersistentBB.class.getName());

    File file = null;

    public void init(Agent ag, String[] args) {
        file = new File(ag.getASLSource().replace(".asl", ".bb"));
        logger.fine("reading from file " + file);
        if (file.exists()) {
            ag.parseAS(file.getAbsolutePath());
        }
    }

    public void stop() {
        try {
            logger.fine("writting to file " + file);
            PrintWriter out = new PrintWriter(new FileWriter(file));
            Iterator<Literal> i = getAll();
            while (i.hasNext()) {
                Literal b = i.next();
                out.println(b.toString()+".");
            }
            out.close();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error writing BB in file " + file, e);
        }
    }
}
