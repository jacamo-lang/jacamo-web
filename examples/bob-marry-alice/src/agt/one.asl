// One agent to rule them all

+!yourBlessing <-
    .wait(3000);
    .broadcast(tell,blessYou);
    .wait(3000);
    .broadcast(tell,goInPeace);
.

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }
{ include("$moiseJar/asl/org-obedient.asl") }
