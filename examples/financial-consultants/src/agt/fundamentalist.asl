/* * * * initial beliefs * * * */

/* * * * setup plans * * * */

//!focusFundamentusArtifact.
//!joinFinancialGroup.
//!acceptScheme.

/* * * * plans * * * */

+!sayHi : .my_name(N) <-
    .print("Hi! I am ",N).

+!saveYourself : .my_name(N) & .concat("/tmp/",N,".asl",CC) <-
    .save_agent(CC).

+!focusFundamentusArtifact <-
    joinWorkspace(financialagents,Omain); 
    focusWhenAvailable("fundamentus");
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
    joinWorkspace(financialagents,Omain);
    s::focusWhenAvailable(financialsch);
    s::lookupArtifact(financialsch, ScArtId);
    s::commitMission("mConsultant")[artifact_id(ScArtId)];
    .
    
            
