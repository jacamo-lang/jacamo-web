// Agent one in project finantialAgents

!create_group.

+!createAgent(A, File) <-
    .create_agent(A, File, [beliefBaseClass("jason.bb.TextPersistentBB")]). 

+!killAgent(A) <-
    .concat("telegram",A,TA);
    !!disposeArtifact(TA);
    .concat("stock",A,SA);
    !!disposeArtifact(SA);
    .send(A,achieve,saveYourself);
    .wait(1000);
    .kill_agent(A).

+!disposeArtifact(Art) <-
    lookupArtifact(Art,AidD);
    disposeArtifact(AidD).

// workspace already exists?
+!create_group <- 
    createWorkspace(financialagents);
    joinWorkspace(financialagents,Ofa);
    .print("DEBUG: Creating group...");
    o::makeArtifact(financialagents, "ora4mas.nopl.OrgBoard", ["src/org/financial.xml"], OrgArtId)[wid(Ofa)];
    o::focus(OrgArtId);
    g::createGroup(financial_team, financial_group, GrArtId);
    !createagents;
    g::focus(GrArtId);
    g::adoptRole(controller);
    .

-!create_group[error(E), error_msg(M), reason(R)] <-
    .print("** Error ",E," creating group: ",M, ". Reason: ", R).

// when I start playing the role
+g::play(Me,generalcontroller,GId) : .my_name(Me) <- 
    .print("DEBUG: Playing role...");
    !create_scheme;
    .

+!create_scheme <- 
    .print("DEBUG: Creating scheme...");
        o::createScheme("scheme", finantial_sch, SchArtId);
        s::focus(SchArtId);
        g::addScheme("scheme");
    s::commitMission(mlaunchsystem);
    .
  
+!createagents <-
    !!createAgent(grahan,"grahan.asl");
    !!createAgent(greenblatt,"greenblatt.asl");
    !!createAgent(bazin,"bazin.asl");
    !!createAgent(myPA,"assistant.asl");
    .

+!destroySystem <-
    !!killAgent(grahan);
    !!killAgent(greenblatt);
    !!killAgent(bazin);
    !!killAgent(myPA);
    .

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }
{ include("$moiseJar/asl/org-obedient.asl") }
