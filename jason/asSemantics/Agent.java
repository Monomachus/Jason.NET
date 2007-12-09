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
import jason.asSyntax.LogicalFormula;
import jason.asSyntax.Plan;
import jason.asSyntax.PlanLibrary;
import jason.asSyntax.Rule;
import jason.asSyntax.Structure;
import jason.asSyntax.Trigger;
import jason.asSyntax.Trigger.TEOperator;
import jason.asSyntax.Trigger.TEType;
import jason.asSyntax.directives.Include;
import jason.asSyntax.parser.ParseException;
import jason.asSyntax.parser.as2j;
import jason.bb.BeliefBase;
import jason.bb.DefaultBeliefBase;
import jason.runtime.Settings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * The Agent class has the belief base and plan library of an
 * AgentSpeak agent. It also implements the default selection
 * functions of the AgentSpeak semantics.
 */
public class Agent {

    // Members
    protected BeliefBase       bb = new DefaultBeliefBase();
    protected PlanLibrary      pl = new PlanLibrary();
    protected TransitionSystem ts = null;
    protected String           aslSource = null;
    
    private List<Literal>      initialGoals = new ArrayList<Literal>(); // initial goals in the source code
    private List<Literal>      initialBels  = new ArrayList<Literal>(); // initial beliefs in the source code

    private Map<String, InternalAction> internalActions = new HashMap<String, InternalAction>();

    private Logger logger = Logger.getLogger(Agent.class.getName());

    /** Creates the TS of this agent, parses its AS source, and sets its Settings */
    public TransitionSystem initAg(AgArch arch, BeliefBase bb, String asSrc, Settings stts) throws JasonException {
        // set the agent
        try {
            setLogger(arch);
            logger.setLevel(stts.logLevel());

            if (bb != null) {
                this.bb = bb;
            }

            setTS(new TransitionSystem(this, new Circumstance(), stts, arch));

            if (asSrc != null) {
                setASLSrc(asSrc);
    
                if (asSrc.startsWith(Include.CRPrefix)) {
                	// loads the class from a jar file (for example)
                	parseAS(Agent.class.getResource(asSrc.substring(Include.CRPrefix.length())).openStream());
                } else {
    	            // check whether source is an URL string
    	            try {
    	            	parseAS(new URL(asSrc));
    	            } catch (MalformedURLException e) {
    	            	parseAS(asSrc);
    	            }
                }
                
                // kqml Plans at the end of the ag PS
                setASLSrc("kqmlPlans.asl");
                parseAS(JasonException.class.getResource("/asl/kqmlPlans.asl"));
                setASLSrc(asSrc);
    
                addInitialBelsInBB();
                addInitialGoalsInTS();
            }
            return ts;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error creating the agent class!", e);
            throw new JasonException("Error creating the agent class! - " + e);
        }
    }
    
    /** 
     *  Clone BB, PL, Circumstance. 
     *  A new TS is created (based on the cloned circumstance).
     */
    public Agent clone(AgArch arch) {
    	Agent a = new Agent();
    	
    	a.setLogger(arch);
        a.logger.setLevel(this.getTS().getSettings().logLevel());

    	a.bb = (BeliefBase)this.bb.clone();
    	a.pl = (PlanLibrary)this.pl.clone();
    	a.aslSource = this.aslSource;
    	
        a.setTS(new TransitionSystem(a, (Circumstance)this.getTS().getC().clone(), this.getTS().getSettings(), arch));

    	return a;
    }
    
    public void setLogger(AgArch arch) {
        if (arch != null) {
            logger = Logger.getLogger(Agent.class.getName() + "." + arch.getAgName());
        }
    }

    public Logger getLogger() {
        return logger;
    }

    /** Returns the .asl file source used to create this agent */
    public String getASLSrc() {
        return aslSource;
    }
    
    public void setASLSrc(String file) {
        if (file.startsWith("."+File.separator)) file = file.substring(2);
        aslSource = file;    	
    }

