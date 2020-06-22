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

+!buyItem <-
    .print("Ordering item...");
    .send(seller,achieve,supplyItem);
    .

+!makePayment[source(S)] <-
    .print("Asking to pay ", S);
    .abolish(itemDelivered);
    .send(payer,achieve,makePayment);
    .

+itemDelivered[source(S)] <-
    .print(S," delivered the item.");
    .send(stocker,achieve,storeItem);
    .

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }
{ include("$moiseJar/asl/org-obedient.asl") }
