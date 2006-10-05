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


@kqmlReceivedTell
+!kqmlReceived(S, tell, KQMLcontentVar, M) : true <- .addAnnot(KQMLcontentVar, source(S), CA); +CA.
@kqmlReceivedUnTell
+!kqmlReceived(S, untell, KQMLcontentVar, M) : true <- .addAnnot(KQMLcontentVar, source(S), CA); -CA.

@kqmlReceivedAchieve
+!kqmlReceived(S, achieve, KQMLcontentVar, M) : true 
   <- .addAnnot(KQMLcontentVar, source(S), CA); !CA.
@kqmlReceivedUnAchieve
+!kqmlReceived(S, unachieve, KQMLcontentVar, M) : true 
   <- // TODO: does not work .addAnnot(KQMLcontentVar, source(S), CA);
      .dropGoal(CA, false).

@kqmlReceivedAskOne1
+!kqmlReceived(S, askOne, KQMLcontentVar, M) : true
   <- ?KQMLcontentVar;
      .send(S, tell, KQMLcontentVar, M).
@kqmlReceivedAskOne2 // erro in askOne
-!kqmlReceived(S, askOne, _, M) : true
   <- .send(S, tell, false, M).      

@kqmlReceivedAskAll
+!kqmlReceived(S, askAll, value(Var,KQMLcontentVar), M) : true    
   <- .findall(Var, KQMLcontentVar, List); 
      .send(S, tell, List, M).

// In tellHow, KQMLcontentVar must be a string representation of the plan 
// (or a list of strings)
@kqmlReceivedTellHow
+!kqmlReceived(S, tellHow, KQMLcontentVar, M) : true <- .addPlan(KQMLcontentVar, S).

// In untellHow, KQMLcontentVar must be a plan's label (or a list of labels)
@kqmlReceivedUnTellHow
+!kqmlReceived(S, untellHow, KQMLcontentVar, M) : true <- .removePlan(KQMLcontentVar, S).

// In askHow, KQMLcontentVar must be a string representation of the trigger event
@kqmlReceivedAskHow
+!kqmlReceived(S, askHow, KQMLcontentVar, M) : true    
   <- .getRelevantPlans(KQMLcontentVar, ListAsString); 
      .send(S, tellHow, ListAsString, M).
