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

+!needingItem <-
    .print("Asking to buy item...");
    .wait(1000);
    .send(buyer,achieve,buyItem);
    .

+!storeItem : true <-
    .wait(1000);
    .print("Incrementing input stock...");
    inc;
    .

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }
// uncomment the include below to have an agent compliant with its organisation
//{ include("$moiseJar/asl/org-obedient.asl") }
