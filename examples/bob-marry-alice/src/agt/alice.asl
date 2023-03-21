+hi <-
    .wait(1000);
    .send(bob,tell,hi);
.

+!marryMe <-
    .wait(2000);
    .send(bob,tell,iDontKnow);
.

+iLoveYou <-
    .wait(2000);
    .send(bob,tell,iLoveYouToo);
    .wait(1000);
    .send(bob,tell,yes);
.

//+blessYou.

//+goInPeace.

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }
// uncomment the include below to have an agent compliant with its organisation
//{ include("$moiseJar/asl/org-obedient.asl") }
