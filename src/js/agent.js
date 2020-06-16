/**
 * IMPORTS
 */
const d3 = require('d3')
const h = require('./helpers')
const p = require('./parameters')
//const w = require('./websockets')
const toastr = require('toastr')

/** LOCK NOTIFICATIONS */
toastr.options.preventDuplicates = true;

/**
 * AGENT FUNCTIONS
 */

/* Fill text area with current agent's asl code */
function getCurrentAslContent() {
  var selectedAgent = new URL(location.href).searchParams.get('agent');
  var selectedASLFile = new URL(location.href).searchParams.get('aslfile');

  h.get("/agents/" + selectedAgent + "/aslfiles/" + selectedASLFile).then(function(response) {
    const submit = document.createElement('button');
    submit.setAttribute("type", "submit");
    submit.setAttribute("onclick", "window.location.href='./agent.html?agent=" + selectedAgent + "';");
    submit.innerHTML = "Save & Reload";
    document.getElementById("footer_menu").appendChild(submit);
    const cancel = document.createElement('button');
    cancel.setAttribute("type", "button");
    cancel.setAttribute("onclick", "localStorage.setItem('agentBuffer', 'No changes made.'); window.history.back();");
    cancel.innerHTML = "Discard changes";
    document.getElementById("footer_menu").appendChild(cancel);
    const text = document.createElement('i');
    text.style.fontSize = "14px";
    text.innerHTML = "Editing: <b>" + selectedASLFile + "</b>";
    document.getElementById("footer_menu").appendChild(text);
    const check = document.createElement('b');
    check.setAttribute("id", "check");
    check.style.fontSize = "14px";
    check.style.color = "Navy"; /*FireBrick DarkGoldenRod ForestGreen Navy*/
    check.innerHTML = "&#160&#160&#160&#160&#160Parsing code...";
    document.getElementById("footer_menu").appendChild(check);

    const form = document.getElementById("usrform");
    form.setAttribute("action", "./agents/" + selectedAgent + "/aslfiles/" + selectedASLFile);
    createAlsEditor(response);
  });
}

let aslEditor = undefined;
function createAlsEditor(content) {
  /* find the textarea */
  var textarea = document.querySelector("form textarea[name='aslfile']");

  /* create ace editor */
  aslEditor = ace.edit();
  aslEditor.session.setValue(content);
  aslEditor.setTheme("ace/theme/tomorrow");
  aslEditor.session.setMode("ace/mode/jason");
  aslEditor.setOptions({
    enableBasicAutocompletion: true
  });

  /* replace textarea with ace */
  textarea.parentNode.insertBefore(aslEditor.container, textarea);
  textarea.style.display = "none";
  /* find the parent form and add submit event listener */
  var form = textarea;
  while (form && form.localName != "form") form = form.parentNode;
  form.addEventListener("submit", function(e) {
    textarea.value = aslEditor.getValue();
    var selectedAgent = new URL(location.href).searchParams.get('agent');
    var selectedASLFile = new URL(location.href).searchParams.get('aslfile');
    h.post("/agents/" + selectedAgent + "/aslfiles/" + selectedASLFile, new FormData(e.target)).then(function(response) {
      localStorage.setItem("agentBuffer", response);
      window.location.replace("./agent.html?agent=" + selectedAgent);
    });
    e.preventDefault();
  }, true);
}

