# Marketplace Example
## Overview
This example intends to demonstrate jacamo-web, an on-the-fly programming IDE, highlighting its collaborative tools.

Each consultant uses a particular formula to recommend an asset or not. The assistant interacts with the user by telegram* sending a compiled suggestion about assets.

![Alt text](https://g.gravizo.com/source/marketplaceOverview?https%3A%2F%2Fraw.githubusercontent.com%2Fjacamo-lang%2Fjacamo-web%2Fmaster%2Fexamples%2Fmarketplace%2FREADME.md)
<details>
<summary>Marketplace Agents Overview</summary>
marketplaceOverview
digraph G { graph [ rankdir="TB" bgcolor="transparent"]
	subgraph cluster_org {
		label="organisation" labeljust="r" pencolor=gray fontcolor=gray
		"sales" [ label = "sales" shape=tab style=filled pencolor=black fillcolor=lightgrey];
		"businessPartners" [ label = "businessPartners" shape=tab style=filled pencolor=black fillcolor=lightgrey];
		"itemTransaction" [ label = "itemTransaction" shape=hexagon style=filled pencolor=black fillcolor=linen];
		{rank=same "businessPartners" "itemTransaction"};
		"purchases" [ label = "purchases" shape=tab style=filled pencolor=black fillcolor=lightgrey];
		"businessPartners" [ label = "businessPartners" shape=tab style=filled pencolor=black fillcolor=lightgrey];
		"itemTransaction" [ label = "itemTransaction" shape=hexagon style=filled pencolor=black fillcolor=linen];
	};
	"sales"->"seller" [arrowtail=normal dir=back label="rseller"]
 	"businessPartners"->"seller" [arrowtail=normal dir=back label="rrseller"]
 	"itemTransaction"->"seller" [arrowtail=normal dir=back label="msupply"]
 	"businessPartners"->"itemTransaction" [arrowtail=normal arrowhead=open label="responsible"]
 	"purchases"->"buyer" [arrowtail=normal dir=back label="rbuyer"]
 	"businessPartners"->"buyer" [arrowtail=normal dir=back label="rrbuyer"]
 	"itemTransaction"->"buyer" [arrowtail=normal dir=back label="mbuy"]
	subgraph cluster_ag {
		label="agents" labeljust="r" pencolor=gray fontcolor=gray
		"seller" [label = "seller" shape = "ellipse" style=filled fillcolor=white];
		"buyer" [label = "buyer" shape = "ellipse" style=filled fillcolor=white];
		{rank=same "seller" "buyer"};
	};
	subgraph cluster_env {
		label="environment" labeljust="r" pencolor=gray fontcolor=gray
		subgraph cluster_main {
			label="main" labeljust="r" style=dashed pencolor=gray40 fontcolor=gray40
		};
		subgraph cluster_marketplace {
			label="marketplace" labeljust="r" style=dashed pencolor=gray40 fontcolor=gray40
		};
		subgraph cluster_sellerStorehouse {
			label="sellerStorehouse" labeljust="r" style=dashed pencolor=gray40 fontcolor=gray40
			"sellerStorehouse_outputStock" [label = "outputStock:\ndynamic.Counter"shape=record style=filled fillcolor=white];
			{rank=same "sellerStorehouse_outputStock"};
		};
		"seller"->"sellerStorehouse_outputStock" [arrowhead=odot]
		subgraph cluster_supplier {
			label="supplier" labeljust="r" style=dashed pencolor=gray40 fontcolor=gray40
			{rank=same "sellerStorehouse_outputStock"};
		};
		subgraph cluster_main {
			label="main" labeljust="r" style=dashed pencolor=gray40 fontcolor=gray40
		};
		subgraph cluster_buyerStorehouse {
			label="buyerStorehouse" labeljust="r" style=dashed pencolor=gray40 fontcolor=gray40
			"buyerStorehouse_inputStock" [label = "inputStock:\ndynamic.Counter"shape=record style=filled fillcolor=white];
			{rank=same "buyerStorehouse_inputStock"};
		};
		"buyer"->"buyerStorehouse_inputStock" [arrowhead=odot]
		subgraph cluster_factory {
			label="factory" labeljust="r" style=dashed pencolor=gray40 fontcolor=gray40
			{rank=same "buyerStorehouse_inputStock"};
		};
		subgraph cluster_marketplace {
			label="marketplace" labeljust="r" style=dashed pencolor=gray40 fontcolor=gray40
			{rank=same "buyerStorehouse_inputStock"};
		};
	};
}
marketplaceOverview
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

## Demo
### Story behind
Let us say we need to develop a marketplace application where suppliers and consumers can make business. We will start with a system containing two organisations: (i) the 'factory', an organisation that will produce something and needs to buy components to do so, (ii) the 'supplier', an organisation that sells components). The system will be launched with these two organisations, each one with one one agent, respectively, 'buyer' and 'seller'. In the environment, each one has one artefact, respectively, 'inputStock' and 'outputStock'. Our demo will show step by step how we could fill this agents with new plans to make then able to make an order, reply when paid, to finally receive the product. Let us say that this system will be developed by two developers, each one responsible for one organisation. We will show jacamo-web facilities to provide communication and coherence between these developers and what they are coding.

### Running the demo
```
ProjectsFolder/jacamo-web/examples/marketplace$ ./gradlew run
```
