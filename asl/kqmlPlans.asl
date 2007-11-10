// Default plans to handle KQML performatives
// Users can override them in their own AS code
// 
// Variables:
//   Sender:  the sender (an atom)
//   Content: content (typically a literal)
//   MsgId:   message id (an atom)
//


/* ---- tell performatives ---- */ 

@kqmlReceivedTellStructure
+!kqml_received(Sender, tell, Content, MsgId) 
   :  .structure(Content) & 
      .ground(Content) &
      not .list(Content)
   <- .add_annot(Content, source(Sender), CA); 
      +CA.
@kqmlReceivedTellList
+!kqml_received(Sender, tell, Content, MsgId) 
   :  .list(Content)
   <- !add_all_kqml_received(Sender,Content).

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
+!kqml_received(Sender, untell, Content, MsgId)
   <- .add_annot(Content, source(Sender), CA); 
      -CA.


/* ---- achieve performatives ---- */ 

@kqmlReceivedAchieve
+!kqml_received(Sender, achieve, Content, MsgId)
   <- .add_annot(Content, source(Sender), CA); 
      !!CA.

@kqmlReceivedUnAchieve[atomic]
+!kqml_received(Sender, unachieve, Content, MsgId)
   <- .drop_desire(Content).


/* ---- ask performatives ---- */ 

@kqmlReceivedAskOne1
+!kqml_received(Sender, askOne, Content, MsgId) 
   <- ?Content;
      .send(Sender, tell, Content, MsgId).

@kqmlReceivedAskOne2 // error in askOne, send untell
-!kqml_received(Sender, askOne, Content, MsgId)
   <- .send(Sender, untell, Content, MsgId).      

@kqmlReceivedAskAll1
+!kqml_received(Sender, askAll, Content, MsgId)
   :  not Content
   <- .send(Sender, untell, Content, MsgId).

@kqmlReceivedAskAll2
+!kqml_received(Sender, askAll, Content, MsgId)
   <- .findall(Content, Content, List); 
      .send(Sender, tell, List, MsgId).


/* ---- know-how performatives ---- */ 

// In tellHow, content must be a string representation
// of the plan (or a list of such strings)

@kqmlReceivedTellHow
+!kqml_received(Sender, tellHow, Content, MsgId)
   <- .add_plan(Content, Sender).

// In untellHow, content must be a plan's
// label (or a list of labels)
@kqmlReceivedUnTellHow
+!kqml_received(Sender, untellHow, Content, MsgId)
   <- .remove_plan(Content, Sender).

// In askHow, content must be a string representing
// the triggering event
@kqmlReceivedAskHow
+!kqml_received(Sender, askHow, Content, MsgId)
   <- .relevant_plans(Content, ListAsString);
      .send(Sender, tellHow, ListAsString, MsgId).
