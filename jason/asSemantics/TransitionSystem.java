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
// CVS information:
//   $Date$
//   $Revision$
//   $Log$
//   Revision 1.24  2005/10/19 15:09:49  bordini
//   Fixed 2 bugs related to the plan failure mechanism:
//     - generated event, in case a plan failed by an action, was
//       not post as originally (lacked unification), see generateGoalDeletion,
//       rather than generateGoalDeletionFromEvent
//     - old (faild) plan which is kept in the intention wasn't removed
//       when the goal deletion plan finished (ClrInt)
//   To solve the first problem, the IntendedMeans class now has an extra
//   member variable called "Trigger" which record the original event.
//
//   Revision 1.23  2005/09/26 11:46:25  jomifred
//   fix bug with source add/remove
//
//   Revision 1.22  2005/08/16 21:03:42  jomifred
//   add some comments on TODOs
//
//   Revision 1.21  2005/08/15 17:41:36  jomifred
//   AgentArchitecture renamed to AgArchInterface
//
//   Revision 1.20  2005/08/12 23:29:11  jomifred
//   support for saci arch in IA createAgent
//
//   Revision 1.19  2005/08/12 22:18:37  jomifred
//   add cvs keywords
//
//
//----------------------------------------------------------------------------

package jason.asSemantics;

import jason.JasonException;
import jason.Settings;
import jason.architecture.AgArchInterface;
import jason.asSyntax.BeliefBase;
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

import org.apache.log4j.Logger;

public class TransitionSystem {

    static final byte      SStartRC   = 0;
    static final byte      SProcMsg   = 0;
    static final byte      SSelEv     = 1;
    static final byte      SRelPl     = 2;
    static final byte      SApplPl    = 3;
    static final byte      SSelAppl   = 4;
    static final byte      SAddIM     = 5;
    static final byte      SProcAct   = 6;
    static final byte      SSelInt    = 7;
    static final byte      SExecInt   = 8;
    static final byte      SClrInt    = 9;

    static final String[]  SRuleNames = { "ProcMsg", "SelEv", "RelPl",
                                          "ApplPl", "SelAppl", "AddIM",
                                          "ProcAct", "SelInt", "ExecInt",
                                          "ClrInt" };

	
	
	private Logger logger = null;	
	
	Agent ag = null;

	Circumstance C = null;

	Settings setts = null;

	AgArchInterface agArch = null;

	private byte step = SStartRC; // First step of the SOS
	private int nrcslbr; // number of reasoning cycles since last belief revision

	// both configuration and configuration' point to this
	// object, this is just to make it look more like the SOS
	TransitionSystem confP;

	// both configuration and configuration' point to this
	// object, this is just to make it look more like the SOS
	TransitionSystem conf;

	public TransitionSystem(Agent a, Circumstance c, Settings s, AgArchInterface ar) {
		ag = a;
		C = c;
		agArch = ar;

		if (s == null) {
			setts = new Settings();
		} else {
			setts = s;
		}

		// we need to initialise this "aliases"
		conf = confP = this;

		nrcslbr = setts.nrcbp(); // to do BR to start with
		
		setLogger(agArch);
		if (setts != null) {
			logger.setLevel(setts.log4JLevel());
		}
	}
	
	public void setLogger(AgArchInterface arch) {
		if (arch != null) {
			logger = Logger.getLogger(TransitionSystem.class.getName()+"."+arch.getAgName());
		} else {
			logger = Logger.getLogger(TransitionSystem.class.getName());			
		}
	}


