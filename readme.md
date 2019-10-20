
Jacamo-web is an interactive programming IDE based on [JaCaMo](http://jacamo.sourceforge.net/), a Multi-Agent System (MAS) Oriented Programming platform. The [interactive development](
https://cgi.csc.liv.ac.uk/~lad/emas2019/accepted/EMAS2019_paper_8.pdf) allows making changes on instances of agents, artefacts and organisations, which means that the system can be updated while it is running.

# Running jacamo-web

After clonning this repository, go to the root folder of this project and type:
* `gradle marcos`: to run the sample MAS marcos.jcm
* `gradle bob`: to run the example bob.jcm
* go to `/examples` for more sample projects and information about how to run them
* see http://yourIP:8080 for a web interface (see the console for the right IP:port)


With docker:
* `sudo docker build  -t jacamo-runrest .` to build a docker image
* `sudo docker network create jcm_net`
* `sudo docker run -ti --rm --net jcm_net  --name host1 -v "$(pwd)":/app jacamo-runrest gradle marcos` to run marcos.jcm
* `sudo docker run -ti --rm --net jcm_net  --name host2 -v "$(pwd)":/app jacamo-runrest gradle bob_d` to run bob.jcm

# More about jacamo-web

It uses [jacamo-rest](https://github.com/jacamo-lang/jacamo-rest) on the back-end and provides a web front-end developed in plain javascript, the [API jacamo-rest 0.3](https://app.swaggerhub.com/apis/sma-das/jacamo-rest/0.3) bind these parts. 

In short, jacao-web provides an interface to develop Multi-Agent Systems interactively allow to send beliefs and plans to agents, inspect, create and destroy them. It is also supported dynamic compiling of CArtAgO artefacts and Moise organisations. The following diagram shows the main functionalities of the interface:

![Alt text](https://g.gravizo.com/source/programmingconcept?https%3A%2F%2Fraw.githubusercontent.com%2Fjacamo-lang%2Fjacamo-web%2Fmaster%2Freadme.md)
<details> 
<summary>Interactive programming concept with jacamo-web</summary>
programmingconcept
digraph G {
	graph [
		rankdir="RL"
	]
	subgraph cluster_1 {
		label="Environment: CArtAgO";
		Artifact [shape = record, label="Artifact"];
		ArtInsp [shape = plain, label="Artifact Inspection"];
		ArtEdit [shape = plain, label="Edit artifact code"];
		ArtCreate [shape = plain, label="Make new artifact"];
		ArtDispo [shape = plain, label="Dispose artifact"];
	}
	subgraph cluster_0 {
		label="Agents: Jason";
		Agent [label="Agent"];
		AgInsp [shape = plain, label="Agent Inspection"];
		AgCmd [shape = plain, label="Send commands"];
		AgEdit [shape = plain, label="Edit Agent"];
		AgCreate [shape = plain, label="Create agent"];
		AgKill [shape = plain, label="Kill agents"];
		AgDF [shape = plain, label="Directory Facilitator"];
	}
	subgraph cluster_2 {
		label="Organisation: Moise";
		Org [shape = tab, label="Organisation"];
		OrgInsp [shape = plain, label="Organisation Inspection"];
		OrgEdit [shape = plain, label="Edit organisation"];
		OrgAgR [shape = plain, label="Adopting role"];
		OrgAgM [shape = plain, label="Commiting mission"];
	}
	AgInsp -> Agent [color = gray20, fontcolor = gray20, style = dotted];
	AgCmd -> Agent [color = gray20, fontcolor = gray20, style = dotted];
	AgEdit -> Agent [color = gray20, fontcolor = gray20, style = dotted];
	AgCreate -> Agent [color = gray20, fontcolor = gray20, style = dotted];
	AgKill -> Agent [color = gray20, fontcolor = gray20, style = dotted];
	AgDF -> Agent [color = gray20, fontcolor = gray20, style = dotted];
	ArtInsp -> Artifact [color = gray20, fontcolor = gray20, style = dotted];
	ArtEdit -> Artifact [color = gray20, fontcolor = gray20, style = dotted];
 	ArtCreate -> Artifact [color = gray20, fontcolor = gray20, style = dotted];
 	ArtDispo -> Artifact [color = gray20, fontcolor = gray20, style = dotted];
 	OrgInsp -> Org [color = gray20, fontcolor = gray20, style = dotted];
	OrgEdit -> Org [color = gray20, fontcolor = gray20, style = dotted];
	OrgAgR -> Org [color = gray20, fontcolor = gray20, style = dotted];
	OrgAgM -> Org [color = gray20, fontcolor = gray20, style = dotted];
}
programmingconcept
</details>
