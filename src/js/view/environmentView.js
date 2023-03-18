/**
 * IMPORTS
 */
const h = require('../helpers')
const p = require('../parameters')

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

/* Get artifact information and render diagram */
export function renderArtGraph(art) {
    const params = new URL(location.href).searchParams;

    let dot = [];

    dot.push("digraph G {\n");
    dot.push("\tgraph [\n");
    dot.push("\t\trankdir = \"LR\"\n");
    dot.push("\t\tbgcolor=\"transparent\"\n");
    dot.push("\t]\n");

    // Artifact name and type
    var s1 = (art.name.length <= p.MAX_LENGTH) ? art.name : art.name.substring(0, p.MAX_LENGTH) + " ...";
    dot.push("\t\"" + art.artifact + "\" [ " + "\n\t\tlabel = \"" + s1 + ":\\n");
    s1 = (art.type.length <= p.MAX_LENGTH) ? art.type :
        art.type.substring(0, p.MAX_LENGTH) + " ...";
    dot.push(s1 + "|");
    dot.push("\"\n");
    dot.push("\t\tshape=record style=filled fillcolor=white\n");
    dot.push("\t\t];\n");
    dot.push("}\n");

    // Transition follows modal top down movement
    import( /* webpackChunkName: "d3" */ 'd3').then(function (d3) {
        import( /* webpackChunkName: "d3-graphviz" */ 'd3-graphviz').then(function (d3G) {
            var t = d3.transition().duration(750).ease(d3.easeLinear);
            d3G.graphviz("#artifactgraph").transition(t).renderDot(dot.join(""));
        });
    });
}

let wkGraphCache = undefined;
export function renderWksGraph(wss) {
    const params = new URL(location.href).searchParams;
    const selectedWorkspace = params.get('workspace');

    let dot = [];
    let wksartifacts = [];

    if (wss != null) {
        dot.push("digraph G {\n\tgraph [ rankdir=\"TB\" bgcolor=\"transparent\"]\n");
        Object.keys(wss).forEach(function (ws) {
            if (selectedWorkspace === wss[ws].name) {
                dot.push("\tsubgraph cluster_" + wss[ws].name + " {\n");
                dot.push("\t\tlabel=\"" + wss[ws].name + "\" labeljust=\"r\" style=dashed pencolor=gray40 fontcolor=gray40\n");
                console.log(wss[ws]);
                if (wss[ws].artifacts != null) Object.keys(wss[ws].artifacts).forEach(function (a) {
                    console.log(wss[ws].artifacts[a]);
                    //if (p.HIDDEN_ARTS.indexOf(wss[ws].artifacts[a].type) < 0) 
                    {
                        let s1 = (wss[ws].artifacts[a].name.length <= p.MAX_LENGTH) ?
                            wss[ws].artifacts[a].name :
                            wss[ws].artifacts[a].name.substring(0, p.MAX_LENGTH) + " ...";
                        dot.push("\t\t\"" + wss[ws].name + "_" + wss[ws].artifacts[a].name + "\" [label = \"" + s1 + ":\\n");
                        s1 = (wss[ws].artifacts[a].type.length <= p.MAX_LENGTH) ?
                            wss[ws].artifacts[a].type :
                            wss[ws].artifacts[a].type.substring(0, p.MAX_LENGTH) + " ...";
                        dot.push(s1 + "\"shape=record style=filled fillcolor=white];\n");
                        wksartifacts.push(wss[ws].name + "_" + wss[ws].artifacts[a].name);
                    }
                });

                dot.push("\t}\n");
            }
        });
        dot.push("}\n");
    } else {
        dot = ["digraph G {\"no information\nto show\"}\n"];
    }

    let graph = dot.join("");
    console.log(graph);
    if (graph !== wkGraphCache) {
        wkGraphCache = graph;
        /* Transition follows modal top down movement */
        import( /* webpackChunkName: "d3" */ 'd3').then(function (d3) {
            import( /* webpackChunkName: "d3-graphviz" */ 'd3-graphviz').then(function (d3G) {
                var t = d3.transition().duration(500).ease(d3.easeLinear);
                d3G.graphviz("#workspacegraph").transition(t).renderDot(wkGraphCache);
            });
        });
    }
}
