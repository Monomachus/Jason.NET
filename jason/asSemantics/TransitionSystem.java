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

import jason.D;
import jason.JasonException;
import jason.Settings;
import jason.architecture.AgentArchitecture;
import jason.architecture.CentralisedAgArch;
import jason.architecture.SaciAgArch;
import jason.asSyntax.BodyLiteral;
import jason.asSyntax.DefaultLiteral;
import jason.asSyntax.Literal;
import jason.asSyntax.Plan;
import jason.asSyntax.Pred;
import jason.asSyntax.Term;
import jason.asSyntax.Trigger;
import jason.asSyntax.VarTerm;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class TransitionSystem {

	Agent ag = null;

	Circumstance C = null;

	Settings setts = null;

	AgentArchitecture agArch = null;

	byte step = D.SStartRC; // First step of the SOS

	// both configuration and configuration' point to this
	// object, this is just to make it look more like the SOS
	TransitionSystem confP;

	// both configuration and configuration' point to this
	// object, this is just to make it look more like the SOS
	TransitionSystem conf;

	public TransitionSystem(Agent a, Circumstance c, Settings s, AgentArchitecture ar) {
		ag = a;
		C = c;
		setts = s;
		agArch = ar;

		// we need to initialise this "aliases"
		conf = confP = this;
	}

	/********************************************************************* */
	/* SEMANTIC RULES */
	/********************************************************************* */
	private void applySemanticRule() throws JasonException {
		// check the current step in the reasoning cycle
		// only the main parts of the interpretation appear here
		// the individual semantic rules appear below
		
		switch (conf.step) {
		case D.SProcMsg:
			applyProcMsg();
			break;
		case D.SSelEv:
			applySelEv();
			break;
		case D.SRelPl:
			applyRelPl();
			break;
		case D.SApplPl:
			applyApplPl();
			break;
		case D.SSelAppl:
			applySelAppl();
			break;
		case D.SAddIM:
			applyAddIM();
			break;
		case D.SProcAct:
			applyProcAct();
			break;
		case D.SSelInt:
			applySelInt();
			break;
		case D.SExecInt:
			applyExecInt();
			break;
		case D.SClrInt:
			applyClrInt();
			break;
		}
	}

	// the semantic rules are referred to in comments in the functions below

	private void applyProcMsg() throws JasonException {
		if (!conf.C.MB.isEmpty()) {
			Message m = conf.ag.selectMessage(conf.C.MB);

            // check if an intention was suspended waiting this message
            Intention intention = (Intention)getC().getPendingActions().remove(m.getInReplyTo());
            // is it a pending intention?
            if (intention != null) {
            	// unify the answer with the parameter
            	Term ans = Term.parse(m.getPropCont());
            	BodyLiteral send = (BodyLiteral)intention.peek().getPlan().getBody().remove(0);
            	intention.peek().getUnif().unifies(send.getLiteral().getTerm(3),ans);
                getC().getIntentions().add(intention);
                
            // the message is not an ask answer
            } else if (conf.ag.socAcc(m)) {
				Term content = Term.parse(m.getPropCont());
				
				//Literal content = Literal.parseLiteral(m.getPropCont());
				//content.addAnnot(Term.parse("source("+m.getSender()+")")); 
				// if it has the source, the context fail for askIf/One.
				// so, the source is added by the receiver plan
	
				Intention focus = D.EmptyInt;
	
				// generate an event
				Literal received = new Literal(D.LPos, new Pred("received"));
				received.addTerm(new Term(m.getSender()));
				received.addTerm(new Term(m.getIlForce()));
				received.addTerm(content);
				received.addTerm(new Term(m.getMsgId()));
				
				Event evt = new Event(new Trigger(D.TEAdd, D.TEBel, received), focus);
				//System.out.println("event = "+evt);
				conf.ag.updateEvents(evt, conf.C);
			}
			
			// old version
			/*
			if (m.getIlForce().equals("tell")) {
				// Rule TellRec
				if (conf.ag.acceptTell(m.getSender(), m.getPropCont())) {
					conf.ag.addBel(m.getPropCont(), m.getSender(), conf.C);
				}
				// Rule TellRec2 (nothing to do, and similarly for the others)
			} else if (m.getIlForce().equals("untell")) {
				// Rule UnTellRec
				if (conf.ag.acceptTell(m.getSender(), m.getPropCont())) {
					conf.ag.delBel(m.getPropCont(), m.getSender(), conf.C);
				}
			} else if (m.getIlForce().equals("achieve")) {
				// Rule AchieveRec
				if (conf.ag.acceptAchieve(m.getSender(), m.getPropCont())) {
					conf.C.addAchvGoal(m.getPropCont(), D.EmptyInt);
				}
			} else if (m.getIlForce().equals("unachieve")) {
				// Rule UnAchieveRec
				// IMPORTANT: this semantic rule is not good, what to do
				// exactly?
				if (conf.ag.acceptAchieve(m.getSender(), m.getPropCont())) {
					conf.C.delAchvGoal(m.getPropCont(), D.EmptyInt);
				}
			} else if (m.getIlForce().equals("tellHow")) {
				// Rule TellHowRec
				if (conf.ag.acceptTell(m.getSender(), m.getPropCont())) {
					conf.ag.addPlan(m.getPropCont(), m.getSender());
				}
			} else if (m.getIlForce().equals("untellHow")) {
				// Rule UnTellHowRec
				if (conf.ag.acceptTell(m.getSender(), m.getPropCont())) {
					conf.ag.delPlan(m.getPropCont(), m.getSender());
				}
			}
			*/
		}
		confP.step = D.SSelEv;
	}

	private void applySelEv() throws JasonException {
		// Rule SelEv1
		if (!conf.C.E.isEmpty()) {
			confP.C.SE = conf.ag.selectEvent(confP.C.E);
			confP.step = D.SRelPl;
		}
		// Rule SelEv2
		else {
			// directly to ProcAct if no event to handle
			confP.step = D.SProcAct;
		}
	}

	private void applyRelPl() throws JasonException {
		// get all relevant plans for the selected event
		confP.C.RP = relevantPlans(conf.C.SE.trigger);
		//System.out.println("\tRP for "+conf.C.SE.trigger+" are "+confP.C.RP);
		
		// Rule Rel1
		if (!confP.C.RP.isEmpty() || setts.retrieve()) { // retrieve is mainly for Coo-AgentSpeak
			confP.step = D.SApplPl;
		}
		// Rule Rel2
		else {
			if (conf.C.SE.trigger.isGoal()) {
				generateGoalDeletionFromEvent();
				System.err.println("*** Warning! Found an internal event for which there is no relevant plan:\n"+ conf.C.SE);
			}
			// e.g. goal addition as internal event, just go ahead
			else if (conf.C.SE.isInternal()) {
				confP.C.SI = conf.C.SE.intention;
				updateIntention();
			}
			// if external, then needs to check settings
			else if (setts.requeue()) {
				confP.C.addEvent(conf.C.SE);
			}
			confP.step = D.SProcAct;
		}
	}

	private void applyApplPl() throws JasonException {
		confP.C.AP = applicablePlans(new ArrayList(confP.C.RP));

		//System.out.println("\tRP="+confP.C.RP+"\n\tAP="+confP.C.AP);
		// Rule Appl1
		if (!confP.C.AP.isEmpty() || setts.retrieve()) { // retrieve is mainly fo Coo-AgentSpeak
			confP.step = D.SSelAppl;
		} else { // Rule Appl2
			generateGoalDeletionFromEvent(); // can't carry on, no applicable plan.
			confP.step = D.SProcAct;
		}
	}

	private void applySelAppl() throws JasonException {
		// Rule SelAppl
		confP.C.SO = conf.ag.selectOption(confP.C.AP);
		if (confP.C.SO != null) {
			confP.step = D.SAddIM;
		} else {
			System.err.println("*** Warning! selectOption returned null.");
			generateGoalDeletionFromEvent(); // can't carry on, no applicable plan.
			confP.step = D.SProcAct;
		}
	}

	private void applyAddIM() throws JasonException {
		// create a new intended means
		IntendedMeans im = new IntendedMeans(conf.C.SO);

		// Rule ExtEv
		if (conf.C.SE.intention == D.EmptyInt) {
			Intention intention = new Intention();
			intention.push(im);
			confP.C.I.add(intention);
		}
		// Rule IntEv
		else {
			confP.C.SE.intention.push(im);
			confP.C.I.add(confP.C.SE.intention);
		}
		confP.step = D.SProcAct;
	}

	private void applyProcAct() throws JasonException {
		if (!conf.C.FA.isEmpty()) {
			ActionExec a = conf.ag.selectAction(conf.C.FA);
			confP.C.SI = a.getIntention();
			if (a.getResult()) {
				updateIntention();
			} else {
				generateGoalDeletion();
			}
			confP.step = D.SClrInt;
		} else {
			confP.step = D.SSelInt;
		}
	}

	private void applySelInt() throws JasonException {

		// TODO we need to have a look if there isn't
		// a more efficient way of doing the Atomic thing. This adds
		// a search linear in the size of the set of intentions
		// at every resoning cycle, right? can't we use a flag
		// just to remember that there is an atomic to search for?

		// TODO JOMI isn't it better if selectAtomicIntention() is defined
		// in this class and not in the circumstance?
		
		// Rule for Atomic Intentions
		confP.C.SI = conf.C.selectAtomicIntention();
		if (confP.C.SI != null) {
			confP.step = D.SExecInt;
			return;
		}

		// Rule SelInt1
		if (!conf.C.I.isEmpty()) {
			confP.C.SI = conf.ag.selectIntention(conf.C.I);
			confP.step = D.SExecInt;
			return;
		}
		
		confP.step = D.SStartRC;
	}


	static Class classParameters[] = { jason.asSemantics.TransitionSystem.class, jason.asSemantics.Unifier.class, (new String[3]).getClass() };
	private Map agInternalAction = new HashMap(); // this agent internal actions (key->IA'name, value->InternalAction object)
	
	public boolean execInternalAction(Term action, Unifier un) throws JasonException {
		String name = action.getFunctor();
		if (name.indexOf('.') == 0)
			name = "jason.stdlib" + name;
		
		// if it implements InternalAction
		try {
			// check if  the agent already has this InternalAction object
			InternalAction objIA = (InternalAction)agInternalAction.get(name);
			if (objIA == null) {
				objIA = (InternalAction)Class.forName(name).newInstance();
				agInternalAction.put(name, objIA);
			}
			// calls execute
			return objIA.execute(this, un, action.getTermsArray());
			
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (ClassCastException e) {
			// tries it as old internal action (static + string pars)
			String pars[] = null;
			if (action.getTerms() == null) {
				pars = new String[0];
			} else {
				pars = new String[action.getTerms().size()];
				int i = 0;
				Iterator j = action.getTerms().iterator();
				while (j.hasNext()) {
					pars[i++] = j.next().toString();
				}
			}
			try {
				Class classDef = Class.forName(name);
				Method executeMethod = classDef.getDeclaredMethod("execute", classParameters);
				Object objectParameters[] = { this, un, pars };
				// Static method, no instance needed
				return ((Boolean) executeMethod.invoke(null, objectParameters)).booleanValue();
			} catch (Exception e2) {
				throw new JasonException("The method execute does not exists in class " + name);
			}
	
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private void applyExecInt() throws JasonException {
		// get next formula in the body of the intended means
		// on the top of the selected intention
		IntendedMeans im = conf.C.SI.peek();
		if (im.getPlan().getBody().size() == 0) { // for empty plans! may need unnif, etc
			updateIntention();
		} else {
			Unifier     u = im.unif;
			BodyLiteral h = (BodyLiteral) im.getPlan().getBody().get(0);
		
			Term bodyTerm = (Term)h.getLiteral().clone();
			Literal l;
			if (bodyTerm.isVar()) {
				u.apply(bodyTerm);
				l = (Literal) ((VarTerm)bodyTerm).getValue();
			} else {
				l = (Literal) bodyTerm;
			}
			u.apply(l);
			
			switch (h.getType()) {
			
			// Rule Action
			case D.HAction:
				if (l.isInternalAction()) {
					if (execInternalAction(l, u)) {
						if (!h.isAsk()) {
							updateIntention();
						}
					}
					else {
						generateGoalDeletion();
					}
				} else {
					confP.C.A = new ActionExec((Pred) l, conf.C.SI);
				}
				break;
				
			// Rule Achieve
			case D.HAchieve:
				conf.C.addAchvGoal(l, conf.C.SI);
				break;
				
			// Rule Test
			case D.HTest:
				Unifier ubel = conf.ag.believes(l, u);
				if (ubel != null) {
					im = conf.C.SI.peek();
					im.unif = ubel;
					updateIntention();
				} else {
					System.err.println("*** Warning! Test Goal '"+h+"' failed as simple query. Generating internal event for it...");
					u.apply(l);
					conf.C.addTestGoal(l, conf.C.SI);
				}
				break;
				
			// Rule AddBel
			case D.HAddBel:
				
				// translate l to a string and parse again to identify
				// problems such as:
				//    X = ~p(a); +p(X)
				l = Literal.parseLiteral(l.toString());
				if (l != null) {
					//System.out.println("*** adding "+l);			
					if (setts.sameFocus())
						conf.ag.addBel(l, D.TSelf, conf.C, conf.C.SI);
					else {
						// TODO: Must COPY the whole intention, for the newFocus!!!!
						// JOMI: acho que esta msg e' velha e nem faz sentido.
						// Nao vejo porque copiar a intention se e' NewFocus
						// Se concordas que isto nao deve ser feito, remove este todo.
						conf.ag.addBel(l, D.TSelf, conf.C, D.EmptyInt);
						updateIntention();
					}
				}
				break;
				
			// Rule DelBel
			case D.HDelBel:
				ubel = conf.ag.believes((Literal)l, u);
				//System.out.println("****00-"+ubel);
				if (ubel != null) {
					ubel.apply(l);
					//System.out.println("****11-"+l);
					if (setts.sameFocus())
						conf.ag.delBel(l, D.TSelf, conf.C, conf.C.SI);
					else {
						conf.ag.delBel(l, D.TSelf, conf.C, D.EmptyInt);
						updateIntention();
					}
				} else
					generateGoalDeletion(); //
				break;
			}
		}
		confP.step = D.SClrInt;
	}

	private void applyClrInt() throws JasonException {
		// Rule ClrInt
		IntendedMeans im = conf.C.SI.peek();
		if (im.getPlan().getBody().isEmpty()) {
			if (conf.C.SI.size() > 1) {
				IntendedMeans oldim = confP.C.SI.pop();
				im = conf.C.SI.peek();
				BodyLiteral g = (BodyLiteral) im.getPlan().getBody().remove(0);
				// use unifier of finished plan accordingly
				im.unif.compose(g.getLiteral(), oldim.unif);
				confP.step = D.SClrInt; // the new top may have become
				// empty! need to keep checking.
			} else {
				confP.C.I.remove(conf.C.SI);
				conf.C.SI = null;
				confP.step = D.SStartRC;
			}
		} else {
			confP.step = D.SStartRC;
		}
	}

	/** ******************************************* */
	/* auxiliary functions for the semantic rules */
	/** ******************************************* */

	public List relevantPlans(Trigger te) throws JasonException {
		List rp = new ArrayList();
		List candidateRPs = conf.ag.fPS.getAllRelevant(te);
		if (candidateRPs == null)
			return rp;
		for (int i=0; i < candidateRPs.size(); i++) {
			Plan pl = (Plan) candidateRPs.get(i);
			Unifier relUn = pl.relevant(te);
			if (relUn != null) {
				//System.out.println("Add="+te+"|"+pl+"|"+relUn);
				rp.add(new Option(pl, relUn));
			}
		}
		return rp;
	}

	public List applicablePlans(List rp) throws JasonException {
		for (Iterator i = rp.iterator(); i.hasNext();) {
			Option opt = (Option) i.next();
			opt.unif = logCons(opt.plan.getContext().iterator(), opt.unif);
			if (opt.unif == null) {
				i.remove();
			}
		}
		return rp;
	}

	/** 
	 * logCons checks whether one particular predicate
	 * is a log(ical)Cons(equence) of the belief base.
	 * It is used in the method that checks whether the plan is applicable.
	 */
	private Unifier logCons(Iterator ctxt, Unifier un) throws JasonException {

		if (!ctxt.hasNext()) {
			return un;
		}

		DefaultLiteral dfl = (DefaultLiteral) ctxt.next();

		Term dflTerm = (Term)dfl.getLiteral().clone();
		Literal l;
		if (dflTerm.isVar()) {
			un.apply(dflTerm);
			l = (Literal) ((VarTerm)dflTerm).getValue();
		} else {
			l = (Literal) dflTerm;
		}
		un.apply(l); // in case we have ... & X & ...
		
		if (l.isInternalAction()) {
			boolean execOk = execInternalAction((Pred) l, un);
			if ((!execOk && !dfl.isDefaultNegated()) 
				|| (execOk && dfl.isDefaultNegated())) {
				return null;
			} else { 
				return logCons(ctxt, un);
			}
		}

		// is not an internal action
		
		List relB = ag.getBS().getRelevant(l);


		if (dfl.isDefaultNegated()) {
			if (relB != null) {
				// only goes ahead (recursively) if can't unify with any
				// predicate
				for (int i=0; i < relB.size(); i++) {
					Literal b = (Literal) relB.get(i);
					// check if literal unifies with belief: order is
					// important as normally literals have no annotations,
					// meaning any belief (from any source) will do
					if (un.unifies(l, b)) // getRelevant already
						// takes care of type of  literal
						return null;
				}
			}
			// negated literals do not change the unification, OK to use un
			return logCons(ctxt, un);
		} else {
			if (relB == null) {
				return null;
			}
			for (int i=0; i < relB.size(); i++) {
				Literal b = (Literal) relB.get(i);
				// here we need a copy of the present unification
				// so that we can "backtrack"
				Unifier unC = (Unifier) un.clone();
				// notice the importance of the order here again (see above)
				if (unC.unifies(l,b)) {
					Unifier res = logCons(ctxt, unC);
					if (res != null)
						// found unification res that makes the plan
						// applicable
						return res;
					else
						// this unification didn't work, try next
						continue;
				} else
					// this predicate doesn't unify with "l", try next
					continue;
			}
			// attempted all (relevant) predicates and the plan is not
			// applicable
			return null;
		}
	}

	/** remove the top action and requeue the current intention */
	private void updateIntention() {
		IntendedMeans im = conf.C.SI.peek();
		if (!im.getPlan().getBody().isEmpty()) // maybe it had an empty plan body
			im.getPlan().getBody().remove(0);
		confP.C.I.add(conf.C.SI);
	}

	private void generateGoalDeletion() throws JasonException {
		IntendedMeans im = conf.C.SI.peek();
		Trigger tevent = im.getPlan().getTriggerEvent();
		if (tevent.isAddition() && tevent.isGoal())
			confP.C.delGoal(tevent.getGoal(), tevent, conf.C.SI); // intention
		// will be suspended
		// if "discard" is set, we are deleting the whole intention!
		// it is simply not going back to I nor anywhere else!
		else if (setts.requeue()) {
			// get the external event (or the one that started
			// the whole focus of attentiont) and requeue it
			im = conf.C.SI.get(0);
			Trigger tr = (Trigger) tevent.clone();
			im.unif.apply((Pred) tr);
			confP.C.addExternalEv(tr);
		} else
			System.err.println("*** "+conf.agArch.getName()+" - Warning! Could not finish intention: " + conf.C.SI);
	}

	// similar to the one above, but for an Event rather than intention
	private void generateGoalDeletionFromEvent() throws JasonException {
		Event ev = conf.C.SE;
		if (ev.trigger.isAddition() && ev.trigger.isGoal() && ev.isInternal()) {
			confP.C.delGoal(ev.getTrigger().getGoal(), (Literal) ev.trigger, ev.intention);
		}
		if (ev.isInternal()) {
			System.err.println("*** "+conf.agArch.getName()+" -- warning! Could not finish intention: " + ev.intention);
		}
		// if "discard" is set, we are deleting the whole intention!
		// it is simply not going back to I nor anywhere else!
		else if (setts.requeue()) {
			confP.C.addEvent(ev);
		} else
			System.err.println("*** Warning! Discarding external event : " + ev);
	}

	/** ********************************************************************* */

	boolean canSleep() {
		if (conf.C.E.isEmpty() && conf.C.I.isEmpty() && conf.C.MB.isEmpty()
				&& conf.C.FA.isEmpty())
			return true;
		else
			return false;
	}

	/** waits for a new message */
	synchronized private void waitMessage() {
		try {
			wait(500); // wait for messages
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	synchronized public void newMessageHasArrived() {
		notifyAll(); // notify waitMessage method
	}

	private Object syncMonitor = new Object(); // an object to synchronize the waitSignal and syncSignal
	private boolean inWaitSyncMonitor = false;
	
	/** waits for a signal to continue the execution (used in synchronized execution mode) */
	private void waitSyncSignal() {
		try {
			//System.out.println("*** "+agArch.getName()+" waiting sync");
			synchronized(syncMonitor) {
				inWaitSyncMonitor = true;
				syncMonitor.wait();
				inWaitSyncMonitor = false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** inform this agent that it can continue, if it is in sync mode and wainting a signal */
	public void receiveSyncSignal() {
		try {
			//System.out.println("*** "+agArch.getName()+"receivied go");
			synchronized(syncMonitor) {
				while (!inWaitSyncMonitor) {
					syncMonitor.wait(50); // waits the agent to enter in waitSyncSignal
					try {
						if ( ! ((CentralisedAgArch)agArch).isRunning() ) {
							break;
						}
					} catch (Exception e) {}
					try {
						if ( ! ((SaciAgArch)agArch).isRunning() ) {
							break;
						}
					} catch (Exception e) {}
				}
				
				syncMonitor.notifyAll();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/** ******************************************************************* */
	/* MAIN LOOP */
	/** ******************************************************************* */
	/* infinite loop on one reasoning cycle */
	/* plus the other parts of the agent architecture besides */
	/* the actual transition system of the AS interpreter */
	/** ******************************************************************* */
	public void reasoningCycle() {
		try {

			if (setts.isSync()) {
				waitSyncSignal();
			} else if (canSleep()) {
				waitMessage();
			}
			
			C.reset();
			
			if (setts.verbose() >= 5)
				System.out.println(agArch.getName() + " perceiving...");
			List percept = agArch.perceive();

			if (setts.verbose() >= 5)
				System.out.println(agArch.getName() + " checking mail...");
			agArch.checkMail();

			if (setts.verbose() >= 5)
				System.out.println(agArch.getName() + " doing belief revision...");
			ag.brf(percept);

			if (setts.verbose() == 2) {
				System.out.println(agArch.getName() + " Beliefs:    " + ag.fBS);
				System.out.println(agArch.getName() + " Intentions: " + C.I);
			} else if (setts.verbose() >= 3) {
				System.out.println(agArch.getName() + " Beliefs:      " + ag.fBS);
				System.out.println(agArch.getName() + " Plans:        " + ag.fPS);
				System.out.println(agArch.getName() + " Desires:      " + C.E);
				System.out.println(agArch.getName() + " Intentions:   " + C.I);
			}

			do {
				if (setts.verbose() >= 6)
					System.out.println(agArch.getName() + " Circumstance: " + C);
				if (setts.verbose() >= 4)
					System.out.println(agArch.getName() + " Step:         " + D.SRuleNames[conf.step]);

				applySemanticRule();
			} while (step != D.SStartRC); // finished a reasoning cycle

			if (setts.verbose() >= 5)
				System.out.println(agArch.getName() + " acting");
			
			agArch.act();

			if (setts.verbose() >= 2)
				System.out.println();

			if (setts.isSync()) {
				agArch.informCycleFinished();
			}
			
		} catch (Exception e) {
			System.err.println("*** ERROR detected at transition system: ");
			e.printStackTrace();
		}
	}

	// Auxiliary functions
	// (for Internal Actions to be able to access the configuration)
	public Agent getAg() {
		return ag;
	}

	public Circumstance getC() {
		return C;
	}

	public byte getStep() {
		return step;
	}

	public Settings getSettings() {
		return setts;
	}

	public AgentArchitecture getAgArch() {
		return agArch;
	}
}
