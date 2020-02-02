/**
 * IMPORTS
 */

const h = require('./helpers')
const p = require('./parameters')
const g = require('./git')

/**
 * OVERVIEW FUNCTIONS
 */

 /*Get list of MAS from backend*/
 function updateMASMenus() {
   updateMASMenu("nav-drawer", true);
   updateMASMenu("nav-drawer-frame", false);
 }

 function updateMASMenu(nav, addCloseButton) {
   if (addCloseButton) {
     const closeButton = document.createElement('label');
     closeButton.setAttribute("for", "doc-drawer-checkbox");
     closeButton.setAttribute("class", "button drawer-x-close");
     document.getElementById(nav).appendChild(closeButton);
     var h3 = document.createElement("h3");
     h3.innerHTML = "&#160";
     document.getElementById(nav).appendChild(h3);
   }
   var lgl = document.createElement('a');
   lgl.setAttribute("href", "./index_launch.html");
   lgl.innerHTML = "launch a MAS";
   document.getElementById(nav).appendChild(lgl);
   document.getElementById(nav).appendChild(h.createDefaultHR());
   var lgc = document.createElement('a');
   lgc.addEventListener("click", function() { g.promptCommitDialog() });
   lgc.innerHTML = "commit changes";
   document.getElementById(nav).appendChild(lgc);
   var lgp = document.createElement('a');
   lgp.addEventListener("click", function() { g.pushChanges() });
   lgp.innerHTML = "push changes";
   document.getElementById(nav).appendChild(lgp);
/*
   var ldag = document.createElement('a');
   ldag.onclick = function() {
     if (confirm("Kill all agents?") === true) {
       deleteResource("./agents").then(function(r){
         instantMessage("Agents killed!");
         setTimeout(window.location.reload(),1000);
       });
     } else {
       instantMessage("Operation cancelled!")
     }
   };
   ldag.innerHTML = "kill all agents";
   document.getElementById(nav).appendChild(ldag);
   var ldar = document.createElement('a');
   ldar.onclick = function() {
     if (confirm("dispose all artifacts?") === true) {
       deleteResource("./workspaces").then(function(r){
         instantMessage("Artifacts disposed!");
         setTimeout(window.location.reload(),1000);
       });
     } else {
       instantMessage("Operation cancelled!")
     }
   };
   ldar.innerHTML = "dispose all artifacts";
   document.getElementById(nav).appendChild(ldar);
   var ldor = document.createElement('a');
   ldor.onclick = function() {
     if (confirm("disband all organisations?") === true) {
       deleteResource("./oe").then(function(r){
         instantMessage("Organisations disbanded!");
         setTimeout(window.location.reload(),1000);
       });
     } else {
       instantMessage("Operation cancelled!")
     }
   };
   ldor.innerHTML = "dismantle all organisations";
   document.getElementById(nav).appendChild(ldor);
*/
 }

 /*Get list of MAS from backend*/
 function getMASs() {
   h.get("./jcm").then(function(resp) {
     let jcms = JSON.parse(resp);

     Object.keys(jcms).forEach(function(f) {
       var d = document.createElement('div');
       d.setAttribute("class", "card fluid");
       document.getElementById("doc-content").appendChild(d);
       var h4 = document.createElement('h4');
       h4.setAttribute("class", "section double-padded");
       h4.innerHTML = "<b>"+jcms[f].jcm.substr(0,jcms[f].jcm.length-4)+"</b>&#160&#160&#160";
       d.appendChild(h4);
       var l = document.createElement('a');
       l.setAttribute("href", "./index.html");
       l.onclick = function() { getaMAS(jcms[f].jcm); };
       l.innerHTML = "[launch]";
       h4.appendChild(l);
       var div = document.createElement('div');
       d.appendChild(div);
       var p = document.createElement('p');
       div.appendChild(p);
       var ia = document.createElement('i');
       ia.innerHTML = "agents: " + jcms[f].agents.join("&#160  ");
       p.appendChild(ia);
       p.appendChild(document.createElement('br'));
       var iw = document.createElement('i');
       if (jcms[f].workspaces.length > 0) {
         iw.innerHTML = "workspaces: " + jcms[f].workspaces.join("&#160  ");
         p.appendChild(iw);
         p.appendChild(document.createElement('br'));
       }
       if (jcms[f].organisations.length > 0) {
         var io = document.createElement('i');
         io.innerHTML = "organisations: " + jcms[f].organisations.join("&#160 ");
         p.appendChild(io);
       }
     });
   });
 }

 function getaMAS(mas) {
   h.get("./jcm/" + mas).then(function(resp) {
     window.location.assign("./index.html");
     window.location.reload();
   });
 }

