/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
RETRIEVE DATA ABOUT ALL WORKSPACES
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
      /* Get Artifacts of the selected workspace */
      if (selectedWorkspace !== null) {
        const Http = new XMLHttpRequest();
        Http.open("GET", "./workspaces/" + selectedWorkspace);
        Http.send();
        Http.onreadystatechange = function() {
          if (this.readyState == 4 && this.status == 200) {
            ar = JSON.parse(Http.responseText);
            updateMenu("nav-drawer", ws, ar, true);
            updateMenu("nav-drawer-frame", ws, ar, false);
          }
        };
      } else {
        updateMenu("nav-drawer", ws, undefined, true);
        updateMenu("nav-drawer-frame", ws, undefined, false);
      }
    };
  };
};

function updateMenu(nav, ws, ar, addCloseButton) {
  if (addCloseButton) {
    const closeButton = document.createElement('label');
    closeButton.setAttribute("for","doc-drawer-checkbox");
    closeButton.setAttribute("class","button drawer-close");
    document.getElementById(nav).appendChild(closeButton);
    var h3 = document.createElement("h3");
    h3.innerHTML = "&#160";
    document.getElementById(nav).appendChild(h3);
  }

  const params = new URL(location.href).searchParams;
  const selectedWorkspace = params.get('workspace');
  const selectedArtifact = params.get('artifact');

  ws.sort();
  ws.forEach(function(n) {
    var lag = document.createElement('a');
    lag.setAttribute("href", "./workspace.html?workspace=" + n);
    if (selectedWorkspace === n) {
      lag.innerHTML = "<h5><b>" + n + "</b></h5>";
      document.getElementById(nav).appendChild(lag);
      if (ar !== undefined) {
        Object.keys(ar.artifacts).forEach(function(a) {

          /* Do not print system artifacts */
          const hidenArts = ["node", "console", "blackboard", "workspace", "manrepo"];
          const isHidden = (hidenArts.indexOf(a) >= 0);
          const isBody = a.endsWith("-body");
          const isOrgArtifact = (ar.artifacts[a].type.endsWith(".OrgBoard") || ar.artifacts[a].type.endsWith(".SchemeBoard") ||
            ar.artifacts[a].type.endsWith(".NormativeBoard") || ar.artifacts[a].type.endsWith(".GroupBoard"));

          if (!isHidden && !isBody && !isOrgArtifact) {
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
          }
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

/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
END OF FILE
* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
