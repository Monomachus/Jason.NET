package jason.asSyntax;

import jason.asSyntax.parser.as2j;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Directives {
    static Logger logger = Logger.getLogger(Directives.class.getName());
    
    public static void process(Pred d, List bels, PlanLibrary pl) {
        try {
            logger.fine("Processing directive "+d);
            if (d.getFunctor().equals("include")) {
                processInclude(((StringTerm)d.getTerm(0)).getString(), bels, pl);
            } else {
                logger.log(Level.SEVERE, "Unknown directive "+d);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error processing directive "+d,e);
        }
    }

    public static void processInclude(String asFileName, List bels, PlanLibrary pl) {
        try {
            as2j parser = new as2j(new FileInputStream(asFileName));
            parser.belief_base(bels);
            PlanLibrary newPl = parser.plan_base(bels);
            pl.addAll(newPl);
            logger.fine("as2j: AgentSpeak program '"+asFileName+"' parsed successfully!");
        } catch (FileNotFoundException e) {
            logger.log(Level.SEVERE,"as2j: the AgentSpeak source file was not found", e);
        } catch (Exception e) {
            logger.log(Level.SEVERE,"as2j: error parsing \"" + asFileName + "\"", e);
        }
    }
}
