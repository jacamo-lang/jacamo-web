/*
var editor = ace.edit("editor"); 
editor.setTheme("ace/theme/textmate"); 
editor.session.setMode("ace/mode/erlang");
editor.setOptions({enableBasicAutocompletion: true});
*/

function createEditor(name) {
    /* find the textarea */
    var textarea = document.querySelector("form textarea[name=" + name + "]");

    /* create ace editor */ 
    var editor = ace.edit();
    editor.container.style.height = "300px";
    editor.session.setValue(textarea.value);
    editor.setTheme("ace/theme/textmate"); 
    editor.session.setMode("ace/mode/erlang");
    editor.setOptions({
    	enableBasicAutocompletion: true
    });

    /* replace textarea with ace */
    textarea.parentNode.insertBefore(editor.container, textarea);
    textarea.style.display = "none";
    /* find the parent form and add submit event listener */
    var form = textarea;
    while (form && form.localName != "form") form = form.parentNode;
    form.addEventListener("submit", function() {
        /* update value of textarea to match value in ace */
        textarea.value = editor.getValue();
    }, true);
}
createEditor("aslfile");

/*function to upload plans library*/
function uploadPlansLib(agent) {
    var textarea = document.querySelector("form textarea[name=editor]");

    h4 = new XMLHttpRequest(); 
    h4.open("POST", "/agents/" + agent + "/plans", true);
    h4.setRequestHeader("Content-type", "multipart/form-data");
    
    
    data = textarea.value;
    
    window.alert("under construction!");

    h4.onreadystatechange = function() { 
    	if (h4.readyState == 4 && h4.status == 200) {
    		location.reload();
    		window.alert(http.responseText);
    	}
    };
    h4.send(data);
} 