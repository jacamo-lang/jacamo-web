price(banana,X) :- X = math.random * 100 + 20.

!start.
+!start
   <-
      lookupArtifact("a",Aid);
      lookupArtifact("b",Bid);
      linkArtifacts(Aid,"out-1",Bid);
      .df_register(vender(banana));
      .df_register(iamhere);
      .create_agent(kk);
      !create_group;
   .

+oi[source(A)] <- .print("I received hello from ",A).

+!create_group <- 
     createWorkspace(wkstest);
     joinWorkspace(wkstest,Ofa);
    .print("DEBUG: Creating group...");
    o::makeArtifact(wkstest, "ora4mas.nopl.OrgBoard", ["src/org/house_building.xml"], OrgArtId)[wid(Ofa)];
    o::focus(OrgArtId);
    g::createGroup(house_group, house_group, GrArtId);
    g::focus(GrArtId);
    g::adoptRole(door_fitter);
    .

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }

// uncomment the include below to have an agent compliant with its organisation
{ include("$moiseJar/asl/org-obedient.asl") }
