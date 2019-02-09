# Finantial Agents Example
This project is an on-the-fly programming application of a Multi-Agent System composed by "financial consultants". 
Each consultant uses a particular formular to recommend an asset or not. 
There are assistants that interacts with the user by telegram* sending a compiled suggestion about some asset.

*The file routes.xml must be filled with a telegram bot's token

![Alt text](https://g.gravizo.com/source/financialAgentsOverview?https%3A%2F%2Fraw.githubusercontent.com%2Fjacamo-lang%2Fjacamo-rest%2Fmaster%2Fexamples%2Ffinancial-consultants%2FREADME.md)
<details> 
<summary>Financial Agents Overview</summary>
financialAgentsOverview
digraph G {
	subgraph cluster_0 {
		label="Multi-Agent System\nFinantial Agents";
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
	Expert1 -> Assistant [color = black, fontcolor = black, label="ABCD\nwill rise"];
	Assistant -> ExpertN [color = gray20, fontcolor = gray20, label="ABCD?"];
	ExpertN -> Assistant [color = black, fontcolor = black, label="ABCD\nis cheap"];
	Assistant -> Human [color = black, fontcolor = black, style = dotted, label="Buy\nABCD"];
}
financialAgentsOverview
</details>
