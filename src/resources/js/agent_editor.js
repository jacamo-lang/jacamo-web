/* Fill text area with current agent's asl code */
function getCurrentAslContent() {
  var selectedAgent = new URL(location.href).searchParams.get('agent');
  var selectedASLFile = new URL(location.href).searchParams.get('aslfile');

  const Http = new XMLHttpRequest();
  Http.open("GET", "/agents/" + selectedAgent + "/aslfile/" + selectedASLFile);
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
      submit.setAttribute("onclick", "window.location.replace('./agent.html?agent=" + selectedAgent + "')");
      submit.innerHTML = "Save & Reload";
      document.getElementById("footer_menu").appendChild(submit);
      const cancel = document.createElement('button');
      cancel.setAttribute("type", "button");
      cancel.setAttribute("onclick", "window.history.back();");
      cancel.innerHTML = "Discard changes";
      document.getElementById("footer_menu").appendChild(cancel);
      const text = document.createElement('i');
      text.style.fontSize = "12px";
      text.innerHTML = "Editing: <b>" + selectedASLFile + "</b>";
      document.getElementById("footer_menu").appendChild(text);

      const form = document.getElementById("usrform");
      form.setAttribute("action", "/agents/" + selectedAgent + "/aslfile/" + selectedASLFile);
      createEditor(Http.responseText);
    }
  };
};

function saveFile(formData) {
  const Http = new XMLHttpRequest();
  Http.onreadystatechange = function() {
    if (this.readyState == 4 && this.status == 200) {
      alert(Http.responseText);
    }
  };
  var selectedAgent = new URL(location.href).searchParams.get('agent');
  var selectedASLFile = new URL(location.href).searchParams.get('aslfile');
  Http.open("post", "/agents/" + selectedAgent + "/aslfile/" + selectedASLFile);
  Http.send(formData);
}

function createEditor(content) {
  /* find the textarea */
  var textarea = document.querySelector("form textarea[name='aslfile']");

  /* create ace editor */
  var editor = ace.edit();
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
  form.addEventListener("submit", function(e) {
    textarea.value = editor.getValue();
    const formData = new FormData(e.target);
    saveFile(formData);
    e.preventDefault();
  }, true);
}
