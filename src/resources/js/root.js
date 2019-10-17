/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
NEW ORGANIZATIONAL ROLE
* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

/* create role: POST in "/{oename}/group/{groupname}" */
function newRole(org, gr) {
	/*
	TODO: The current method used window.open on the js, which is not showing the html to feedback the user after the post. Maybe a solution is changing it to a form using submit function
	var f = document.getElementById('TheForm');
	f.something.value = something;
	f.more.value = additional;
	f.other.value = misc;
	window.open('', 'TheWindow');
	f.submit();*/

	http = new XMLHttpRequest();
	var input = document.getElementById('orgGrRole').value;
	var lastDot = input.lastIndexOf('.');
	var firstPart = input.substring(0, lastDot);
	var role = input.substring(lastDot + 1);
	var firstDot = firstPart.indexOf('.');
	var org = firstPart.substring(0, firstDot);
	var group = firstPart.substring(firstDot + 1);

	console.log(org + "/" + group + "/" + role);

    http.open("POST", '/oe/' + org + '/group/'+ group, false);
    http.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
    var data = "role=" + encodeURIComponent(role);
    http.send(data);
    window.open('/oe/' + org + '/os', 'mainframe', 'location=yes,status=yes');
}

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
function getAgent(agent) {
	return new Promise(function(res, rej) {
		const Http = new XMLHttpRequest();
	  Http.open("GET", "/agents/" + agent);
		Http.send();
		Http.onreadystatechange = function() {
			console.log("Trying to acces " + "/agents/" + agent);
	    if (this.readyState == 4 && this.status == 200) {
				res(JSON.parse(Http.responseText));
	    }
	  };
	});
};


function getMASAsDot() {
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
			getAgents.then((agents) => {agents.forEach(function(a) {
				var s1 = (a.length <= MAX_LENGTH) ? a : a.substring(0, MAX_LENGTH) + " ...";
	      dot.push("\t\t\"" + a + "\" [ ");
	      dot.push("\n\t\t\tlabel = \"" + s1 + "\"");
	      dot.push("\n\t\t\tshape = \"ellipse\" style=filled fillcolor=white\n");
	      dot.push("\t\t];\n");
	      /* get workspaces the agent are in (including organizations) */
	      var workspacesIn = [];
				getAgent(a).then((ag) => {
					ag.workspaces.forEach(function(w) {
						workspacesIn.push(w.workspace);
					});
				});
	      allwks.push(workspacesIn);
				dot.push("\t\t{rank=same " +
		      agents +
		      "};\n");
		    dot.push("\t};\n");
			})});
	  }
	}





  dot.push("}\n");
	console.log(dot);
}

/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
END OF FILE
* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
