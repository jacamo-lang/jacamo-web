price(banana,X) :- X = math.random * 100 + 20.

!longTermSomething(1).

!start.

+!start
   <- lookupArtifact("a",Aid);
      lookupArtifact("b",Bid);
      linkArtifacts(Aid,"out-1",Bid);
      .df_register(vender(banana));
      .df_register(iamhere);
      .create_agent(kk);
      !create_group;
   .

+oi[source(A)] 
    <- .print("I received hello from ",A)
    .

+tstWithOneTerm(X)[source(A)] 
    <- .print("I received ", X, " from ",A)
    .

+tstWithTwoTerms(X,Y)[source(A)] 
    <- .print("I received ", X, " and ", Y, " from ",A)
    .

+!create_group 
    <- createWorkspace(wkstest);
       joinWorkspace(wkstest,Ofa);
       .print("DEBUG: Creating group...");
       o::makeArtifact(wkstest, "ora4mas.nopl.OrgBoard", ["src/org/house_building.xml"], OrgArtId)[wid(Ofa)];
       o::focus(OrgArtId);
       g::createGroup(house_group, house_group, GrArtId);
       g::focus(GrArtId);
       g::adoptRole(door_fitter);
    .

+!longTermSomething(X) : .length("xxx") > 0 & X < 1000
   <- .wait(5000);
      !g3(4000);
      .print(something,X);
      !longTermSomething(X+1);
   .

+!g3(Vl)
   <- !!g4(Vl*2);
      .suspend;
   .
   
+!g4(T)
   <- .wait(T);
      .resume(longTermSomething(_));
   .

+!setPrice
    <- -+price(banana,2.60);
    .
    
+!updatePrice
    : price(banana,P) & P < 10
    <- .print("increase price by 1, from: ",P," to ",P+1);
       -+price(banana,P+1);
       .wait(2000);
       -+someCondition;
       !updatePrice;
    .  
    
+!updatePrice
    : someCondition
    <- ?price(banana,P);
       if (P < 20) {
            -+price(banana,P+1);
            -someCondition;
            .print("testing failure -!");
            !updatePrice;
       }
    .
       
-!updatePrice
    <- .print("-! performed");
       .print("testing +? from ?someCondition");
       ?someCondition;
    .
   
 +?someCondition
    <- .print("+? performed");
    .
    
{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }

// uncomment the include below to have an agent compliant with its organisation
{ include("$moiseJar/asl/org-obedient.asl") }
            
            
+!testNewPlan(X)    : X > 10 <- .print(X*4).

            
+!goal2 <- .print("Ok do goal 2").
            
