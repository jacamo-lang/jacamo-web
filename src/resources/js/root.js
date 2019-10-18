/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
WHOLE SYSTEM AS DOT
* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

/*Get list of agent from backend*/
var getAgents = new Promise((res, rej) => {
  var agents = ["agent1", "agent2"];
  const Http = new XMLHttpRequest();
  Http.open("GET", "./agents");
  Http.send();
  Http.onreadystatechange = function() {
    if (this.readyState == 4 && this.status == 200) {
      res(JSON.parse(Http.responseText));
    }
  };
});

/*Get one agent from backend*/
async function getAgent(agent) {
	return new Promise(function(res, rej) {
		const Http = new XMLHttpRequest();
	  Http.open("GET", "/agents/" + agent);
		Http.send();
		Http.onreadystatechange = function() {
	    if (this.readyState == 4 && this.status == 200) {
				res(JSON.parse(Http.responseText));
	    }
	  };
	});
};

function get(url) {
  return new Promise(function(resolve, reject) {
    var req = new XMLHttpRequest();
    req.open('GET', url);
    req.onload = function() {
      if (req.status == 200) {
        resolve(req.response);
      } else {
        reject(Error(req.statusText));
      }
    };
    req.onerror = function() {
      reject(Error("Network Error"));
    };
    req.send();
  });
}

function getMASAsDot() {
	return new Promise(function(res, rej) {
	const MAX_LENGTH = 35;
  var dot = [];
  var allwks = [];

  dot.push("digraph G {\n");
  dot.push("\tgraph [\n");
  dot.push("\t\trankdir=\"TB\"\n");
  dot.push("\t\tbgcolor=\"transparent\"\n");
  dot.push("\t]\n");

	{
	  /* agents dimension */
	  dot.push("\tsubgraph cluster_ag {\n");
	  dot.push("\t\tlabel=\"agents\"\n");
	  dot.push("\t\tlabeljust=\"r\"\n");
	  dot.push("\t\tpencolor=gray\n");
	  dot.push("\t\tfontcolor=gray\n");
	  {
	    /* agent's mind */

			get('./agents').then(function(agents) {
				console.log("Trying to acces " + "/agents/" + a);
				console.log(a + " - " + dot.join(""));
					var s1 = (a.length <= MAX_LENGTH) ? a : a.substring(0, MAX_LENGTH) + " ...";
		      dot.push("\t\t\"" + a + "\" [ ");
		      dot.push("\n\t\t\tlabel = \"" + s1 + "\"");
		      dot.push("\n\t\t\tshape = \"ellipse\" style=filled fillcolor=white\n");
		      dot.push("\t\t];\n");
		      /* get workspaces the agent are in (including organizations) */

		      var workspacesIn = [];
					console.log("Trying to acces " + "/agents/" + a);
			}, function(error) {
			  console.error("Failed!", error);
			})

			getAgents.then((agents) => {agents.forEach(function(a) {
			console.log("Trying to acces " + "/agents/" + a);
			console.log(a + " - " + dot.join(""));
				var s1 = (a.length <= MAX_LENGTH) ? a : a.substring(0, MAX_LENGTH) + " ...";
	      dot.push("\t\t\"" + a + "\" [ ");
	      dot.push("\n\t\t\tlabel = \"" + s1 + "\"");
	      dot.push("\n\t\t\tshape = \"ellipse\" style=filled fillcolor=white\n");
	      dot.push("\t\t];\n");
	      /* get workspaces the agent are in (including organizations) */

	      var workspacesIn = [];
				console.log("Trying to acces " + "/agents/" + a);
				getAgent(a).then((ag) => {
					ag.workspaces.forEach(function(w) {
						console.log("Got from " + "/agents/" + a);
						workspacesIn.push(w.workspace);
					});
				});
	      allwks.join(workspacesIn);
				dot.push("\t\t{rank=same " + agents + "}\n");
			})});
	  }
		dot.push("\t};\n");
	}
  dot.push("}\n");

	res(dot.join(""));
});
}

/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
END OF FILE
* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
