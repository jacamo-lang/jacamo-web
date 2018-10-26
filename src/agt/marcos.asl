price(banana,20).

!start.
+!start 
   <- .df_register(vender(banana));
      .df_register(iamhere);
   .

+oi[source(A)] <- .print("I received hello from ",A).

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }

// uncomment the include below to have an agent compliant with its organisation
//{ include("$moiseJar/asl/org-obedient.asl") }
