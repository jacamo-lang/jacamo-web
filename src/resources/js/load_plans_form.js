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
