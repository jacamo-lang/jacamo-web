/**
 * IMPORTS
 */

//const d3 = require('d3')
const h = require('./helpers')
const p = require('./parameters')
const toastr = require('toastr')

/** LOCK NOTIFICATIONS */
toastr.options.preventDuplicates = true;

/**
 * ORGANISATION FUNCTIONS
 */

/*Get Organisations */
function getOE() {
  h.get("./organisations").then(function(resp) {
    updateOrganisationMenu("nav-drawer", JSON.parse(resp), true);
    updateOrganisationMenu("nav-drawer-frame", JSON.parse(resp), false);
  });
}

function updateOrganisationMenu(nav, set, addCloseButton) {
  if (addCloseButton) {
    const closeButton = document.createElement('label');
    closeButton.setAttribute("for", "doc-drawer-checkbox");
    closeButton.setAttribute("class", "button drawer-close");
    document.getElementById(nav).appendChild(closeButton);
    var h3 = document.createElement("h3");
    h3.innerHTML = "&#160";
    document.getElementById(nav).appendChild(h3);
  }

  const selectedItem = new URL(location.href).searchParams.get('organisation');
  set.sort();
  set.forEach(function(n) {
    var lag = document.createElement('a');
    lag.setAttribute("href", "./organisation.html?organisation=" + n);
    if (selectedItem === n) {
      lag.innerHTML = "<h5><b>" + n + "</b></h5>";
      document.getElementById(nav).appendChild(lag);
    } else {
      lag.innerHTML = "<h5>" + n + "</h5>";
      document.getElementById(nav).appendChild(lag);
    }
  });
  document.getElementById(nav).appendChild(h.createDefaultHR());
  var lnew = document.createElement('a');
  lnew.setAttribute("href", "./oe_role_new.html");
  lnew.innerHTML = "create role";
  document.getElementById(nav).appendChild(lnew);
}

/* create role: POST in "/{oename}/group/{groupname}" */
function newRole(org, gr) {
  http = new XMLHttpRequest();
  var input = document.getElementById('orgGrRole').value;
  var lastDot = input.lastIndexOf('.');
  var firstPart = input.substring(0, lastDot);
  var role = input.substring(lastDot + 1);
  var firstDot = firstPart.indexOf('.');
  var org = firstPart.substring(0, firstDot);
  var group = firstPart.substring(firstDot + 1);

  var data = "role=" + encodeURIComponent(role);
  h.post('/organisations/' + org + '/groups/' + group + '/roles/' + role, data, "application/x-www-form-urlencoded")
  .then(
    function(resolve) {
      localStorage.setItem("organisationBuffer", `Role '${role}' on group '${group}' of organisation ${org} created!`);
      window.location.assign('/oe.html');
    },
    function(error) { toastr.error(error, { timeOut: 10000 }); }
  );
}

/* SHOW MESSAGE IN THE BUFFER IF EXISTS */

const showBuffer = () => {
  var buffer = localStorage.getItem("organisationBuffer");
  if ((typeof(buffer) == "string") && (buffer != "")) toastr.info(buffer, { timeOut: 3000 });
  localStorage.removeItem("organisationBuffer");
};

function getGroupsDetails() {
  const selectedItem = new URL(location.href).searchParams.get('organisation');
  h.get("./organisations/" + selectedItem).then(function(r) {
    item = JSON.parse(r);
    /* GROUPS */
    Object.keys(item.groups).forEach(function(g) {
      let s = document.getElementById("infodetailsection");
      let child = s.firstElementChild;
      while (child) { s.removeChild(child); child = s.firstElementChild; }
      var table = h.createTable("infodetailsection");
      h.addTwoCellsInARow(table, "group", item.groups[g].group);
      h.addTwoCellsInARow(table, "well formed", item.groups[g].isWellFormed);
      var roles = "";
      Object.keys(item.groups[g].roles).forEach(function(r) {
        roles += item.groups[g].roles[r].role + " ( " +
          item.groups[g].roles[r].cardinality + " )";
        if (item.groups[g].roles[r].superRoles.length > 0) roles += " <- " +
          item.groups[g].roles[r].superRoles.join(', ');
        roles += " <br />"
      });
      h.addTwoCellsInARow(table, "roles", roles);
    });
    if (Object.keys(item.groups).length <= 0) {
      p = document.createElement('p');
      p.innerText = "nothing to show";
      let s = document.getElementById("infodetailsection");
      let child = s.firstElementChild;
      while (child) { s.removeChild(child); child = s.firstElementChild; }
      s.appendChild(p);
    }
  });
}

