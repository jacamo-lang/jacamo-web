/* Fill text area with current artifact's java code */
function getCurrentJavaContent() {
  var selectedJAVAFile = window.location.hash.substr(1);
  const Http = new XMLHttpRequest();
  Http.open("GET", "/workspaces/temp/javafile/" + selectedJAVAFile);
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
      text.innerHTML = "Editing: " + selectedJAVAFile;
      document.getElementById("footer_menu").appendChild(text);
      const submit = document.createElement('button');
      submit.setAttribute("type", "submit");
      submit.innerHTML = "Save & Reload";
      document.getElementById("footer_menu").appendChild(submit);
      const cancel = document.createElement('button');
      cancel.setAttribute("type", "button");
      cancel.setAttribute("onclick", "location.href='./workspaces.html'");
      cancel.innerHTML = "Discard changes";
      document.getElementById("footer_menu").appendChild(cancel);

      const form = document.getElementById("usrform");
      form.setAttribute("action", "/workspaces/temp/javafile/" + selectedJAVAFile);
    }
  };
};

function createEditor(content) {
  /* find the textarea */
  var textarea = document.querySelector("form textarea[name='javafile']");

  /* create ace editor */
  var editor = ace.edit();
  editor.container.style.height = "300px";
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
  form.addEventListener("submit", function() {
    /* update value of textarea to match value in ace */
    textarea.value = editor.getValue();
  }, true);
}
