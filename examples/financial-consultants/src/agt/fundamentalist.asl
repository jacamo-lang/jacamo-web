/* * * * initial beliefs * * * */

/* * * * setup plans * * * */

!focusFundamentusArtifact.
!joinFinancialGroup.

/* * * * plans * * * */

+!saveYourself : .my_name(N) & .concat("/tmp/",N,".asl",CC) <-
    .save_agent(CC).

+!focusFundamentusArtifact <-
    joinWorkspace(financialagents,Omain); 
    focusWhenAvailable("fundamentus")
    .

+!makeFundamentusArtifact <-
    joinWorkspace(financialagents,Omain); 
    makeArtifact("fundamentus","dynamic.stock.FundamentusArtifact",[],Aid);
    .
    
+!joinFinancialGroup[source(self)] <-
    joinWorkspace(financialagents,Ofa);
    .print("DEBUG: Joining group...");
    g::lookupArtifact(financialteam, GrArtId)[wid(Ofa)];
    g::focus(GrArtId)[wid(Ofa)];
    g::adoptRole(consultant)[wid(Ofa)];
    .
  
+!acceptScheme[source(self)] <-
    s::lookupArtifact(financialsch, ScArtId);
    s::focus(ScArtId);
    s::commitMission("mConsultant")[artifact_id(ScArtId)];
    .
    
            