/* sintax check */
function syntaxCheck() {
  var selectedAgent = new URL(location.href).searchParams.get('agent');
  var selectedASLFile = new URL(location.href).searchParams.get('aslfile');

  const FD = new FormData();
  const XHR = new XMLHttpRequest();
  const boundary = "blob";
  if (!aslEditor) return;
  let data = "--" + boundary + "\r\ncontent-disposition: form-data; name=aslfile\r\n\r\n" + aslEditor.getValue() + "\r\n--" + boundary + "--";

  var check = document.getElementById("check");
  h.post("/agents/" + selectedAgent + "/aslfiles/" + selectedASLFile + "/parse", data, 'multipart/form-data; boundary=' + boundary).then(function(response) {
    check.style.color = "ForestGreen"; /*FireBrick DarkGoldenRod ForestGreen*/
    check.innerHTML = "&#160&#160&#160&#160&#160"+response;
    aslEditor.setTheme("ace/theme/textmate");
  }).catch(function(e) {
    if (e.startsWith("Info")) {
      check.style.color = "Navy"; /*FireBrick DarkGoldenRod ForestGreen*/
      check.innerHTML = "&#160&#160&#160&#160&#160"+e;
    } else if (e.startsWith("Warning")) {
      check.style.color = "DarkGoldenRod"; /*FireBrick DarkGoldenRod ForestGreen*/
      check.innerHTML = "&#160&#160&#160&#160&#160"+e;
    } else {
      check.style.color = "FireBrick"; /*FireBrick DarkGoldenRod ForestGreen*/
      check.innerHTML = "&#160&#160&#160&#160&#160"+e;
      aslEditor.setTheme("ace/theme/katzenmilch");
    }
  });
}

/* sintax checking */
function setAutoSyntaxChecking() {
  setInterval(function() {
    syntaxCheck();
  }, 2000);
}

/* update agents interface automatically */
let agentsList = undefined;
function setAutoUpdateAgInterface() {
  /*do it immediately at first time*/
  if (agentsList === undefined) getAgents();

  setInterval(function() {
    getAgents();
  }, 1000);
}

/*Get list of agent from backend*/
function getAgents() {
  h.get("./agents").then(function(resp) {
    if (agentsList != resp) {
      agentsList = resp;
      updateAgentsMenu("nav-drawer", JSON.parse(resp), true);
      updateAgentsMenu("nav-drawer-frame", JSON.parse(resp), false);
    }
  });
}

function updateAgentsMenu(nav, agents, addCloseButton) {
  navElement = document.getElementById(nav);
  var child = navElement.lastElementChild;
  while (child) {
    navElement.removeChild(child);
    child = navElement.lastElementChild;
  }

  if (addCloseButton) {
    const closeButton = document.createElement('label');
    closeButton.setAttribute("for", "doc-drawer-checkbox");
    closeButton.setAttribute("class", "button drawer-close");
    navElement.appendChild(closeButton);
    var h3 = document.createElement("h3");
    h3.innerHTML = "&#160";
    navElement.appendChild(h3);
  }

  const params = new URL(location.href).searchParams;
  const selectedAgent = params.get('agent');
  for (n in agents) {
    var lag = document.createElement('a');
    lag.setAttribute("href", "./agent.html?agent=" + n);
    lag.setAttribute('onclick', '{window.location.assign("./agent.html?agent=' + n + '");window.location.reload();}');
    if (selectedAgent === n) {
      lag.innerHTML = "<h5><b>" + n + "</b></h5>";
    } else {
      lag.innerHTML = "<h5>" + n + "</h5>";
    }
    navElement.appendChild(lag);
  }
  navElement.appendChild(h.createDefaultHR());
  var ldf = document.createElement('a');
  ldf.setAttribute("href", "./agents_df.html");
  ldf.innerHTML = "directory facilitator";
  navElement.appendChild(ldf);
  var lnew = document.createElement('a');
  lnew.setAttribute("href", "./agent_new.html");
  lnew.innerHTML = "create agent";
  navElement.appendChild(lnew);
}

/* create agent */
function newAg() {
  h.post('/agents/' + document.getElementById('createAgent').value);
  window.location.assign('./agent.html?agent=' + document.getElementById('createAgent').value);
}

/* KILL AN AGENT */

function killAg() {
  const params = new URL(location.href).searchParams;
  const selectedAgent = params.get('agent');

  var r = confirm("Kill agent '" + selectedAgent + "'?");
  if (r == true) {
    h.deleteResource("./agents/" + selectedAgent).then(function(resp){
      window.location.assign("./agents.html");
    });
  } else {
    toastr.info("Don't worry, agent '" + selectedAgent + "' is still here.", { timeOut: 3000 });
  }
}

/* SHOW MESSAGE IN THE BUFFER IF EXISTS */

const showBuffer = () => {
  var buffer = localStorage.getItem("agentBuffer");
  if ((typeof(buffer) == "string") && (buffer != "")) toastr.info(buffer, { timeOut: 3000 });
  localStorage.removeItem("agentBuffer");
};

