stocks::radar(['ABEV3','B3SA3','BBAS3','CYRE3','ECOR3','EGIE3','ELET3','VIVT4']).

!joinFinancialGroup.

+!joinFinancialGroup <-
    joinWorkspace(financialagents,Ofa);
    .print("DEBUG: Joining group...");
    g::lookupArtifact(financialteam, GrArtId)[wid(Ofa)];
    g::focus(GrArtId)[wid(Ofa)];
    g::adoptRole(assistant)[wid(Ofa)];
    .
    
+!acceptScheme[source(self)] <-
    s::lookupArtifact(financialsch, ScArtId)[wid(Ofa)];
    s::focus(ScArtId)[wid(Ofa)];
    s::commitMission("mAssistant")[artifact_id(ScArtId),wid(Ofa)];
    .

+!lookForOpportunities : stocks::radar(L) <-

    //TODO: I've tried to make a sequence on org, firstly making sure 3 mConsultant where committed to than do this plan, but unfortunatelly a .wait is still necessary
    .wait(3000); 

    .print("Looking for opportunities on the stocks of my radar...");
    .abolish(stocks::recommend(_,_,_));
    for (.member(Item,L)) {
        .send(bazin,achieve,opinion(Item));
        .send(graham,achieve,opinion(Item));
        .send(greenblatt,achieve,opinion(Item));
        .print("Asking for: '",Item,"'.");
        
        //Why this wait is necessary?
        .wait(2000);
    };
    .

+!checkOpportunities : stocks::radar(L) <-

    //TODO: I've tried to make a sequence on org, firstly making sure 3 mConsultant where committed to than do this plan, but unfortunatelly a .wait is still necessary
    .wait(30000); 

    .print("Checking if there are good opportunities...");
    for (.member(T,L)) {
        .term2string(T,Item)
        .count(stocks::recommend(Item,comprar,_),N);
        if (N == 3) {
            .concat("Recomendado COMPRAR por todos os consultores: ",Item, CCC);
            .send(toTelegram,tell,CCC);
        }
    };
    for (.member(T,L)) {
        .term2string(T,Item)
        .count(stocks::recommend(Item,comprar,_),N);
        if (N == 2) {
            .concat("Recomendado COMPRAR por 2/3 dos consultores: ",Item, CCC);
            .send(toTelegram,tell,CCC);
        }
    };
    .
    
+!getOpportunities <- 
    println("*** Finished ***");
    .

+!opinion(S) <-
    .abolish(stocks::recommend(S,_,_));
    .send(bazin,achieve,opinion(S));
    .send(graham,achieve,opinion(S));
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

+!reply(T) :
    .term2string(T,S) 
    & stocks::recommend(S,Y1,Z1)[source(bazin)] & stocks::recommend(S,Y2,Z2)[source(graham)] 
    & stocks::recommend(S,Y3,Z3)[source(greenblatt)] & .count(stocks::recommend(S,comprar,_),N) & N >= 2 
    <-
    .concat(Z1,"\n\n",Z2,"\n\n",Z3,"\n\nResumo: COMPRAR", CCC);
    .send(toTelegram,tell,CCC);
    .

+!reply(T) : 
    .term2string(T,S)
    & stocks::recommend(S,Y1,Z1)[source(bazin)] & stocks::recommend(S,Y2,Z2)[source(graham)] 
    & stocks::recommend(S,Y3,Z3)[source(greenblatt)] 
    <-
    .concat(Z1,"\n\n",Z2,"\n\n",Z3,"\n\nResumo: NAO COMPRAR", CCC);
    .send(toTelegram,tell,CCC);
    .

+!reply(T) : .term2string(T,S) <-
    .concat("Ocorreu um erro ao tentar obter as opinioes dos consultores em ",S, CCC);
    .send(toTelegram,tell,CCC);
    .

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }
{ include("$jacamoJar/templates/org-obedient.asl") }
            
            
