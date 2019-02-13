
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
    
+!joinFinancialGroup[source(self)] <-
    joinWorkspace(financialorg,Ofa);
    .print("DEBUG: Joining group...");
    g::lookupArtifact(financialteam, GrArtId);
    g::focus(GrArtId);
    g::adoptRole(consultant);
    .

+!opinion(S)[source(Q)] <- 
    getFundamentals(S);
    .wait(1000);
    !reply(S,Q);
    .
