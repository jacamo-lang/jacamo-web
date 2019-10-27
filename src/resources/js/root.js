/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
GLOBAL CONSTANTS AND VARIABLES
* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

const MAX_LENGTH = 35;
const HIDDEN_ARTS = ["cartago.WorkspaceArtifact", "cartago.tools.Console", "cartago.ManRepoArtifact",
  "cartago.tools.TupleSpace", "cartago.NodeArtifact", "ora4mas.nopl.GroupBoard", "ora4mas.nopl.OrgBoard",
  "ora4mas.nopl.SchemeBoard", "ora4mas.nopl.NormativeBoard", "cartago.AgentBodyArtifact"
];

/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
USEFULL FUNCTIONS
* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

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
function post(url, data) {
  return new Promise(function(resolve, reject) {
    var req = new XMLHttpRequest();
    req.open('POST', url);
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
}

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

/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
DF FUNCTIONS
* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

/* RETRIEVE DATA FROM DIRECTORY FACILITATOR */
function getDF() {
  get('./services').then(function(dfstr) {
    df = JSON.parse(dfstr);
    if (Object.keys(df).length > 0) {
      var table = createTable("dfsection");
      Object.keys(df).forEach(function(a) {
        df[a].services.sort().forEach(function(s) {
          addTwoCellsInARow(table,df[a].agent,s);
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

/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
AGENT EDITOR FUNCTIONS
* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

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

/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
ARTIFACT EDITOR FUNCTIONS
* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

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



/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
OVERVIEW FUNCTIONS
* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

/*const distinct = (v, i, s) => s.indexOf(v) === v;*/

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

    console.log(dot.join(""));
    /* Transition follows modal top down movement */
    var t = d3.transition().duration(750).ease(d3.easeLinear);
    d3.select("#overviewgraph").graphviz().transition(t).renderDot(dot.join(""));
  });
}

/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
END OF FILE
* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
