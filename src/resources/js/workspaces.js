/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
RETRIEVE DATA ABOUT ONE WORKSPACE
* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

function getWorkspaces() {
  var ws = [];
  var ar = [];
  const params = new URL(location.href).searchParams;
  const selectedWorkspace = params.get('workspace');

  /*Get Workspaces */
  const Http = new XMLHttpRequest();
  Http.open("GET", "./workspaces");
  Http.send();
  Http.onreadystatechange = function() {
    if (this.readyState == 4 && this.status == 200) {
      ws = JSON.parse(Http.responseText);
      /* Get Artifacts of the selected workspaces*/
      if (selectedWorkspace !== null) {
        const Http = new XMLHttpRequest();
        Http.open("GET", "./workspaces/" + selectedWorkspace);
        Http.send();
        Http.onreadystatechange = function() {
          if (this.readyState == 4 && this.status == 200) {
            ar = JSON.parse(Http.responseText);
            updateMenu("nav-drawer", ws, ar);
            updateMenu("nav-drawer-frame", ws, ar);
          }
        };
      } else {
        updateMenu("nav-drawer", ws, undefined);
        updateMenu("nav-drawer-frame", ws, undefined);
      }
    };
  };
};

function updateMenu(nav, ws, ar) {
  /* Remove all existing children from the menu*/
  var menu = document.getElementById(nav);
  while (menu.firstChild) {
    menu.removeChild(menu.firstChild);
  }

  /* Add each agent and then DF and Create Agent*/
  const params = new URL(location.href).searchParams;
  const selectedWorkspace = params.get('workspace');
  const selectedArtifact = params.get('artifact');

  ws.forEach(function(n) {
    var lag = document.createElement('a');
    lag.setAttribute("href", "./workspace.html?workspace=" + n);
    if (selectedWorkspace === n) {
      lag.innerHTML = "<h5><b>" + n + "</b></h5>";
      document.getElementById(nav).appendChild(lag);
      if (ar !== undefined) {
        ar.artifacts.forEach(function(a) {
          var lar = document.createElement('a');
          if (selectedArtifact === a.artifact) {
            lar.innerHTML = "<h5><b>&#160;&#160;&#160;" + a.artifact + "</b></h5>";
          } else {
            lar.innerHTML = "<h5>&#160;&#160;&#160;" + a.artifact + "</h5>";
          }
          lar.setAttribute("href", "./artifact.html?workspace=" + n + "&artifact=" + a.artifact);
          document.getElementById(nav).appendChild(lar);
        });
      }
    } else {
      lag.innerHTML = "<h5>" + n + "</h5>";
      document.getElementById(nav).appendChild(lag);
    }
  });
  var br = document.createElement("br");
  document.getElementById(nav).appendChild(br);
  document.getElementById(nav).appendChild(br);
  var lnew = document.createElement('a');
  lnew.setAttribute("href", "./artifact_new.html");
  lnew.innerHTML = "create artifact";
  document.getElementById(nav).appendChild(lnew);
}
