!start.

+!start
   <- .df_subscribe(iamhere);
      .df_subscribe(vender(abacaxi)).
      
+!buy 
   <- .df_search(vender(banana),L);
      .print("Banana sellers ", L);
      for (.member(A,L)) {
          .send(A,askOne,price(banana,_),price(_,P),2000);
          .printf("%20s = %5.2f",A,P);
      }
   .
   
+provider(marcos,"iamhere") 
   <- .send(marcos, tell, oi);
      !buy;
   .

+provider(A,S) <- .print(A, " is provider of ",S).

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }

// uncomment the include below to have an agent compliant with its organisation
//{ include("$moiseJar/asl/org-obedient.asl") }
