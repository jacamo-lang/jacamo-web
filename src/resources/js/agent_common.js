/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
RETRIEVE LIST OF AGENTS TO UPDATE THE MENU
* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

/*Get list of agent from backend*/
function createMenu() {
  var agents = ["agent1", "agent2"];
  const Http = new XMLHttpRequest();
  Http.open("GET", "./agents");
  Http.send();
  Http.onreadystatechange = (e) => {
    agents = Http.responseText.replace('[', '').replace(/"/g, '').replace(']', '').split(',');
    /* agents = JSON.parse(Http.responseText);
    It is producing error since the response is something
    like ["agent1", "agent2"] which is an array, not an JSON
    console.log(agents);
    */
    updateMenu("nav-drawer", agents);
    updateMenu("nav-drawer-frame", agents);
  };
};

function updateMenu(nav, agents) {

  /* Remove all existing children from the menu*/
  var menu = document.getElementById(nav);
  while (menu.firstChild) {
    menu.removeChild(menu.firstChild);
  }

  /* Add each agent and then DF and Create Agent*/
  var selectedAgent = window.location.hash.substr(1);
  agents.forEach(function(n) {
    var lag = document.createElement('a');
    lag.setAttribute("href", "./agent_mind.html#" + n);
    lag.setAttribute('onclick', '{window.location.assign("./agent_mind.html#' + n + '");window.location.reload();}');
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
  ldf.setAttribute("href", "./df.html");
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
  http = new XMLHttpRequest();
  http.open("POST", '/agents/' + document.getElementById('createAgent').value, false);
  http.send();
  window.location.assign('/agent_mind.html#' + document.getElementById('createAgent').value);
}