/* WHOLE SYSTEM AS DOT */
function getMASAsDot() {
  let dot = [];

  h.get('./overview').then(function(mas) {
    let overview = JSON.parse(mas);

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
          let resp = "\t\"" + r + "\"->\"" + m.scheme +
            "\" [arrowtail=normal arrowhead=open label=\"responsible\"]\n";
          if (!orglinks.includes(resp)) { /*avoid duplicates*/
            orglinks.push(resp);
            dot.push("\t\t{rank=same \"" + r + "\" \"" + m.scheme + "\"}\n");
          }
        });
      });
    });
    dot.push("\t}\n");
    dot.push(orglinks.join(" "));

    /* agents dimension */
    dot.push("\tsubgraph cluster_ag {\n");
    dot.push("\t\tlabel=\"agents\" labeljust=\"r\" pencolor=gray fontcolor=gray\n");
    let ags = [];
    overview.agents.forEach(function(x) {
      ags.push(x.agent);
      var s1 = (x.agent.length <= p.MAX_LENGTH) ? x.agent : x.agent.substring(0, p.MAX_LENGTH) + " ...";
      dot.push("\t\t\"" + x.agent + "\" [label = \"" + s1 + "\" shape = \"ellipse\" style=filled fillcolor=white];\n");
    });
    if (ags.length > 0) dot.push("\t\t{rank=same \"" + ags.join("\" \"") + "\"}\n");
    dot.push("\t}\n");

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
            if (p.HIDDEN_ARTS.indexOf(wks.artifacts[a].type) < 0) {
              s1 = (wks.artifacts[a].artifact.length <= p.MAX_LENGTH) ? wks.artifacts[a].artifact :
                wks.artifacts[a].artifact.substring(0, p.MAX_LENGTH) + " ...";
              dot.push("\t\t\t\"" + wksName + "_" + wks.artifacts[a].artifact + "\" [label = \"" + s1 + ":\\n");
              s1 = (wks.artifacts[a].type.length <= p.MAX_LENGTH) ? wks.artifacts[a].type :
                wks.artifacts[a].type.substring(0, p.MAX_LENGTH) + " ...";
              dot.push(s1 + "\"shape=record style=filled fillcolor=white];\n");
              wksartifacts.push(wksName + "_" + wks.artifacts[a].artifact);
              envlinks.push("\t\t\"" + ag.agent + "\"->\"" + wksName + "_" + wks.artifacts[a].artifact + "\" [arrowhead=odot]\n");
            }
          });

          if (wksartifacts.length > 0) dot.push("\t\t\t{rank=same \"" + wksartifacts.join("\" \"") + "\"}\n");
          dot.push("\t\t}\n");
          dot.push(envlinks.join(" "));
        });
      }
    });
    dot.push("\t}\n");

    dot.push("}\n");

    /* Transition follows modal top down movement */
    var t = d3.transition().duration(750).ease(d3.easeLinear);
    if (overview.agents.length === 0) dot = ["digraph G { graph [ rankdir=\"TB\" bgcolor=\"transparent\"]\n noAg [label=<There is<br />no agents>]\n}\n"];
    d3.select("#overviewgraph").graphviz().transition(t).renderDot(dot.join(""));
  });
}

/**
 * EXPORTS
 */

window.updateMASMenus = updateMASMenus;
window.getMASAsDot = getMASAsDot;
window.getMASs = getMASs;

/**
 * END OF FILE
 */