/* SEND COMMANDS TO AN AGENT */

function runCMD() {
  const params = new URL(location.href).searchParams;
  const selectedAgent = params.get('agent');

  data = "c=" + encodeURIComponent(document.getElementById("inputcmd").value);
  h.post("./agents/" + selectedAgent + "/command",data,"application/x-www-form-urlencoded");
  document.getElementById("inputcmd").value = "";
  document.getElementById("inputcmd").focus();
  window.location.reload();
}

/* show agent's log */
let alreadySaidLogHasMessage = false;
let currentLog = "";
function showLog() {
  const params = new URL(location.href).searchParams;
  const selectedAgent = params.get('agent');

  h.get("./agents/" + selectedAgent + "/log").then(function(resp) {
    var textarea = document.getElementById('log');
    /*currentLog avoids differences between received and shown in textarea.innerHTML*/
    if (currentLog != resp) {
      /*do not update log area if the user has focused it*/
      if (textarea != document.activeElement) {
        textarea.innerHTML = resp;
        currentLog = resp;
        textarea.scrollTop = textarea.scrollHeight;
        alreadySaidLogHasMessage = false;
      } else {
        if (!alreadySaidLogHasMessage) {
          /*do not bother the user with too many messages*/
          alreadySaidLogHasMessage = true;
          toastr.warning('Log has new message(s) to show.', { timeOut: 3000 });
        }
      }
    }
  });
}

/* scroll log automatically */
function setAutoUpdateLog() {
  setInterval(function() {
    showLog();
  }, 1000);
}

/* GET AGENT'S GRAPH */

/* modal window */
function setGraphWindow() {
  let modal = document.getElementById('modalinspection');
  let btnModal = document.getElementById("btninspection");
  let spanClose = document.getElementsByClassName("close")[0];
  btnModal.onclick = function() {
    getGraph();
    modal.style.display = "block";
  };
  spanClose.onclick = function() {
    modal.style.display = "none";
  };
  window.onclick = function(event) {
    if (event.target == modal) {
      modal.style.display = "none";
    }
  };
}

function getGraph() {
  const params = new URL(location.href).searchParams;
  const selectedAgent = params.get('agent');

  h.get("./agents/" + selectedAgent).then(function(resp){
    renderGraphvizFromAgentJson(selectedAgent, JSON.parse(resp));
  });
}

