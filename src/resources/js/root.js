
/* create agent */
function newAg() {
	http = new XMLHttpRequest(); 
    http.open("POST", '/agents/'+document.getElementById('createAgent').value, false);
    http.send();
    window.open('/', '_top');
}

setInterval(function() { 
	document.getElementById("nav-drawer").innerHTML = sessionStorage.getItem("menucontent"); 
}, 500);
