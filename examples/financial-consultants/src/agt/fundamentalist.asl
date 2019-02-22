
/* * * * initial beliefs * * * */

/* * * * setup plans * * * */

//!focusFundamentusArtifact.
//!joinFinancialGroup.

/* * * * plans * * * */

+!saveYourself : .my_name(N) & .concat("/tmp/",N,".asl",CC) <-
    .save_agent(CC).

+!focusFundamentusArtifact <-
    joinWorkspace(financialagents,Omain); 
    focusWhenAvailable("fundamentus")
    .
    
+!joinFinancialGroup[source(self)] <-
    joinWorkspace(financialorg,Ofa);
    .print("DEBUG: Joining group...");
    g::lookupArtifact(financialteam, GrArtId)[wid(Ofa)];
    g::focus(GrArtId)[wid(Ofa)];
    g::adoptRole(consultant)[wid(Ofa)];
    s::lookupArtifact(financialsch, ScArtId)[wid(Ofa)];
    s::focus(ScArtId)[wid(Ofa)];
    s::commitMission("mConsultant")[artifact_id(ScArtId),wid(Ofa)];
    .
  
    
