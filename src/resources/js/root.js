/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
WHOLE SYSTEM AS DOT
* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

function get(url) {
  return new Promise(function(resolve, reject) {
    var req = new XMLHttpRequest();
    req.open('GET', url);
    req.onload = function() {
      if (req.status == 200) {
        resolve(req.response);
      } else {
        reject(Error(req.statusText));
      }
    };
    req.onerror = function() {
      reject(Error("Network Error"));
    };
    req.send();
  });
}

const distinct = (v, i, s) => s.indexOf(v) === v;

function getMASAsDot() {
  const MAX_LENGTH = 35;
  let hiddenArts = ["cartago.WorkspaceArtifact", "cartago.tools.Console", "cartago.ManRepoArtifact",
    "cartago.tools.TupleSpace", "cartago.NodeArtifact", "ora4mas.nopl.GroupBoard", "ora4mas.nopl.OrgBoard",
    "ora4mas.nopl.SchemeBoard", "ora4mas.nopl.NormativeBoard", "cartago.AgentBodyArtifact"
  ];
  let dot = [];

  get('./overview').then(function(mas) {
    let overview = JSON.parse(mas);

    console.log(overview);
    dot.push("digraph G { graph [ rankdir=\"TB\" bgcolor=\"transparent\"]\n");

    /* Organisation dimension */
    dot.push("\tsubgraph cluster_org {\n");
    dot.push("\t\tlabel=\"organisation\" labeljust=\"r\" pencolor=gray fontcolor=gray\n");
    let orglinks = [];
    overview.agents.forEach(function(a) {
      a.roles.forEach(function(r) {
        dot.push("\t\t\"" + r.group + "\" [ " + "label = \"" + r.group + "\" shape=tab style=filled pencolor=black fillcolor=lightgrey];\n");
        orglinks.push("\t\"" + r.group + "\"->\"" + a.agent + "\" [arrowtail=normal dir=back label=\"" + r.role + "\"]\n");
      });
      a.missions.forEach(function(m) {
        dot.push("\t\t\"" + m.scheme + "\" [ " + "label = \"" + m.scheme + "\" shape=hexagon style=filled pencolor=black fillcolor=linen];\n");
        orglinks.push("\t\"" + m.scheme + "\"->\"" + a.agent + "\" [arrowtail=normal dir=back label=\"" + m.mission + "\"]\n");
        m.responsibles.forEach(function(r) {
          orglinks.push("\t\"" + r + "\"->\"" + m.scheme +
            "\" [arrowtail=normal arrowhead=open label=\"responsible\nfor\"]\n");
          dot.push("\t\t{rank=same " + r + " " + m.scheme + "};\n");
        });
      });
    });
    dot.push("\t};\n");
    dot.push(orglinks.join(" "));

    /* agents dimension */
    dot.push("\tsubgraph cluster_ag {\n");
    dot.push("\t\tlabel=\"agents\" labeljust=\"r\" pencolor=gray fontcolor=gray\n");
    let ags = [];
    overview.agents.forEach(function(x) {
      ags.push(x.agent);
      var s1 = (x.agent.length <= MAX_LENGTH) ? x.agent : x.agent.substring(0, MAX_LENGTH) + " ...";
      dot.push("\t\t\"" + x.agent + "\" [label = \"" + s1 + "\" shape = \"ellipse\" style=filled fillcolor=white];\n");
    });
    dot.push("\t\t{rank=same " + ags.join(" ") + "};\n");
    dot.push("\t};\n");

    /* Environment dimension */
    dot.push("\tsubgraph cluster_env {\n");
    dot.push("\t\tlabel=\"environment\" labeljust=\"r\" pencolor=gray fontcolor=gray\n");
    overview.agents.forEach(function(ag) {
      let wksartifacts = [];
      if (Object.keys(ag.workspaces).length > 0) {
        Object.keys(ag.workspaces).forEach(function(w) {
          let envlinks = [];
          let wks = ag.workspaces[w];
          let wksName = wks.workspace;
          dot.push("\t\tsubgraph cluster_" + wksName + " {\n");
          dot.push("\t\t\tlabel=\"" + wksName + "\" labeljust=\"r\" style=dashed pencolor=gray40 fontcolor=gray40\n");
          Object.keys(wks.artifacts).forEach(function(a) {
            if (hiddenArts.indexOf(wks.artifacts[a].type) < 0) {
              s1 = (wks.artifacts[a].artifact.length <= MAX_LENGTH) ? wks.artifacts[a].artifact :
                wks.artifacts[a].artifact.substring(0, MAX_LENGTH) + " ...";
              dot.push("\t\t\t\"" + wksName + "_" + wks.artifacts[a].artifact + "\" [label = \"" + s1 + ":\\n");
              s1 = (wks.artifacts[a].type.length <= MAX_LENGTH) ? wks.artifacts[a].type :
                wks.artifacts[a].type.substring(0, MAX_LENGTH) + " ...";
              dot.push(s1 + "\"shape=record style=filled fillcolor=white];\n");
            }
            wksartifacts.push(wksName + "_" + wks.artifacts[a].artifact);
            envlinks.push("\t\t\"" + ag.agent + "\"->\"" + wksName + "_" + wks.artifacts[a].artifact + "\" [arrowhead=odot]\n");
          });

          dot.push("\t\t\t{rank=same " + wksartifacts.join(" ") + "};\n");
          dot.push("\t\t};\n");
          dot.push(envlinks.join(" "));
        });
      }
    });
    dot.push("\t};\n");

    dot.push("}\n");
    console.log(dot.join(""));
    /* Transition follows modal top down movement */
    var t = d3.transition().duration(750).ease(d3.easeLinear);
    d3.select("#overviewgraph").graphviz().transition(t).renderDot(dot.join(""));
  });
}

/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
END OF FILE
* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
