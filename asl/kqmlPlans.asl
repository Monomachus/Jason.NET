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
+!kqml_received(S, tell, KQMLcontentVar, M) 
   :  .literal(KQMLcontentVar) & 
      .ground(KQMLcontentVar) 
   <- .add_annot(KQMLcontentVar, source(S), CA); 
      +CA.
@kqmlReceivedTellList
+!kqml_received(S, tell, KQMLcontentVar, M) 
   :  .list(KQMLcontentVar)
   <- !addAllkqmlReceived(S,KQMLcontentVar).

@kqmlReceivedTellList1
+!addAllkqmlReceived(_,[]).   

@kqmlReceivedTellList2
+!addAllkqmlReceived(S,[H|T])
   :  .literal(H) & 
      .ground(H)
   <- .add_annot(H, source(S), CA); 
      +CA;
      !addAllkqmlReceived(S,T).

@kqmlReceivedTellList3
+!addAllkqmlReceived(S,[_|T])
   <- !addAllkqmlReceived(S,T).
      
@kqmlReceivedUnTell
+!kqml_received(S, untell, KQMLcontentVar, M) : true 
   <- .add_annot(KQMLcontentVar, source(S), CA); 
      -CA.


/* ---- achieve performatives ---- */ 

@kqmlReceivedAchieve
+!kqml_received(S, achieve, KQMLcontentVar, M) : true 
   <- .add_annot(KQMLcontentVar, source(S), CA); 
      !CA.
@kqmlReceivedUnAchieve
+!kqml_received(S, unachieve, KQMLcontentVar, M) : true 
   <- .add_annot(KQMLcontentVar, source(S), CA);
      .drop_goal(CA, false).


/* ---- ask performatives ---- */ 

@kqmlReceivedAskOne1
+!kqml_received(S, askOne, KQMLcontentVar, M) : true
   <- ?KQMLcontentVar;
      .send(S, tell, KQMLcontentVar, M).
@kqmlReceivedAskOne2 // error in askOne
-!kqml_received(S, askOne, _, M) : true
   <- .send(S, tell, false, M).      

@kqmlReceivedAskAll1
+!kqml_received(S, askAll, value(Var,KQMLcontentVar), M) : true    
   <- .findall(Var, KQMLcontentVar, List); 
      .send(S, tell, List, M).
@kqmlReceivedAskAll2
+!kqml_received(S, askAll, KQMLcontentVar, M) : true
   <- .findall(KQMLcontentVar, KQMLcontentVar, List); 
      .send(S, tell, List, M).


/* ---- tell how performatives ---- */ 

// In tellHow, KQMLcontentVar must be a string representation of the plan 
// (or a list of strings)
@kqmlReceivedTellHow
+!kqml_received(S, tellHow, KQMLcontentVar, M) : true <- .add_plan(KQMLcontentVar, S).

// In untellHow, KQMLcontentVar must be a plan's label (or a list of labels)
@kqmlReceivedUnTellHow
+!kqml_received(S, untellHow, KQMLcontentVar, M) : true <- .remove_plan(KQMLcontentVar, S).

// In askHow, KQMLcontentVar must be a string representation of the trigger event
@kqmlReceivedAskHow
+!kqml_received(S, askHow, KQMLcontentVar, M) : true    
   <- .relevant_plans(KQMLcontentVar, ListAsString); 
      .send(S, tellHow, ListAsString, M).