	/********************************************************************* */
	/* SEMANTIC RULES */
	/********************************************************************* */
	private void applySemanticRule() throws JasonException {
		// check the current step in the reasoning cycle
		// only the main parts of the interpretation appear here
		// the individual semantic rules appear below
		
		switch (conf.step) {
		case SProcMsg:
			applyProcMsg();
			break;
		case SSelEv:
			applySelEv();
			break;
		case SRelPl:
			applyRelPl();
			break;
		case SApplPl:
			applyApplPl();
			break;
		case SSelAppl:
			applySelAppl();
			break;
		case SAddIM:
			applyAddIM();
			break;
		case SProcAct:
			applyProcAct();
			break;
		case SSelInt:
			applySelInt();
			break;
		case SExecInt:
			applyExecInt();
			break;
		case SClrInt:
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
            	// unify the message answer with the .send fourth parameter
				// the send that put the intention in Pending state was something like
				//  .send(ask, ag1, value, X)
				// if the answer was 3, unifies X=3
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
	
				Intention focus = Intention.EmptyInt;
	
				// generate an event
				Literal received = new Literal(Literal.LPos, new Pred("received"));
				received.addTerm(new Term(m.getSender()));
				received.addTerm(new Term(m.getIlForce()));
				received.addTerm(content);
				received.addTerm(new Term(m.getMsgId()));
				
				Event evt = new Event(new Trigger(Trigger.TEAdd, Trigger.TEBel, received), focus);
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
		confP.step = SSelEv;
	}

	private void applySelEv() throws JasonException {
		// Rule SelEv1
		if (!conf.C.E.isEmpty()) {
			confP.C.SE = conf.ag.selectEvent(confP.C.E);
			confP.step = SRelPl;
		}
		// Rule SelEv2
		else {
			// directly to ProcAct if no event to handle
			confP.step = SProcAct;
		}
	}

	private void applyRelPl() throws JasonException {
		// get all relevant plans for the selected event
		confP.C.RP = relevantPlans(conf.C.SE.trigger);
		
		// Rule Rel1
		if (!confP.C.RP.isEmpty() || setts.retrieve()) { // retrieve is mainly for Coo-AgentSpeak
			confP.step = SApplPl;
		}
		// Rule Rel2
		else {
			if (conf.C.SE.trigger.isGoal()) {
				generateGoalDeletionFromEvent();
				logger.warn("Found a goal for which there is no relevant plan:\n"+ conf.C.SE);
			}
			// e.g. belief addition as internal event, just go ahead
			else if (conf.C.SE.isInternal()) {
				confP.C.SI = conf.C.SE.intention;
				updateIntention();
			}
			// if external, then needs to check settings
			else if (setts.requeue()) {
				confP.C.addEvent(conf.C.SE);
			}
			confP.step = SProcAct;
		}
	}

	private void applyApplPl() throws JasonException {
		if (confP.C.RP == null) {
			logger.warn("applyPl was called even RP is null!");
			confP.step = SProcAct;
			return;
		}
		confP.C.AP = applicablePlans(new ArrayList(confP.C.RP));

		// Rule Appl1
		if (!confP.C.AP.isEmpty() || setts.retrieve()) { // retrieve is mainly fo Coo-AgentSpeak
			confP.step = SSelAppl;
		} else { // Rule Appl2
			if (conf.C.SE.trigger.isGoal()) {
				generateGoalDeletionFromEvent(); // can't carry on, no applicable plan.
				logger.warn("Found a goal for which there is no applicable plan:\n"+ conf.C.SE);
			}
			// e.g. belief addition as internal event, just go ahead
			// but note that the event was relevant, yet it is possible
			// the programmer just wanted to add the belief and it was
			// relevant by chance, so just carry on instead of dropping the intention
			// TODO: RECONSIDER THIS PROBLEM IN THE SEMANTICS!
			else if (conf.C.SE.isInternal()) {
				confP.C.SI = conf.C.SE.intention;
				updateIntention();
			}
			// if external, then needs to check settings
			else if (setts.requeue()) {
				confP.C.addEvent(conf.C.SE);
			}
			confP.step = SProcAct;
		}
	}

	private void applySelAppl() throws JasonException {
		// Rule SelAppl
		confP.C.SO = conf.ag.selectOption(confP.C.AP);
		if (confP.C.SO != null) {
			confP.step = SAddIM;
		} else {
			logger.warn("selectOption returned null.");
			generateGoalDeletionFromEvent(); // can't carry on, no applicable plan.
			confP.step = SProcAct;
		}
	}

	private void applyAddIM() throws JasonException {
		// create a new intended means
		IntendedMeans im = new IntendedMeans(conf.C.SO);
		im.setTrigger(conf.C.SE.getTrigger());

		// Rule ExtEv
		if (conf.C.SE.intention == Intention.EmptyInt) {
			Intention intention = new Intention();
			intention.push(im);
			confP.C.I.add(intention);
		}
		// Rule IntEv
		else {
			confP.C.SE.intention.push(im);
			confP.C.I.add(confP.C.SE.intention);
		}
		confP.step = SProcAct;
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
			confP.step = SClrInt;
		} else {
			confP.step = SSelInt;
		}
	}

