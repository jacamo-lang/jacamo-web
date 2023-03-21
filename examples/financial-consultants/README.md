# Financial Agents Example
## Overview
This project is an on-the-fly programming application of a Multi-Agent System composed by "financial consultants". 
Each consultant uses a particular formula to recommend an asset or not.

It was presented on [EMAS 2019 - 7th International Workshop on Engineering Multi-Agent Systems](http://cgi.csc.liv.ac.uk/~lad/emas2019/). [Slides available](https://pt.slideshare.net/clebercbr/jacamo-web-is-on-the-fly-an-interactive-multiagent-systems-programming-environment).

![Alt text](https://g.gravizo.com/source/financialAgentsOverview?https%3A%2F%2Fraw.githubusercontent.com%2Fjacamo-lang%2Fjacamo-web%2Fmaster%2Fexamples%2Ffinancial-consultants%2FREADME.md)
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
1. [Java JDK 17+](https://www.oracle.com/technetwork/pt/java/javase/).

### Preparing the system
1. Grab jacamo-web project 
```
ProjectsFolder$ git clone https://github.com/jacamo-lang/jacamo-web
```
2. Run the example:
```
ProjectsFolder/jacamo0-web/examples/financial-consultants$ ./gradlew
```
3. Open your browser on the address indicated in the console, which is probably: "jacamo-web Rest API is running on http://127.0.0.1:8080/":
