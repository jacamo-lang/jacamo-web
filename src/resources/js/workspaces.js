/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
RETRIEVE DATA FROM WORKSPACES
* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

/*Get Workspaces */
function getWS() {
  var ws = [];
  const Http = new XMLHttpRequest();
  Http.open("GET", "./workspaces");
  Http.send();
  Http.onreadystatechange = function() {
    if (this.readyState == 4 && this.status == 200) {
      ws = JSON.parse(Http.responseText);
      updateMenu("nav-drawer", ws);
      updateMenu("nav-drawer-frame", ws);
    }
  };
};

function updateMenu(nav, ws) {

  /* Remove all existing children from the menu*/
  var menu = document.getElementById(nav);
  while (menu.firstChild) {
    menu.removeChild(menu.firstChild);
  }

  /* Add each agent and then DF and Create Agent*/
  var selectedWS = window.location.hash.substr(1);
  ws.forEach(function(n) {
    var lag = document.createElement('a');
    lag.setAttribute("href", "./workspace.html#" + n.name);
    lag.setAttribute('onclick', '{window.location.assign("./workspace.html#' + n + '");window.location.reload();}');
    if (selectedWS === n.toString()) {
      lag.innerHTML = "<h5><b>" + n.name + "</b></h5>";
    } else {
      lag.innerHTML = "<h5>" + n.name + "</h5>";
    }
    document.getElementById(nav).appendChild(lag);
  });
  var br = document.createElement("br");
  document.getElementById(nav).appendChild(br);
  document.getElementById(nav).appendChild(br);
  var lnew = document.createElement('a');
  lnew.setAttribute("href", "./artifact_new.html");
  lnew.innerHTML = "create artifact";
  document.getElementById(nav).appendChild(lnew);
}
