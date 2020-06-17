//Agent created automatically1

+!start <- 
    .wait(3000);
    .send(alice,tell,hi);
.

+hi <-
    .wait(4000);
    .send(alice,achieve,maryMe);
.

+iDontKnow <- 
    .wait(3000);
    .send(alice,tell,iLoveYou);
.

+iLoveYouToo.

+yes <-
    .wait(2000);
    .send(one,achieve,yourBlessing);
.

+blessYou.

+goInPeace.

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }
// uncomment the include below to have an agent compliant with its organisation
//{ include("$moiseJar/asl/org-obedient.asl") }
