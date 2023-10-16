[![Gitpod ready-to-code](https://img.shields.io/badge/Gitpod-ready--to--code-blue?logo=gitpod)](https://gitpod.io/#https://github.com/jacamo-lang/jacamo-web)

Jacamo-web is an interactive programming IDE based on [JaCaMo](http://jacamo.sourceforge.net/), a Multi-Agent System (MAS) Oriented Programming platform. The [interactive development](
https://cgi.csc.liv.ac.uk/~lad/emas2019/accepted/EMAS2019_paper_8.pdf) allows making changes on instances of agents, artefacts and organisations, which means that the system can be updated while it is running.

## Running locally

### Using a local gradle

```sh
$ git clone https://github.com/jacamo-lang/jacamo-web.git
$ cd jacamo-web
$ ./gradlew run
```
See http://yourIP:8080 for a web interface (see the console for the right IP:port). You can also try `./gradlew run --args="src/jcm/marcos.jcm"`, `./gradlew run --args="src/jcm/bob.jcm"`, and go to `/examples` for more sample projects and information about how to run them.


### Making front end changes
If you want to make changes to the jacamo-web front end, install [Node.js](https://nodejs.org/) and install the webpack client with ``yarn install -g webpack client``.
Then, install the jacamo-web front end dependencies by running ``yarn install`` in the project's root repository.
Run the webpack watch task with ``yarn run watch``. All changes you make to JavaScript files will be automatically built and deployed to your local jacamo-web instance.

### Using a local docker

You can create a local docker image of jacamo-web. First, make sure you are in the `jacamo-web` root folder, then run the following command (super user privileges may be necessary):

```sh
jacamo-web$ docker build -t jacamo-web:0.5 .
```

It will take some minutes to build the docker image. After finishing, the jacamo-web image should be listed by the command `docker image ls`. Execute the following command to launch a container based on jacamo-web docker image:

```sh
$ docker run -it --name jacamo-web jacamo-web:0.5
```

 At the very end of the log, you should see a message similar to "jacamo-web Rest API is running on http://yourIP:8080/" (e.g. http://172.17.0.2:8080/), so you can open your browser on the referred URL.

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
