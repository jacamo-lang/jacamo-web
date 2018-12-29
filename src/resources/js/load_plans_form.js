var editor = ace.edit("editor"); 
editor.setTheme("ace/theme/textmate"); 
editor.session.setMode("ace/mode/erlang");
editor.setOptions({enableBasicAutocompletion: true});

/*function to upload plans library*/
function uploadPlansLib(agent) {
	h4 = new XMLHttpRequest(); 
    h4.open("POST", "/agents/" + agent + "/plans", true);
    h4.setRequestHeader("Content-type", "multipart/form-data");
    data = editor.getValue();
    
    window.alert("under construction!");

    h4.onreadystatechange = function() { 
    	if (h4.readyState == 4 && h4.status == 200) {
    		location.reload();
    		window.alert(http.responseText);
    	}
    };
    h4.send(data);
} 
