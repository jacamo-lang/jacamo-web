/* kill agent */
function killAg(agent) {
  var r = confirm("Kill agent '"+agent+"'?");
  if (r == true) {
    h2 = new XMLHttpRequest();
    h2.onreadystatechange = function() {
      if ((h2.status == 200) || (h2.status == 204)) {
        location.assign("./agents.html");
      }
    };
    h2.open("DELETE", "./agents/" + agent, false);
    h2.send();
  }
}

/*function to run Jason commands*/
function runCMD() {
  var selectedAgent = window.location.hash.substr(1);
  h3 = new XMLHttpRequest();
  h3.onreadystatechange = function() {
    if (h3.readyState == 4 && h3.status == 200) {
      location.reload();
    }
  };
  h3.open("POST", "./agents/" + selectedAgent + "/cmd", true);
  h3.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
  data = "c=" + encodeURIComponent(document.getElementById("inputcmd").value);
  h3.send(data);
}

/*
LOG FUNCTIONS
*/

/* clear agent's log */
function delLog() {
  var selectedAgent = window.location.hash.substr(1);
  console.log(selectedAgent);
  h2 = new XMLHttpRequest();
  h2.onreadystatechange = function() {
    if (h2.readyState == 4 && h2.status == 200) {
      location.reload();
    }
  };
  h2.open("DELETE", "./agents/" + selectedAgent + "/log");
  h2.send();
}

/* show agent's log */
function showLog() {
  var selectedAgent = window.location.hash.substr(1);
  http = new XMLHttpRequest();
  http.onreadystatechange = function() {
    if (http.readyState == 4 && http.status == 200) {
      var textarea = document.getElementById('log');
      textarea.innerHTML = http.responseText;
      textarea.scrollTop = textarea.scrollHeight;
    }
  };
  http.open('GET', "./agents/" + selectedAgent + "/log", true);
  http.send();
}

/*
CODE COMPLETION FUNCTIONS
*/

/* fill suggestions for code completion */
var suggestions = [
  ['undefined']
];

/* get sugestions for the selected agent */
function updateSuggestions() {
  var selectedAgent = window.location.hash.substr(1);
  h4 = new XMLHttpRequest();
  h4.onreadystatechange = function() {
    if (h4.readyState == 4 && h4.status == 200) {
      var a = h4.responseText;
      a = a.replace(/'/g, '\"');
      window.suggestions = JSON.parse(a);
    }
  };
  h4.open('GET', "./agents/" + selectedAgent + "/code", true);
  h4.send();
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

/* update cmb box with suggestions */
function updateCmdCodeCompletion() {
  autocomplete(document.getElementById("inputcmd"), suggestions);
}

/* just to have sure the /code content was taken */
function waitToFillSuggestions() {
  if (typeof suggestions[0] !== 'undefined') {
    updateSuggestions();
  } else {
    setTimeout(waitToFillSuggestions, 100);
  }
}

/* run initialization */
updateSuggestions();
updateCmdCodeCompletion();
waitToFillSuggestions();

/* scroll log automatically
setInterval(function() {
  showLog();
}, 1000);
*/


/* paint and fill drawer menu from frame to on page */
var buttonClose = "<label for='doc-drawer-checkbox' class='button drawer-close'></label>";
var pageContent = document.getElementById("nav-drawer-frame").innerHTML;
var fullMenu = buttonClose + " " + pageContent;
sessionStorage.setItem("menucontent", fullMenu);

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
  };
};

/*
XML Agent's mind
*/
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
