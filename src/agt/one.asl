// One agent to rule them all

!launchSystem.

+!createAgent(A, File) <-
    .create_agent(A, File, [beliefBaseClass("jason.bb.TextPersistentBB")]).

+!killAgent(A) <-
    .send(A,achieve,saveYourself);
    .wait(1000);
    .kill_agent(A).

+!create_organisation(Ofa) <-
    organisation::makeArtifact(org,"ora4mas.nopl.OrgBoard",["src/org/org.xml"],OIa)[wid(Ofa)];
    organisation::focus(OIa);
    .

+!create_group <-
    organisation::createGroup(group1,group1,Gid);
    organisation::focus(Gid);
    organisation::adoptRole(role0);
    .

-!create_group[error(E), error_msg(M), reason(R)] <-
    .print("** Error ",E," creating group: ",M, ". Reason: ", R);
    .

+!create_scheme <-
    .print("DEBUG: Creating scheme...");
    organisation::createScheme("scheme1",scheme1,Sid);
    organisation::focus(Sid);
    organisation::addScheme("scheme1");
    organisation::commitMission(mission0)[artifact_id(Sid)];
    .

+!create_agents <-
    !createAgent(bob,"bob.asl");
    !createAgent(alice,"alice.asl");
    .send(bob,achieve,adopt_role(role1,group1,orgWks));
    .send(alice,achieve,adopt_role(role2,group1,orgWks));
    .

+!create_artifact <-
    environment::createWorkspace(wks);
    environment::joinWorkspace(wks,Ofa);
    environment::makeArtifact(counterA,"tools.Counter",[10],OIa)[wid(Ofa)];
    environment::makeArtifact(counterB,"tools.Counter",[20],OIb)[wid(Ofa)];
    .send(bob,achieve,focusIn(wks,counterA));
    .send(alice,achieve,focusIn(wks,counterB));
    .

+!launchSystem <-
    createWorkspace(orgWks);
    joinWorkspace(orgWks,Ofa);
    !create_organisation(Ofa);
    !create_group;
    !create_agents;
    !create_scheme;
    .wait(2000);
    .send(bob,achieve,acceptScheme(scheme1,mission1));
    .send(alice,achieve,acceptScheme(scheme1,mission2));
    !create_artifact;
    .

+!destroySystem <-
    organisation::lookupArtifact(scheme1,Sid);
    organisation::disposeArtifact(Sid);
    organisation::lookupArtifact(group1,Gid);
    organisation::disposeArtifact(Gid);
    organisation::lookupArtifact(org,OIa);
    organisation::disposeArtifact(OIa);
    !killAgent(bob);
    !killAgent(alice);
    .

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }
{ include("$moiseJar/asl/org-obedient.asl") }
