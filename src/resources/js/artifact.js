/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
RETRIEVE DATA ABOUT ONE WORKSPACE
* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

/*Get DF */
function getArtifactDetails() {
  var art = [];
  const params = new URL(location.href).searchParams;
  const selectedWorkspace = params.get('workspace');
  const selectedArtifact = params.get('artifact');

  const Http = new XMLHttpRequest();
  Http.open("GET", "./workspaces/" + selectedWorkspace + "/" + selectedArtifact);
  Http.send();
  Http.onreadystatechange = function() {
    if (this.readyState == 4 && this.status == 200) {
      art = JSON.parse(Http.responseText);
      updateArtifactTable(art);
    }
  };
};

function updateArtifactTable(art) {
  var table = document.getElementById("arttable");
  Object.keys(art).forEach(function(p) {

    var tr, cellProperty, cellDetail;
    if (p === "artifact") {
      var header = table.createTHead();
      tr = header.insertRow(0);
      cellDetail = tr.insertCell(0);
      cellProperty = tr.insertCell(0);
    } else {
      tr = table.insertRow(-1);
      cellProperty = tr.insertCell(-1);
      cellDetail = tr.insertCell(-1);
    }

    cellProperty.innerHTML = p;
    if (typeof(art[p]) === "string") {
      cellDetail.innerHTML = art[p];
    } else {
      Object.keys(art[p]).forEach(function(a) {
        if (p === "properties") {
          Object.keys(art[p][a]).forEach(function(b) {
            cellDetail.innerHTML += b + "(" + art[p][a][b] + ")<br />";
          });
        } else {
          cellDetail.innerHTML += art[p][a] + "<br />";
        }
      });
    }
  });
}

/* modal window */
var modal = document.getElementById('modalartgraph');
var btnModal = document.getElementById("btnartdiagram");
var span = document.getElementsByClassName("close")[0];
btnModal.onclick = function() {
  /*getArtGraph();*/
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

function getArtGraph() {
  const params = new URL(location.href).searchParams;
  const selectedWorkspace = params.get('workspace');
  const selectedArtifact = params.get('artifact');

  const Http = new XMLHttpRequest();
  Http.onreadystatechange = function() {
    if (Http.readyState == 4 && Http.status == 200) {
      renderGraphvizFromArtifactJson(selectedWorkspace, JSON.parse(Http.responseText));
    }
  };
  Http.open('GET', "./workspaces/" + selectedWorkspace + "/" + selectedArtifact);
  Http.send();
}

function renderGraphvizFromArtifactJson(artName, art) {
  const MAX_LENGTH = 35;
  var dot = [];
  var validContent = 0;
  dot.push("digraph G {\n");
  dot.push("\tgraph [\n");
  dot.push("\t\trankdir = \"LR\"\n");
  dot.push("\t\tbgcolor=\"transparent\"\n");
  dot.push("\t]\n");


console.log("test");
  console.log(art);

    var s1 = (artName.length <= MAX_LENGTH) ? artName : artName.substring(0, MAX_LENGTH) + " ...";
    dot.push("\t\"" + artName + "\" [ " + "\n\t\tlabel = \"" + s1 + ":\n");
    s1 = (art.type.length <= MAX_LENGTH) ? art.type.length : art.type.length.substring(0, MAX_LENGTH) + " ...";
    dot.push(s1 + "|");

    /* observable properties */
    Object.keys(art.properties).forEach(function(y) {
      var s2 = (y.length <= MAX_LENGTH) ? y : y.substring(0, MAX_LENGTH) + " ...";
      dot.push("\t" + s2);
    });
    dot.push("\t|");

    /* operations */
    art.operations.forEach(function(y) {
      var s2 = (y.length <= MAX_LENGTH) ? y : y.substring(0, MAX_LENGTH) + " ...";
      dot.push("\t" + s2 );
    });
    dot.push("\n");

  dot.push("\t\tshape=record style=filled fillcolor=white\n");
  dot.push("\t];\n");

  dot.push("}\n");

console.log(dot.join(""));

  /* Transition follows modal top down movement */
  var t = d3.transition().duration(750).ease(d3.easeLinear);
  d3.select("#artifactgraph").graphviz().transition(t).renderDot(dot.join(""));
}


/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
END OF FILE
* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