function renderGraphvizFromAgentJson(agName, agentinfo) {
  var dot = [];

  /* Beginning of the graph */
  dot.push(
    "digraph G {\n",
    "\tgraph [\n",
    "\t\trankdir=\"LR\"\n",
    "\t\tbgcolor=\"transparent\"\n",
    "\t]\n");

  /* beliefs will be placed on the left */
  dot.push(
    "\tsubgraph cluster_mind {\n",
    "\t\tstyle=rounded\n");
  agentinfo.namespaces.forEach(function(x) {
    dot.push(
      "\t\t\"" + x + "\" [ " + "\n\t\t\tlabel = \"" + x + "\"",
      "\n\t\t\tshape=\"box\" style=filled pencolor=black fillcolor=cornsilk\n",
      "\t\t]\n");
  });
  dot.push("\t}\n");
  agentinfo.namespaces.forEach(function(x) {
    dot.push("\t\"" + agName + "\"->\"" + x + "\" [arrowhead=none constraint=false style=dotted]\n");
  });

  /* agent will be placed on center */
  var s1 = (agName.length <= p.MAX_LENGTH) ? agName : agName.substring(0, p.MAX_LENGTH) + " ...";
  dot.push(
    "\t\"" + agName + "\" [ " + "\n\t\tlabel = \"" + s1 + "\"\n",
    "\t\tshape = \"ellipse\" style=filled fillcolor=white\n",
    "\t]\n"
  );

  /* agent roles */
  agentinfo.roles.forEach(function(x) {
    dot.push(
      "\t\"" + x.group + "\" [ " + "\n\t\tlabel = \"" + x.group + "\"",
      "\n\t\tshape=tab style=filled pencolor=black fillcolor=lightgrey\n",
      "\t]\n",
      "\t\"" + x.group + "\"->\"" + agName + "\" [arrowtail=normal dir=back label=\"" + x.role + "\"]\n");
  });

  /* agent missions */
  agentinfo.missions.forEach(function(x) {
    dot.push(
      "\t\t\"" + x.scheme + "\" [ " + "\n\t\tlabel = \"" + x.scheme + "\"",
      "\n\t\t\tshape=hexagon style=filled pencolor=black fillcolor=linen\n",
      "\t\t]\n");
    x.responsibles.forEach(function(y) {
      dot.push("\t\"" + y + "\"->\"" + x.scheme,
        "\" [arrowtail=normal arrowhead=open label=\"responsible\"]\n",
        "\t{rank=same \"" + y + "\" \"" + x.scheme + "\"}\n");
    });
    dot.push("\t\"" + x.scheme + "\"->\"" + agName, "\" [arrowtail=normal dir=back label=\"" + x.mission + "\"]\n");
  });

  /* agent workspaces */
  agentinfo.workspaces.forEach(function(w) {
    dot.push("\tsubgraph cluster_" + w.workspace + " {\n",
      "\t\tlabel=\"" + w.workspace + "\"\n",
      "\t\tlabeljust=\"r\"\n",
      "\t\tgraph[style=dashed]\n");
    w.artifacts.forEach(function(a) {
      if (p.HIDDEN_ARTS.indexOf(a.type) < 0) {
        var str1 = (a.artifact.length <= p.MAX_LENGTH) ? a.artifact : a.artifact.substring(0, p.MAX_LENGTH) + " ...";
        /* It is possible to have same artifact name in different workspaces */
        dot.push("\t\t\"" + w.workspace + "_" + a.artifact + "\" [ ",
          "\n\t\t\tlabel=\"" + str1 + " :\\n");
        str1 = (a.type.length <= p.MAX_LENGTH) ? a.type : a.type.substring(0, p.MAX_LENGTH) + " ...";
        dot.push(str1 + "\"\n");
        dot.push("\t\t\tshape=record style=filled fillcolor=white\n");
        dot.push("\t\t]\n");
      }
    });
    w.artifacts.forEach(function(a) {
      if (p.HIDDEN_ARTS.indexOf(a.type) < 0) {
        dot.push("\t\"" + agName + "\"->\"" + w.workspace + "_" + a.artifact + "\" [arrowhead=odot]\n");
      }
    });
    dot.push("\t}\n");
  });

  dot.push("}\n");

  /* Transition follows modal top down movement */
  var t = d3.transition().duration(750).ease(d3.easeLinear);
  d3.select("#agentdiagram").graphviz().transition(t).renderDot(dot.join(""));
}

/* CODE COMPLETION FUNCTIONS */

/* get sugestions for the selected agent */
function updateSuggestions() {
  var suggestions = [
    ['undefined']
  ];
  /*var selectedAgent = window.location.hash.substr(1);*/
  var parameters = location.search.substring(1).split("&");
  var temp = parameters[0].split("=");
  selectedAgent = unescape(temp[1]);

  h.get("./agents/" + selectedAgent + "/code").then(function(resp) {
    autocomplete(document.getElementById("inputcmd"), JSON.parse(resp));
  });
}

