/*Get list of agent from backend*/
function createMenu() {
  var agents = ["agent1", "agent2"];
  const Http = new XMLHttpRequest();
  Http.open("GET", "./agents");
  Http.send();
  Http.onreadystatechange = (e) => {
    agents = Http.responseText.replace('[', '').replace(/"/g, '').replace(']', '').split(',');
    /* agents = JSON.parse(Http.responseText); is producing error since the response is something
    like ["agent1", "agent2"] which is an array, not an JSON */
    console.log(agents);

    /* Remove all existing children */
    const menu = document.getElementById("nav-drawer-frame");
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
      document.getElementById("nav-drawer-frame").appendChild(lag);
    });
    var br = document.createElement("br");
    document.getElementById("nav-drawer-frame").appendChild(br);
    document.getElementById("nav-drawer-frame").appendChild(br);
    var ldf = document.createElement('a');
    ldf.setAttribute("href", "./services");
    ldf.innerHTML = "directory facilitator";
    document.getElementById("nav-drawer-frame").appendChild(ldf);
    var lnew = document.createElement('a');
    lnew.setAttribute("href", "./new_agent.html");
    lnew.innerHTML = "create agent";
    document.getElementById("nav-drawer-frame").appendChild(lnew);
  };
};

/* create agent */
function newAg() {
	http = new XMLHttpRequest();
    http.open("POST", '/agents/'+document.getElementById('createAgent').value, false);
    http.send();
    window.open('/agent_mind.html#'+document.getElementById('createAgent').value);
}
