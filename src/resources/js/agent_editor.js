/* Fill text area with current agent's asl code */
function getCurrentAslContent() {
  /*var selectedASLFile = window.location.hash.substr(1);*/
  var parameters = location.search.substring(1).split("&");
  var temp = parameters[0].split("=");
  selectedAgent = unescape(temp[1]);
  temp = parameters[1].split("=");
  selectedASLFile = unescape(temp[1]);

  const Http = new XMLHttpRequest();
  Http.open("GET", "/agents/" + selectedAgent + "/aslfile/" + selectedASLFile);
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
      submit.setAttribute("onclick", "window.location.replace('./agent.html?agent=" + selectedAgent  + "')");
      submit.innerHTML = "Save & Reload";
      document.getElementById("footer_menu").appendChild(submit);
      const cancel = document.createElement('button');
      cancel.setAttribute("type", "button");
      cancel.setAttribute("onclick", "window.history.back();");
      cancel.innerHTML = "Discard changes";
      document.getElementById("footer_menu").appendChild(cancel);

      const form = document.getElementById("usrform");
      form.setAttribute("action", "/agents/" + selectedAgent+ "/aslfile/" + selectedASLFile);
    }
  };
};

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
  form.addEventListener("submit", function() {
    /* update value of textarea to match value in ace */
    textarea.value = editor.getValue();
  }, true);
}
