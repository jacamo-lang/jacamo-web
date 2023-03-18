/**
 * IMPORTS
 */
 const h = require('./helpers')
 const p = require('./parameters')
 
 /**
  * ARTIFACT EDITOR FUNCTIONS
  */
 
 /* Fill text area with current artifact's java code */
 function getCurrentJavaContent() {
   var selectedJAVAFile = new URL(location.href).searchParams.get('javafile');
 
   h.get("/workspaces/temp/javafile/" + selectedJAVAFile).then(function(response) {
     const submit = document.createElement('button');
     submit.setAttribute("type", "submit");
     /*submit.setAttribute("onclick", "window.location.replace('./workspaces.html')");*/
     submit.innerHTML = "Save & Reload";
     document.getElementById("footer_menu").appendChild(submit);
     const cancel = document.createElement('button');
     cancel.setAttribute("type", "button");
     cancel.setAttribute("onclick", "location.href='./workspaces.html'");
     cancel.innerHTML = "Discard changes";
     document.getElementById("footer_menu").appendChild(cancel);
     const text = document.createElement('i');
     text.style.fontSize = "12px";
     text.innerHTML = "Editing: <b>" + selectedJAVAFile + "</b>";
     document.getElementById("footer_menu").appendChild(text);
 
     const form = document.getElementById("usrform");
     createJavaEditor(response);
   });
 }
 
 function createJavaEditor(content) {
   /* find the textarea */
   var textarea = document.querySelector("form textarea[name='javafile']");
 
   /* create ace editor */
   var editor = ace.edit();
   editor.session.setValue(content);
   editor.setTheme("ace/theme/textmate");
   editor.session.setMode("ace/mode/java");
   editor.setOptions({
     enableBasicAutocompletion: true
   });
 
   /* replace textarea with ace */
   textarea.parentNode.insertBefore(editor.container, textarea);
   textarea.style.display = "none";
   /* find the parent form and add submit event listener */
   var form = textarea;
   while (form && form.localName != "form") form = form.parentNode;
   form.addEventListener("submit", function(e) {
     e.preventDefault();
     textarea.value = editor.getValue();
     var selectedJAVAFile = new URL(location.href).searchParams.get('javafile');
     h.post("/workspaces/temp/javafile/" + selectedJAVAFile, new FormData(e.target)).then(function(response) {
       localStorage.setItem("workspaceBuffer", response);
       window.location.replace('./workspaces.html');
     });
 
   }, true);
 }
 
 /**
  * ARTIFACT FUNCTIONS
  */
 
 /* Setup edit artifact button */
 function setEditButton() {
   document.getElementById('btneditartifact').setAttribute(
     "href", "artifact_editor.html?workspace=" + (new URL(location.href).searchParams.get('workspace')) +
     "&artifact=" + (new URL(location.href).searchParams.get('artifact')) +
     "&javafile=" + (new URL(location.href).searchParams.get('javafile'))
   );
 }
 
 /**
  * WORKSPACE FUNCTIONS
  */
 /*
 function getWorkspaces() {
   const params = new URL(location.href).searchParams;
   const selectedWorkspace = params.get('workspace');
 
   h.get("./workspaces").then(function(resp) {
     let ws = JSON.parse(resp);
     updateWorkspaceMenu("nav-drawer", ws, undefined, true);
     updateWorkspaceMenu("nav-drawer-frame", ws, undefined, false);
   });
 }
 
 function updateWorkspaceMenu(nav, ws, ar, addCloseButton) {
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
 
   let promissesWS = [];
   ws.sort().forEach(function(n) {
     promissesWS.push(h.get("./workspaces/" + n));
   });
   Promise.all(promissesWS).then(function(r) {
       r.forEach(function(e) {
         let ar = JSON.parse(e);
         let validContent = 0;
         Object.keys(ar.artifacts).sort().forEach(function(a) {
           //if (p.HIDDEN_ARTS.indexOf(ar.artifacts[a].type) < 0) {
             validContent++;
             // if printing the first artifact, also print the workspace
             if (selectedWorkspace === ar.workspace) {
               if (validContent === 1) {
                 var lag = document.createElement('a');
                 lag.setAttribute("href", "./workspace.html?workspace=" + ar.workspace);
                 lag.innerHTML = "<h5><b>" + ar.workspace + "</b></h5>";
                 document.getElementById(nav).appendChild(lag);
               }
               // Add artifacts on menu 
               var lar = document.createElement('a');
               if (selectedArtifact === a) {
                 lar.innerHTML = "<h5><b>&#160;&#160;&#160;" + a + "</b></h5>";
               } else {
                 lar.innerHTML = "<h5>&#160;&#160;&#160;" + a + "</h5>";
               }
               lar.setAttribute("href", "./artifact.html?workspace=" + ar.workspace + "&artifact=" + a 
                 //+ "&javafile=" + ar.artifacts[a].type + ".java"
                 );
               document.getElementById(nav).appendChild(lar);
             } else {
               // if would print at least one artifact, also print the workspace 
               if (validContent === 1) {
                 var lag = document.createElement('a');
                 lag.setAttribute("href", "./workspace.html?workspace=" + ar.workspace);
                 lag.innerHTML = "<h5>" + ar.workspace + "</h5>";
                 document.getElementById(nav).appendChild(lag);
               }
             }
           //}
         });
       });
   }).then(function(r){
     document.getElementById(nav).appendChild(h.createDefaultHR());
     var lnew = document.createElement('a');
     lnew.setAttribute("href", "./artifact_new.html");
     lnew.innerHTML = "create template";
     document.getElementById(nav).appendChild(lnew);
   });
 }
 */

 /* WHOLE WORKSPACES AS DOT */
 
 function getWorkspacesNonVolatileGraph() {
   h.get('./overview').then(function(mas) {
     getWorkspacesAsDot(JSON.parse(mas));
   });
 }
 
 let wksDataCache = undefined;
 let wksGraphCache = undefined;
 
 function getWorkspacesAsDot(nonVolatileMAS) {
   if (nonVolatileMAS != undefined)
     wksDataCache = nonVolatileMAS;
 
   // graph header 
   let header = [];
   header.push("digraph G { graph [ rankdir=\"TB\" bgcolor=\"transparent\" ranksep=0.25 ]\n");
 
   // Environment dimension 
   wksDataCache.agents.forEach(function(ag) {
     if (Object.keys(ag.workspaces).length > 0) {
       Object.keys(ag.workspaces).forEach(function(w) {
         let envlinks = [];
         let wks = ag.workspaces[w];
         let wksName = wks.workspace.value.replace(/\//g, "_"); //Replace forward slash by underscore
         header.push("\t\tsubgraph cluster_" + wksName + " {\n");
         header.push("\t\t\tlabel=\"" + wks.workspace.value + "\" labeljust=\"r\" style=dashed pencolor=gray40 fontcolor=gray40\n");
         if (wks.artifacts != null) Object.keys(wks.artifacts).forEach(function(a) {
           if (p.HIDDEN_ARTS.indexOf(wks.artifacts[a].type.value) < 0) {
             s1 = (wks.artifacts[a].artifact.value.length <= p.MAX_LENGTH) ? wks.artifacts[a].artifact.value :
               wks.artifacts[a].artifact.value.substring(0, p.MAX_LENGTH) + " ...";
             header.push("\t\t\t\"" + wksName + "_" + wks.artifacts[a].artifact.value + "\" [label = \"" + s1 + ":\\n");
             s1 = (wks.artifacts[a].type.value.length <= p.MAX_LENGTH) ? wks.artifacts[a].type.value :
               wks.artifacts[a].type.value.substring(0, p.MAX_LENGTH) + " ...";
             header.push(s1 + "\"shape=record style=filled fillcolor=white];\n");
             envlinks.push("\t\t\"" + ag.agent.value + "\"->\"" + wksName + "_" + wks.artifacts[a].artifact.value + "\" [arrowhead=odot]\n");
           }
         });
         header.push("\t\t}\n");
         header.push(envlinks.join(" "));
       });
     }
   });
 
   /* graph footer */
   let footer = [];
   footer.push("}\n");
 
   let graph = header.join("").concat(footer.join(""));
   if (graph !== wksGraphCache) {
     wksGraphCache = graph;
     /* Transition follows modal top down movement */
     import( /* webpackChunkName: "d3" */ 'd3').then(function(d3) {
       import( /* webpackChunkName: "d3-graphviz" */ 'd3-graphviz').then(function(d3G) {
         var t = d3.transition().duration(500).ease(d3.easeLinear);
         d3G.graphviz("#workspacesgraph").transition(t).renderDot(wksGraphCache);
       });
     });
   }
 }
 
 /**
  * EXPORTS
  */
 
 window.getCurrentJavaContent = getCurrentJavaContent;
  window.setEditButton = setEditButton;
 window.getWorkspacesNonVolatileGraph = getWorkspacesNonVolatileGraph;
 
 /**
  * END OF FILE
  */
 