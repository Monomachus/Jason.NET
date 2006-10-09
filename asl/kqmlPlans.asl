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


/* ---- tell performatives ---- */ 

@kqmlReceivedTellStructure
+!kqmlReceived(S, tell, KQMLcontentVar, M) 
   :  .structure(KQMLcontentVar) & 
      .ground(KQMLcontentVar) & 
       not .list(KQMLcontentVar)
   <- .addAnnot(KQMLcontentVar, source(S), CA); 
      +CA.
@kqmlReceivedTellList
+!kqmlReceived(S, tell, KQMLcontentVar, M) 
   :  .list(KQMLcontentVar)
   <- !addAllkqmlReceived(S,KQMLcontentVar).
@kqmlReceivedTellList1
+!addAllkqmlReceived(_,[]).   
@kqmlReceivedTellList2
+!addAllkqmlReceived(S,[H|T])
   :  .structure(H) & 
      .ground(H) & 
       not .list(H)
   <- .addAnnot(H, source(S), CA); 
      +CA;
      !addAllkqmlReceived(S,T).
@kqmlReceivedTellList3
+!addAllkqmlReceived(S,[_|T])
   <- !addAllkqmlReceived(S,T).
      
@kqmlReceivedUnTell
+!kqmlReceived(S, untell, KQMLcontentVar, M) : true 
   <- .addAnnot(KQMLcontentVar, source(S), CA); 
      -CA.


/* ---- achieve performatives ---- */ 

@kqmlReceivedAchieve
+!kqmlReceived(S, achieve, KQMLcontentVar, M) : true 
   <- .addAnnot(KQMLcontentVar, source(S), CA); 
      !CA.
@kqmlReceivedUnAchieve
+!kqmlReceived(S, unachieve, KQMLcontentVar, M) : true 
   <- .addAnnot(KQMLcontentVar, source(S), CA);
      .dropGoal(CA, false).


/* ---- ask performatives ---- */ 

@kqmlReceivedAskOne1
+!kqmlReceived(S, askOne, KQMLcontentVar, M) : true
   <- ?KQMLcontentVar;
      .send(S, tell, KQMLcontentVar, M).
@kqmlReceivedAskOne2 // error in askOne
-!kqmlReceived(S, askOne, _, M) : true
   <- .send(S, tell, false, M).      

@kqmlReceivedAskAll
+!kqmlReceived(S, askAll, value(Var,KQMLcontentVar), M) : true    
   <- .findall(Var, KQMLcontentVar, List); 
      .send(S, tell, List, M).


/* ---- tell how performatives ---- */ 

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
