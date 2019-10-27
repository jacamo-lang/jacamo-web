//Agent created automatically

!start.

+!start <- .print("Hi").

+!goal1 <-
    organisation::commitGoal(goal1);
    organisation::goalAchieved(goal1);
    .

+!adopt_role(R,G,O) <-
    joinWorkspace(O,Ofa);
    organisation::lookupArtifact(G,A);
    organisation::focus(A);
    organisation::adoptRole(R);
    .

+!acceptScheme(S,M) <-
    organisation::lookupArtifact(S, ScArtId);
    organisation::commitMission(M)[artifact_id(ScArtId)];
    .

+!focusIn(W,A) <-
    environment::joinWorkspace(W,Ofa);
    environment::lookupArtifact(A,Aid)[wid(Ofa)];
    environment::focus(Aid);
    .

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }
// uncomment the include below to have an agent compliant with its organisation
//{ include("$moiseJar/asl/org-obedient.asl") }
