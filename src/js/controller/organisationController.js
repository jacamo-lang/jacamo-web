/**
 * IMPORTS
 */
 const h = require('../helpers')
 const model = require('../model/organisationModel')
 const view = require('../view/organisationView')

function getOrganisationsNonVolatileGraph() {
    let organisations = [];
    let agents = [];
    h.get('./overview').then(function (resp) {
        let overview = JSON.parse(resp);

        if (overview.organisations != null) overview.organisations.forEach(function (o) {
            let groups = [];
            let schemes = [];
            const org = new model.Organisation(
                o.organisation,
                groups,
                schemes
            )
            organisations.push(org);
            
            o.groups.forEach(function (g) {
                let group = new model.Group(g.id);
                groups.push(group);
            });

            o.schemes.forEach(function (s) {
                let scheme = new model.Scheme(s.scheme);
                schemes.push(scheme);
            });
        });

        if (overview.agents != null) overview.agents.forEach(function (a) {
            let roles = [];
            let missions = [];
            if (a.roles != null) a.roles.forEach(function (r) {
                let role = new model.Role(
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
                let mission = new model.Mission(
                    m.mission.value, 
                    m.scheme.value, 
                    responsibles
                );
                missions.push(mission);
            });
            let agent = new model.Agent(
                a.agent.value,
                roles,
                missions
            )
            agents.push(agent);
        });

        view.getOrganisationsAsDot(organisations, agents);
    });
}


/**
 * EXPORTS
 */

window.getOrganisationsNonVolatileGraph = getOrganisationsNonVolatileGraph;