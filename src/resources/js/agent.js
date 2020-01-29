/* KILL AN AGENT */

function killAg() {
  const params = new URL(location.href).searchParams;
  const selectedAgent = params.get('agent');

  var r = confirm("Kill agent '" + selectedAgent + "'?");
  if (r == true) {
    deleteResource("./agents/" + selectedAgent).then(function(resp){
      window.location.assign("./agents.html");
    });
  } else {
    instantMessage("Don't worry, agent '" + selectedAgent + "' is still here.");
  }
}

/* SHOW MESSAGE IN THE BUFFER IF EXISTS */

const showBuffer = () => {
  var buffer = localStorage.getItem("agentBuffer");
  instantMessage(buffer);
  localStorage.removeItem("agentBuffer");
};

/* SEND COMMANDS TO AN AGENT */

function runCMD() {
  const params = new URL(location.href).searchParams;
  const selectedAgent = params.get('agent');

  data = "c=" + encodeURIComponent(document.getElementById("inputcmd").value);
  post("./agents/" + selectedAgent + "/cmd",data,"application/x-www-form-urlencoded");
  document.getElementById("inputcmd").value = "";
  document.getElementById("inputcmd").focus();
  window.location.reload();
}

/* clear agent's log */
function delLog() {
  const params = new URL(location.href).searchParams;
  const selectedAgent = params.get('agent');

  deleteResource("./agents/" + selectedAgent + "/log").then(function(resp) {
    /* Keep focus on command box */
    document.getElementById("inputcmd").focus();
    instantMessage('Log is empty.');
  });
}

/* show agent's log */
let alreadySaidLogHasMessage = false;
let currentLog = "";
function showLog() {
  const params = new URL(location.href).searchParams;
  const selectedAgent = params.get('agent');

  get("./agents/" + selectedAgent + "/log").then(function(resp) {
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
          instantMessage('Log has new message(s) to show.');
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
var modal = document.getElementById('modalinspection');
var btnModal = document.getElementById("btninspection");
var span = document.getElementsByClassName("close")[0];
btnModal.onclick = function() {
  getGraph();
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

function getGraph() {
  const params = new URL(location.href).searchParams;
  const selectedAgent = params.get('agent');

  get("./agents/" + selectedAgent).then(function(resp){
    renderGraphvizFromAgentJson(selectedAgent, JSON.parse(resp));
  });
}

function renderGraphvizFromAgentJson(agName, agentinfo) {
  const MAX_LENGTH = 35;
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
  var s1 = (agName.length <= MAX_LENGTH) ? agName : agName.substring(0, MAX_LENGTH) + " ...";
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
      if (HIDDEN_ARTS.indexOf(a.type) < 0) {
        var str1 = (a.artifact.length <= MAX_LENGTH) ? a.artifact : a.artifact.substring(0, MAX_LENGTH) + " ...";
        /* It is possible to have same artifact name in different workspaces */
        dot.push("\t\t\"" + w.workspace + "_" + a.artifact + "\" [ ",
          "\n\t\t\tlabel=\"" + str1 + " :\\n");
        str1 = (a.type.length <= MAX_LENGTH) ? a.type : a.type.substring(0, MAX_LENGTH) + " ...";
        dot.push(str1 + "\"\n");
        dot.push("\t\t\tshape=record style=filled fillcolor=white\n");
        dot.push("\t\t]\n");
      }
    });
    w.artifacts.forEach(function(a) {
      if (HIDDEN_ARTS.indexOf(a.type) < 0) {
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

  get("./agents/" + selectedAgent + "/code").then(function(resp) {
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

/* AGENT'S MIND (XML FUNCTIONS) */

function getInspectionDetails() {
  /*var selectedAgent = window.location.hash.substr(1);*/
  var parameters = location.search.substring(1).split("&");
  var temp = parameters[0].split("=");
  selectedAgent = unescape(temp[1]);

  loadAndTransform('/agents/' + selectedAgent + '/mind', '/xml/agInspection.xsl', document.getElementById('inspection'));
}

/* TODO: Implement show/hide agent's properties on fullstack pure JS version */
function makeRequest(url, loadedData, property, elementToAddResult) {
  var req = new XMLHttpRequest();
  req.open('GET', url);
  /* to allow us doing XSLT in IE */
  try {
    req.responseType = "msxml-document"
  } catch (ex) {}
  req.onload = function() {
    loadedData[property] = req.responseXML;
    if (checkLoaded(loadedData)) {
      displayResult(loadedData.xmlInput, loadedData.xsltSheet, elementToAddResult);
    };
  };
  req.send();
}

function checkLoaded(loadedData) {
  return loadedData.xmlInput != null && loadedData.xsltSheet != null;
}

function loadAndTransform(xml, xsl, elementToAddResult) {
  var loadedData = {
    xmlInput: null,
    xsltSheet: null
  };

  makeRequest(xml, loadedData, 'xmlInput', elementToAddResult);
  makeRequest(xsl, loadedData, 'xsltSheet', elementToAddResult);
}

function displayResult(xmlInput, xsltSheet, elementToAddResult) {
  if (typeof XSLTProcessor !== 'undefined') {
    var proc = new XSLTProcessor();
    proc.importStylesheet(xsltSheet);
    elementToAddResult.appendChild(proc.transformToFragment(xmlInput, document));

  } else if (typeof xmlInput.transformNode !== 'undefined') {
    elementToAddResult.innerHTML = xmlInput.transformNode(xsltSheet);
  }
}

/**
 * END OF THE FILE
 */
