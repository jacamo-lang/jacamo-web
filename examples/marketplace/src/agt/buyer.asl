!start.

+!start <- .print("Hi").

+!doBusiness <- .print("Here we go!").

+!buyItem <-
    .print("Ordering item...");
    .wait(1000);
    .send(seller,achieve,supplyItem);
    .

+!makePayment[source(S)] <-
    .print("Paying ", S);
    .wait(1000);
    .abolish(itemDelivered);
    .send(seller,tell,paid);
    .

+itemDelivered[source(S)] <-
    .print(S," delivered the item.");
    !!storeItem;
    .
    
+!storeItem : itemDelivered <-
    .print("Incrementing input stock...");
    inc;
    .

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }
{ include("$moiseJar/asl/org-obedient.asl") }
