// ----------------------------------------------------------------------------
// Copyright (C) 2003 Rafael H. Bordini, Jomi F. Hubner, et al.
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//
// To contact the authors:
// http://www.dur.ac.uk/r.bordini
// http://www.inf.furb.br/~jomi
//
//----------------------------------------------------------------------------

package jason.asSemantics;

import jason.JasonException;
import jason.architecture.AgArch;
import jason.asSyntax.Literal;
import jason.asSyntax.PlanLibrary;
import jason.asSyntax.Term;
import jason.asSyntax.Trigger;
import jason.asSyntax.parser.ParseException;
import jason.asSyntax.parser.as2j;
import jason.bb.BeliefBase;
import jason.bb.DefaultBeliefBase;
import jason.runtime.Settings;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Agent class has the belief base and plan library of an AgentSpeak agent. It
 * also implements the default selection functions of the AgentSpeak semantics.
 */
public class Agent {

    // Members
    protected BeliefBase                fBS             = new DefaultBeliefBase();

    protected PlanLibrary               fPS             = new PlanLibrary();

    private Map<String, InternalAction> internalActions = new HashMap<String, InternalAction>();

    protected TransitionSystem          fTS             = null;

    protected String                    aslSource       = null;

    private Logger                      logger;

    /** creates the TS of this agent, parse its AS source, and set its Settings */
    public TransitionSystem initAg(AgArch arch, BeliefBase bb, String asSrc, Settings stts) throws JasonException {
        // set the agent
        try {
            setLogger(arch);
            logger.setLevel(stts.logLevel());
            
            if (bb != null) {
                this.fBS = bb;
            }
            
            setTS(new TransitionSystem(this, new Circumstance(), stts, arch));

            this.aslSource = asSrc;
            parseAS(asSrc);
            // kqml Plans at the end of the ag PS
            parseAS(JasonException.class.getResource("/asl/kqmlPlans.asl"));

            return fTS;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error creating the agent class!", e);
            throw new JasonException("Error creating the agent class! - " + e);
        }
    }

    public void setLogger(AgArch arch) {
        if (arch != null) {
            logger = Logger.getLogger(Agent.class.getName() + "." + arch.getAgName());
        } else {
            logger = Logger.getLogger(Agent.class.getName());
        }
    }

    public Logger getLogger() {
        return logger;
    }

    /** returns the .asl file source used to create this agent */
    public String getASLSource() {
        return aslSource;
    }
    
    /** add beliefs and plan form a URL */
    public boolean parseAS(URL asURL) {
        try {
            parseAS(asURL.openStream());
            logger.fine("as2j: AgentSpeak program '" + asURL + "' parsed successfully!");
            return true;
        } catch (IOException e) {
            logger.log(Level.SEVERE, "as2j: the AgentSpeak source file was not found", e);
        } catch (ParseException e) {
            logger.log(Level.SEVERE, "as2j: error parsing \"" + asURL + "\"", e);
        }
        return false;
    }

    /** add beliefs and plan form a file */
    public boolean parseAS(String asFileName) {
        try {
            parseAS(new FileInputStream(asFileName));
            logger.fine("as2j: AgentSpeak program '" + asFileName + "' parsed successfully!");
            return true;
        } catch (FileNotFoundException e) {
            logger.log(Level.SEVERE, "as2j: the AgentSpeak source file was not found", e);
        } catch (ParseException e) {
            logger.log(Level.SEVERE, "as2j: error parsing \"" + asFileName + "\"", e);
        }
        return false;
    }

    void parseAS(InputStream asIn) throws ParseException {
        as2j parser = new as2j(asIn);
        parser.ag(this);
    }

    public InternalAction getIA(Term action) throws Exception {
        String iaName = action.getFunctor();
        if (iaName.indexOf('.') == 0)
            iaName = "jason.stdlib" + iaName;
        InternalAction objIA = internalActions.get(iaName);
        if (objIA == null) {
            objIA = (InternalAction) Class.forName(iaName).newInstance();
            internalActions.put(iaName, objIA);
        }
        return objIA;
    }

    /**
     * Follows the default implementation for the agent's message acceptance
     * relation and selection functions
     */
    public boolean socAcc(Message m) {
        return true;
    }

    public Event selectEvent(List<Event> evList) {
        // make sure the selected Event is removed from evList
        return evList.remove(0);
    }

    public Option selectOption(List<Option> optList) {
        if (optList.size() > 0) {
            return optList.remove(0);
        } else {
            return null;
        }
    }

    public Intention selectIntention(List<Intention> intList) {
        // make sure the selected Intention is removed from intList AND
        // make sure no intention will "starve"!!!
        return intList.remove(0);
    }

    public Message selectMessage(List<Message> msgList) {
        // make sure the selected Message is removed from msgList
        return msgList.remove(0);
    }

    public ActionExec selectAction(List<ActionExec> actList) {
        // make sure the selected Action is removed from actList
        return actList.remove(0);
    }

    /** TS Initialisation (called by the AgArch) */
    public void setTS(TransitionSystem ts) {
        this.fTS = ts;
    }

    public TransitionSystem getTS() {
        return fTS;
    }

    // Accessing the agent's belief base and plans
    public BeliefBase getBS() {
        return fBS;
    }

    public PlanLibrary getPS() {
        return fPS;
    }

