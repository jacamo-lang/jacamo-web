/**
 * IMPORTS
 */
const h = require('./helpers')
const p = require('./parameters')

/**
 * ARTIFACT EDITOR FUNCTIONS
 */

/* Fill text area with current artifact's java code */
function getCurrentJavaContent() {
  var selectedJAVAFile = new URL(location.href).searchParams.get('javafile');

  h.get("/workspaces/temp/javafile/" + selectedJAVAFile).then(function(response) {
    const submit = document.createElement('button');
    submit.setAttribute("type", "submit");
    /*submit.setAttribute("onclick", "window.location.replace('./workspaces.html')");*/
    submit.innerHTML = "Save & Reload";
    document.getElementById("footer_menu").appendChild(submit);
    const cancel = document.createElement('button');
    cancel.setAttribute("type", "button");
    cancel.setAttribute("onclick", "location.href='./workspaces.html'");
    cancel.innerHTML = "Discard changes";
    document.getElementById("footer_menu").appendChild(cancel);
    const text = document.createElement('i');
    text.style.fontSize = "12px";
    text.innerHTML = "Editing: <b>" + selectedJAVAFile + "</b>";
    document.getElementById("footer_menu").appendChild(text);

    const form = document.getElementById("usrform");
    createJavaEditor(response);
  });
}

function createJavaEditor(content) {
  /* find the textarea */
  var textarea = document.querySelector("form textarea[name='javafile']");

  /* create ace editor */
  var editor = ace.edit();
  editor.session.setValue(content);
  editor.setTheme("ace/theme/textmate");
  editor.session.setMode("ace/mode/java");
  editor.setOptions({
    enableBasicAutocompletion: true
  });

  /* replace textarea with ace */
  textarea.parentNode.insertBefore(editor.container, textarea);
  textarea.style.display = "none";
  /* find the parent form and add submit event listener */
  var form = textarea;
  while (form && form.localName != "form") form = form.parentNode;
  form.addEventListener("submit", function(e) {
    e.preventDefault();
    textarea.value = editor.getValue();
    var selectedJAVAFile = new URL(location.href).searchParams.get('javafile');
    h.post("/workspaces/temp/javafile/" + selectedJAVAFile, new FormData(e.target)).then(function(response) {
      localStorage.setItem("workspaceBuffer", response);
      window.location.replace('./workspaces.html');
    });

  }, true);
}

/* create artifact */
function newArt() {
  /*Hopefully there is not an artifact with the referred name in a wks called 'temp', if so, its source will be opened*/
  h.get('/workspaces/temp/javafile/' + document.getElementById('createArtifact').value).then(function(resp) {
    window.location.assign('/artifact_editor.html?javafile=' + document.getElementById('createArtifact').value);
  });
}


/**
 * ARTIFACT FUNCTIONS
 */

/* RETRIEVE DATA FROM DIRECTORY FACILITATOR */
function getArtifactDetails() {
  const params = new URL(location.href).searchParams;
  const selectedWorkspace = params.get('workspace');
  const selectedArtifact = params.get('artifact');

  h.get("./workspaces/" + selectedWorkspace + "/" + selectedArtifact).then(function(artstr) {
    art = JSON.parse(artstr);
    if (Object.keys(art).length > 0) {
      var table = h.createTable("artsection");
      Object.keys(art).forEach(function(p) {
        if (typeof(art[p]) === "string") {
          h.addTwoCellsInARow(table, p, art[p]);
        } else {
          let content = "";
          Object.keys(art[p]).forEach(function(a) {
            if (p === "properties") {
              Object.keys(art[p][a]).forEach(function(b) {
                content += b + "(" + art[p][a][b] + ")<br />";
              });
            } else {
              content += art[p][a] + "<br />"
            }
          });
          h.addTwoCellsInARow(table, p, content);
        }
      });
    } else {
      p = document.createElement('p');
      p.innerText = "nothing to show";
      let s = document.getElementById("artsection");
      s.appendChild(p);
    }
  });
}

/* Setup edit artifact button */
function setEditButton() {
  document.getElementById('btneditartifact').setAttribute(
    "href", "artifact_editor.html?workspace=" + (new URL(location.href).searchParams.get('workspace')) +
    "&artifact=" + (new URL(location.href).searchParams.get('artifact')) +
    "&javafile=" + (new URL(location.href).searchParams.get('javafile'))
  );
}

/* Get artifact information and render diagram */
function getArtGraph() {
  const params = new URL(location.href).searchParams;
  const selectedWorkspace = params.get('workspace');
  const selectedArtifact = params.get('artifact');

  h.get("./workspaces/" + selectedWorkspace + "/" + selectedArtifact).then(function(serialArt) {
    const MAX_LENGTH = 35;
    let dot = [];
    let art = JSON.parse(serialArt);

    dot.push("digraph G {\n");
    dot.push("\tgraph [\n");
    dot.push("\t\trankdir = \"LR\"\n");
    dot.push("\t\tbgcolor=\"transparent\"\n");
    dot.push("\t]\n");

    /* Artifact name and type */
    var s1 = (art.artifact.length <= p.MAX_LENGTH) ? art.artifact : art.artifact.substring(0, p.MAX_LENGTH) + " ...";
    dot.push("\t\"" + art.artifact + "\" [ " + "\n\t\tlabel = \"" + s1 + ":\\n");
    s1 = (art.type.length <= p.MAX_LENGTH) ? art.type :
      art.type.substring(0, p.MAX_LENGTH) + " ...";
    dot.push(s1 + "|");

    /* observable properties */
    Object.keys(art.properties).forEach(function(y) {
      var ss = Object.keys(art.properties[y])[0] + "(" + Object.values(art.properties[y])[0].toString() + ")";
      var s2 = (ss.length <= p.MAX_LENGTH) ? ss : ss.substring(0, p.MAX_LENGTH) + " ...";
      dot.push(s2 + "|");
    });

    /* operations */
    art.operations.forEach(function(y) {
      var s2 = (y.length <= p.MAX_LENGTH) ? y : y.substring(0, p.MAX_LENGTH) + " ...";
      dot.push(s2 + "\\n");
    });
    dot.push("\"\n");
    dot.push("\t\tshape=record style=filled fillcolor=white\n");
    dot.push("\t\t];\n");

    /* Linked Artifacts */
    art.linkedArtifacts.forEach(function(y) {
      var str1 = (y.length <= p.MAX_LENGTH) ? y :
        y.substring(0, p.MAX_LENGTH) + " ...";
      dot.push("\t\t\"" + y + "\" [ label=\"" + str1 + "\"");
      dot.push("\t\tshape=record style=filled fillcolor=white\n");
      dot.push("\t]\n");
      dot.push("\t\"" + art.artifact + "\" -> \"" + y + "\" [arrowhead=\"onormal\"]");
    });

    /* observers */
    art.observers.forEach(function(y) {
      if (art.type !== "cartago.AgentBodyArtifact") {
        var s2 = (y.length <= p.MAX_LENGTH) ? y : y.substring(0, p.MAX_LENGTH) + "...";
        dot.push("\t\"" + y + "\" [ " + "\n\t\tlabel = \"" + s2 + "\"\n");
        dot.push("\t\tshape = \"ellipse\" style=filled fillcolor=white\n");
        dot.push("\t];\n");
        dot.push("\t\"" + y + "\" -> \"" + art.artifact + "\" [arrowhead=\"odot\"];\n");
      }
    });

    dot.push("}\n");

    /* Transition follows modal top down movement */
    var t = d3.transition().duration(750).ease(d3.easeLinear);
    d3.select("#artifactgraph").graphviz().transition(t).renderDot(dot.join(""));
  });
}

