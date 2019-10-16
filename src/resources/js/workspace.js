/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
RETRIEVE DATA ABOUT ONE WORKSPACE
* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

/*Get DF */
function getWorkspaceDetails() {
  var wks = [];
  const params = new URL(location.href).searchParams;
  const selectedWorkspace = params.get('workspace');

  const Http = new XMLHttpRequest();
  Http.open("GET", "./workspaces/" + selectedWorkspace);
  Http.send();
  Http.onreadystatechange = function() {
    if (this.readyState == 4 && this.status == 200) {
      wks = JSON.parse(Http.responseText);
      updateWksTable(wks);
    }
  };
};

function updateWksTable(wks) {
  var table = document.getElementById("wkstable");
  Object.keys(wks).forEach(function(p) {

/*TODO: it is not really working*/
    var tr = table.insertRow(-1);
    if (wks[p] === "workspace") {
      tr = table.createTHead();
    } else {
      tr = table.insertRow(-1);
    }

    var tr = table.insertRow(-1);
    var cellProperty = tr.insertCell(-1);
    var cellDetail = tr.insertCell(-1);
    cellProperty.innerHTML = p;
    if (typeof(wks[p]) === "string") {
      cellDetail.innerHTML = wks[p];
    } else {

      Object.keys(wks[p]).forEach(function(a) {
        /* Do not print system artifacts */
        const hidenArts = ["node", "console", "blackboard", "workspace", "manrepo"];
        const isHidden = (hidenArts.indexOf(a) >= 0);
        const isBody = a.endsWith("-body");
        const isOrgArtifact = (wks.artifacts[a].type.endsWith(".OrgBoard") || wks.artifacts[a].type.endsWith(".SchemeBoard") ||
          wks.artifacts[a].type.endsWith(".NormativeBoard") || wks.artifacts[a].type.endsWith(".GroupBoard"));

        if (!isHidden && !isBody && !isOrgArtifact) {
          cellDetail.innerHTML += a + "<br />";
        }
      });

    }
  });
}

/* modal window */
var modal = document.getElementById('modalwksgraph');
var btnModal = document.getElementById("btndiagram");
var span = document.getElementsByClassName("close")[0];
btnModal.onclick = function() {
  getWksGraph();
  modal.style.display = "block";
};
span.onclick = function() {
  modal.style.display = "none";
};
window.onclick = function(event) {
  if (event.target == modal) {
    modal.style.display = "none";
  }
};

function getWksGraph() {
  const params = new URL(location.href).searchParams;
  const selectedWorkspace = params.get('workspace');

  const Http = new XMLHttpRequest();
  Http.onreadystatechange = function() {
    if (Http.readyState == 4 && Http.status == 200) {
      renderGraphvizFromWorkspaceJson(selectedWorkspace, JSON.parse(Http.responseText));
    }
  };
  Http.open('GET', "./workspaces/" + selectedWorkspace);
  Http.send();
}


function renderGraphvizFromWorkspaceJson(wksName, wks) {
  const MAX_LENGTH = 35;
  var dot = [];
  var validContent = 0;
  dot.push("digraph G {\n");
  dot.push("\tgraph [\n");
  dot.push("\t\trankdir = \"LR\"\n");
  dot.push("\t\tbgcolor=\"transparent\"\n");
  dot.push("\t]\n");
  dot.push("\tsubgraph cluster_0 {\n");
  dot.push("\t\tlabel=\"" + wksName + "\"\n");
  dot.push("\t\tlabeljust=\"r\"\n");
  dot.push("\t\tgraph[style=dashed]\n");

  Object.keys(wks.artifacts).forEach(function(a) {
    if ((wks.artifacts[a].type === "cartago.WorkspaceArtifact") ||
      (wks.artifacts[a].type === "cartago.tools.Console") ||
      (wks.artifacts[a].type === "cartago.ManRepoArtifact") ||
      (wks.artifacts[a].type === "cartago.tools.TupleSpace") ||
      (wks.artifacts[a].type === "cartago.NodeArtifact") ||
      (wks.artifacts[a].type === "cartago.AgentBodyArtifact")) {
      ; /* do not print system artifacts */
    } else {
      validContent++;

      var s1;
      s1 = (wks.artifacts[a].artifact.length <= MAX_LENGTH) ? wks.artifacts[a].artifact :
        wks.artifacts[a].artifact.substring(0, MAX_LENGTH) + " ...";
      dot.push("\t\"" + wks.artifacts[a].artifact + "\" [ " + "\n\t\tlabel = \"" + s1 + ":\n");
      s1 = (wks.artifacts[a].type.length <= MAX_LENGTH) ? wks.artifacts[a].type :
        wks.artifacts[a].type.substring(0, MAX_LENGTH) + " ...";
      dot.push(s1 + "\"\n");
      dot.push("\t\tshape=record style=filled fillcolor=white\n");
      dot.push("\t\t];\n");

      /* agents that are observing this artifact */
      wks.artifacts[a].observers.forEach(function(y) {
        /* do not print agents_body observation */
        if (!wks.artifacts[a].type === "cartago.AgentBodyArtifact") {
          /* print node with defined shape */
          var s2 = (y.length <= MAX_LENGTH) ? y : y.substring(0, MAX_LENGTH) + "...";
          dot.push("\t\"" + y + "\" [ " + "\n\t\tlabel = \"" + s2 + "\"\n");
          dot.push("\t\tshape = \"ellipse\" style=filled fillcolor=white\n");
          dot.push("\t];\n");

          /* print arrow */
          dot.push("\t\t\"" + y + "\" -> \"" + wks.artifacts[a].artifact +
            "\" [arrowhead=\"odot\"];\n");
        }
      });

      /* linked artifacts */
      wks.artifacts[a].linkedArtifacts.forEach(function(y) {
        /* linked artifact node already exists if it belongs to this workspace */
        dot.push("\t\"" + wks.artifacts[a].artifact + "\" -> \"" + y +
          "\" [arrowhead=\"onormal\"];\n");
      });
    }
  });

  dot.push("\t}\n");
  dot.push("}\n");

  if (validContent <= 0) dot = ["digraph G {\"no artifacts\nto show\"}\n"];

  /* Transition follows modal top down movement */
  var t = d3.transition().duration(750).ease(d3.easeLinear);
  d3.select("#overviewgraph").graphviz().transition(t).renderDot(dot.join(""));
}


/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
END OF FILE
* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
