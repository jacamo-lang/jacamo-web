/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
KILL AN AGENT
* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

function killAg() {
  /*var selectedAgent = window.location.hash.substr(1);*/
  var parameters = location.search.substring(1).split("&");
  var temp = parameters[0].split("=");
  selectedAgent = unescape(temp[1]);

  var r = confirm("Kill agent '" + selectedAgent + "'?");
  if (r == true) {
    const Http = new XMLHttpRequest();
    Http.onreadystatechange = function() {
      if ((Http.status == 200) || (Http.status == 204)) {

      }
      $('#top-alert-message').text(Http.responseText);
      $('#top-alert').fadeTo(2000, 500).slideUp(500, function() {
        $('#top-alert').slideUp(500);
      });
      $('#btkillag').href = './agents.html';
    };
    Http.open("DELETE", "./agents/" + selectedAgent);
    Http.send();
  } else {
    $('#btkillag').href = './agent.html?agent=' + selectedAgent;
  }
}

/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
SEND COMMANDS TO AN AGENT
* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

function runCMD() {
  var parameters = location.search.substring(1).split("&");
  var temp = parameters[0].split("=");
  selectedAgent = unescape(temp[1]);

  const Http = new XMLHttpRequest();
  Http.onreadystatechange = function() {
    if (Http.readyState == 4 && Http.status == 200) {
      document.getElementById("inputcmd").value = "";
      document.getElementById("inputcmd").focus();

      $('#top-alert-message').text(Http.responseText);
      $('#top-alert').fadeTo(2000, 500).slideUp(500, function() {
        $('#top-alert').slideUp(500);
      });
    }
  };
  Http.open("POST", "./agents/" + selectedAgent + "/cmd");
  Http.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
  data = "c=" + encodeURIComponent(document.getElementById("inputcmd").value);
  Http.send(data);
}

/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
LOG FUNCTIONS
* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

/* clear agent's log */
function delLog() {
  const params = new URL(location.href).searchParams;
  const selectedAgent = params.get('agent');

  const Http = new XMLHttpRequest();
  Http.onreadystatechange = function() {
    if (Http.readyState == 4 && Http.status == 200) {
      /* Keep focus on command box */
      document.getElementById("inputcmd").focus();
    }
  };
  Http.open("DELETE", "./agents/" + selectedAgent + "/log");
  Http.send();
}

/* show agent's log */
function showLog() {
  var parameters = location.search.substring(1).split("&");
  var temp = parameters[0].split("=");
  selectedAgent = unescape(temp[1]);

  const Http = new XMLHttpRequest();
  Http.onreadystatechange = function() {
    if (Http.readyState == 4 && Http.status == 200) {
      var textarea = document.getElementById('log');
      textarea.innerHTML = Http.responseText;
      textarea.scrollTop = textarea.scrollHeight;
    }
  };
  Http.open('GET', "./agents/" + selectedAgent + "/log", true);
  Http.send();
}

/* scroll log automatically */
function setAutoUpdateLog() {
  setInterval(function() {
    showLog();
  }, 1000);
}

/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
GET AGENT'S GRAPH
* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

function getGraph() {
  const params = new URL(location.href).searchParams;
  const selectedAgent = params.get('agent');

  const Http = new XMLHttpRequest();
  Http.onreadystatechange = function() {
    if (Http.readyState == 4 && Http.status == 200) {
      renderGraphvizFromAgentJson(selectedAgent, JSON.parse(Http.responseText));
      console.log(Http.responseText);
    }
  };
  Http.open('GET', "./agents/" + selectedAgent);
  Http.send();
}

function renderGraphvizFromAgentJson(agName, agentinfo) {
  const MAX_LENGTH = 35;
  var dot = [];
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

  agentinfo.forEach(function(x) {
    dot.push(
      "\t\t\"" + x + "\" [ " + "\n\t\t\tlabel = \"" + x + "\"",
      "\n\t\t\tshape=\"box\" style=filled pencolor=black fillcolor=cornsilk\n",
      "\t\t]\n");
  });
  dot.push("\t}\n");

  /* just to avoid put agent node into the cluster */
  agentinfo.forEach(function(x) {
    dot.push("\t\"" + agName + "\"->\"" + x + "\" [arrowhead=none constraint=false style=dotted]\n");
  });

  /* agent will be placed on center */
  var s1 = (agName.length <= MAX_LENGTH) ? agName : agName.substring(0, MAX_LENGTH) + " ...";
  dot.push(
    "\t\"" + agName + "\" [ " + "\n\t\tlabel = \"" + s1 + "\"",
    "\t\tshape = \"ellipse\" style=filled fillcolor=white\n",
    "\t]\n");

  dot.push("}\n");

  console.log(dot.join(""));
  /*document.getElementById('overviewgraph').setAttribute('data', "./agents/" + selectedAgent + "/mind/img.svg");*/
  /*d3.select("#overviewgraph").graphviz().renderDot('digraph {a -> b}');*/
  d3.select("#overviewgraph").graphviz().renderDot(dot.join(""));
}

/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
CODE COMPLETION FUNCTIONS
* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

/* get sugestions for the selected agent */
function updateSuggestions() {
  var suggestions = [
    ['undefined']
  ];
  /*var selectedAgent = window.location.hash.substr(1);*/
  var parameters = location.search.substring(1).split("&");
  var temp = parameters[0].split("=");
  selectedAgent = unescape(temp[1]);

  const Http = new XMLHttpRequest();
  Http.onreadystatechange = function() {
    if (Http.readyState == 4 && Http.status == 200) {
      var a = Http.responseText;
      a = a.replace(/'/g, '\"');
      suggestions = JSON.parse(a);
      autocomplete(document.getElementById("inputcmd"), suggestions);
    }
  };
  Http.open('GET', "./agents/" + selectedAgent + "/code", true);
  Http.send();
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
    for (i = 0; i < arr.length; i++) {
      if (arr[i][0].substr(0, val.length).toUpperCase() == val.toUpperCase()) {
        b = document.createElement("DIV");
        b.innerHTML = "<strong>" + arr[i][0].substr(0, val.length) + "</strong>";
        b.innerHTML += arr[i][0].substr(val.length);
        b.innerHTML += "<input type='hidden' value='" + arr[i][0] + "'>";
        b.addEventListener("click", function(e) {
          inp.value = this.getElementsByTagName("input")[0].value;
          closeAllLists();
        });
        a.appendChild(b);
        c = document.createElement("SPAN");
        c.setAttribute("class", "autocomplete-items-comments");
        /* print hint about this command */
        if (arr[i][1]) c.innerHTML = "//" + arr[i][1];
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

/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
AGENT'S MIND (XML FUNCTIONS)
* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

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

/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
MODAL WINDOW
* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

/* modal window */
var modal = document.getElementById('modalinspection');
var btnModal = document.getElementById("btninspection");
var span = document.getElementsByClassName("close")[0];
btnModal.onclick = function() {
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

/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
END OF THE FILE
* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
