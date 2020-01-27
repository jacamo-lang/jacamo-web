!start.

+!start <- .print("Hi").

+!supplyItem[source(S)] <-
    .print(S, " ordered item...");
    .wait(1000);
    .abolish(paid);
    .send(buyer,achieve,makePayment);
    .
    
+paid[source(S)] <-
    .print(S, " has paid the invoice.");
    .wait(1000);
    .
    
+!deliverItem : paid <-
    .print("Decrementing output stock and delivering item...");
    dec;
    .send(buyer,tell,itemDelivered);
    .

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }
{ include("$moiseJar/asl/org-obedient.asl") }
