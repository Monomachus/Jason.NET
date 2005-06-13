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
// http://www.csc.liv.ac.uk/~bordini
// http://www.inf.furb.br/~jomi
//----------------------------------------------------------------------------

package jason.asSemantics;

import jIDE.JasonID;
import jason.D;
import jason.JasonException;
import jason.Settings;
import jason.architecture.AgentArchitecture;
import jason.asSyntax.BeliefBase;
import jason.asSyntax.Literal;
import jason.asSyntax.Plan;
import jason.asSyntax.PlanLibrary;
import jason.asSyntax.StringTerm;
import jason.asSyntax.Term;
import jason.asSyntax.Trigger;
import jason.asSyntax.parser.ParseException;
import jason.asSyntax.parser.as2j;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Agent {

	// Members
	protected BeliefBase bs = new BeliefBase();
	protected PlanLibrary ps = new PlanLibrary();

	protected TransitionSystem ts = null;

	/**
	 * args[0] is the user Agent class (ignored here)
	 * args[1] is the AgentSpeak source file
	 */
    public TransitionSystem initAg(String[] args, AgentArchitecture arch) throws JasonException {
        // set the agent
        try {
            String asSource = null;
            if (args.length < 2) { // error
                throw new JasonException("The AS source file was not informed for the Agent creation!");
            } else {
                asSource = args[1].trim();
            }
            parseAS(asSource);
            // kqml Plans at the end of the ag PS
			parseAS(JasonID.class.getResource("/asl/kqmlPlans.asl"));            
            Circumstance C = new Circumstance();
            Settings setts = new Settings();
            if (args.length > 2) {
                if (args[2].equals("options")) {
                    setts.setOptions("["+args[3]+"]");
                }
            }
            setTS(new TransitionSystem(this,C,setts,arch));
            return ts;
        } catch (Exception e) {
            throw new JasonException("Error initializing creating the agent class! - "+e);
        }
    }
	

	public boolean parseAS(URL asURL) {
		try {
			parseAS(asURL.openStream());
			return true;
			//System.out.println("as2j: AgentSpeak program '"+asURL+"' parsed successfully!");
		} catch (IOException e) {
			System.err.println("as2j: the AS source file was not found\n" + e);
		} catch (ParseException e) {
			System.err.println("as2j: error parsing \"" + asURL + "\"\n" + e);
		}
		return false;
	}
	public boolean parseAS(String asFileName) {
		try {
			parseAS(new FileInputStream(asFileName));
			return true;
			//System.out.println("as2j: AgentSpeak program '"+asFileName+"' parsed successfully!");
		} catch (FileNotFoundException e) {
			System.err.println("as2j: the AS source file was not found\n" + e);
		} catch (ParseException e) {
			System.err.println("as2j: error parsing \"" + asFileName + "\"\n" + e);
		}
		return false;
	}
	void parseAS(InputStream asIn) throws ParseException {
		as2j parser = new as2j(asIn);
		parser.ag(this);
	}

	
	/** Follows the default implementation for the agent's
	 *  message acceptance relation and selection functions
	 */
	public boolean socAcc(Message m) {
		return true;
	}
	
	/*
	public boolean acceptTell(String sender, String content) {
		// docile agent
		return (true);
	}

	public boolean acceptAchieve(String sender, String content) {
		// benevolent agent
		return (true);
	}
	*/

	public Event selectEvent(List evList) {
		// make sure the selected Event is removed from evList
		return ((Event) evList.remove(0));
	}

	public Option selectOption(List optList) {
		if (optList.size() > 0) {
			return ((Option) optList.remove(0));
		} else {
			return null;
		}
	}

	public Intention selectIntention(List intList) {
		// make sure the selected Intention is removed from intList AND
		// make sure no intention will "starve"!!!
		return ((Intention) intList.remove(0));
	}

	public Message selectMessage(List msgList) {
		// make sure the selected Message is removed from msgList
		return ((Message) msgList.remove(0));
	}

	public ActionExec selectAction(List actList) {
		// make sure the selected Action is removed from actList
		return ((ActionExec) actList.remove(0));
	}

	/** Agent's Source Beliefs Initialisation (called by the parser) */
	public void addBS(BeliefBase bb) {
		bs.addAll(bb);
	}

	/** Agent's Source Plans Initialisation (called by the parser) */
	public void addPS(PlanLibrary pp) {
		ps.addAll(pp);
	}

	/** TS Initialisation (called by the AgArch) */
	public void setTS(TransitionSystem ts) {
		this.ts = ts;
	}

	// Accessing the agent's belief base and plans
	public BeliefBase getBS() {
		return bs;
	}

	public PlanLibrary getPS() {
		return ps;
	}

	// Other auxiliary methods

	public Unifier believes(Literal l, Unifier un) {
		List relB = bs.getRelevant(l);
		if (relB != null) {
			for (int i=0; i < relB.size(); i++) {
				Literal b = (Literal) relB.get(i);
				Unifier newUn = (un == null) ? new Unifier() : (Unifier) un.clone();
				// recall that order is important because of annotations!
				if (newUn.unifies(l,b))
					// if literals are negated or not
					return newUn;
			}
		}
		return null;
	}

	public Literal findBel(Literal l, Unifier un) {
		List relB = bs.getRelevant(l);
		if (relB != null) {
			for (int i=0; i < relB.size(); i++) {
				Literal b = (Literal) relB.get(i);
				Unifier newUn = (un == null) ? new Unifier() : (Unifier) un.clone();
				// recall that order is important because of annotations!
				if (newUn.unifies(l,b))
					// if literals are negated or not
					return b;
			}
		}
		return null;
	}
	
	
	/*
	public boolean addBel(String sl, String sSource, Circumstance c) {
		return addBel(Literal.parseLiteral(sl), Term.parse("source("+sSource+")"), c, D.EmptyInt);
	}

	public boolean addBel(String sl, Term source, Circumstance c) {
		return addBel(Literal.parseLiteral(sl), source, c, D.EmptyInt);
	}

	public boolean addBel(Literal l, Term source, Circumstance c) {
		return addBel(l, source, c, D.EmptyInt);
	}
	*/

	/** the Literal will be cloned before added in the BB */
	public boolean addBel(Literal l, Term source, Circumstance c, Intention focus) {
		if (source != null && !source.isGround()) {
			System.err.println("Error: Annotations must be ground!\n Cannot use "+source+" as annotation.");
		} else {
			l = (Literal)l.clone();
			if (source != null) {
				l.addAnnot(source);
			}
			if (bs.add(l)) {
				//System.out.println("*** adding "+l);
				updateEvents(new Event(new Trigger(D.TEAdd, D.TEBel, l), focus), c);
				return true;
			}
			return false;
		}
		return false;
	}

	
	public boolean delBel(String sl, String sSource, Circumstance c) {
		return delBel(Literal.parseLiteral(sl), Term.parse("source("+sSource+")"), c);
	}

	public boolean delBel(Literal l, Term source, Circumstance c) {
		return delBel(l, source, c, D.EmptyInt);
	}

	public boolean delBel(Literal l, Term source, Circumstance c, Intention focus) {
		if(source != null && !source.isGround()) {
			System.err.println("Error: Annotations must be ground!\n Cannot use "+source+" as annotation.");
		} else {
			if (source != null) {
				//l.clearAnnot();
				l.addAnnot(source);
			}
			//System.out.println("removing "+l);
			if (bs.remove(l)) {
				//System.out.println("removed "+l);
				updateEvents(new Event(new Trigger(D.TEDel, D.TEBel, l), focus), c);
				return true;
			}
		}
		return false;
	}

	// only add External Event if it is relevant in respect to the PlanLibrary
	public void updateEvents(Event e, Circumstance c) {
		if (e.isInternal() || ps.isRelevant(e.trigger))
			c.E.add(e);
	}

	// TODO IMPORTANT: this is not making sure the label of the new plan is unique!!!
	// TODO: use pl contains (to have only a MAP in plan library)
	public void addPlan(StringTerm stPlan, Term tSource) {
		String sPlan = stPlan.getString();
		try {
			// remove \" -> "
			StringBuffer sTemp = new StringBuffer();
			for (int c=0; c <sPlan.length(); c++) {
				if (sPlan.charAt(c) != '\\') {
					sTemp.append(sPlan.charAt(c));
				}
			}
			sPlan = sTemp.toString();
			Plan p = Plan.parse(sPlan);
			int i = ps.indexOf(p);
			if (i < 0) {
				ps.add(p);
			} else {
				p = (Plan) ps.get(i);
			}
			if (p.getLabel() == null) {
				p.setLabel("alabel");
			}
			//p.getLabel().addAnnot(Term.parse("source("+sSource+")"));
			if (tSource != null) {
				p.getLabel().addSource(tSource);
			}
			
			//System.out.println("**** adding plan "+p+" from "+sSource);		

			/*
			Iterator l = p.getLabel().getAnnots().iterator();
			Term t = null;
			Term as = null;
			while (l.hasNext()) {
				t = (Term) l.next();
				if (t.getFunctor().equals("sources")) {
					as = (Term) t.getTerms().remove(0);
					break;
				}
			}
			if (as != null) {
				ParseList pl = new ParseList(as);
				List ls = pl.getAsList();
				if (!ls.contains(tSource)) {
					ls.add(tSource);
					pl.set(ls);
				}
				t.getTerms().add(0, pl.getList());
			} else {
				Term n = new Term("sources");
				n.addTerm(new ParseList("[" + tSource + "]").getList());
				p.getLabel().addAnnot(n);
			}
			*/
		} catch (Exception e) {
			System.err.println("Error adding plan "+sPlan);
			e.printStackTrace();
		}
	}

	/** 
	 * remove a plan represented by the string sPlan 
	 * that comes from source (normally the agent name)
	 */
	public void removePlan(StringTerm sPlan, Term source) {
		Plan p = Plan.parse(sPlan.getFunctor());
		int i = ps.indexOf(p);
		if (i >= 0) {
			p = (Plan)ps.get(i);
			boolean hasSource = p.getLabel().delSource(source);
			
			// if no source anymore, remove the plan
			if (hasSource && ! p.getLabel().hasSource()) {
				ps.remove(i);
			}
			/*
			Iterator l = p.getLabel().getAnnots().iterator();
			Term tPlanSources = null;
			Term sourcesList = null;
			while (l.hasNext()) {
				tPlanSources = (Term) l.next();
				if (tPlanSources.getFunctor().equals("sources")) {
					sourcesList = (Term) tPlanSources.getTerms().remove(0);
					break;
				}
			}
			if (sourcesList != null) {
				ParseList pl = new ParseList(sourcesList);
				List ls = pl.getAsList();
				if (ls.contains(tSource)) {
					ls.remove(tSource);
					if (!ls.isEmpty()) {
						pl.set(ls);
						tPlanSources.getTerms().add(0, pl.getList());
					} else {
						ps.remove(i);
					}
				}
			}
			*/
		}
	}

	static DocumentBuilder builder = null;
	

	/** get the agent "mind" (Beliefs, plans and circumstance) as XML */
	public Document getAgState() {
		if (builder == null) {
			try {
				builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			} catch (Exception e) {
				System.err.println("Error creating XML builder\n");
				e.printStackTrace();
				return null;
			}
		}
		Document document = builder.newDocument();
		Element ag = getAsDOM(document);
		document.appendChild(ag);
		
		ag.appendChild(ts.getC().getAsDOM(document));
		return document;
	}
	
	/** get the agent "mind" as XML */
	public Element getAsDOM(Document document) {
		Element ag = (Element) document.createElement("agent");
		ag.setAttribute("name", ts.getAgArch().getName());
		ag.appendChild(bs.getAsDOM(document));
		//ag.appendChild(ps.getAsDOM(document));
		return ag;
	}

}