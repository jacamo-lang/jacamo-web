{ include("fundamentalist.asl") }

/* * * * initial beliefs * * * */

/* * * * setup plans * * * */

/* * * * perceptions from artifact * * * */

+setDivYield(V) <- -+fundamentals::divYield(V).
+setDivBrutaPatr(V) <- -+fundamentals::divBrutaPatr(V).

/* * * * plans * * * */

+!reply(S,Q)[source(self)] : fundamentals::divYield(P) & fundamentals::divBrutaPatr(D) & P >= 6 & D < 1 <-
    .concat("Baseado no metodo de Bazin, ", S," tem Yield >=6% (",P,"%) e Div./ Patr <=1 (",D,"): COMPRAR", CCC)
    .send(Q,tell,stocks::recommend(S,comprar,CCC));
    .

+!reply(S,Q)[source(self)] : fundamentals::divYield(P) & fundamentals::divBrutaPatr(D) <-
    .concat("Baseado no metodo de Bazin, ", S," NAO cumpre os requisitos de Yield >=6% (",P,"%) e Div./ Patr <=1 (",D,"): NAO COMPRAR", CCC)
    .send(Q,tell,stocks::recommend(S,neutro,CCC));
    .

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }
{ include("$jacamoJar/templates/org-obedient.asl") }
