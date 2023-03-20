/**
 * IMPORTS
 */
const h = require('../helpers')
const agentModel = require('../model/agentModel')
const agentView = require('../view/agentView')
 
/* AGENT'S DETAILS */

function getInspectionDetails() {
    var selectedAgent = new URL(location.href).searchParams.get('agent');
/*
    var selectedAgent = window.location.hash.substr(1);
    var parameters = location.search.substring(1).split("&");
    var temp = parameters[0].split("=");
    selectedAgent = unescape(temp[1]);
*/
    let agent = new agentModel.Agent(selectedAgent);
    h.get("./agents/" + selectedAgent + "/bb").then(function (resp) {
        let details = JSON.parse(resp);
        let bels = [];
        console.log(details);
        details.beliefs.forEach(function (b) {
            console.log(b);
            let bel = new agentModel.Belief(b.predicate);
            bels.push(bel);
        });
        agent.setBeliefs(bels);
        
        console.log(agent);
        
        agentView.renderInspectionDetails(agent)
    });

    h.get("./agents/" + selectedAgent + '/aslfiles').then(function (resp) {
        aslfiles = JSON.parse(resp);
        agent.setAslFiles(aslfiles)
        console.log(agent);
        agentView.renderAgentEditBar(agent)
    });
}

/* update agents interface automatically */
let agentsList = undefined;
function setAutoUpdateAgInterface() {
    // Do it immediately at first time
    if (agentsList === undefined) getAgents();

    setInterval(function () {
        getAgents();
    }, 1000);
}

/*Get list of agent from backend*/
function getAgents() {
    h.get("./agents").then(function (resp) {
        if (agentsList != resp) {
            agentsList = resp;
            agentView.updateAgentsMenu("nav-drawer", JSON.parse(agentsList), true);
            agentView.updateAgentsMenu("nav-drawer-frame", JSON.parse(agentsList), false);
        }
    });
}

/**
 * EXPORTS
 */

window.getInspectionDetails = getInspectionDetails;
window.setAutoUpdateAgInterface = setAutoUpdateAgInterface;
