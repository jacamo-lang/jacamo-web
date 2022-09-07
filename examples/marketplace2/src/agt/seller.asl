/*
In the 'buyer' agent, Moise coordination mechanism will set on 'marketplace.xml' will trigger 
the goal 'buyitem' once, the goal 'supplyItem' in the 'seller', then the goal 'makePayment' again in the
buyer to finally trigger 'deliverItem' in the seller.

Besides the coordination mechanism, here we have redundant messages from agent to agent to illustrate
jacamo-web message exchanging interface.

@author Cleber Jorge Amaral
*/

!start.

+!start <- .print("Hi").

+!supplyItem[source(S)] <-
    .print(S, " ordered item...");
    .wait(2000);
    .abolish(paid);
    .send(buyer,achieve,makePayment);
    .
    
+paid[source(S)] <-
    .wait(1000);
    .print(S, " has paid the invoice.");
    .send(collector,tell,paid);
    .abolish(paid);
    .

+!deliverItem : true <-
    .print("Item will be packed...");
    .

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }
{ include("$moiseJar/asl/org-obedient.asl") }
