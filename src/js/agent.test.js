const index = require('./agent')
/*
sample file
*/

kqmlns = [
    "kqml::bel_no_source_self(NS::Content,Ans)[source(self)] :- (NS::Content[|LA] \u0026 (kqml::clear_source_self(LA,NLA) \u0026 ((Content \u003d.. [F,T,_74]) \u0026 (Ans \u003d.. [NS,F,T,NLA]))))",
    "kqml::clear_source_self([],[])[source(self)]",
    "kqml::clear_source_self([source(self)|T],NT)[source(self)] :- kqml::clear_source_self(T,NT)"
];
ons = [
    "o::focused(orgWks,group1[artifact_type(\"ora4mas.nopl.GroupBoard\")],cobj_4)[source(percept)]",
    "o::play(one,role0,group1)[artifact_id(cobj_4),artifact_name(cobj_4,group1),percept_type(obs_prop),source(percept),workspace(cobj_4,orgWks,cobj_2)]",
    "o::specification(group_specification(group1,[role(role0,[],[soc],1,1,[],[link(authority,role2,intra_group),link(authority,role1,intra_group)]),role(role1,[],[soc],1,1,[role2],[link(communication,role2,intra_group)]),role(role2,[],[soc],1,1,[],[link(communication,role1,intra_group)])],[],properties([])))[artifact_id(cobj_4),artifact_name(cobj_4,group1),percept_type(obs_prop),source(percept),workspace(cobj_4,orgWks,cobj_2)]"
];
defaultns = [
    "joined(wks,cobj_6)[artifact_id(cobj_7),artifact_name(cobj_7,bob_body),percept_type(obs_prop),source(percept),workspace(cobj_7,wks,cobj_6)]",
    "bel_no_source_self(NS::Content,Ans)[source(self)] :- (NS::Content[|LA] \u0026 (kqml::clear_source_self(LA,NLA) \u0026 ((Content \u003d.. [F,T,_74]) \u0026 (Ans \u003d.. [NS,F,T,NLA]))))",
    "ancestor(X,Y) :- parent(X,Y)"
];

test('kqml-namespace', () => {
    kqmlns.forEach(function(item) {
        expect(getNamespace(item)).toBe('kqml');
    });
});

test('o-namespace', () => {
    ons.forEach(function(item) {
        expect(getNamespace(item)).toBe('o');
    });
});

test('default-namespace', () => {
    defaultns.forEach(function(item) {
        expect(getNamespace(item)).toBe('default');
    });
});
