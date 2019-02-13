{ include("fundamentalist.asl") }

/* * * * initial beliefs * * * */

/* * * * setup plans * * * */

/* * * * perceptions from artifact * * * */

+setPreco(V) <- -+fundamentals::preco(V).
+setLPA(V) <- -+fundamentals::lpa(V).
+setVPA(V) <- -+fundamentals::vpa(V).

/* * * * plans * * * */

+!reply(S,Q)[source(self)] : fundamentals::preco(P) & fundamentals::lpa(L) & fundamentals::vpa(V) & P < math.sqrt(22.5 * L * V) <-
    .concat("Baseado no metodo de Grahan, ", S," tem Preco (",P,") menor que raiz quadrada de 22.5*",L,"*", V," (",math.round(math.sqrt(22.5 * L * V)*100)/100,"): COMPRAR", CCC)
    .send(Q,tell,stocks::recommend(S,comprar,CCC));
    .

+!reply(S,Q)[source(self)] : fundamentals::preco(P) & fundamentals::lpa(L) & fundamentals::vpa(V) <-
    .concat("Baseado no metodo de Grahan, ", S," NAO cumpre os requisitos de Preco (",P,") menor que raiz quadrada de 22.5*",L,"*", V," (",math.round(math.sqrt(22.5 * L * V)*100)/100,"): NAO COMPRAR", CCC)
    .send(Q,tell,stocks::recommend(S,neutro,CCC));
    .

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }
{ include("$jacamoJar/templates/org-obedient.asl") }
