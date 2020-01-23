!start.

+!start <- .print("Hi").

+!supplyComponent[source(S)] <-
    .print(S, " ordered components...");
    .wait(1000);
    .abolish(paid);
    .send(buyer,achieve,payment);
    .
    
+paid[source(S)] <-
    .print(S, " has paid the invoice. decrementing output stock and sending component...");
    .wait(1000);
    dec;
    .send(buyer,tell,componentDelivered);
    .

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }
