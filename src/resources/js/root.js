/**
 * GLOBAL CONSTANTS AND VARIABLES
 */

const MAX_LENGTH = 35;
const HIDDEN_ARTS = ["cartago.WorkspaceArtifact", "cartago.tools.Console", "cartago.ManRepoArtifact",
  "cartago.tools.TupleSpace", "cartago.NodeArtifact", "ora4mas.nopl.GroupBoard", "ora4mas.nopl.OrgBoard",
  "ora4mas.nopl.SchemeBoard", "ora4mas.nopl.NormativeBoard", "cartago.AgentBodyArtifact", "ora4mas.light.LightOrgBoard",
  "ora4mas.light.LightNormativeBoard", "ora4mas.light.LightGroupBoard", "ora4mas.light.LightSchemeBoard"
];

/**
 * USEFULL FUNCTIONS
 */

/* GET A GIVEN URL CONTENT */
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

/* POST ON A GIVEN URL, RETURN SERIALISED CONTENT */
function post(url, data, type) {
  return new Promise(function(resolve, reject) {
    var req = new XMLHttpRequest();
    req.open('POST', url);
    if (type != undefined) req.setRequestHeader("Content-type", type);
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
    req.send(data);
  });
};

/* SEND A DELETE COMMAND TO A GIVEN RESOURCE */
let deleteResource = (url) => {
  return new Promise(function(resolve, reject) {
    var req = new XMLHttpRequest();
    req.open('DELETE', url);
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
};

/* TABLE FUNCTIONS: CREATE TABLE IN A SECTION, ADD A ROW IN A TABLE */
let createTable = (section) => {
  t = document.createElement('table');
  t.setAttribute("class", 'striped');
  t.style.maxHeight = "100%";
  let s = document.getElementById(section);
  s.appendChild(t);
  return t;
};

function addTwoCellsInARow(table, p, v) {
  var tr, cellProperty, cellDetail;
  tr = table.insertRow(-1);
  cellProperty = tr.insertCell(-1);
  cellDetail = tr.insertCell(-1);
  cellProperty.innerHTML = p;
  cellDetail.innerHTML = v;
}

/* INSTANT MESSAGE - HTML must have a top-alert DIV with a top-alert-message LABEL */
const instantMessage = (msg) => {
  if (msg != null) {
    $('#top-alert-message').text(msg);
    $('#top-alert').fadeTo(2000+(msg.length*10), 500).slideUp(500, function() {
      $('#top-alert').slideUp(500);
    });
  }
};


/**
 * DF FUNCTIONS
 */

/* RETRIEVE DATA FROM DIRECTORY FACILITATOR */
function getDF() {
  get('./services').then(function(dfstr) {
    df = JSON.parse(dfstr);
    if (Object.keys(df).length > 0) {
      var table = createTable("dfsection");
      Object.keys(df).forEach(function(a) {
        df[a].services.sort().forEach(function(s) {
          addTwoCellsInARow(table, df[a].agent, s);
        });
      });
    } else {
      p = document.createElement('p');
      p.innerText = "nothing to show";
      let s = document.getElementById("dfsection");
      s.appendChild(p);
    }
  });
}

/**
 * AGENT FUNCTIONS
 */

/* Fill text area with current agent's asl code */
function getCurrentAslContent() {
  var selectedAgent = new URL(location.href).searchParams.get('agent');
  var selectedASLFile = new URL(location.href).searchParams.get('aslfile');

  get("/agents/" + selectedAgent + "/aslfile/" + selectedASLFile).then(function(response) {
    const submit = document.createElement('button');
    submit.setAttribute("type", "submit");
    /*submit.setAttribute("onclick", "window.location.replace('./agent.html?agent=" + selectedAgent + "')");*/
    submit.innerHTML = "Save & Reload";
    document.getElementById("footer_menu").appendChild(submit);
    const cancel = document.createElement('button');
    cancel.setAttribute("type", "button");
    cancel.setAttribute("onclick", "localStorage.setItem('agentBuffer', 'No changes made.'); window.history.back();");
    cancel.innerHTML = "Discard changes";
    document.getElementById("footer_menu").appendChild(cancel);
    const text = document.createElement('i');
    text.style.fontSize = "12px";
    text.innerHTML = "Editing: <b>" + selectedASLFile + "</b>";
    document.getElementById("footer_menu").appendChild(text);

    const form = document.getElementById("usrform");
    form.setAttribute("action", "/agents/" + selectedAgent + "/aslfile/" + selectedASLFile);
    createAlsEditor(response);
  });
};

function createAlsEditor(content) {
  /* find the textarea */
  var textarea = document.querySelector("form textarea[name='aslfile']");

  /* create ace editor */
  var editor = ace.edit();
  editor.session.setValue(content);
  editor.setTheme("ace/theme/textmate");
  editor.session.setMode("ace/mode/erlang");
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
    textarea.value = editor.getValue();
    var selectedAgent = new URL(location.href).searchParams.get('agent');
    var selectedASLFile = new URL(location.href).searchParams.get('aslfile');
    post("/agents/" + selectedAgent + "/aslfile/" + selectedASLFile, new FormData(e.target)).then(function(response) {
      localStorage.setItem("agentBuffer", response);
      window.location.replace("./agent.html?agent=" + selectedAgent);
    });
    e.preventDefault();
  }, true);
}


/*Get list of agent from backend*/
function getAgents() {
  get("./agents").then(function(resp) {
    updateAgentsMenu("nav-drawer", JSON.parse(resp), true);
    updateAgentsMenu("nav-drawer-frame", JSON.parse(resp), false);
  });
};

function updateAgentsMenu(nav, agents, addCloseButton) {
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
  const selectedAgent = params.get('agent');
  agents.forEach(function(n) {
    var lag = document.createElement('a');
    lag.setAttribute("href", "./agent.html?agent=" + n);
    lag.setAttribute('onclick', '{window.location.assign("./agent.html?agent=' + n + '");window.location.reload();}');
    if (selectedAgent === n) {
      lag.innerHTML = "<h5><b>" + n + "</b></h5>";
    } else {
      lag.innerHTML = "<h5>" + n + "</h5>";
    }
    document.getElementById(nav).appendChild(lag);
  });
  var br = document.createElement("br");
  document.getElementById(nav).appendChild(br);
  document.getElementById(nav).appendChild(br);
  var ldf = document.createElement('a');
  ldf.setAttribute("href", "./agents_df.html");
  ldf.innerHTML = "directory facilitator";
  document.getElementById(nav).appendChild(ldf);
  var lnew = document.createElement('a');
  lnew.setAttribute("href", "./agent_new.html");
  lnew.innerHTML = "create agent";
  document.getElementById(nav).appendChild(lnew);
}

/* create agent */
function newAg() {
  post('/agents/' + document.getElementById('createAgent').value).then(function(r) {
    window.location.assign('/agent.html?agent=' + document.getElementById('createAgent').value);
  });
}

/**
 * ARTIFACT EDITOR FUNCTIONS
 */

/* Fill text area with current artifact's java code */
function getCurrentJavaContent() {
  var selectedJAVAFile = new URL(location.href).searchParams.get('javafile');

  get("/workspaces/temp/javafile/" + selectedJAVAFile).then(function(response) {
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
};

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
    post("/workspaces/temp/javafile/" + selectedJAVAFile, new FormData(e.target)).then(function(response) {
      localStorage.setItem("workspaceBuffer", response);
      window.location.replace('./workspaces.html');
    });

  }, true);
}

/* create artifact */
function newArt() {
  /*Hopefully there is not an artifact with the referred name in a wks called 'temp', if so, its source will be opened*/
  get('/workspaces/temp/javafile/' + document.getElementById('createArtifact').value).then(function(resp) {
    window.location.assign('/artifact_editor.html?javafile=' + document.getElementById('createArtifact').value);
  });
}

/**
 * OVERVIEW FUNCTIONS
 */


 /*Get list of agent from backend*/
 function getMASs() {
   get("./jcm").then(function(resp) {
     updateMASMenu("nav-drawer", JSON.parse(resp), true);
   });
 };

 function updateMASMenu(nav, jcms, addCloseButton) {

   if (addCloseButton) {
     const closeButton = document.createElement('label');
     closeButton.setAttribute("for", "doc-drawer-checkbox");
     closeButton.setAttribute("class", "button drawer-x-close");
     document.getElementById(nav).appendChild(closeButton);
     var h3 = document.createElement("h3");
     h3.innerHTML = "&#160";
     document.getElementById(nav).appendChild(h3);
   }

   jcms.forEach(function(n) {
     var lag = document.createElement('a');
     lag.setAttribute("href", "./index.html");
     lag.onclick = function() { getaMAS(n); };
     lag.innerHTML = "<h5>" + n.substr(0,n.length-4) + "</h5>";
     document.getElementById(nav).appendChild(lag);
   });

   var br = document.createElement("br");
   document.getElementById(nav).appendChild(br);
   document.getElementById(nav).appendChild(br);
   var ldag = document.createElement('a');
   ldag.onclick = function() {
     if (confirm("Kill all agents?") === true) {
       deleteResource("./agents").then(function(r){
         instantMessage("Agents killed!");
         setTimeout(window.location.reload(),1000);
       });
     } else {
       instantMessage("Operation cancelled!")
     };
   };
   ldag.innerHTML = "kill all agents";
   document.getElementById(nav).appendChild(ldag);
   var ldar = document.createElement('a');
   ldar.onclick = function() {
     if (confirm("dispose all artifacts?") === true) {
       deleteResource("./workspaces").then(function(r){
         instantMessage("Artifacts disposed!");
         setTimeout(window.location.reload(),1000);
       });
     } else {
       instantMessage("Operation cancelled!")
     };
   };
   ldar.innerHTML = "dispose all artifacts";
   document.getElementById(nav).appendChild(ldar);
   var ldor = document.createElement('a');
   ldor.onclick = function() {
     if (confirm("disband all organisations?") === true) {
       deleteResource("./oe").then(function(r){
         instantMessage("Organisations disbanded!");
         setTimeout(window.location.reload(),1000);
       });
     } else {
       instantMessage("Operation cancelled!")
     };
   };
   ldor.innerHTML = "disband all organisations";
   document.getElementById(nav).appendChild(ldor);
 }

 function getaMAS(mas) {
   get("./jcm/" + mas).then(function(resp) {
     window.location.assign("./index.html");
     window.location.reload();
   });
 };

/* WHOLE SYSTEM AS DOT */
function getMASAsDot() {
  let dot = [];

  get('./overview').then(function(mas) {
    let overview = JSON.parse(mas);

    dot.push("digraph G { graph [ rankdir=\"TB\" bgcolor=\"transparent\"]\n");

    /* Organisation dimension */
    dot.push("\tsubgraph cluster_org {\n");
    dot.push("\t\tlabel=\"organisation\" labeljust=\"r\" pencolor=gray fontcolor=gray\n");
    let orglinks = [];
    overview.agents.forEach(function(a) {
      a.roles.forEach(function(r) {
        dot.push("\t\t\"" + r.group + "\" [ " + "label = \"" + r.group + "\" shape=tab style=filled pencolor=black fillcolor=lightgrey];\n");
        orglinks.push("\t\"" + r.group + "\"->\"" + a.agent + "\" [arrowtail=normal dir=back label=\"" + r.role + "\"]\n");
      });
      a.missions.forEach(function(m) {
        dot.push("\t\t\"" + m.scheme + "\" [ " + "label = \"" + m.scheme + "\" shape=hexagon style=filled pencolor=black fillcolor=linen];\n");
        orglinks.push("\t\"" + m.scheme + "\"->\"" + a.agent + "\" [arrowtail=normal dir=back label=\"" + m.mission + "\"]\n");
        m.responsibles.forEach(function(r) {
          orglinks.push("\t\"" + r + "\"->\"" + m.scheme +
            "\" [arrowtail=normal arrowhead=open label=\"responsible\\nfor\"]\n");
          dot.push("\t\t{rank=same \"" + r + "\" \"" + m.scheme + "\"};\n");
        });
      });
    });
    dot.push("\t};\n");
    dot.push(orglinks.join(" "));

    /* agents dimension */
    dot.push("\tsubgraph cluster_ag {\n");
    dot.push("\t\tlabel=\"agents\" labeljust=\"r\" pencolor=gray fontcolor=gray\n");
    let ags = [];
    overview.agents.forEach(function(x) {
      ags.push(x.agent);
      var s1 = (x.agent.length <= MAX_LENGTH) ? x.agent : x.agent.substring(0, MAX_LENGTH) + " ...";
      dot.push("\t\t\"" + x.agent + "\" [label = \"" + s1 + "\" shape = \"ellipse\" style=filled fillcolor=white];\n");
    });
    if (ags.length > 0) dot.push("\t\t{rank=same \"" + ags.join("\" \"") + "\"};\n");
    dot.push("\t};\n");

    /* Environment dimension */
    dot.push("\tsubgraph cluster_env {\n");
    dot.push("\t\tlabel=\"environment\" labeljust=\"r\" pencolor=gray fontcolor=gray\n");
    overview.agents.forEach(function(ag) {
      let wksartifacts = [];
      if (Object.keys(ag.workspaces).length > 0) {
        Object.keys(ag.workspaces).forEach(function(w) {
          let envlinks = [];
          let wks = ag.workspaces[w];
          let wksName = wks.workspace;
          dot.push("\t\tsubgraph cluster_" + wksName + " {\n");
          dot.push("\t\t\tlabel=\"" + wksName + "\" labeljust=\"r\" style=dashed pencolor=gray40 fontcolor=gray40\n");
          Object.keys(wks.artifacts).forEach(function(a) {
            if (HIDDEN_ARTS.indexOf(wks.artifacts[a].type) < 0) {
              s1 = (wks.artifacts[a].artifact.length <= MAX_LENGTH) ? wks.artifacts[a].artifact :
                wks.artifacts[a].artifact.substring(0, MAX_LENGTH) + " ...";
              dot.push("\t\t\t\"" + wksName + "_" + wks.artifacts[a].artifact + "\" [label = \"" + s1 + ":\\n");
              s1 = (wks.artifacts[a].type.length <= MAX_LENGTH) ? wks.artifacts[a].type :
                wks.artifacts[a].type.substring(0, MAX_LENGTH) + " ...";
              dot.push(s1 + "\"shape=record style=filled fillcolor=white];\n");
              wksartifacts.push(wksName + "_" + wks.artifacts[a].artifact);
              envlinks.push("\t\t\"" + ag.agent + "\"->\"" + wksName + "_" + wks.artifacts[a].artifact + "\" [arrowhead=odot]\n");
            }
          });

          if (wksartifacts.length > 0) dot.push("\t\t\t{rank=same \"" + wksartifacts.join("\" \"") + "\"};\n");
          dot.push("\t\t};\n");
          dot.push(envlinks.join(" "));
        });
      }
    });
    dot.push("\t};\n");

    dot.push("}\n");

    /* Transition follows modal top down movement */
    var t = d3.transition().duration(750).ease(d3.easeLinear);
    d3.select("#overviewgraph").graphviz().transition(t).renderDot(dot.join(""));
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

  get("./workspaces/" + selectedWorkspace + "/" + selectedArtifact).then(function(artstr) {
    art = JSON.parse(artstr);
    if (Object.keys(art).length > 0) {
      var table = createTable("artsection");
      Object.keys(art).forEach(function(p) {
        if (typeof(art[p]) === "string") {
          addTwoCellsInARow(table, p, art[p]);
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
          addTwoCellsInARow(table, p, content);
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

  get("./workspaces/" + selectedWorkspace + "/" + selectedArtifact).then(function(serialArt) {
    const MAX_LENGTH = 35;
    let dot = [];
    let art = JSON.parse(serialArt);

    dot.push("digraph G {\n");
    dot.push("\tgraph [\n");
    dot.push("\t\trankdir = \"LR\"\n");
    dot.push("\t\tbgcolor=\"transparent\"\n");
    dot.push("\t]\n");

    /* Artifact name and type */
    var s1 = (art.artifact.length <= MAX_LENGTH) ? art.artifact : art.artifact.substring(0, MAX_LENGTH) + " ...";
    dot.push("\t\"" + art.artifact + "\" [ " + "\n\t\tlabel = \"" + s1 + ":\\n");
    s1 = (art.type.length <= MAX_LENGTH) ? art.type :
      art.type.substring(0, MAX_LENGTH) + " ...";
    dot.push(s1 + "|");

    /* observable properties */
    Object.keys(art.properties).forEach(function(y) {
      var ss = Object.keys(art.properties[y])[0] + "(" + Object.values(art.properties[y])[0].toString() + ")";
      var s2 = (ss.length <= MAX_LENGTH) ? ss : ss.substring(0, MAX_LENGTH) + " ...";
      dot.push(s2 + "|");
    });

    /* operations */
    art.operations.forEach(function(y) {
      var s2 = (y.length <= MAX_LENGTH) ? y : y.substring(0, MAX_LENGTH) + " ...";
      dot.push(s2 + "\\n");
    });
    dot.push("\"\n");
    dot.push("\t\tshape=record style=filled fillcolor=white\n");
    dot.push("\t\t];\n");

    /* Linked Artifacts */
    art.linkedArtifacts.forEach(function(y) {
      var str1 = (y.length <= MAX_LENGTH) ? y :
        y.substring(0, MAX_LENGTH) + " ...";
      dot.push("\t\t\"" + y + "\" [ label=\"" + str1 + "\"");
      dot.push("\t\tshape=record style=filled fillcolor=white\n");
      dot.push("\t]\n");
      dot.push("\t\"" + art.artifact + "\" -> \"" + y + "\" [arrowhead=\"onormal\"]");
    });

    /* observers */
    art.observers.forEach(function(y) {
      if (art.type !== "cartago.AgentBodyArtifact") {
        var s2 = (y.length <= MAX_LENGTH) ? y : y.substring(0, MAX_LENGTH) + "...";
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

  get("./workspaces").then(function(resp) {
    let ws = JSON.parse(resp);
    updateWorkspaceMenu("nav-drawer", ws, undefined, true);
    updateWorkspaceMenu("nav-drawer-frame", ws, undefined, false);
  });
};

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

  ws.sort().forEach(function(n) {
    get("./workspaces/" + n).then(function(r) {
      let ar = JSON.parse(r);
      let validContent = 0;
      Object.keys(ar.artifacts).sort().forEach(function(a) {
        if (HIDDEN_ARTS.indexOf(ar.artifacts[a].type) < 0) {
          validContent++;
          /* if printing the first artifact, also print the workspace */
          if (selectedWorkspace === n) {
            if (validContent === 1) {
              var lag = document.createElement('a');
              lag.setAttribute("href", "./workspace.html?workspace=" + n);
              lag.innerHTML = "<h5><b>" + n + "</b></h5>";
              document.getElementById(nav).appendChild(lag);
            }
            /* Add artifacts on menu */
            var lar = document.createElement('a');
            if (selectedArtifact === a) {
              lar.innerHTML = "<h5><b>&#160;&#160;&#160;" + a + "</b></h5>";
            } else {
              lar.innerHTML = "<h5>&#160;&#160;&#160;" + a + "</h5>";
            }
            lar.setAttribute("href", "./artifact.html?workspace=" + n + "&artifact=" + a +
              "&javafile=" + ar.artifacts[a].type + ".java");
            document.getElementById(nav).appendChild(lar);
          } else {
            /* if would print at least one artifact, also print the workspace */
            if (validContent === 1) {
              var lag = document.createElement('a');
              lag.setAttribute("href", "./workspace.html?workspace=" + n);
              lag.innerHTML = "<h5>" + n + "</h5>";
              document.getElementById(nav).appendChild(lag);
            }
          }
        }
      });
      /* Bypass for promisses challenge. Did I just printed the last element content? */
      setTimeout(function(f) {
        if (ws[ws.length -1] === n) {
          var br = document.createElement("br");
          document.getElementById(nav).appendChild(br);
          document.getElementById(nav).appendChild(br);
          var lnew = document.createElement('a');
          lnew.setAttribute("href", "./artifact_new.html");
          lnew.innerHTML = "create template";
          document.getElementById(nav).appendChild(lnew);
        }
      },500);
    });
  });
}

function getWorkspaceDetails() {
  const params = new URL(location.href).searchParams;
  const selectedWorkspace = params.get('workspace');

  get("./workspaces/" + selectedWorkspace).then(function(w) {
    wks = JSON.parse(w);
    var table = document.getElementById("wkstable");
    Object.keys(wks).forEach(function(p) {

      if (typeof(wks[p]) === "string") {
        addTwoCellsInARow(table, p, wks[p]);
      } else {
        let content = "";
        Object.keys(wks[p]).forEach(function(a) {
          if (HIDDEN_ARTS.indexOf(wks.artifacts[a].type) < 0) {
            content += a + "<br />";
          }
        });
        addTwoCellsInARow(table, p, content);
      }
    });
  });
};

function getWksGraph() {
  const wksName = new URL(location.href).searchParams.get('workspace');

  get("./workspaces/" + wksName).then(function(resp) {
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
      if (HIDDEN_ARTS.indexOf(wks.artifacts[a].type) < 0) {
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
 * ORGANISATION FUNCTIONS
 */

/*Get Organisations */
function getOE() {
  get("./oe").then(function(resp) {
    updateOrganisationMenu("nav-drawer", JSON.parse(resp), true);
    updateOrganisationMenu("nav-drawer-frame", JSON.parse(resp), false);
  });
};

function updateOrganisationMenu(nav, set, addCloseButton) {
  if (addCloseButton) {
    const closeButton = document.createElement('label');
    closeButton.setAttribute("for", "doc-drawer-checkbox");
    closeButton.setAttribute("class", "button drawer-close");
    document.getElementById(nav).appendChild(closeButton);
    var h3 = document.createElement("h3");
    h3.innerHTML = "&#160";
    document.getElementById(nav).appendChild(h3);
  }

  const selectedItem = new URL(location.href).searchParams.get('organisation');
  set.sort();
  set.forEach(function(n) {
    var lag = document.createElement('a');
    lag.setAttribute("href", "./organisation.html?organisation=" + n);
    if (selectedItem === n) {
      lag.innerHTML = "<h5><b>" + n + "</b></h5>";
      document.getElementById(nav).appendChild(lag);
    } else {
      lag.innerHTML = "<h5>" + n + "</h5>";
      document.getElementById(nav).appendChild(lag);
    }
  });
  var br = document.createElement("br");
  document.getElementById(nav).appendChild(br);
  document.getElementById(nav).appendChild(br);
  var lnew = document.createElement('a');
  lnew.setAttribute("href", "./oe_role_new.html");
  lnew.innerHTML = "create role";
  document.getElementById(nav).appendChild(lnew);
}

/* create role: POST in "/{oename}/group/{groupname}" */
function newRole(org, gr) {
  http = new XMLHttpRequest();
  var input = document.getElementById('orgGrRole').value;
  var lastDot = input.lastIndexOf('.');
  var firstPart = input.substring(0, lastDot);
  var role = input.substring(lastDot + 1);
  var firstDot = firstPart.indexOf('.');
  var org = firstPart.substring(0, firstDot);
  var group = firstPart.substring(firstDot + 1);

  http.open("POST", '/oe/' + org + '/group/' + group, false);
  http.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
  var data = "role=" + encodeURIComponent(role);
  http.send(data);
  window.location.assign('/oe.html');
}

function getOrganisationDetails() {
  var itemDetails = "";
  const selectedItem = new URL(location.href).searchParams.get('organisation');

  const Http = new XMLHttpRequest();
  Http.open("GET", "./oe/" + selectedItem + "/os/");
  Http.send();
  Http.onreadystatechange = function() {
    if (this.readyState == 4 && this.status == 200) {
      itemDetails = JSON.parse(Http.responseText);
      updateTable(selectedItem, itemDetails);
    }
  };
};

function getOrganisationDetails() {
  const selectedItem = new URL(location.href).searchParams.get('organisation');
  get("./oe/" + selectedItem + "/os/").then(function(r) {
    item = JSON.parse(r);
    /* GROUPS */
    Object.keys(item.groups).forEach(function(g) {
      var table = createTable("groupssection");
      addTwoCellsInARow(table, "group", item.groups[g].group +
        "&#160;&#160;&#160;<a href='/oe/" + selectedItem + "/group/" + item.groups[g].group + "/" +
        item.groups[g].group + ".npl'>[specification]</a>&#160;<a href='/oe/" + selectedItem +
        "/group/" + item.groups[g].group + "/debug'>[instance]</a>"
      );
      addTwoCellsInARow(table, "well formed", item.groups[g].isWellFormed);
      var roles = "";
      Object.keys(item.groups[g].roles).forEach(function(r) {
        roles += item.groups[g].roles[r].role + " ( " +
          item.groups[g].roles[r].cardinality + " )";
        if (item.groups[g].roles[r].superRoles.length > 0) roles += " <- " +
          item.groups[g].roles[r].superRoles.join(', ');
        roles += " <br />"
      });
      addTwoCellsInARow(table, "roles", roles);
    });
    if (Object.keys(item.groups).length <= 0) {
      p = document.createElement('p');
      p.innerText = "nothing to show";
      let s = document.getElementById("groupssection");
      s.appendChild(p);
    }
    /* SCHEMES */
    Object.keys(item.schemes).forEach(function(s) {
      var table = createTable("schemessection");
      addTwoCellsInARow(table, "scheme", item.schemes[s].scheme);
      addTwoCellsInARow(table, "well formed", item.schemes[s].isWellFormed);
      var goals = "";
      Object.keys(item.schemes[s].goals).forEach(function(g) {
        goals += item.schemes[s].goals[g].goal + "<br />";
      });
      addTwoCellsInARow(table, "goals", goals);
      var missions = "";
      Object.keys(item.schemes[s].missions).forEach(function(m) {
        missions += item.schemes[s].missions[m].mission + " ( " +
          item.schemes[s].missions[m].missionGoals.join(', ') + " ) <br />"
      });
      addTwoCellsInARow(table, "missions", missions);
      var players = "";
      Object.keys(item.schemes[s].players).forEach(function(p) {
        players += item.schemes[s].players[p].agent + '<br />';
      });
      addTwoCellsInARow(table, "players", players);
    });
    if (Object.keys(item.schemes).length <= 0) {
      p = document.createElement('p');
      p.innerText = "nothing to show";
      let s = document.getElementById("schemessection");
      s.appendChild(p);
    }
    /* NORMS */
    if (Object.keys(item.norms).length <= 0) {
      p = document.createElement('p');
      p.innerText = "nothing to show";
      let s = document.getElementById("normssection");
      s.appendChild(p);
    } else {
      var table = createTable("normssection");
      Object.keys(item.norms).forEach(function(n) {
        addTwoCellsInARow(table, "norm", item.norms[n].norm + ": " +
          item.norms[n].role + " -> " + item.norms[n].type + " -> " + item.norms[n].mission);
      });
    }
  });
}

/* modal window */
function setOrganisationModalWindow() {
  var modal = document.getElementById('modalorggraph');
  var span = document.getElementsByClassName("close")[0];
  document.getElementById("btngroupdiagram").onclick = function() {
    getOrgGroupGraph();
    modal.style.display = "block";
  };
  document.getElementById("btnschemediagram").onclick = function() {
    getOrgSchemeGraph();
    modal.style.display = "block";
  };
  document.getElementById("btnnormdiagram").onclick = function() {
    getOrgNormGraph();
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

function getOrgGroupGraph() {
  const params = new URL(location.href).searchParams;
  const selectedOrganisation = params.get('organisation');

  get("./oe/" + selectedOrganisation + "/os").then(function(serialOrg) {
    const MAX_LENGTH = 35;
    let dot = [];
    let org = JSON.parse(serialOrg);

    dot.push("digraph G {graph [rankdir=BT bgcolor=transparent; compound=true;]\n");
    dot.push("\tsubgraph cluster_SS { \n");
    dot.push("\t\tpencolor=transparent fontcolor=transparent\n");

    let orglinks = [];
    org.groups.forEach(function(g) {
      g.roles.forEach(function(r) {
        r.superRoles.forEach(function(s) {
          if (s !== "soc") {
            dot.push("\t\t\"" + r.role + "\" -> \"" + s + "\" [arrowhead=onormal,arrowsize=1.5];\n");
          }
        });
      });
    });
    org.groups.forEach(function(g) {
      if (g.isWellFormed === true) {
        dot.push("\t\t\"" + g.group + "\" [label=\"" + g.group + "\",shape=tab, fontname=\"Courier\",style=filled,fillcolor=gold];\n");
      } else {
        dot.push("\t\t\"" + g.group + "\" [label=\"" + g.group + "\",shape=tab, fontname=\"Courier\",style=filled,fillcolor=lightgrey];\n");
      }
      g.roles.forEach(function(r) {
        if (r.role !== "soc") {
          dot.push("\t\t\"" + r.role + "\" [fontname=\"Arial\",shape=box,style=rounded];\n");
          dot.push("\t\t\"" + g.group + "\" -> \"" + r.role + "\"  [arrowtail=odiamond, arrowhead=none, dir=both, label=\"" + r.cardinality + "\",fontname=\"Times\",arrowsize=1.5];\n");
        }
      });
      g.subGroups.forEach(function(s) {
        dot.push("\t\t\"" + g.group + "\" -> \"" + s + "\"  [arrowtail=odiamond, arrowhead=none, dir=both, label=\"" + r.cardinality + "\",fontname=\"Times\",arrowsize=1.5];\n");
      });
      g.links.forEach(function(l) {
        let type = "normal";
        if (l.type === "communication") type = "dot";
        else if (l.type === "acquaintance") type = "vee";
        let dir = "";
        if (l.isBiDir) dir += ",arrowtail=" + type;
        let shape = "";
        if (l.scope === "IntraGroup") shape = ",style=dotted";
        dot.push("\t\t\"" + l.source + "\" -> \"" + l.target + "\" [arrowhead=" + type + dir + shape + "];\n");
      });
      g.compatibilities.forEach(function(c) {
        let dir = "arrowhead=diamond";
        if (c.isBiDir) dir += ",arrowtail=diamond";
        let shape = "";
        if (c.scope === "IntraGroup") shape = ",style=dotted";
        dot.push("\t\t\"" + c.source + "\" -> \"" + c.target + "\"  [" + dir + shape + "];\n");
      });
      g.players.forEach(function(p) {
        dot.push("\t\t\"" + p.agent + "\" [shape=ellipse];\n");
        dot.push("\t\t\"" + p.agent + "\" -> \"" + p.role + "\" [arrowsize=0.5];\n");
      });
      g.responsibleFor.forEach(function(r) {
        let fillcolor = "lightgrey";
        if (r.isWellFormed) fillcolor = "gold";
        dot.push("\t\t\"" + r.scheme + "\" [shape=hexagon, style=filled, fontname=\"Courier\", fillcolor=" + fillcolor + "];\n");
        dot.push("\t\t\"" + g.group + "\" -> \"" + r.scheme + "\" [arrowsize=0.5];\n");
      });
    });
    dot.push("}\n");
    dot.push("\t}\n");
    dot.push(orglinks.join(" "));

    dot.push("}\n");

    /* Transition follows modal top down movement */
    var t = d3.transition().duration(750).ease(d3.easeLinear);
    d3.select("#orgdiagram").graphviz().transition(t).renderDot(dot.join(""));
  });
}

function getOrgSchemeGraph() {
  const params = new URL(location.href).searchParams;
  const selectedOrganisation = params.get('organisation');

  get("./oe/" + selectedOrganisation + "/os").then(function(serialOrg) {
    const MAX_LENGTH = 35;
    let dot = [];
    let org = JSON.parse(serialOrg);

    dot.push("digraph G {graph [rankdir=BT bgcolor=transparent; compound=true;]\n");
    dot.push("\tsubgraph cluster_SS { \n");
    dot.push("\t\tpencolor=transparent fontcolor=transparent\n");
    org.schemes.forEach(function(s) {
      s.goals.forEach(function(g) {
        var color = "black";
        if (g.isSatisfied) {
          color = "blue";
        } else if (s.isWellFormed && g.enabled) {
          color = "green";
        }
        var shape = "plaintext";
        var peri = "0";
        if (g.operation !== "") {
          if (g.operation === "choice") {
            shape = "underline";
            peri = "1";
          } else if (g.operation === "parallel") {
            shape = "underline";
            peri = "2";
          }
        }
        dot.push("\t\t" + g.goal + " [label=\"" + g.goal + "\", shape=" + shape + ",peripheries=" + peri + ",fontname=\"fantasy\",fontcolor=" + color + "];\n");
        if (g.operation !== "") {
          var previous = null;
          var ppos = 0;
          if (g.operation === "sequence")
            ppos = 1;
          s.goals.forEach(function(sg) {
            if (sg.parent === g.goal) {
              dot.push("\t\t" + sg.goal + " -> " + g.goal + " [samehead=true arrowhead=none];\n");
              if (ppos > 0) {
                ppos++;
                if (previous != null)
                  dot.push("\t\t" + previous.goal + " -> " + sg.goal + " [style=dotted, constraint=false, arrowhead=empty,arrowsize=0.5,color=grey];\n");
                previous = sg;
              }
            }
          });
        }
      });
      s.missions.forEach(function(m) {
        dot.push("\t\t\"" + s.scheme + "." + m.mission + "\" [ label = \"" + m.mission + "\" fontname=\"Arial\", shape=plaintext, style=rounded];\n");
        m.missionGoals.forEach(function(mg) {
          dot.push("\t\t\"" + s.scheme + "." + m.mission + "\" -> \"" + mg + "\" [fontname=times,label=\""+m.cardinality+"\",arrowsize=0.5];\n");
        });
      });
      s.players.forEach(function(p) {
        dot.push("\t\t\"" + p.agent + "\" [ label = \"" + p.agent + "\" fontname=\"Arial\", shape=plaintext, style=rounded];\n");
        dot.push("\t\t\"" + p.agent + "\" -> \"" + s.scheme + "." + p.mission  + "\" [fontname=times,arrowsize=0.5];\n");
      });
    });
    dot.push("}\n");
    dot.push("\t}\n");
    dot.push("}\n");

    /* Transition follows modal top down movement */
    var t = d3.transition().duration(750).ease(d3.easeLinear);
    d3.select("#orgdiagram").graphviz().transition(t).renderDot(dot.join(""));
  });
}
function getOrgNormGraph() {
  const params = new URL(location.href).searchParams;
  const selectedOrganisation = params.get('organisation');

  get("./oe/" + selectedOrganisation + "/os").then(function(serialOrg) {
    const MAX_LENGTH = 35;
    let dot = [];
    let org = JSON.parse(serialOrg);

    dot.push("digraph G {graph [rankdir=BT bgcolor=transparent; compound=true;]\n");
    dot.push("\tsubgraph cluster_SS { \n");
    dot.push("\t\tpencolor=transparent fontcolor=transparent\n");
    org.norms.forEach(function(n) {
      var s = "bold";
      if (n.type === "permission")
        s = "filled";
      dot.push("\t\t\"" + n.role + "\" [fontname=\"Arial\",shape=box,style=rounded];\n");
      dot.push("\t\t\"" + n.mission + "\" [fontname=\"Arial\", shape=plaintext, style=rounded];\n");
      dot.push("\t\t\"" + n.role + "\" -> \"" + n.mission + "\" [arrowhead=inv,style=" + s + "];\n");
    });
    dot.push("}\n");
    dot.push("\t}\n");

    dot.push("}\n");

    /* Transition follows modal top down movement */
    var t = d3.transition().duration(750).ease(d3.easeLinear);
    d3.select("#orgdiagram").graphviz().transition(t).renderDot(dot.join(""));
  });
}

/**
 * END OF FILE
 */
