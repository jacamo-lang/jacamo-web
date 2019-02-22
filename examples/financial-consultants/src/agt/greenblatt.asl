{ include("fundamentalist.asl") }

/* * * * initial beliefs * * * */

/* * * * setup plans * * * */

/* * * * perceptions from artifact * * * */

+setValorMercado(S,V)   : .time(HH,MM,SS) <- -+fundamentals::valorMercado(S,V)[seconds_of_day(SS+MM*60+HH*60*60)].
+setDivYield(S,V)       : .time(HH,MM,SS) <- -+fundamentals::divYield(S,V)[seconds_of_day(SS+MM*60+HH*60*60)].
+setLPA(S,V)            : .time(HH,MM,SS) <- -+fundamentals::lpa(S,V)[seconds_of_day(SS+MM*60+HH*60*60)].
+setVPA(S,V)            : .time(HH,MM,SS) <- -+fundamentals::vpa(S,V)[seconds_of_day(SS+MM*60+HH*60*60)].
+setDividaLiq(S,V)      : .time(HH,MM,SS) <- -+fundamentals::dividaLiq(S,V)[seconds_of_day(SS+MM*60+HH*60*60)].
+setEBIT(S,V)           : .time(HH,MM,SS) <- -+fundamentals::ebit(S,V)[seconds_of_day(SS+MM*60+HH*60*60)].
+setROIC(S,V)           : .time(HH,MM,SS) <- -+fundamentals::roic(S,V)[seconds_of_day(SS+MM*60+HH*60*60)].

/* * * * plans * * * */

// get cached Fundamentals if the earlier data is younger than 5 minutes 
+!opinion(T)[source(Q)] 
    : .term2string(T,S) & fundamentals::valorMercado(S,_)[seconds_of_day(SSS)] & .time(HH,MM,SS) & (SS+MM*60+HH*60*60 - SSS < 5*60) 
    & fundamentals::divYield(S,_) & fundamentals::lpa(S,_) & fundamentals::vpa(S,_)
    & fundamentals::dividaLiq(S,_) & fundamentals::ebit(S,_) & fundamentals::roic(S,_)
    <- 
    !reply(S,Q);
    .

// get Fundamentals again
+!opinion(T)[source(Q)] : .term2string(T,S) <- 
    .print("Getting fundamentals from ",S); 
    getFundamentals(S);
    !reply(S,Q);
    .

+!reply(S,Q)[source(self)] : 
    fundamentals::ebit(S,E) & fundamentals::valorMercado(S,V) & fundamentals::dividaLiq(S,D) & E/(V+D) >= 0.1 & 
    fundamentals::roic(S,R) & R >= 0.1
    <-
    .concat("Baseado no metodo de Greenblatt, ", S," tem EBIT (",math.round(E/1000000),"mi) sobre valor de mercado + divida liquida (",math.round(V/1000000),"mi+",math.round(D/1000000),"mi) e ROIC (",R,"%) superiores a 10%: COMPRAR", CCC)
    .send(Q,tell,stocks::recommend(S,comprar,CCC));
    .

+!reply(S,Q)[source(self)] : fundamentals::ebit(S,E) & fundamentals::valorMercado(S,V) & fundamentals::dividaLiq(S,D) & fundamentals::roic(S,R) <-
    .concat("Baseado no metodo de Greenblatt, ", S," NAO cumpre os requisitos de EBIT (",math.round(E/1000000),"mi) sobre valor de mercado + divida liquida (",math.round(V/1000000),"mi+",math.round(D/1000000),"mi) e ROIC (",R,"%) superiores a 10%: NAO COMPRAR", CCC)
    .send(Q,tell,stocks::recommend(S,neutro,CCC));
    .

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }
{ include("$jacamoJar/templates/org-obedient.asl") }