function getSchemesDetails() {
  const selectedItem = new URL(location.href).searchParams.get('organisation');
  h.get("./organisations/" + selectedItem).then(function(r) {
    item = JSON.parse(r);
    /* SCHEMES */
    Object.keys(item.schemes).forEach(function(s) {
      let e = document.getElementById("infodetailsection");
      let child = e.firstElementChild;
      while (child) { e.removeChild(child); child = e.firstElementChild; }
      var table = h.createTable("infodetailsection");
      h.addTwoCellsInARow(table, "scheme", item.schemes[s].scheme);
      h.addTwoCellsInARow(table, "well formed", item.schemes[s].isWellFormed);
      var goals = "";
      Object.keys(item.schemes[s].goals).forEach(function(g) {
        goals += item.schemes[s].goals[g].goal + "<br />";
      });
      h.addTwoCellsInARow(table, "goals", goals);
      var missions = "";
      Object.keys(item.schemes[s].missions).forEach(function(m) {
        missions += item.schemes[s].missions[m].mission + " ( " +
          item.schemes[s].missions[m].missionGoals.join(', ') + " ) <br />"
      });
      h.addTwoCellsInARow(table, "missions", missions);
      var players = "";
      Object.keys(item.schemes[s].players).forEach(function(p) {
        players += item.schemes[s].players[p].agent + '<br />';
      });
      h.addTwoCellsInARow(table, "players", players);
    });
    if (Object.keys(item.schemes).length <= 0) {
      p = document.createElement('p');
      p.innerText = "nothing to show";
      let s = document.getElementById("infodetailsection");
      let child = s.firstElementChild;
      while (child) { s.removeChild(child); child = s.firstElementChild; }
      s.appendChild(p);
    }
  });
}


function getNormsDetails() {
  const selectedItem = new URL(location.href).searchParams.get('organisation');
  h.get("./organisations/" + selectedItem).then(function(r) {
    item = JSON.parse(r);
    /* NORMS */
    if (Object.keys(item.norms).length <= 0) {
      p = document.createElement('p');
      p.innerText = "nothing to show";
      let s = document.getElementById("infodetailsection");
      let child = s.firstElementChild;
      while (child) { s.removeChild(child); child = s.firstElementChild; }
      s.appendChild(p);
    } else {
      let s = document.getElementById("infodetailsection");
      let child = s.firstElementChild;
      while (child) { s.removeChild(child); child = s.firstElementChild; }
      var table = h.createTable("infodetailsection");
      Object.keys(item.norms).forEach(function(n) {
        h.addTwoCellsInARow(table, "norm", item.norms[n].norm + ": " +
          item.norms[n].role + " -> " + item.norms[n].type + " -> " + item.norms[n].mission);
      });
    }
  });
}

/* modal window */
function setOrganisationModalWindow() {
  var modal = document.getElementById('modalorggraph');
  var span = document.getElementsByClassName("close")[0];
  document.getElementById("btngroupdiagram").onclick = function() {
    getGroupsDetails();
    modal.style.display = "block";
  };
  document.getElementById("btnschemediagram").onclick = function() {
    getSchemesDetails();
    modal.style.display = "block";
  };
  document.getElementById("btnnormdiagram").onclick = function() {
    getNormsDetails();
    modal.style.display = "block";
  };
  span.onclick = function() {
    modal.style.display = "none";
  };
  window.onclick = function(event) {
    if (event.target == modal) {
      modal.style.display = "none";
    }
  };
}