/* automcomplete for cmd box */
function autocomplete(inp, arr) {
  var currentFocus;

  inp.addEventListener("input", function(e) {
    var a, b, c, i, val = this.value;
    closeAllLists();
    if (!val) {
      return false;
    }
    currentFocus = -1;
    a = document.createElement("DIV");
    a.setAttribute("id", this.id + "autocomplete-list");
    a.setAttribute("class", "autocomplete-items");

    this.parentNode.appendChild(a);
    for (i = 0; i < Object.keys(arr).length; i++) {
      if (Object.keys(arr)[i].substr(0, val.length).toUpperCase() == val.toUpperCase()) {
        b = document.createElement("DIV");
        b.innerHTML = "<strong>" + Object.keys(arr)[i].substr(0, val.length) + "</strong>";
        b.innerHTML += Object.keys(arr)[i].substr(val.length);
        b.innerHTML += "<input type='hidden' value='" + Object.keys(arr)[i] + "'>";
        b.addEventListener("click", function(e) {
          inp.value = this.getElementsByTagName("input")[0].value;
          closeAllLists();
        });
        a.appendChild(b);
        c = document.createElement("SPAN");
        c.setAttribute("class", "autocomplete-items-comments");
        /* print hint about this command */
        if (Object.values(arr)[i]) c.innerHTML = "//" + Object.values(arr)[i];
        b.appendChild(c);
      }
    }
  });
  inp.addEventListener("keydown", function(e) {
    var x = document.getElementById(this.id + "autocomplete-list");
    if (x) x = x.getElementsByTagName("div");
    /* left arrow 37, up arrow 38, right arrow 39, down arrow 40 */
    if (e.keyCode == 40) {
      if (currentFocus >= 0) x[currentFocus].classList.remove("autocomplete-active");
      currentFocus++;
      addActive(x);
    } else if (e.keyCode == 38) {
      if (currentFocus >= 0) x[currentFocus].classList.remove("autocomplete-active");
      currentFocus--;
      addActive(x);
    } else if (e.keyCode == 39) {
      if (currentFocus > -1) {
        if (x) x[currentFocus].click();
      }
    }
  });

  function addActive(x) {
    if (!x) return false;
    if (currentFocus >= x.length) currentFocus = 0;
    if (currentFocus < 0) currentFocus = (x.length - 1);
    x[currentFocus].classList.add("autocomplete-active");
  }

  function closeAllLists(elmnt) {
    var x = document.getElementsByClassName("autocomplete-items");
    for (var i = 0; i < x.length; i++) {
      if (elmnt != x[i] && elmnt != inp) {
        x[i].parentNode.removeChild(x[i]);
      }
    }
  }

  document.addEventListener("click", function(e) {
    closeAllLists(e.target);
  });
}

/* AGENT'S DETAILS */

function getInspectionDetails() {
  var selectedAgent = window.location.hash.substr(1);
  var parameters = location.search.substring(1).split("&");
  var temp = parameters[0].split("=");
  selectedAgent = unescape(temp[1]);

  inspection = document.getElementById('inspection');
  beliefs = document.createElement("details");
  beliefs.innerHTML = "<summary>Beliefs</summary>";
  inspection.appendChild(beliefs);
  h.get("./agents/" + selectedAgent).then(function(resp){
    details = JSON.parse(resp);
    details.beliefs.forEach(function(item) {
      const text = document.createElement('i');
      text.innerHTML = item + "<br>";
      beliefs.appendChild(text);
    });
  });

  footMenu = document.getElementById('agentfootmenu');
  h.get("./agents/" + selectedAgent + '/aslfiles').then(function(resp){
    alsfiles = JSON.parse(resp);
    alsfiles.forEach(function(item) {
      editAsl = document.createElement("a");
      if (item.lastIndexOf("/") > 0) {
        var basename = item.substr(item.lastIndexOf("/") + 1, item.length - 1);
        editAsl.innerHTML = basename;
        editAsl.setAttribute('href','./agent_editor.html?aslfile=' + basename + '&agent=' + selectedAgent);
      } else {
        editAsl.innerHTML = item;
        editAsl.setAttribute('href','./agent_editor.html?aslfile=' + item + '&agent=' + selectedAgent);
      }
      footMenu.appendChild(editAsl);
      footMenu.innerHTML += "&#160;&#160;&#160";
    });
  });
}


/**
 * EXPORTS
 */

window.setAutoUpdateAgInterface = setAutoUpdateAgInterface;
window.getCurrentAslContent = getCurrentAslContent;
window.setAutoSyntaxChecking = setAutoSyntaxChecking;
window.getInspectionDetails = getInspectionDetails;
window.updateSuggestions = updateSuggestions;
window.setAutoUpdateLog = setAutoUpdateLog;
window.showBuffer = showBuffer;
window.killAg = killAg;
window.runCMD = runCMD;
window.newAg = newAg;
window.setGraphWindow = setGraphWindow;

/**
 * PREPARING FOR TESTS
 */

function sum(a, b){
   return a + b
}

function division(a, b){
   return a / b
}

module.exports = {
  sum,
  division
}

/**
 * END OF FILE
 */
