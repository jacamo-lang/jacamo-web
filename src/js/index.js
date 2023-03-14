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
   lgc.addEventListener("click", function() { g.commitChanges() });
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

let masGraphCache = undefined;
 /* WHOLE SYSTEM AS DOT */
function getMASAsDot() {
  let dot = [];
  
  h.get('./overview').then(function(mas) {
    let overview = JSON.parse(mas);

    if (overview.agents.length === 0) {
      dot = ["digraph G { graph [ rankdir=\"TB\" bgcolor=\"transparent\"]\n noAg [label=<There are<br />no agents>]\n}\n"];
    } else {
      dot.push("digraph G { graph [ rankdir=\"TB\" bgcolor=\"transparent\"]\n");

      /* Organisation dimension */
      dot.push("\tsubgraph cluster_org {\n");
      dot.push("\t\tlabel=\"organisation\" labeljust=\"r\" pencolor=gray fontcolor=gray\n");
      overview.organisations.forEach(function(o) {
        dot.push("\t\tsubgraph cluster_" + o.organisation + " {\n");
        dot.push("\t\t\tlabel=\"" + o.organisation + "\" labeljust=\"r\" pencolor=gray40 fontcolor=gray40\n");
        o.groups.forEach(function(g) {
          dot.push("\t\t\t\"" + g.id + "\" [ " + "label = \"" + g.id + "\" shape=tab style=filled pencolor=black fillcolor=lightgrey];\n");
        });
        dot.push("\t\t}\n");
      });
  
      let orglinks = [];
      overview.agents.forEach(function(a) {
        a.roles.forEach(function(r) {
          orglinks.push("\t\"" + r.group.value + "\"->\"" + a.agent.value + "\" [arrowtail=normal dir=back label=\"" + r.role.value + "\"]\n");
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
        var s1 = (x.agent.value.length <= p.MAX_LENGTH) ? x.agent.value : x.agent.value.substring(0, p.MAX_LENGTH) + " ...";
        dot.push("\t\t\"" + x.agent.value + "\" [label = \"" + s1 + "\" shape = \"ellipse\" style=filled fillcolor=white];\n");
      });
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
            let wksName = wks.workspace.value.replace(/\//g, "_"); //Replace forward slash by underscore
            dot.push("\t\tsubgraph cluster_" + wksName + " {\n");
            dot.push("\t\t\tlabel=\"" + wks.workspace.value + "\" labeljust=\"r\" style=dashed pencolor=gray40 fontcolor=gray40\n");
            if (wks.artifacts != null) Object.keys(wks.artifacts).forEach(function(a) {
              if (p.HIDDEN_ARTS.indexOf(wks.artifacts[a].type.value) < 0) {
                s1 = (wks.artifacts[a].artifact.value.length <= p.MAX_LENGTH) ? 
                  wks.artifacts[a].artifact.value :
                  wks.artifacts[a].artifact.value.substring(0, p.MAX_LENGTH) + " ...";
                dot.push("\t\t\t\"" + wksName + "_" + wks.artifacts[a].artifact.value + "\" [label = \"" + s1 + ":\\n");
                s1 = (wks.artifacts[a].type.value.length <= p.MAX_LENGTH) ? 
                  wks.artifacts[a].type.value :
                  wks.artifacts[a].type.value.substring(0, p.MAX_LENGTH) + " ...";
                dot.push(s1 + "\"shape=record style=filled fillcolor=white];\n");
                wksartifacts.push(wksName + "_" + wks.artifacts[a].artifact.value);
                envlinks.push("\t\t\"" + ag.agent.value + "\"->\"" + wksName + "_" + wks.artifacts[a].artifact.value + "\" [arrowhead=odot]\n");
              }
            });
  
            dot.push("\t\t}\n");
            dot.push(envlinks.join(" "));
          });
        }
      });
      dot.push("\t}\n");
  
      dot.push("}\n");  
    }

    let graph = dot.join("");
    //console.log(graph);
    if (graph !== masGraphCache) {
      masGraphCache = graph;
      /* Transition follows modal top down movement */
      import( /* webpackChunkName: "d3" */ 'd3').then(function(d3) {
        import( /* webpackChunkName: "d3-graphviz" */ 'd3-graphviz').then(function(d3G) {
          var t = d3.transition().duration(500).ease(d3.easeLinear);
          d3G.graphviz("#overviewgraph").transition(t).renderDot(masGraphCache);
        });
      });
    }

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
