{ include("fundamentalist.asl") }

/* * * * initial beliefs * * * */

/* * * * setup plans * * * */
//!makeFundamentusArtifact.

/* * * * perceptions from artifact * * * */

+setDivYield(S,V)       : .time(HH,MM,SS) <- -+fundamentals::divYield(S,V)[seconds_of_day(SS+MM*60+HH*60*60)].
+setDivBrutaPatr(S,V)   : .time(HH,MM,SS) <- -+fundamentals::divBrutaPatr(S,V)[seconds_of_day(SS+MM*60+HH*60*60)].

/* * * * plans * * * */

// get cached Fundamentals if the earlier data is younger than 5 minutes 
+!opinion(T)[source(Q)] 
    : .term2string(T,S) & fundamentals::divYield(S,_)[seconds_of_day(SSS)] & .time(HH,MM,SS) & (SS+MM*60+HH*60*60 - SSS < 30*60)
    & .date(YY,OO,DD) & lastDate(YYY,OOO,DDD) & YY == YYY & OO == OOO & DD == DDD 
    & fundamentals::divBrutaPatr(S,_)
    <- 
    .print("Getting cached fundamentals from ",S); 
    !reply(T,Q);
    .

// get Fundamentals again
+!opinion(T)[source(Q)] : .term2string(T,S) & .date(YY,OO,DD) <- 
    .print("Getting fundamentals from ",S); 
    getFundamentals(T);
    -+lastDate(YY,OO,DD);
    !reply(T,Q);
    .

+!reply(S,Q)[source(self)] : fundamentals::divYield(S,P) & fundamentals::divBrutaPatr(S,D) & P >= 6 & D < 1 <-
    .concat("Baseado no metodo de Bazin, ", S," tem Yield >=6% (",P,"%) e Div./ Patr <=1 (",D,"): COMPRAR", CCC);
    .send(Q,tell,stocks::recommend(S,comprar,CCC));
    .

+!reply(S,Q)[source(self)] : fundamentals::divYield(S,P) & fundamentals::divBrutaPatr(S,D) <-
    .concat("Baseado no metodo de Bazin, ", S," NAO cumpre os requisitos de Yield >=6% (",P,"%) e Div./ Patr <=1 (",D,"): NAO COMPRAR", CCC);
    .send(Q,tell,stocks::recommend(S,neutro,CCC));
    .

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }
{ include("$jacamoJar/templates/org-obedient.asl") }