function getOrgGroupGraph() {
  const params = new URL(location.href).searchParams;
  const selectedOrganisation = params.get('organisation');

  h.get("./organisations/" + selectedOrganisation).then(function(serialOrg) {
    let dot = [];
    let org = JSON.parse(serialOrg);

    dot.push("digraph G {graph [rankdir=BT bgcolor=transparent; compound=true;]\n");
    dot.push("\tsubgraph cluster_SS { \n");
    dot.push("\t\tpencolor=transparent fontcolor=transparent\n");

    let orglinks = [];
    org.groups.forEach(function(g) {
      g.roles.forEach(function(r) {
        r.superRoles.forEach(function(s) {
          if (s !== "soc") {
            dot.push("\t\t\"" + r.role + "\" -> \"" + s + "\" [arrowhead=onormal,arrowsize=1.5];\n");
          }
        });
      });
    });
    org.groups.forEach(function(g) {
      if (g.isWellFormed === true) {
        dot.push("\t\t\"" + g.group + "\" [label=\"" + g.group + "\",shape=tab, fontname=\"Courier\",style=filled,fillcolor=gold];\n");
      } else {
        dot.push("\t\t\"" + g.group + "\" [label=\"" + g.group + "\",shape=tab, fontname=\"Courier\",style=filled,fillcolor=lightgrey];\n");
      }
      g.roles.forEach(function(r) {
        if (r.role !== "soc") {
          dot.push("\t\t\"" + r.role + "\" [fontname=\"Arial\",shape=box,style=rounded];\n");
          dot.push("\t\t\"" + g.group + "\" -> \"" + r.role + "\"  [arrowtail=odiamond, arrowhead=none, dir=both, label=\"" + r.cardinality + "\",fontname=\"Times\",arrowsize=1.5];\n");
        }
      });
      g.subGroups.forEach(function(s) {
        dot.push("\t\t\"" + g.group + "\" -> \"" + s + "\"  [arrowtail=odiamond, arrowhead=none, dir=both, label=\"" + r.cardinality + "\",fontname=\"Times\",arrowsize=1.5];\n");
      });
      g.links.forEach(function(l) {
        let type = "normal";
        if (l.type === "communication") type = "dot";
        else if (l.type === "acquaintance") type = "vee";
        let dir = "";
        if (l.isBiDir) dir += ",arrowtail=" + type;
        let shape = "";
        if (l.scope === "IntraGroup") shape = ",style=dotted";
        dot.push("\t\t\"" + l.source + "\" -> \"" + l.target + "\" [arrowhead=" + type + dir + shape + "];\n");
      });
      g.compatibilities.forEach(function(c) {
        let dir = "arrowhead=diamond";
        if (c.isBiDir) dir += ",arrowtail=diamond";
        let shape = "";
        if (c.scope === "IntraGroup") shape = ",style=dotted";
        dot.push("\t\t\"" + c.source + "\" -> \"" + c.target + "\"  [" + dir + shape + "];\n");
      });
      g.players.forEach(function(p) {
        dot.push("\t\t\"" + p.agent + "\" [shape=ellipse];\n");
        dot.push("\t\t\"" + p.agent + "\" -> \"" + p.role + "\" [arrowsize=0.5];\n");
      });
      g.responsibleFor.forEach(function(r) {
        let fillcolor = "lightgrey";
        if (r.isWellFormed) fillcolor = "gold";
        dot.push("\t\t\"" + r.scheme + "\" [shape=hexagon, style=filled, fontname=\"Courier\", fillcolor=" + fillcolor + "];\n");
        dot.push("\t\t\"" + g.group + "\" -> \"" + r.scheme + "\" [arrowsize=0.5];\n");
      });
    });
    dot.push("}\n");
    dot.push("\t}\n");
    dot.push(orglinks.join(" "));

    dot.push("}\n");

    /* Transition follows modal top down movement */
    import( /* webpackChunkName: "d3" */ 'd3').then(function(d3) {
      import( /* webpackChunkName: "d3-graphviz" */ 'd3-graphviz').then(function(d3G) {
        var t = d3.transition().duration(750).ease(d3.easeLinear);
        d3G.graphviz("#orggroupdiagram").transition(t).renderDot(dot.join(""));
      });
    });

  });
}

