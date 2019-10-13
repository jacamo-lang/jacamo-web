/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
CREATE ARTIFACT
* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

/* create artifact */
function newArt() {
  http = new XMLHttpRequest();
  /*Hopefully there is not an artifact with the referred name in a wks called 'temp', if so, its source will be opened*/
  http.open("GET", '/workspaces/temp/javafile/' + document.getElementById('createArtifact').value, false);
  http.send();
  window.location.assign('/artifact_editor.html#' + document.getElementById('createArtifact').value);
}
