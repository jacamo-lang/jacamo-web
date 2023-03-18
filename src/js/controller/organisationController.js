/**
 * IMPORTS
 */
 const h = require('../helpers')
 const model = require('../model/organisationModel')
 const view = require('../view/organisationView')

function getOrganisationsNonVolatileGraph() {
    let organisations = [];
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
        view.getOrganisationsAsDot(organisations);
    });
}


/**
 * EXPORTS
 */

window.getOrganisationsNonVolatileGraph = getOrganisationsNonVolatileGraph;