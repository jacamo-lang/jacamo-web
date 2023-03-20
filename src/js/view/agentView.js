/**
 * IMPORTS
 */
 const h = require('../helpers')

/* render agent's inspection details */
export function renderInspectionDetails(agent) {
    const inspection = document.getElementById('inspection');
    const beliefs = document.createElement("details");
    beliefs.innerHTML = "<summary>Beliefs</summary>";
    inspection.appendChild(beliefs);
    agent.beliefs.forEach(function (item) {
        const text = document.createElement('i');
        text.innerHTML = item.predicate + "<br>";
        beliefs.appendChild(text);
    });
}

/* render agent's edit and kill bar */
export function renderAgentEditBar(agent) {
    let footMenu = document.getElementById('agentfootmenu');
    agent.aslFiles.forEach(function (item) {
        let editAsl = document.createElement("a");
        if (item.lastIndexOf("/") > 0) {
            var basename = item.substr(item.lastIndexOf("/") + 1, item.length - 1);
            editAsl.innerHTML = basename;
            editAsl.setAttribute('href', './agent_editor.html?aslfile=' + basename + '&agent=' + agent.name);
        } else {
            editAsl.innerHTML = item;
            editAsl.setAttribute('href', './agent_editor.html?aslfile=' + item + '&agent=' + agent.name);
        }
        footMenu.appendChild(editAsl);
        footMenu.innerHTML += "&#160;&#160;&#160";
    });
}

export function updateAgentsMenu(nav, agents, addCloseButton) {
    let navElement = document.getElementById(nav);
    let child = navElement.lastElementChild;
    while (child) {
        navElement.removeChild(child);
        child = navElement.lastElementChild;
    }

    if (addCloseButton) {
        const closeButton = document.createElement('label');
        closeButton.setAttribute("for", "doc-drawer-checkbox");
        closeButton.setAttribute("class", "button drawer-close");
        navElement.appendChild(closeButton);
        var h3 = document.createElement("h3");
        h3.innerHTML = "&#160";
        navElement.appendChild(h3);
    }

    const params = new URL(location.href).searchParams;
    const selectedAgent = params.get('agent');
    for (const n in agents) {
        var lag = document.createElement('a');
        lag.setAttribute("href", "./agent.html?agent=" + n);
        lag.setAttribute('onclick', '{window.location.assign("./agent.html?agent=' + n + '");window.location.reload();}');
        if (selectedAgent === n) {
            lag.innerHTML = "<h5><b>" + n + "</b></h5>";
        } else {
            lag.innerHTML = "<h5>" + n + "</h5>";
        }
        navElement.appendChild(lag);
    }
    navElement.appendChild(h.createDefaultHR());
    var ldf = document.createElement('a');
    ldf.setAttribute("href", "./agents_df.html");
    ldf.innerHTML = "directory facilitator";
    navElement.appendChild(ldf);
    var lnew = document.createElement('a');
    lnew.setAttribute("href", "./agent_new.html");
    lnew.innerHTML = "create agent";
    navElement.appendChild(lnew);
}

/**
 * EXPORTS
 */