/* modal window */
function setArtifactModalWindow() {
  var modal = document.getElementById('modalartgraph');
  var btnModal = document.getElementById("btnartdiagram");
  var span = document.getElementsByClassName("close")[0];
  btnModal.onclick = function() {
    getArtGraph();
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
}

/**
 * WORKSPACE FUNCTIONS
 */

function getWorkspaces() {
  const params = new URL(location.href).searchParams;
  const selectedWorkspace = params.get('workspace');

  h.get("./workspaces").then(function(resp) {
    let ws = JSON.parse(resp);
    updateWorkspaceMenu("nav-drawer", ws, undefined, true);
    updateWorkspaceMenu("nav-drawer-frame", ws, undefined, false);
  });
}

function updateWorkspaceMenu(nav, ws, ar, addCloseButton) {
  if (addCloseButton) {
    const closeButton = document.createElement('label');
    closeButton.setAttribute("for", "doc-drawer-checkbox");
    closeButton.setAttribute("class", "button drawer-close");
    document.getElementById(nav).appendChild(closeButton);
    var h3 = document.createElement("h3");
    h3.innerHTML = "&#160";
    document.getElementById(nav).appendChild(h3);
  }

  const params = new URL(location.href).searchParams;
  const selectedWorkspace = params.get('workspace');
  const selectedArtifact = params.get('artifact');

  let promisses = [];
  ws.sort().forEach(function(n) {
    promisses.push(h.get("./workspaces/" + n));
  });
  Promise.all(promisses).then(function(r) {
      r.forEach(function(e) {
        let ar = JSON.parse(e);
        let validContent = 0;
        Object.keys(ar.artifacts).sort().forEach(function(a) {
          if (p.HIDDEN_ARTS.indexOf(ar.artifacts[a].type) < 0) {
            validContent++;
            /* if printing the first artifact, also print the workspace */
            if (selectedWorkspace === ar.workspace) {
              if (validContent === 1) {
                var lag = document.createElement('a');
                lag.setAttribute("href", "./workspace.html?workspace=" + ar.workspace);
                lag.innerHTML = "<h5><b>" + ar.workspace + "</b></h5>";
                document.getElementById(nav).appendChild(lag);
              }
              /* Add artifacts on menu */
              var lar = document.createElement('a');
              if (selectedArtifact === a) {
                lar.innerHTML = "<h5><b>&#160;&#160;&#160;" + a + "</b></h5>";
              } else {
                lar.innerHTML = "<h5>&#160;&#160;&#160;" + a + "</h5>";
              }
              lar.setAttribute("href", "./artifact.html?workspace=" + ar.workspace + "&artifact=" + a +
                "&javafile=" + ar.artifacts[a].type + ".java");
              document.getElementById(nav).appendChild(lar);
            } else {
              /* if would print at least one artifact, also print the workspace */
              if (validContent === 1) {
                var lag = document.createElement('a');
                lag.setAttribute("href", "./workspace.html?workspace=" + ar.workspace);
                lag.innerHTML = "<h5>" + ar.workspace + "</h5>";
                document.getElementById(nav).appendChild(lag);
              }
            }
          }
        });
      });
  }).then(function(r){
    document.getElementById(nav).appendChild(h.createDefaultHR());
    var lnew = document.createElement('a');
    lnew.setAttribute("href", "./artifact_new.html");
    lnew.innerHTML = "create template";
    document.getElementById(nav).appendChild(lnew);
  });
}

function getWorkspaceDetails() {
  const HIDDEN_ARTS = p.HIDDEN_ARTS;
  const params = new URL(location.href).searchParams;
  const selectedWorkspace = params.get('workspace');

  h.get("./workspaces/" + selectedWorkspace).then(function(w) {
    wks = JSON.parse(w);
    var table = document.getElementById("wkstable");
    Object.keys(wks).forEach(function(p) {

      if (typeof(wks[p]) === "string") {
        h.addTwoCellsInARow(table, p, wks[p]);
      } else {
        let content = "";
        Object.keys(wks[p]).forEach(function(a) {
          if (HIDDEN_ARTS.indexOf(wks.artifacts[a].type) < 0) {
            content += a + "<br />";
          }
        });
        h.addTwoCellsInARow(table, p, content);
      }
    });
  });
}

function getWksGraph() {
  const wksName = new URL(location.href).searchParams.get('workspace');

  h.get("./workspaces/" + wksName).then(function(resp) {
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
      if (p.HIDDEN_ARTS.indexOf(wks.artifacts[a].type) < 0) {
        validContent++;

        var s1;
        s1 = (wks.artifacts[a].artifact.length <= p.MAX_LENGTH) ? wks.artifacts[a].artifact :
          wks.artifacts[a].artifact.substring(0, p.MAX_LENGTH) + " ...";
        dot.push("\t\"" + wks.artifacts[a].artifact + "\" [ " + "\n\t\tlabel = \"" + s1 + ":\n");
        s1 = (wks.artifacts[a].type.length <= p.MAX_LENGTH) ? wks.artifacts[a].type :
          wks.artifacts[a].type.substring(0, p.MAX_LENGTH) + " ...";
        dot.push(s1 + "\"\n");
        dot.push("\t\tshape=record style=filled fillcolor=white\n");
        dot.push("\t\t];\n");

        /* agents that are observing this artifact */
        wks.artifacts[a].observers.forEach(function(y) {
          /* do not print agents_body observation */
          if (!wks.artifacts[a].type === "cartago.AgentBodyArtifact") {
            /* print node with defined shape */
            var s2 = (y.length <= p.MAX_LENGTH) ? y : y.substring(0, p.MAX_LENGTH) + "...";
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
  });
}

/* modal window */
function setWorkspaceModalWindow() {
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
}

/**
 * EXPORTS
 */

window.getCurrentJavaContent = getCurrentJavaContent;
window.getWorkspaces = getWorkspaces;
window.getArtifactDetails = getArtifactDetails;
window.setEditButton = setEditButton;
window.setArtifactModalWindow = setArtifactModalWindow;
window.newArt = newArt;
window.getWorkspaceDetails = getWorkspaceDetails;
window.setWorkspaceModalWindow = setWorkspaceModalWindow;

/**
 * END OF FILE
 */
