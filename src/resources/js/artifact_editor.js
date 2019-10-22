/* Fill text area with current artifact's java code */
function getCurrentJavaContent() {
  var selectedJAVAFile = new URL(location.href).searchParams.get('javafile');

  const Http = new XMLHttpRequest();
  Http.open("GET", "/workspaces/temp/javafile/" + selectedJAVAFile);
  Http.send();
  Http.onreadystatechange = function() {
    if (this.readyState == 4 && this.status == 200) {
      /* Remove all existing children */
      const menu = document.getElementById("footer_menu");
      while (menu.firstChild) {
        menu.removeChild(menu.firstChild);
      }

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
      createEditor(Http.responseText);
    }
  };
};

function saveFile(formData) {
  const Http = new XMLHttpRequest();
  Http.onreadystatechange = function() {
    if (this.readyState == 4 || this.readyState == 200) {
      alert(Http.responseText);
      window.location.replace('./workspaces.html');
    }
  };
  var selectedJAVAFile = new URL(location.href).searchParams.get('javafile');
  Http.open("post", "/workspaces/temp/javafile/" + selectedJAVAFile);
  Http.send(formData);
}

function createEditor(content) {
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
    const formData = new FormData(e.target);
    saveFile(formData);
  }, true);
}
