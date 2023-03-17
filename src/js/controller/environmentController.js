/**
 * IMPORTS
 */
const h = require('../helpers')
const p = require('../parameters')
const model = require('../model/environmentModel')
const view = require('../view/environmentView')

function getWorkspacesMenu() {
    let workspaces = [];
    h.get('./overview').then(function (resp) {
        let overview = JSON.parse(resp);
        overview.agents.forEach(function (ag) {
                // Create a set of workspaces
                if (ag.workspaces != null) Object.keys(ag.workspaces).forEach(function (w) {
                const workspace = new model.Workspace(
                    ag.workspaces[w].workspace.value.replace(/\//g, "_"), //Replace forward slash by underscore
                    "",
                    []
                );

                // Avoid adding a workspace twice
                if (workspaces.filter(function (w) {
                        return w.name === workspace.name;
                    }).length === 0) {

                    workspaces.push(workspace);

                    // Create a set of artifacts for the current workspace
                    let artifacts = [];
                    if (ag.workspaces[w].artifacts != null) Object.keys(ag.workspaces[w].artifacts).forEach(function (a) {
                        let ar = ag.workspaces[w].artifacts[a];
                        const artifact = new model.Artifact(
                            ar.artifact.value,
                            ar.type.value
                        );
                        artifacts.push(artifact);

                        artifacts.sort(h.orderByNameAsc);
                
                    });
                    workspace.setArtifacts(artifacts);
                }
            });
        });

        workspaces.sort(h.orderByNameAsc);

        // Render the menu for widscreen (frame) and for compact screens (drawer)
        view.renderMenu("nav-drawer", workspaces, true);
        view.renderMenu("nav-drawer-frame", workspaces, false);
    });
}

/**
 * EXPORTS
 */

window.getWorkspacesMenu = getWorkspacesMenu;