    /** Adds beliefs and plans form an URL */
    public boolean parseAS(URL asURL) {
        try {
            parseAS(asURL.openStream());
            logger.fine("as2j: AgentSpeak program '" + asURL + "' parsed successfully!");
            return true;
        } catch (IOException e) {
            logger.log(Level.SEVERE, "as2j: the AgentSpeak source file '"+asURL+"' was not found!");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "as2j: error parsing \"" + asURL + "\"", e);
        }
        return false;
    }

    /** Adds beliefs and plans form a file */
    public boolean parseAS(String asFileName) {
        try {
            parseAS(new FileInputStream(asFileName));
            logger.fine("as2j: AgentSpeak program '" + asFileName + "' parsed successfully!");
            return true;
        } catch (FileNotFoundException e) {
            logger.log(Level.SEVERE, "as2j: the AgentSpeak source file '"+asFileName+"' was not found!");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "as2j: error parsing \"" + asFileName + "\"", e);
        }
        return false;
    }

    void parseAS(InputStream asIn) throws ParseException, JasonException {
        as2j parser = new as2j(asIn);
        parser.agent(this);
    }

    public InternalAction getIA(Structure action) throws Exception {
        String iaName = action.getFunctor();
        if (iaName.charAt(0) == '.')
            iaName = "jason.stdlib" + iaName;
        InternalAction objIA = internalActions.get(iaName);
        if (objIA == null) {
            objIA = (InternalAction) Class.forName(iaName).newInstance();
            internalActions.put(iaName, objIA);
        }
        return objIA;
    }
    
    public void addInitialGoal(Literal g) {
    	initialGoals.add(g);
    }
    public void addInitialBel(Literal b) {
    	initialBels.add(b);
    }
    public List<Literal> getInitialBels() {
    	return initialBels;
    }
    
    /** add the initial beliefs in BB and produce the corresponding events */
    public void addInitialBelsInBB() {
        Unifier u = new Unifier();
        // Once beliefs are stored in a Stack in the BB, insert them in inverse order
        for (int i=initialBels.size()-1; i >=0; i--) {
            Literal b = initialBels.get(i);
            b.apply(u); // to solve arithmetic expressions

            // if l is not a rule and has free vars (like l(X)), convert it into a rule like "l(X) :- true."
            if (!b.isRule() && !b.isGround()) {
                b = new Rule(b,Literal.LTrue);
            }
            
            // does not do BRF for rules (and so do produce events +bel for rules)
            if (b.isRule()) {
            	getBB().add(b);
            } else {
            	addBel(b);
            }
        }
        initialBels.clear();
    }
    
    public void addInitialGoalsInTS() {
	    for (Literal g: initialGoals) {
	    	g.makeVarsAnnon();
	    	getTS().getC().addAchvGoal(g,Intention.EmptyInt);
	    }
    }

    /** Imports beliefs, plans and initial goals from another agent. Initial beliefs and goals 
     *  are stored in "initialBels" and "initialGoals" lists but not included in the BB / TS.
     *  The methods addInitialBelsInBB and addInitialGoalsInTS should be called in the sequel to
     *  add those beliefs and goals into the agent. */
    public void importComponents(Agent a) throws JasonException {
    	if (a != null) {
	    	for (Literal b: a.initialBels) {
	    		this.addInitialBel(b);
	    	}
	    	for (Literal g: a.initialGoals) {
	    		this.addInitialGoal(g);
	    	}
	    	for (Plan p: a.getPL()) {
	    		this.getPL().add(p);
	    	}
    	}
    }
    
    
    /**
     * Follows the default implementation for the agent's message acceptance
     * relation and selection functions
     */
    public boolean socAcc(Message m) {
        return true;
    }

    public Event selectEvent(Queue<Event> events) {
        // make sure the selected Event is removed from 'events' queue
        return events.poll();
    }

    public Option selectOption(List<Option> options) {
        if (options != null && !options.isEmpty()) {
            return options.remove(0);
        } else {
            return null;
        }
    }

    public Intention selectIntention(Queue<Intention> intentions) {
        // make sure the selected Intention is removed from 'intentions'
        // and make sure no intention will "starve"!!!
        return intentions.poll();
    }

    public Message selectMessage(Queue<Message> messages) {
        // make sure the selected Message is removed from 'messages'
        return messages.poll();
    }

    public ActionExec selectAction(List<ActionExec> actList) {
        // make sure the selected Action is removed from actList
        // (do not return suspended intentions)
    	synchronized (actList) {
            Iterator<ActionExec> i = actList.iterator();
            while (i.hasNext()) {
                ActionExec a = i.next();
                if (!a.getIntention().isSuspended()) {
                    i.remove();
                    return a;
                }
            }			
		}
        return null;
    }

    /** TS Initialisation (called by the AgArch) */
    public void setTS(TransitionSystem ts) {
        this.ts = ts;
    }

    public TransitionSystem getTS() {
        return ts;
    }

    public BeliefBase getBB() {
        return bb;
    }

    public PlanLibrary getPL() {
        return pl;
    }

    /** Belief Update Function: adds/removes percepts into belief base */
    public void buf(List<Literal> percepts) {
        if (percepts == null) {
            return;
        }

        // deleting percepts in the BB that is not perceived anymore
        Iterator<Literal> perceptsInBB = getBB().getPercepts();
        while (perceptsInBB.hasNext()) { 
            Literal l = perceptsInBB.next();

            // could not use percepts.contains(l), since equalsAsTerm must be
            // used (to ignore annotations)
            boolean wasPerceived = false;
            Iterator<Literal> ip = percepts.iterator();
            while (ip.hasNext()) {
            	Literal t = ip.next();
            	
                // if perception t is already in BB
                if (l.equalsAsStructure(t) && l.negated() == t.negated()) {
                    wasPerceived = true;
                    ip.remove(); // remove in percepts, since it already is in BB
                    break;
                }
            }
            if (!wasPerceived) {
                // new version (it is sure that l is in BB, only clone l when the event is relevant)
                perceptsInBB.remove(); // remove l as perception from BB
                
                if (ts.getC().hasListener() || pl.isRelevant(l.getPredicateIndicator())) {
                    l = (Literal)l.clone();
                    l.clearAnnots();
                    l.addAnnot(BeliefBase.TPercept);
                    ts.getC().addEvent(new Event(new Trigger(TEOperator.del, TEType.belief, l), Intention.EmptyInt));
                }
        
                /*
                // old version
                // can not delete l, but l[source(percept)]
                l = (Literal)l.clone();
                l.clearAnnots();
                l.addAnnot(BeliefBase.TPercept);
	            if (bb.remove(l)) {
	                ts.updateEvents(new Event(new Trigger(TEOperator.del, TEType.belief, l), Intention.EmptyInt));
	            }
	            */
            }
        }

        // BUF only adds a belief when appropriate
        // checking all percepts for new beliefs
        for (Literal lp : percepts) {
            try {
                lp = (Literal) lp.clone();
                lp.addAnnot(BeliefBase.TPercept);
                if (getBB().add(lp)) {
                    Trigger te = new Trigger(TEOperator.add, TEType.belief, lp);
                    ts.updateEvents(new Event(te, Intention.EmptyInt));
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error adding percetion " + lp, e);
            }
        }
    }

    /**
     * Returns true if BB contains the literal <i>bel</bel> (using unification to test).
     * The unifier <i>un</i> is updated by the method.
     */
    public boolean believes(LogicalFormula bel, Unifier un) {
        try {
            Iterator<Unifier> iun = bel.logicalConsequence(this, un);
            if (iun != null && iun.hasNext()) {
                un.compose(iun.next());
                return true;
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error in believe("+bel+","+un+").",e);
        }
        return false;
    }

    /**
     * Find a literal in BB using unification to test.
     * 
     * Returns the belief as it is in BB, e.g. findBel(a(_),...) 
     * may returns a(10)[source(ag)].
     *   
     * The unifier <i>un</i> is updated by the method.
     */
    public Literal findBel(Literal bel, Unifier un) {
        Iterator<Literal> relB = bb.getRelevant(bel);
        if (relB != null) {
            while (relB.hasNext()) {
                Literal b = relB.next();
                
                // recall that order is important because of annotations!
                if (!b.isRule() && un.unifies(bel, b)) {
                    return b;
                }
            }
        } 
        return null;
    }

    
    /**
     * This function should revise the belief base with the given literal to
     * add, to remove, and the current intention that triggered the operation.
     * 
     * <p>In its return, List[0] has the list of actual additions to
     * the belief base, and List[1] has the list of actual deletions;
     * this is used to generate the appropriate internal events. If
     * nothing change, returns null.
     */
    @SuppressWarnings("unchecked")
    public List<Literal>[] brf(Literal beliefToAdd, Literal beliefToDel,  Intention i) {
        // This class does not implement belief revision! It
        // is supposed that a subclass will do it.
        // It simply add/del the belief.

        List<Literal>[] result = null;
        try {
	        if (beliefToAdd != null) {
	        	if (logger.isLoggable(Level.FINE)) logger.fine("adding " + beliefToAdd);
	            
	            if (getBB().add(beliefToAdd)) {
	            	result = new List[2];
	                result[0] = Collections.singletonList(beliefToAdd);
	                result[1] = Collections.emptyList();
	            }
	        }
	
	        if (beliefToDel != null) {
	            Unifier u = null;
	            try {
	                u = i.peek().unif; // get from current intention
	            } catch (Exception e) {
	                u = new Unifier();
	            }
	
	            if (logger.isLoggable(Level.FINE)) logger.fine("doing brf for " + beliefToDel + " in BB=" + believes(beliefToDel, u));
	            
	            if (believes(beliefToDel, u)) {
	            	beliefToDel.apply(u);
	                if (getBB().remove(beliefToDel)) {
	                    if (logger.isLoggable(Level.FINE)) logger.fine("Removed:" + beliefToDel);
	                    if (result == null) {
	                    	result = new List[2];
	                        result[0] = Collections.emptyList();
	                    }
	                    result[1] = Collections.singletonList(beliefToDel);
	                }
	            }
	
	        }
    	} catch (Exception e) {
    		logger.log(Level.WARNING, "Error at BRF.",e);
    	}
        return result;
    }

    /**
     * Adds <i>bel</i> in belief base (calling brf) and generates the
     * events. If <i>bel</i> has no source, add
     * <code>source(self)</code>. (the belief is not cloned!)
     */
    public boolean addBel(Literal bel) {
        if (!bel.hasSource()) {
            bel.addAnnot(BeliefBase.TSelf);
        }
        List<Literal>[] result = brf(bel, null, Intention.EmptyInt);
        if (result != null && ts != null) {
            ts.updateEvents(result, Intention.EmptyInt);
            return true;
        } else {
            return false;
        }
    }

    /**
     * If the agent believes in <i>bel</i>, removes it (calling brf)
     * and generate the event.
     */
    public boolean delBel(Literal bel) {
        List<Literal>[] result = brf(null, bel, Intention.EmptyInt);
        if (result != null && ts != null) {
            ts.updateEvents(result, Intention.EmptyInt);
            return true;
        } else {
            return false;
        }
    }
    
    /** Removes all occurrences of <i>bel</i> in BB. 
        If <i>un</i> is null, an empty Unifier is used. 
     */
    public void abolish(Literal bel, Unifier un) {
        List<Literal> toDel = new ArrayList<Literal>();
        
        Iterator<Literal> il = getBB().getRelevant(bel);
        if (il != null) {
            while (il.hasNext()) {
                Literal inBB = il.next();
                if (!inBB.isRule()) {
                    // need to clone unifier since it is changed in previous iteration
                    Unifier unC = (un == null ? new Unifier() : (Unifier)un.clone());
                    if (unC.unifies(bel, inBB)) {
                        toDel.add(inBB);
                    }
                }
            }
        }
        
        for (Literal l: toDel) {
            delBel(l);
        }
    }

    static DocumentBuilder builder = null;

    /** Gets the agent "mind" (Beliefs, plans and circumstance) as XML */
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

        ag.appendChild(ts.getC().getAsDOM(document));
        return document;
    }

    /** Gets the agent "mind" as XML */
    public Element getAsDOM(Document document) {
        Element ag = (Element) document.createElement("agent");
        ag.setAttribute("name", ts.getUserAgArch().getAgName());
        ag.setAttribute("cycle", ""+ts.getUserAgArch().getCycleNumber());

        ag.appendChild(bb.getAsDOM(document));
        // ag.appendChild(ps.getAsDOM(document));
        return ag;
    }

    /** Gets the agent program (Beliefs and plans) as XML */
    public Document getAgProgram() {
        if (builder == null) {
            try {
                builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error creating XML builder\n");
                return null;
            }
        }
        Document document = builder.newDocument();
        Element ag = (Element) document.createElement("agent");
        if (getASLSrc() != null && getASLSrc().length() > 0) {
        	ag.setAttribute("source", getASLSrc());
        }
        ag.appendChild(bb.getAsDOM(document));
        ag.appendChild(pl.getAsDOM(document));
        document.appendChild(ag);

        return document;
    }

}
