// Default plans to handle KQML performatives
// Users can override them in their own AS code
// 
// Variables:
//   KQML_Sender_Var:  the sender (an atom)
//   KQML_Content_Var: content (typically a literal)
//   KQML_MsgId:       message id (an atom)
//


/* ---- tell performatives ---- */ 

@kqmlReceivedTellStructure
+!kqml_received(KQML_Sender_Var, tell, KQML_Content_Var, KQML_MsgId) 
   :  .structure(KQML_Content_Var) & 
      .ground(KQML_Content_Var) &
      not .list(KQML_Content_Var)
   <- .add_annot(KQML_Content_Var, source(KQML_Sender_Var), CA); 
      +CA.
@kqmlReceivedTellList
+!kqml_received(KQML_Sender_Var, tell, KQML_Content_Var, KQML_MsgId) 
   :  .list(KQML_Content_Var)
   <- !add_all_kqml_received(KQML_Sender_Var,KQML_Content_Var).

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
+!kqml_received(KQML_Sender_Var, untell, KQML_Content_Var, KQML_MsgId)
   <- .add_annot(KQML_Content_Var, source(KQML_Sender_Var), CA); 
      -CA.


/* ---- achieve performatives ---- */ 

@kqmlReceivedAchieve
+!kqml_received(KQML_Sender_Var, achieve, KQML_Content_Var, KQML_MsgId)
   <- .add_annot(KQML_Content_Var, source(KQML_Sender_Var), CA); 
      !!CA.

@kqmlReceivedUnAchieve[atomic]
+!kqml_received(KQML_Sender_Var, unachieve, KQML_Content_Var, KQML_MsgId)
   <- .drop_desire(KQML_Content_Var);
      .drop_intention(KQML_Content_Var).


/* ---- ask performatives ---- */ 

@kqmlReceivedAskOne1
+!kqml_received(KQML_Sender_Var, askOne, KQML_Content_Var, KQML_MsgId) 
   <- ?KQML_Content_Var;
      .send(KQML_Sender_Var, tell, KQML_Content_Var, KQML_MsgId).

@kqmlReceivedAskOne2 // error in askOne, send untell
-!kqml_received(KQML_Sender_Var, askOne, KQML_Content_Var, KQML_MsgId)
   <- .send(KQML_Sender_Var, untell, KQML_Content_Var, KQML_MsgId).      

@kqmlReceivedAskAll1
+!kqml_received(KQML_Sender_Var, askAll, KQML_Content_Var, KQML_MsgId)
   :  not KQML_Content_Var
   <- .send(KQML_Sender_Var, untell, KQML_Content_Var, KQML_MsgId).

@kqmlReceivedAskAll2
+!kqml_received(KQML_Sender_Var, askAll, KQML_Content_Var, KQML_MsgId)
   <- .findall(KQML_Content_Var, KQML_Content_Var, List); 
      .send(KQML_Sender_Var, tell, List, KQML_MsgId).


/* ---- know-how performatives ---- */ 

// In tellHow, content must be a string representation
// of the plan (or a list of such strings)

@kqmlReceivedTellHow
+!kqml_received(KQML_Sender_Var, tellHow, KQML_Content_Var, KQML_MsgId)
   <- .add_plan(KQML_Content_Var, KQML_Sender_Var).

// In untellHow, content must be a plan's
// label (or a list of labels)
@kqmlReceivedUnTellHow
+!kqml_received(KQML_Sender_Var, untellHow, KQML_Content_Var, KQML_MsgId)
   <- .remove_plan(KQML_Content_Var, KQML_Sender_Var).

// In askHow, content must be a string representing
// the triggering event
@kqmlReceivedAskHow
+!kqml_received(KQML_Sender_Var, askHow, KQML_Content_Var, KQML_MsgId)
   <- .relevant_plans(KQML_Content_Var, ListAsString);
      .send(KQML_Sender_Var, tellHow, ListAsString, KQML_MsgId).
