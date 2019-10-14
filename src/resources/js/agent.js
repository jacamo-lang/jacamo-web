/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
KILL AN AGENT
* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

function killAg() {
  var selectedAgent = window.location.hash.substr(1);
  var r = confirm("Kill agent '" + selectedAgent + "'?");
  if (r == true) {
    const Http = new XMLHttpRequest();
    Http.onreadystatechange = function() {
      if ((Http.status == 200) || (Http.status == 204)) {
        $('#top-alert-message').text(Http.responseText);
        $('#top-alert').fadeTo(2000, 500).slideUp(500, function() {
          $('#top-alert').slideUp(500);
        });
      }
    };
    Http.open("DELETE", "./agents/" + selectedAgent);
    Http.send();
  }
}

/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
SEND COMMANDS TO AN AGENT
* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

function runCMD() {
  var selectedAgent = window.location.hash.substr(1);
  const Http = new XMLHttpRequest();
  Http.onreadystatechange = function() {
    if (Http.readyState == 4 && Http.status == 200) {
      window.location.assign("./agent.html#" + selectedAgent);
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
  var selectedAgent = window.location.hash.substr(1);
  console.log(selectedAgent);
  const Http = new XMLHttpRequest();
  Http.onreadystatechange = function() {
    if (Http.readyState == 4 && Http.status == 200) {
      location.reload();
    }
  };
  Http.open("DELETE", "./agents/" + selectedAgent + "/log");
  Http.send();
}

/* show agent's log */
function showLog() {
  var selectedAgent = window.location.hash.substr(1);
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
  var selectedAgent = window.location.hash.substr(1);
  /*document.getElementById('overviewgraph').data = "./agents/" + window.location.hash.substr(1) + "/mind/img.svg";*/
  document.getElementById('overviewgraph').setAttribute('data', "./agents/" + selectedAgent + "/mind/img.svg");
}

/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
CODE COMPLETION FUNCTIONS
* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

/* get sugestions for the selected agent */
function updateSuggestions() {
  var suggestions = [
    ['undefined']
  ];
  var selectedAgent = window.location.hash.substr(1);
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
  var selectedAgent = window.location.hash.substr(1);
  loadAndTransform('./agents/' + selectedAgent + '/mind', './xml/agInspection.xsl', document.getElementById('inspection'));
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
END OF THE FILE
* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
