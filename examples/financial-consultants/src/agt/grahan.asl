{ include("fundamentalist.asl") }

/* * * * initial beliefs * * * */

/* * * * setup plans * * * */

!createFundamentusArtifact.

/* * * * perceptions from artifact * * * */

+setPreco(V) <- -+fundamentals::preco(V).
+setLPA(V) <- -+fundamentals::lpa(V).
+setVPA(V) <- -+fundamentals::vpa(V).

/* * * * plans * * * */

+!createFundamentusArtifact[source(self)] <-
    joinWorkspace(financialagents,Omain); 
    makeArtifact("fundamentus","dynamic.stock.FundamentusArtifact",[],Aid)[wid(Omain)];
    .

+!reply(S,Q)[source(self)] : fundamentals::preco(P) & fundamentals::lpa(L) & fundamentals::vpa(V) & P < math.sqrt(22.5 * L * V) <-
    .concat("Baseado no metodo de Grahan, ", S," tem Preco (",P,") menor que raiz quadrada de 22.5*",L,"*", V," (",math.sqrt(22.5 * L * V),"): COMPRAR", CCC)
    .send(Q,tell,recommend(S,comprar,CCC));
    .

+!reply(S,Q)[source(self)] : fundamentals::preco(P) & fundamentals::lpa(L) & fundamentals::vpa(V) <-
    .concat("Baseado no metodo de Grahan, ", S," NAO tem Preco (",P,") menor que raiz quadrada de 22.5*",L,"*", V," (",math.sqrt(22.5 * L * V),"): NAO COMPRAR", CCC)
    .send(Q,tell,recommend(S,neutro,CCC));
    .

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }
{ include("$jacamoJar/templates/org-obedient.asl") }
