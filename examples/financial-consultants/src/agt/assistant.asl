stocks::radar(['ABEV3','B3SA3','BBAS3','BBDC3','BBDC4','BBSE3','BRAP4','BRDT3','BRFS3','BRKM5','BRML3','BTOW3','CCRO3','CIEL3','CMIG4','CSAN3','CSNA3','CVCB3','CYRE3','ECOR3','EGIE3','ELET3','ELET6','EMBR3','ENBR3','EQTL3','ESTC3','FLRY3','GGBR4','GOAU4','GOLL4','HYPE3','IGTA3','ITSA4','ITUB4','JBSS3','KLBN11','KROT3','LAME4','LOGG3','LREN3','MGLU3','MRFG3','MRVE3','MULT3','NATU3','PCAR4','PETR3','PETR4','QUAL3','RADL3','RAIL3','RENT3','SANB11','SBSP3','SMLS3','SUZB3','TAEE11','TIMP3','UGPA3','USIM5','VALE3','VIVT4','VVAR3','WEGE3']).

!joinFinancialGroup.


+!lookforopportunities : stocks::radar(L) <-
    .abolish(stocks::recommend(_,_,_));
    .wait(500);
    for (.member(Item,L)) {
        .send(bazin,achieve,opinion(Item));
        .send(grahan,achieve,opinion(Item));
        .send(greenblatt,achieve,opinion(Item));
        .print("Asking for: '",Item,"'.");
        .wait(1500);
    };
    // back to .wait model to make 'look for opportunities' similarly
    .wait(1500); 
    !checkopportunities;
    .

+!checkopportunities : stocks::radar(L) <-
    for (.member(Item,L)) {
        .count(stocks::recommend(Item,comprar,_),N);
        if (N == 3) {
            .concat("Recomendado COMPRAR por todos os consultores: ",Item, CCC);
            .send(toTelegram,tell,CCC);
        }
    };
    for (.member(Item,L)) {
        .count(stocks::recommend(Item,comprar,_),N);
        if (N == 2) {
            .concat("Recomendado COMPRAR por 2/3 dos consultores: ",Item, CCC);
            .send(toTelegram,tell,CCC);
        }
    };
    .

+!opinion(S) <-
    .abolish(stocks::recommend(S,_,_));
    .wait(500);
    .send(bazin,achieve,opinion(S));
    .send(grahan,achieve,opinion(S));
    .send(greenblatt,achieve,opinion(S));
    .wait(2500); // back to .wait model to make 'look for opportunities' similarly
    !!reply(S);
    .
/*
+stocks::recommend(S,_,_) : .count(stocks::recommend(S,_,_),N) & N >= 3 <-
    !reply(S);
    .

+stocks::recommend(_,_,_).
*/

+!reply(S) : 
    stocks::recommend(S,Y1,Z1)[source(bazin)] & stocks::recommend(S,Y2,Z2)[source(grahan)] & 
    stocks::recommend(S,Y3,Z3)[source(greenblatt)] & .count(stocks::recommend(S,comprar,_),N) & N >= 2 
    <-
    .concat(Z1,"\n\n",Z2,"\n\n",Z3,"\n\nResumo: COMPRAR", CCC);
    .send(toTelegram,tell,CCC);
    .

+!reply(S) : 
    stocks::recommend(S,Y1,Z1)[source(bazin)] & stocks::recommend(S,Y2,Z2)[source(grahan)] & 
    stocks::recommend(S,Y3,Z3)[source(greenblatt)] 
    <-
    .concat(Z1,"\n\n",Z2,"\n\n",Z3,"\n\nResumo: NAO COMPRAR", CCC);
    .send(toTelegram,tell,CCC);
    .

+!reply(S) <-
    .concat("Ocorreu um erro ao tentar obter as opinioes dos consultores.", CCC);
    .send(toTelegram,tell,CCC);
    .
+!joinFinancialGroup <-
    joinWorkspace(financialorg,Ofa);
    .print("DEBUG: Joining group...");
    g::lookupArtifact(financialteam, GrArtId);
    g::focus(GrArtId);
    g::adoptRole(assistant);
    .


{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }
//{ include("$jacamoJar/templates/org-obedient.asl") }
