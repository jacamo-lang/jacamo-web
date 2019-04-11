/* create agent */
function newAg() {
	http = new XMLHttpRequest(); 
    http.open("POST", '/agents/'+document.getElementById('createAgent').value, false);
    http.send();
    window.open('/agents/'+document.getElementById('createAgent').value+'/mind', 'mainframe');
}

/* create agent */
function newArt() {
	http = new XMLHttpRequest();
	/*Probably there is not an artifact with the referred name in a wks called 'temp', if so, its source will be opened*/
    http.open("POST", '/workspaces/temp/'+document.getElementById('createArtifact').value, false);
    http.send();
    window.open('/workspaces/temp/javafile/'+document.getElementById('createArtifact').value, 'mainframe');
}

/* update navigation bar according to frames changes */
setInterval(function() { 
	document.getElementById("nav-drawer").innerHTML = sessionStorage.getItem("menucontent"); 
}, 500);