    /**
     * This function should revise the belief base with the given literals to
     * add and delete.
     * 
     * In its return, List[0] has the list of actual additions to the belief
     * base, and List[1] has the list of actual deletions; this is used to
     * generate the appropriate internal events.
     */
    public List<Literal>[] brf(List<Literal> adds, List<Literal> dels) {
        return null;
    }

    /** Belief Update Function: add/remove perceptions into belief base */
    public void buf(List<Literal> percepts) {
        if (percepts == null) {
            return;
        }

        // List<Literal> added = new ArrayList<Literal>();
        List<Literal> removed = new ArrayList<Literal>();

        // deleting percepts in the BB that is not perceived anymore
        Iterator<Literal> perceptsInBB = getBS().getPercepts();
        while (perceptsInBB.hasNext()) { // for (int i = 0; i <
            // perceptsInBB.size(); i++) {
            Literal l = perceptsInBB.next(); // get(i);

            // could not use percepts.contains(l), since equalsAsTerm must be
            // used (to ignore annotations)
            boolean wasPerceived = false;
            for (Literal t : percepts) {
                // if percept t is already in BB
                if (l.equalsAsTerm(t) && l.negated() == t.negated()) {
                    wasPerceived = true;
                    break;
                }
            }
            if (!wasPerceived) {
                // perceptsInBB.remove();
                // l.addAnnot(BeliefBase.TPercept);
                removed.add(l); // do not remove here, concurrent modification!
                // if (delBel(l, fTS.getC(), Intention.EmptyInt)) {
                // i--;
                // TODO: remove with (not implemented!!!!!)
                // }
            }
        }

        for (Literal lr : removed) {
            // TODO: do not generate events form removed here
            delBel(lr, fTS.getC(), Intention.EmptyInt);
        }

        // addBel only adds a belief when appropriate
        // checking all percepts for new beliefs
        for (Literal l : percepts) {
            try {
                addBel(l, BeliefBase.TPercept, fTS.getC(), Intention.EmptyInt);
                // TODO: do not generate event here
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error adding percetion " + l, e);
            }
        }
    }

    /**
     * If BB contains l (using unification to test), returns the literal that is
     * the BB; otherwise, returns null. E.g.: if l is g(_,_) and BB is={....,
     * g(10,20), ...}, this method returns g(10,20).
     * 
     */
    public Literal believes(Literal l, Unifier un) {
        Iterator<Literal> relB = fBS.getRelevant(l);
        if (relB != null) {
            while (relB.hasNext()) {
                Literal b = relB.next();

                // recall that order is important because of annotations!
                if (un.unifies(l, b)) {
                    return b;
                }
            }
        }
        return null;
    }

    /**
     * Adds a new Literal <i>l</i> in BB with "source(<i>source</i>)"
     * annotation. <i>l</i> will be cloned before being added in the BB
     */
    public boolean addBel(Literal l, Term source, Circumstance c, Intention focus) {
        if (source != null && !source.isGround()) {
            logger.log(Level.SEVERE, "Error: Annotations must be ground!\n Cannot use " + source + " as annotation.");
        } else if (l.equals(Literal.LTrue) || l.equals(Literal.LFalse)) {
            logger.log(Level.SEVERE, "Error: <true> or <false> can not be added as beliefs.");
        } else {
            l = (Literal) l.clone();
            if (source != null) {
                l.addAnnot(source);
            }
            if (fBS.add(l)) {
                if (logger.isLoggable(Level.FINE))
                    logger.fine("Added belief " + l);
                updateEvents(new Event(new Trigger(Trigger.TEAdd, Trigger.TEBel, l), focus), c);
                return true;
            }
            return false;
        }
        return false;
    }

    public boolean delBel(Literal l, Circumstance c, Intention focus) {
        if (fBS.remove(l)) {
            if (logger.isLoggable(Level.FINE))
                logger.fine("Removed:" + l);
            updateEvents(new Event(new Trigger(Trigger.TEDel, Trigger.TEBel, l), focus), c);
            return true;
        } else {
            if (logger.isLoggable(Level.FINE))
                logger.fine("Not removed: " + l);
        }
        return false;
    }

    // only add External Event if it is relevant in respect to the PlanLibrary
    public void updateEvents(Event e, Circumstance c) {
        if (c != null) {
            if (e.isInternal() || c.hasListener() || fPS.isRelevant(e.trigger.getPredicateIndicator())) {
                c.addEvent(e);
                if (logger.isLoggable(Level.FINE))
                    logger.fine("Added event " + e);
            }
        }
    }

    static DocumentBuilder builder = null;

    /** get the agent "mind" (Beliefs, plans and circumstance) as XML */
    public Document getAgState() {
        if (builder == null) {
            try {
                builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error creating XML builder\n");
                return null;
            }
        }
        Document document = builder.newDocument();
        Element ag = getAsDOM(document);
        document.appendChild(ag);

        ag.appendChild(fTS.getC().getAsDOM(document));
        return document;
    }

    /** get the agent "mind" as XML */
    public Element getAsDOM(Document document) {
        Element ag = (Element) document.createElement("agent");
        ag.setAttribute("name", fTS.getUserAgArch().getAgName());
        ag.appendChild(fBS.getAsDOM(document));
        // ag.appendChild(ps.getAsDOM(document));
        return ag;
    }

}