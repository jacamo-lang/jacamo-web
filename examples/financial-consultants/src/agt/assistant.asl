!joinFinancialGroup.

+!opinion(S) <-
    .abolish(recommend(S,_,_));
    .wait(500);
    .send(bazin,achieve,opinion(S));
    .send(grahan,achieve,opinion(S));
    .send(greenblatt,achieve,opinion(S));
    .

+recommend(S,_,_) : .count(recommend(S,_,_),N) & N >= 3 <-
    !reply(S);
    .

+recommend(_,_,_).

+!reply(S) : 
    recommend(S,Y1,Z1)[source(bazin)] & recommend(S,Y2,Z2)[source(grahan)] & 
    recommend(S,Y3,Z3)[source(greenblatt)] & .count(recommend(S,comprar,_),N) & N >= 2 
    <-
    .concat(Z1,"\n\n",Z2,"\n\n",Z3,"\n\nResumo: COMPRAR", CCC);
    .send(toTelegram,tell,CCC);
    .

+!reply(S) : 
    recommend(S,Y1,Z1)[source(bazin)] & recommend(S,Y2,Z2)[source(grahan)] & 
    recommend(S,Y3,Z3)[source(greenblatt)] 
    <-
    .concat(Z1,"\n\n",Z2,"\n\n",Z3,"\n\nResumo: NAO COMPRAR", CCC);
    .send(toTelegram,tell,CCC);
    .

+!reply(S) <-
    .concat("Ocorreu um erro ao tentar obter as opinioes dos consultores.", CCC);
    .send(toTelegram,tell,CCC);
    .
+!joinFinancialGroup <-
    joinWorkspace(financialagents,Ofa);
    .print("DEBUG: Joining group...");
    g::lookupArtifact(financial_team, GrArtId);
    g::focus(GrArtId);
    g::adoptRole(assistant);
    .


{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }
//{ include("$jacamoJar/templates/org-obedient.asl") }
