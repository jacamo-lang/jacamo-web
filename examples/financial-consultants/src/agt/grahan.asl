{ include("fundamentalist.asl") }

/* * * * initial beliefs * * * */

/* * * * setup plans * * * */

/* * * * perceptions from artifact * * * */

+setPreco(S,V)  : .time(HH,MM,SS) <- -+fundamentals::preco(S,V)[seconds_of_day(SS+MM*60+HH*60*60)].
+setLPA(S,V)    : .time(HH,MM,SS) <- -+fundamentals::lpa(S,V)[seconds_of_day(SS+MM*60+HH*60*60)].
+setVPA(S,V)    : .time(HH,MM,SS) <- -+fundamentals::vpa(S,V)[seconds_of_day(SS+MM*60+HH*60*60)].

/* * * * plans * * * */

//TODO: It was not easy to realize that the artifact is sending a string back and it is not being treated as a term

// get cached Fundamentals if the earlier data is younger than 5 minutes
+!opinion(T)[source(Q)] 
    : .term2string(T,S) & fundamentals::preco(S,_)[seconds_of_day(SSS)] & .time(HH,MM,SS) & (SS+MM*60+HH*60*60 - SSS < 30*60)
    & .date(YY,OO,DD) & lastDate(YYY,OOO,DDD) & YY == YYY & OO == OOO & DD == DDD 
    & fundamentals::lpa(S,_) & fundamentals::vpa(S,_) 
    <- 
    .print("Getting cached fundamentals from ",S); 
    !reply(S,Q);
    .

// get Fundamentals again
+!opinion(T)[source(Q)] : .term2string(T,S) & .date(YY,OO,DD) <- 
    .print("Getting fundamentals from ",S); 
    getFundamentals(S);
    -+lastDate(YY,OO,DD);
    !reply(S,Q);
    .

+!reply(S,Q)[source(self)] : fundamentals::preco(S,P) & fundamentals::lpa(S,L) & fundamentals::vpa(S,V) & P < math.sqrt(22.5 * L * V) <-
    .concat("Baseado no metodo de Grahan, ", S," tem Preco (",P,") menor que raiz quadrada de 22.5*",L,"*", V," (",math.round(math.sqrt(22.5 * L * V)*100)/100,"): COMPRAR", CCC)
    .send(Q,tell,stocks::recommend(S,comprar,CCC));
    .

+!reply(S,Q)[source(self)] : fundamentals::preco(S,P) & fundamentals::lpa(S,L) & fundamentals::vpa(S,V) <-
    .concat("Baseado no metodo de Grahan, ", S," NAO cumpre os requisitos de Preco (",P,") menor que raiz quadrada de 22.5*",L,"*", V," (",math.round(math.sqrt(22.5 * L * V)*100)/100,"): NAO COMPRAR", CCC)
    .send(Q,tell,stocks::recommend(S,neutro,CCC));
    .

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }
{ include("$jacamoJar/templates/org-obedient.asl") }
