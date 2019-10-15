/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
RETRIEVE LIST OF AGENTS TO UPDATE THE MENU
* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

/*Get list of agent from backend*/
function createMenu() {
  var agents = ["agent1", "agent2"];
  const Http = new XMLHttpRequest();
  Http.open("GET", "./agents");
  Http.send();
  Http.onreadystatechange = function() {
    if (this.readyState == 4 && this.status == 200) {
      agents = JSON.parse(Http.responseText);
      updateMenu("nav-drawer", agents);
      updateMenu("nav-drawer-frame", agents);
    }
  };
};

function updateMenu(nav, agents) {

  /* Remove all existing children from the menu*/
  var menu = document.getElementById(nav);
  while (menu.firstChild) {
    menu.removeChild(menu.firstChild);
  }

  /* Add each agent and then DF and Create Agent*/
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

/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
CREATE AGENT
* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

/* create agent */
function newAg() {
  const Http = new XMLHttpRequest();
  Http.open("POST", '/agents/' + document.getElementById('createAgent').value);
  Http.send();
  Http.onreadystatechange = function() {
    if (Http.readyState == 4 && Http.status == 200) {
      window.location.assign('/agent.html?agent=' + document.getElementById('createAgent').value);
    }
  };

}
