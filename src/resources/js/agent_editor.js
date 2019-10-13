/* Fill text area with current agent's asl code */
function getCurrentAslContent() {
  var selectedASLFile = window.location.hash.substr(1);
  const Http = new XMLHttpRequest();
  Http.open("GET", "/agents/" + selectedASLFile.split(".")[0] + "/aslfile/" + selectedASLFile);
  Http.send();
  Http.onreadystatechange = function() {
    if (this.readyState == 4 && this.status == 200) {
      createEditor(Http.responseText);

      /* Remove all existing children */
      const menu = document.getElementById("footer_menu");
      while (menu.firstChild) {
        menu.removeChild(menu.firstChild);
      }

      const text = document.createElement('b');
      text.innerHTML = "Editing: " + selectedASLFile;
      document.getElementById("footer_menu").appendChild(text);
      const submit = document.createElement('button');
      submit.setAttribute("type", "submit");
      submit.innerHTML = "Save & Reload";
      document.getElementById("footer_menu").appendChild(submit);
      const cancel = document.createElement('button');
      cancel.setAttribute("type", "button");
      cancel.setAttribute("onclick", "window.history.back();");
      cancel.innerHTML = "Discard changes";
      document.getElementById("footer_menu").appendChild(cancel);

      const form = document.getElementById("usrform");
      /* Here there is a limitation, it is not properly ready to edit files which does not match with agent's name */
      form.setAttribute("action", "/agents/" + selectedASLFile.split(".")[0] + "/aslfile/" + selectedASLFile);
    }
  };
};

/*
var editor = ace.edit("editor");
editor.setTheme("ace/theme/textmate");
editor.session.setMode("ace/mode/erlang");
editor.setOptions({enableBasicAutocompletion: true});
*/

function createEditor(content) {
  /* find the textarea */
  var textarea = document.querySelector("form textarea[name='aslfile']");

  /* create ace editor */
  var editor = ace.edit();
  editor.container.style.height = "300px";
  editor.session.setValue(content);
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
