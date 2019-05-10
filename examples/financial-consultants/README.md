# Financial Agents Example
## Overview
This project is an on-the-fly programming application of a Multi-Agent System composed by "financial consultants". 
Each consultant uses a particular formula to recommend an asset or not. The assistant interacts with the user by telegram* sending a compiled suggestion about assets.

![Alt text](https://g.gravizo.com/source/financialAgentsOverview?https%3A%2F%2Fraw.githubusercontent.com%2Fjacamo-lang%2Fjacamo-rest%2Fmaster%2Fexamples%2Ffinancial-consultants%2FREADME.md)
<details> 
<summary>Financial Agents Overview</summary>
financialAgentsOverview
digraph G {
	subgraph cluster_0 {
		label="Multi-Agent System\nFinancial Agents";
		Assistant [label="Personal Assistant"];
		Expert1 [label="Expert 1"];
		ExpertN [label="Expert N"];
	}
	subgraph cluster_1 {
		label="Humans";
		Human [shape=circle];
	}
	subgraph cluster_2 {
		label="Legend";
		node[ shape = plaintext ];
		leg2[ label = "Through\nTelegram" ];
		leg4[ label = "ACL\nMessage" ];
		node [ shape = point height = 0 width = 0 margin = 0 ];
		leg1 leg3
		{ rank = same; leg1 leg2 }
		{ rank = same; leg3 leg4 }
		edge[ minlen = 1 ];
		leg1 -> leg2[ style = dotted ];
		leg3 -> leg4;
	}
	Human -> Assistant [color = gray20, fontcolor = gray20, style = dotted, label="Recomendation?"];
	Assistant -> Expert1 [color = gray20, fontcolor = gray20, label="ABCD?"];
	Expert1 -> Assistant [color = black, fontcolor = black, label="Buy\nABCD"];
	Assistant -> ExpertN [color = gray20, fontcolor = gray20, label="ABCD?"];
	ExpertN -> Assistant [color = black, fontcolor = black, label="Buy\nABCD"];
	Assistant -> Human [color = black, fontcolor = black, style = dotted, label="Buy\nABCD"];
}
financialAgentsOverview
</details>

### Table of contents
1. [Getting Started](#getting-started)
  * [Prerequisites](#prerequisites)
2. [Demo](#demo)
  * [Story behind](#story-behind)
  * [Running the demo](#running-the-demo)

## Getting Started
### Prerequisites
1. [Java JDK 8+](https://www.oracle.com/technetwork/pt/java/javase/).
2. [Gradle](https://gradle.org/install/).

### Preparing the system
1. Grab jacamo-web project 
```
ProjectsFolder$ git clone https://github.com/jacamo-lang/jacamo-web
```
2. Compiling jacamo-web to be used by financial agents example
```
ProjectsFolder/jacamo-web$ gradle build
```
3. Create a [Telegram Bot](https://core.telegram.org/bots) and add its token in routes.xml file
```
<routes xmlns="http://camel.apache.org/schema/spring" >
  <route id="myPAproduces">
    <from uri="jason:toTelegram"/>
    <convertBodyTo type="java.lang.String"/>
    <to uri="telegram:bots/BOT'S-TOKEN-HERE"/>
  </route>
  <route id="myPAconsumes">
    <from uri="telegram:bots/BOT'S-TOKEN-HERE"/>
    <convertBodyTo type="java.lang.String"/>
    <to uri="jason:myPA?performative=achieve"/>
  </route>
</routes>
```

## Demo
### Story behind
We will start with an empty system. On the first part we will create a simple artifact and simple agents to show how the system can be developed while it is running. In this sense, first we will create an artifact to represent a bank account, able to perform debit and credit operations. Then agents to use this artifact will be created, it refers to a couple that are going to marry and have a family, which later will be presented as a Multi-Agent organisation. Finally, we will use existing files in the folder of this example to quickly instantiate the "financial agents" which could be consultants of this family.

### Running the demo
1. Run financial agents example
```
ProjectsFolder/jacamo-web/examples/financial-consultants$ gradle run
```
2. Creating dynamic.Account artifact filling it with:
```
package dynamic;
import cartago.*;
public class Account extends Artifact {
	void init() {
	    defineObsProperty("balance", 0);
    }
    @OPERATION void debit(int debit) {
        ObsProperty prop = getObsProperty("balance");
        prop.updateValue(prop.intValue()-debit);
        signal("tick");
    }
    @OPERATION void credit(int credit) {
        ObsProperty prop = getObsProperty("balance");
        prop.updateValue(prop.intValue()+credit);
        signal("tick");
    }
}
```
3. Create agent bob. From bob intantiate a bank account
```
[bob] 
makeArtifact(account,"dynamic.Account",[],I); 
focus(I)
```

4. Create agent father. From father watch bob's account and give him some money
```
[bob] 
lookupArtifact(account,I); 
focus(I);
credit(100)
```
5. Let's edit bob's code, making him react when the balance changes
```
+balance(X) <- 
    .print("Balance changed to ",X).
```

6. Let's see how intentions are kept running while changed plans will produce different intentions
```
+balance(X) <- 
    .print("Balance changed to ",X).
```

7. Create agent alice. Then, bob ask alice for marriage, but at this moment she denies
```
[bob] 
.send(alice,askOne,marry(bob),R); 
.print(R)
```

8. alice becomes willing to marry bob
```
[alice] 
+marry(bob)
```

9. bob tries again
```
[bob] 
.send(alice,askOne,marry(bob),R); 
.print(R)
```

10. Since the folder src/org/ already has family.xml file, let's create this organisation
```
[bob] 
createWorkspace(family); 
joinWorkspace(family,Ofa);
o::makeArtifact(family, "ora4mas.nopl.OrgBoard", ["src/org/family.xml"], OrgArtId)[wid(Ofa)];
o::focus(OrgArtId);
g::createGroup(familygroup, familygroup, GrArtId);
g::focus(GrArtId);
```
11. bob takes husband role
```
[bob] 
joinWorkspace(family,Ofa);
g::lookupArtifact(familygroup, GrArtId)[wid(Ofa)];
g::focus(GrArtId)[wid(Ofa)];
g::adoptRole(husband)[wid(Ofa)];
```
12. alice takes wife role
```
[alice] 
joinWorkspace(family,Ofa);
g::lookupArtifact(familygroup, GrArtId)[wid(Ofa)];
g::focus(GrArtId)[wid(Ofa)];
g::adoptRole(wife)[wid(Ofa)];
```
13. They will have a child, create role "family.familygroup.son"

14. Using other existing files (one.asl, graham.asl,...), create agent called "one"
```
[one]
!launchFinancialAgents
```
15. Send a message through Telegram:
```
[myPA]
.send(toTelegram,tell,hiEmas)
```
16. Now the system are fully instantiated, we can explore the MAS dimensions and finally shutdown the demo:
```
[one] 
.stopMAS
```
