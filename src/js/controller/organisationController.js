/**
 * IMPORTS
 */
const h = require('../helpers')
const orgModel = require('../model/organisationModel')
const orgView = require('../view/organisationView')
const agentModel = require('../model/agentModel')

function getOrganisationsNonVolatileGraph() {
    let organisations = [];
    let agents = [];
    h.get('./overview').then(function (resp) {
        let overview = JSON.parse(resp);

        if (overview.organisations != null) overview.organisations.forEach(function (o) {
            let groups = [];
            let schemes = [];
            const org = new orgModel.Organisation(
                o.organisation,
                groups,
                schemes
            )
            organisations.push(org);
            
            o.groups.forEach(function (g) {
                let group = new orgModel.Group(g.id);
                groups.push(group);
            });

            o.schemes.forEach(function (s) {
                let scheme = new orgModel.Scheme(s.scheme);
                schemes.push(scheme);
            });
        });

        if (overview.agents != null) overview.agents.forEach(function (a) {
            let roles = [];
            let missions = [];
            if (a.roles != null) a.roles.forEach(function (r) {
                let role = new orgModel.Role(
                    r.role.value, 
                    r.group.value
                );
                roles.push(role);
            });
            if (a.missions != null) a.missions.forEach(function (m) {
                let responsibles = [];
                if (m.responsibles != null) m.responsibles.forEach(function (r) {
                    responsibles.push(r);
                });
                let mission = new orgModel.Mission(
                    m.mission.value, 
                    m.scheme.value, 
                    responsibles
                );
                missions.push(mission);
            });
            let agent = new agentModel.Agent(a.agent.value);
            agentModel.setRoles(roles);
            agentModel.setMissons(missions);
            agents.push(agent);
        });

        orgView.getOrganisationsAsDot(organisations, agents);
    });
}


/**
 * EXPORTS
 */

window.setAutoUpdateAgInterface = setAutoUpdateAgInterface;
window.getOrganisationsNonVolatileGraph = getOrganisationsNonVolatileGraph;