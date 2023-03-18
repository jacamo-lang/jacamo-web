let orgGraphCache = undefined;
export function getOrganisationsAsDot(organisations, agents) {
    /* graph header */
    let header = [];
    header.push("digraph G { graph [ rankdir=\"TB\" bgcolor=\"transparent\" ranksep=0.25 ]\n");

    let orglinks = [];
    organisations.forEach(function (o) {
        header.push("\tsubgraph cluster_" + o.name + " {\n");
        header.push("\t\tlabel=\"" + o.name + "\" labeljust=\"r\" pencolor=gray fontcolor=gray\n");
        o.groups.forEach(function (g) {
            header.push("\t\t\"" + g.id + "\" [ " + "label = \"" + g.id + "\" shape=tab style=filled pencolor=black fillcolor=lightgrey];\n");
        });
        o.schemes.forEach(function (s) {
            header.push("\t\t\"" + s.scheme + "\" [ " + "label = \"" + s.scheme + "\" shape=hexagon style=filled pencolor=black fillcolor=linen];\n");
        });
        header.push("\t}\n");
    });

    if (agents != null) agents.forEach(function (a) {
        a.roles.forEach(function (r) {
            orglinks.push("\t\"" + r.group + "\"->\"" + a.name + "\" [arrowtail=normal dir=back label=\"" + r.role + "\"]\n");
        });
        a.missions.forEach(function (m) {
            orglinks.push("\t\"" + m.scheme + "\"->\"" + a.name + "\" [arrowtail=normal dir=back label=\"" + m.mission + "\"]\n");
            if (m.responsibles != null) m.responsibles.forEach(function (r) {
                let resp = "\t\"" + r + "\"->\"" + m.scheme +
                    "\" [arrowtail=normal arrowhead=open label=\"responsible\"]\n";
                if (!orglinks.includes(resp)) {
                    //avoid duplicates
                    orglinks.push(resp);
                }
            });
        });
    });

    header.push(orglinks.join(" "));

    /* graph footer */
    let footer = [];
    footer.push("}\n");
    let graph = header.join("").concat(footer.join(""));
    //console.log(graph);
    if (graph !== orgGraphCache) {
        orgGraphCache = graph;
        /* Transition follows modal top down movement */
        import( /* webpackChunkName: "d3" */ 'd3').then(function (d3) {
            import( /* webpackChunkName: "d3-graphviz" */ 'd3-graphviz').then(function (d3G) {
                var t = d3.transition().duration(500).ease(d3.easeLinear);
                d3G.graphviz("#organisationsgraph").transition(t).renderDot(orgGraphCache);
            });
        });
    }
}