// Default plans to handle KQML performatives
// Users can override them in their own AS code
// 
// Do not change this file!
//
// Variables:
// S:   the sender
// M:   message id
// KQMLcontentVar: content (using a strange var name so as not to conflict with user's vars)
//
// CVS information:
//   $Date$
//   $Revision$
//   $Log$
//   Revision 1.2  2005/09/04 23:25:24  jomifred
//   add labels in plans
//


@kqmlReceiveTell
+received(S, tell  ,    KQMLcontentVar, M) : true <- .addAnnot(KQMLcontentVar, source(S), CA); +CA.
@kqmlReceiveUnTell
+received(S, untell,    KQMLcontentVar, M) : true <- .addAnnot(KQMLcontentVar, source(S), CA); -CA.

@kqmlReceiveAchieve
+received(S, achieve  , KQMLcontentVar, M) : true <- .addAnnot(KQMLcontentVar, source(S), CA); !CA.
@kqmlReceiveUnAchieve
+received(S, unachieve, KQMLcontentVar, M) : true <- .dropDesire(KQMLcontentVar).

@kqmlReceiveAskIf1
+received(S, askIf,     KQMLcontentVar, M) : KQMLcontentVar     <- .send(S, tell, true, M).
@kqmlReceiveAskIf2
+received(S, askIf,     KQMLcontentVar, M) : not KQMLcontentVar <- .send(S, tell, false, M).

@kqmlReceiveAskOne1
+received(S, askOne,    KQMLcontentVar, M) : KQMLcontentVar     <- .send(S, tell, KQMLcontentVar, M). // KQMLcontentVar unified
@kqmlReceiveAskOne2
+received(S, askOne,    KQMLcontentVar, M) : not KQMLcontentVar <- .send(S, tell, error, M).


@kqmlReceiveAskAll
+received(S, askAll,    value(Var,KQMLcontentVar), M) : true    
    <- .findall(Var, KQMLcontentVar, List); 
       .send(S, tell, List, M).

	// In tellHow, KQMLcontentVar must be a string representation of the plan (or a list of strings)
@kqmlReceiveTellHow
+received(S, tellHow  , KQMLcontentVar, M) : true <- .addPlan(KQMLcontentVar, S).

	// In untellHow, KQMLcontentVar must be a string representation of the plan (or a list of strings)
@kqmlReceiveUnTellHow
+received(S, untellHow, KQMLcontentVar, M) : true <- .removePlan(KQMLcontentVar, S).

    // In askHow, KQMLcontentVar must be a string representation of the trigger event
@kqmlReceiveAskHow
+received(S, askHow, KQMLcontentVar, M) : true    <- .getRelevantPlans(KQMLcontentVar, ListAsString); 
                                                     .send(S, tellHow, ListAsString, M).