function getOrgSchemeGraph() {
  const params = new URL(location.href).searchParams;
  const selectedOrganisation = params.get('organisation');

  h.get("./organisations/" + selectedOrganisation).then(function(serialOrg) {
    let dot = [];
    let org = JSON.parse(serialOrg);

    dot.push("digraph G {graph [rankdir=BT bgcolor=transparent; compound=true;]\n");
    dot.push("\tsubgraph cluster_SS { \n");
    dot.push("\t\tpencolor=transparent fontcolor=transparent\n");
    org.schemes.forEach(function(s) {
      s.goals.forEach(function(g) {
        var color = "black";
        if (g.isSatisfied) {
          color = "blue";
        } else if (s.isWellFormed && g.enabled) {
          color = "green";
        }
        var shape = "plaintext";
        var peri = "0";
        if (g.operation !== "") {
          if (g.operation === "choice") {
            shape = "underline";
            peri = "1";
          } else if (g.operation === "parallel") {
            shape = "underline";
            peri = "2";
          }
        }
        dot.push("\t\t" + g.goal + " [label=\"" + g.goal + "\", shape=" + shape + ",peripheries=" + peri + ",fontname=\"fantasy\",fontcolor=" + color + "];\n");
        if (g.operation !== "") {
          var previous = null;
          var ppos = 0;
          if (g.operation === "sequence")
            ppos = 1;
          s.goals.forEach(function(sg) {
            if (sg.parent === g.goal) {
              dot.push("\t\t" + sg.goal + " -> " + g.goal + " [samehead=true arrowhead=none];\n");
              if (ppos > 0) {
                ppos++;
                if (previous != null)
                  dot.push("\t\t" + previous.goal + " -> " + sg.goal + " [style=dotted, constraint=false, arrowhead=empty,arrowsize=0.5,color=grey];\n");
                previous = sg;
              }
            }
          });
        }
      });
      s.missions.forEach(function(m) {
        dot.push("\t\t\"" + s.scheme + "." + m.mission + "\" [ label = \"" + m.mission + "\" fontname=\"Arial\", shape=plaintext, style=rounded];\n");
        m.missionGoals.forEach(function(mg) {
          dot.push("\t\t\"" + s.scheme + "." + m.mission + "\" -> \"" + mg + "\" [fontname=times,label=\""+m.cardinality+"\",arrowsize=0.5];\n");
        });
      });
      s.players.forEach(function(p) {
        dot.push("\t\t\"" + p.agent + "\" [ label = \"" + p.agent + "\" fontname=\"Arial\", shape=plaintext, style=rounded];\n");
        dot.push("\t\t\"" + p.agent + "\" -> \"" + s.scheme + "." + p.mission  + "\" [fontname=times,arrowsize=0.5];\n");
      });
    });
    dot.push("}\n");
    dot.push("\t}\n");
    dot.push("}\n");


    /* Transition follows modal top down movement */
    import( /* webpackChunkName: "d3" */ 'd3').then(function(d3) {
      import( /* webpackChunkName: "d3-graphviz" */ 'd3-graphviz').then(function(d3G) {
        var t = d3.transition().duration(750).ease(d3.easeLinear);
        d3G.graphviz("#orgschemediagram").transition(t).renderDot(dot.join(""));
      });
    });

  });
}