	private void applySelInt() throws JasonException {

		// TODO we need to have a look if there isn't
		// a more efficient way of doing the Atomic thing. This adds
		// a search linear in the size of the set of intentions
		// at every resoning cycle, right? can't we use a flag
		// just to remember that there is an atomic to search for?
		
		// RAFA: see the new imple of selectAtomicIntention below. Does
		// it do what you want? 
		// If so, remove this "todo".
		// JOMI: Are you sure you can use the conf.C.SI from the
		// previous reasoning cycle? I'm not sure this isn't changed
		// in some of the rules. And even if it works, it still doesn't
		// do what I mean, but it's not important, doesn't need to be
		// done now. If there is NOT an atomic intention already
		// selected, it is still checking every single intention
		// trying to find an atomic one.
		// Wouldn't it be more efficient to have a flag which is set
		// whenever a plan with [atomic] become intended so that we
		// "remember" that it worth searching for an atomic? Do you
		// understand what I mean?
		// RAFA: this flag is just the last C.SI and
		// C.SI.isAtomic (see implementation of this
		// method, it is not computed every time)?
		// I guess the current implementation already select
		// the last atomic with searching all intention.
		// However, I agree that using C.SI may be "unsafe".
		
		// Rule for Atomic Intentions
		confP.C.SI = selectAtomicIntention();
		if (confP.C.SI != null) {
			confP.step = SExecInt;
			return;
		}

		// Rule SelInt1
		if (!conf.C.I.isEmpty()) {
			confP.C.SI = conf.ag.selectIntention(conf.C.I);
			confP.step = SExecInt;
			return;
		}
		
		confP.step = SStartRC;
	}

	public Intention selectAtomicIntention() {
		if (conf.C.SI != null && conf.C.SI.isAtomic()) {
			return conf.C.SI;
		}
		Iterator i = conf.C.getIntentions().iterator();
		while (i.hasNext()) {
			Intention inte = (Intention)i.next();
			if (inte.isAtomic()) {
				i.remove();
				return inte;
			}
		}
		return null;
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
			logger.error("Error in IA ",e);
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
				throw new JasonException("Method execute does not exists in class " + name);
			}
	
		} catch (Exception e) {
			logger.error("Error in IA ",e);
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
			case BodyLiteral.HAction:
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
			case BodyLiteral.HAchieve:
				conf.C.addAchvGoal(l, conf.C.SI);
				break;
				
			// Rule Test
			case BodyLiteral.HTest:
				Unifier ubel = conf.ag.believes(l, u);
				if (ubel != null) {
					im = conf.C.SI.peek();
					im.unif = ubel;
					updateIntention();
				} else {
					logger.warn("Test Goal '"+h+"' failed as simple query. Generating internal event for it...");
					u.apply(l);
					conf.C.addTestGoal(l, conf.C.SI);
				}
				break;
				
			// Rule AddBel
			case BodyLiteral.HAddBel:
				
				// translate l to a string and parse again to identify
				// problems such as:
				//    X = ~p(a); +p(X)
				l = Literal.parseLiteral(l.toString());
				if (l != null) {
					Term source = BeliefBase.TSelf;
					if (l.hasSource()) {
						source = null; // do not add source(self) in case the programmer set the source
					}
					if (setts.sameFocus())
						conf.ag.addBel(l, source, conf.C, conf.C.SI);
					else {
						conf.ag.addBel(l, source, conf.C, Intention.EmptyInt);
						updateIntention();
					}
				}
				break;
				
			// Rule DelBel
			case BodyLiteral.HDelBel:
				ubel = conf.ag.believes((Literal)l, u);
				if (ubel != null) {
					ubel.apply(l);
					Term source = BeliefBase.TSelf;
					if (l.hasSource()) {
						source = null; // do not add source(self) in case the programmer set the source
					}
					if (setts.sameFocus())
						conf.ag.delBel(l, source, conf.C, conf.C.SI);
					else {
						conf.ag.delBel(l, source, conf.C, Intention.EmptyInt);
						updateIntention();
					}
				} else
					generateGoalDeletion(); //
				break;
			}
		}
		confP.step = SClrInt;
	}

	private void applyClrInt() throws JasonException {
		// Rule ClrInt
		confP.step = SStartRC; // default next step
		if (conf.C.SI != null) {
			IntendedMeans im = conf.C.SI.peek();
			if (im.getPlan().getBody().isEmpty()) {
				if (conf.C.SI.size() > 1) {
					IntendedMeans oldim = confP.C.SI.pop();
					if (im.getTrigger().isGoal() && !im.getTrigger().isAddition()) {
						// needs to get rid of the failed plan when finished handling failure
						confP.C.SI.pop();
					}
					im = conf.C.SI.peek();
					BodyLiteral g = (BodyLiteral) im.getPlan().getBody().remove(0);
					// use unifier of finished plan accordingly
					im.unif.compose(g.getLiteral(), oldim.unif);
					confP.step = SClrInt; // the new top may have become
					// empty! need to keep checking.
				} else {
					confP.C.I.remove(conf.C.SI);
					conf.C.SI = null;
				}
			}
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
		Trigger tevent = im.getTrigger();
		if (tevent.isAddition() && tevent.isGoal())
			confP.C.delGoal(tevent.getGoal(), tevent, conf.C.SI); // intention will be suspended
		// if "discard" is set, we are deleting the whole intention!
		// it is simply not going back to 'I' nor anywhere else!
		else if (setts.requeue()) {
			// get the external event (or the one that started
			// the whole focus of attentiont) and requeue it
			im = conf.C.SI.get(0);
			confP.C.addExternalEv(tevent);
		} else {
			logger.warn("Could not finish intention: " + conf.C.SI);
		}
	}

	// similar to the one above, but for an Event rather than intention
	private void generateGoalDeletionFromEvent() throws JasonException {
		Event ev = conf.C.SE;
		// TODO: double check all cases here
		if (ev.trigger.isAddition() && ev.trigger.isGoal() && ev.isInternal()) {
			confP.C.delGoal(ev.getTrigger().getGoal(), ev.getTrigger(), ev.intention);
			logger.warn("Generating goal deletion from event: " + ev);
		}
		else if (ev.isInternal()) {
			logger.warn("Could not finish intention: " + ev.intention);
		}
		// if "discard" is set, we are deleting the whole intention!
		// it is simply not going back to I nor anywhere else!
		else if (setts.requeue()) {
			confP.C.addEvent(ev);
			logger.warn("Requeing external event: " + ev);
		} else
			logger.warn("Discarding external event: " + ev);
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
			synchronized(syncMonitor) {
				while (!inWaitSyncMonitor) {
					syncMonitor.wait(50); // waits the agent to enter in waitSyncSignal
					if ( !agArch.isRunning() ) {
						break;
					}
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
				// changed here: now conditinal on NRCSLBR
				if(nrcslbr <= 1)
					waitMessage();
			}
			
			C.reset();

			if (nrcslbr >= setts.nrcbp() || canSleep()) {
				nrcslbr = 0;
				
				logger.debug("perceiving...");
				List percept = agArch.perceive();

				logger.debug("checking mail...");
				agArch.checkMail();

				logger.debug("doing belief revision...");
				ag.brf(percept);
			}

			/* use mind inspector to get these infs
			if (logger.isDebugEnabled()) {
				logger.debug("Beliefs:    " + ag.fBS);
				logger.debug("Intentions: " + C.I);
				logger.debug("Beliefs:      " + ag.fBS);
				logger.debug("Plans:        " + ag.fPS);
				logger.debug("Desires:      " + C.E);
				logger.debug("Intentions:   " + C.I);
			}
			*/

			do {
				/* use mind inspector to get these infs
				if (logger.isDebugEnabled()) {
					logger.debug("Circumstance: " + C);
					logger.debug("Step:         " + SRuleNames[conf.step]);
				}
				*/
				
				applySemanticRule();
			} while (step != SStartRC); // finished a reasoning cycle

			logger.debug("acting... ");
			agArch.act();

			// counting number of cycles since last belief revision
			nrcslbr++;
			
			if (setts.isSync()) {
				boolean isBreakPoint = false;
				try {
					isBreakPoint = getC().getSelectedOption().getPlan().getLabel().hasAnnot(Plan.TBreakPoint);
				} catch (Exception e) {
					// no problem, the plan has no label
					//logger.error("E!",e);
				}
				if (logger.isDebugEnabled()) {
					logger.debug("Informing controller that I finished a reasoning cycle. Breakpoint is "+isBreakPoint);
				}
				agArch.informCycleFinished(isBreakPoint);
			}
			
		} catch (Exception e) {
			logger.error("*** ERROR in the transition system: ",e);
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

	public AgArchInterface getAgArch() {
		return agArch;
	}
	
	public Logger getLogger() {
		return logger;
	}
}
