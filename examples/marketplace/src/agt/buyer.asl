!start.

+!start <- .print("Hi").

+!buyComponent <-
    .print("Ordering components...");
    .wait(1000);
    .send(seller,achieve,supplyComponent);
    .

+!payment[source(S)] <-
    .print("Paying ", S);
    .wait(1000);
    .abolish(componentDelivered);
    .send(seller,tell,paid);
    .

+componentDelivered[source(S)] <-
    .print(S," delivered the component. Incrementing input stock...");
    inc;
    .

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }
