/**
 * IMPORTS
 */
const h = require('../helpers')

export function renderMenu(nav, wss, addCloseButton) {
    if (addCloseButton) {
        const closeButton = document.createElement('label');
        closeButton.setAttribute("for", "doc-drawer-checkbox");
        closeButton.setAttribute("class", "button drawer-close");
        document.getElementById(nav).appendChild(closeButton);
        var h3 = document.createElement("h3");
        h3.innerHTML = "&#160";
        document.getElementById(nav).appendChild(h3);
    }

    const params = new URL(location.href).searchParams;
    const selectedWorkspace = params.get('workspace');
    const selectedArtifact = params.get('artifact');

    // Render the list of workspaces
    if (wss != null) wss.forEach((ws, iws) => {

        if (selectedWorkspace === ws.name) {
            var lag = document.createElement('a');
            lag.setAttribute("href", "./workspace.html?workspace=" + ws.name);
            lag.innerHTML = "<h5><b>" + ws.name + "</b></h5>";
            document.getElementById(nav).appendChild(lag);
            if (ws.artifacts != null) ws.artifacts.forEach((ar, iar) => {
                // Add artifacts on menu 
                var lar = document.createElement('a');
                if (selectedArtifact === ar.name) {
                    lar.innerHTML = "<h5><b>&#160;&#160;&#160;" + ar.name + "</b></h5>";
                } else {
                    lar.innerHTML = "<h5>&#160;&#160;&#160;" + ar.name + "</h5>";
                }
                lar.setAttribute("href", "./artifact.html?workspace=" + ws.name + "&artifact=" + ar.name + "&javafile=" + ar.type + ".java");
                document.getElementById(nav).appendChild(lar);

            });
        } else {
            // if would print at least one artifact, also print the workspace 
            var lag = document.createElement('a');
            lag.setAttribute("href", "./workspace.html?workspace=" + ws.name);
            lag.innerHTML = "<h5>" + ws.name + "</h5>";
            document.getElementById(nav).appendChild(lag);
        }
    });
    document.getElementById(nav).appendChild(h.createDefaultHR());
    var lnew = document.createElement('a');
    lnew.setAttribute("href", "./artifact_new.html");
    lnew.innerHTML = "create template";
    document.getElementById(nav).appendChild(lnew);
}
