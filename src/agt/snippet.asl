/*
Multi
line comment in Jason AgentSpeak
*/

!goalToAchieve. //a desire

//single line comment

+!goalToAchieve <- //something triggered this plan
    .print("Hi"); //internal action starts with "."
    .

+!anotherGoal(Arg1,Arg2) <- //variables starts with Capital letters
    someNameSpace::anExternalAction(SomeArgument);
    +belief[annotation(100)];
    .

{ include("$moiseJar/asl/org-obedient.asl") } //an include
