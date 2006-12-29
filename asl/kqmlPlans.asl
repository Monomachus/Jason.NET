// Default plans to handle KQML performatives
// Users can override them in their own AS code
// 
// Variables:
//   S:   the sender (an atom)
//   M:   message id (an atom)
//   KQMLcontentVar: content (a literal)
//


/* ---- tell performatives ---- */ 

@kqmlReceivedTellStructure
+!kqml_received(S, tell, KQMLcontentVar, M) 
   :  .structure(KQMLcontentVar) & 
      .ground(KQMLcontentVar) &
      not .list(KQMLcontentVar)
   <- .add_annot(KQMLcontentVar, source(S), CA); 
      +CA.
@kqmlReceivedTellList
+!kqml_received(S, tell, KQMLcontentVar, M) 
   :  .list(KQMLcontentVar)
   <- !add_all_kqml_received(S,KQMLcontentVar).

@kqmlReceivedTellList1
+!add_all_kqml_received(_,[]).   

@kqmlReceivedTellList2
+!add_all_kqml_received(S,[H|T])
   :  .structure(H) & 
      .ground(H)
   <- .add_annot(H, source(S), CA); 
      +CA;
      !add_all_kqml_received(S,T).

@kqmlReceivedTellList3
+!add_all_kqml_received(S,[_|T])
   <- !add_all_kqml_received(S,T).
      
@kqmlReceivedUnTell
+!kqml_received(S, untell, KQMLcontentVar, M)
   <- .add_annot(KQMLcontentVar, source(S), CA); 
      -CA.


/* ---- achieve performatives ---- */ 

@kqmlReceivedAchieve
+!kqml_received(S, achieve, KQMLcontentVar, M)
   <- .add_annot(KQMLcontentVar, source(S), CA); 
      !CA.

@kqmlReceivedUnAchieve
+!kqml_received(S, unachieve, KQMLcontentVar, M)
   <- .add_annot(KQMLcontentVar, source(S), CA);
      .drop_goal(CA, false).


/* ---- ask performatives ---- */ 

@kqmlReceivedAskOne1
+!kqml_received(S, askOne, KQMLcontentVar, M) 
   <- ?KQMLcontentVar;
      .send(S, tell, KQMLcontentVar, M).

@kqmlReceivedAskOne2 // error in askOne, send false
-!kqml_received(S, askOne, _, M)
   <- .send(S, tell, false, M).      

@kqmlReceivedAskAll
+!kqml_received(S, askAll, KQMLcontentVar, M)
   <- .findall(KQMLcontentVar, KQMLcontentVar, List); 
      .send(S, tell, List, M).


/* ---- tell how performatives ---- */ 

// In tellHow, content must be a string representation
// of the plan (or a list of strings)

@kqmlReceivedTellHow
+!kqml_received(S, tellHow, KQMLcontentVar, M)
   <- .add_plan(KQMLcontentVar, S).

// In untellHow, content must be a plan's label (or a list of labels)
@kqmlReceivedUnTellHow
+!kqml_received(S, untellHow, KQMLcontentVar, M)
   <- .remove_plan(KQMLcontentVar, S).

// In askHow, content must be a string representing the trigger event
@kqmlReceivedAskHow
+!kqml_received(S, askHow, KQMLcontentVar, M)
   <- .relevant_plans(KQMLcontentVar, ListAsString);
      .send(S, tellHow, ListAsString, M).
