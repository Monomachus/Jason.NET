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


@kqmlReceiveTell
+!received(S, tell  ,    KQMLcontentVar, M) : true <- .addAnnot(KQMLcontentVar, source(S), CA); +CA.
@kqmlReceiveUnTell
+!received(S, untell,    KQMLcontentVar, M) : true <- .addAnnot(KQMLcontentVar, source(S), CA); -CA.

@kqmlReceiveAchieve
+!received(S, achieve  , KQMLcontentVar, M) : true <- .addAnnot(KQMLcontentVar, source(S), CA); !CA.
@kqmlReceiveUnAchieve
+!received(S, unachieve, KQMLcontentVar, M) : true <- .dropDesire(KQMLcontentVar).

@kqmlReceiveAskOne1
+!received(S, ask,    KQMLcontentVar, M) : KQMLcontentVar     <- .send(S, tell, KQMLcontentVar, M). // KQMLcontentVar unified
@kqmlReceiveAskOne2
+!received(S, ask,    KQMLcontentVar, M) : not KQMLcontentVar <- .send(S, tell, error, M).

@kqmlReceiveAskAll
+!received(S, askAll,    value(Var,KQMLcontentVar), M) : true    
    <- .findall(Var, KQMLcontentVar, List); 
       .send(S, tell, List, M).

// In tellHow, KQMLcontentVar must be a string representation of the plan 
// (or a list of strings)
@kqmlReceiveTellHow
+!received(S, tellHow  , KQMLcontentVar, M) : true <- .addPlan(KQMLcontentVar, S).

// In untellHow, KQMLcontentVar must be a plan's label (or a list of labels)
@kqmlReceiveUnTellHow
+!received(S, untellHow, KQMLcontentVar, M) : true <- .removePlan(KQMLcontentVar, S).

// In askHow, KQMLcontentVar must be a string representation of the trigger event
@kqmlReceiveAskHow
+!received(S, askHow, KQMLcontentVar, M) : true    <- .getRelevantPlans(KQMLcontentVar, ListAsString); 
                                                     .send(S, tellHow, ListAsString, M).