function getOrgNormGraph() {
  const params = new URL(location.href).searchParams;
  const selectedOrganisation = params.get('organisation');

  h.get("./organisations/" + selectedOrganisation).then(function(serialOrg) {
    let dot = [];
    let org = JSON.parse(serialOrg);

    dot.push("digraph G {graph [rankdir=BT bgcolor=transparent; compound=true;]\n");
    dot.push("\tsubgraph cluster_SS { \n");
    dot.push("\t\tpencolor=transparent fontcolor=transparent\n");
    org.norms.forEach(function(n) {
      var s = "bold";
      if (n.type === "permission")
        s = "filled";
      dot.push("\t\t\"" + n.role + "\" [fontname=\"Arial\",shape=box,style=rounded];\n");
      dot.push("\t\t\"" + n.mission + "\" [fontname=\"Arial\", shape=plaintext, style=rounded];\n");
      dot.push("\t\t\"" + n.role + "\" -> \"" + n.mission + "\" [arrowhead=inv,style=" + s + "];\n");
    });
    dot.push("}\n");
    dot.push("\t}\n");

    dot.push("}\n");

    /* Transition follows modal top down movement */
    import( /* webpackChunkName: "d3" */ 'd3').then(function(d3) {
      import( /* webpackChunkName: "d3-graphviz" */ 'd3-graphviz').then(function(d3G) {
        var t = d3.transition().duration(750).ease(d3.easeLinear);
        d3G.graphviz("#orgnormdiagram").transition(t).renderDot(dot.join(""));
      });
    });

  });
}

/* WHOLE ORGANISATION AS DOT */

function getOrganisationsNonVolatileGraph() {
  h.get('./overview').then(function(mas) {
    getOrganisationsAsDot(JSON.parse(mas));
  });
}

let orgDataCache = undefined;
let orgGraphCache = undefined;

function getOrganisationsAsDot(nonVolatileMAS) {
  if (nonVolatileMAS != undefined)
    orgDataCache = nonVolatileMAS;

  /* graph header */
  let header = [];
  header.push("digraph G { graph [ rankdir=\"TB\" bgcolor=\"transparent\" ranksep=0.25 ]\n");

  let orglinks = [];
  orgDataCache.organisations.forEach(function(o) {
    header.push("\tsubgraph cluster_" + o.organisation + " {\n");
    header.push("\t\tlabel=\"" + o.organisation + "\" labeljust=\"r\" pencolor=gray fontcolor=gray\n");
    o.groups.forEach(function(g) {
      header.push("\t\t\"" + g.group + "\" [ " + "label = \"" + g.group + "\" shape=tab style=filled pencolor=black fillcolor=lightgrey];\n");
    });
    o.schemes.forEach(function(s) {
      header.push("\t\t\"" + s.scheme + "\" [ " + "label = \"" + s.scheme + "\" shape=hexagon style=filled pencolor=black fillcolor=linen];\n");
    });
    header.push("\t}\n");
  });

  orgDataCache.agents.forEach(function(a) {
    a.roles.forEach(function(r) {
      orglinks.push("\t\"" + r.group + "\"->\"" + a.agent + "\" [arrowtail=normal dir=back label=\"" + r.role + "\"]\n");
    });
    a.missions.forEach(function(m) {
      orglinks.push("\t\"" + m.scheme + "\"->\"" + a.agent + "\" [arrowtail=normal dir=back label=\"" + m.mission + "\"]\n");
      m.responsibles.forEach(function(r) {
        let resp = "\t\"" + r + "\"->\"" + m.scheme +
          "\" [arrowtail=normal arrowhead=open label=\"responsible\"]\n";
        if (!orglinks.includes(resp)) {
          /*avoid duplicates*/
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
  if (graph !== orgGraphCache) {
    orgGraphCache = graph;
    /* Transition follows modal top down movement */
    import( /* webpackChunkName: "d3" */ 'd3').then(function(d3) {
      import( /* webpackChunkName: "d3-graphviz" */ 'd3-graphviz').then(function(d3G) {
        var t = d3.transition().duration(500).ease(d3.easeLinear);
        d3G.graphviz("#organisationsgraph").transition(t).renderDot(orgGraphCache);
      });
    });
  }
}

/**
 * EXPORTS
 */

window.getOE = getOE;
window.getOrganisationsNonVolatileGraph = getOrganisationsNonVolatileGraph;
window.newRole = newRole;
window.getOrgGroupGraph = getOrgGroupGraph;
window.getOrgSchemeGraph = getOrgSchemeGraph;
window.getOrgNormGraph = getOrgNormGraph;
window.setOrganisationModalWindow = setOrganisationModalWindow;
window.showBuffer = showBuffer;

/**
 * END OF FILE
 */
