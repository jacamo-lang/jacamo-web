/**
 * IMPORTS
 */

const h = require('./helpers')

/**
 * GIT INTEGRATION FUNCTIONS
 */

/** CREDENTIAL PROMPT */
let usernameCookie = getCookieValue('username');
if (!usernameCookie) {
  promptCredentialDialog();
  var gitUsernameInput = document.querySelector('#git-username');
  var gitPasswordInput = document.querySelector('#git-password');
  gitUsernameInput.addEventListener('input', checkSaveActivation);
  gitPasswordInput.addEventListener('input', checkSaveActivation);
}

function promptCredentialDialog() {
  var div = document.createElement('div');
  div.setAttribute('id', 'git-dialog-full');
  var divmain = document.createElement('div');
  divmain.setAttribute('id', 'git-dialog-main');
  div.appendChild(divmain);
  var divbody = document.createElement('div');
  divbody.setAttribute('id', 'git-dialog-body');
  divbody.innerHTML = "Enter your credentials for the jacamo-web git server: <br>Username:";
  divmain.appendChild(divbody);
  var user = document.createElement('input');
  user.setAttribute('id', 'git-username');
  user.setAttribute('placeholder', 'Username');
  divbody.appendChild(user);
  divbody.innerHTML += "Password: &zwnj;"
  var password = document.createElement('input');
  password.setAttribute('id', 'git-username');
  password.setAttribute('type', 'password');
  password.setAttribute('placeholder', 'Password');
  divbody.appendChild(password);
  var butcancel = document.createElement('button');
  butcancel.setAttribute('class', 'git-button');
  butcancel.addEventListener('click', function(r) { cancelDialog('git-dialog-full'); });
  butcancel.innerText = "Cancel";
  divmain.appendChild(butcancel);
  var butsave = document.createElement('button');
  butsave.setAttribute('class', 'git-button');
  butsave.setAttribute('id', 'git-credential-save');
  butsave.setAttribute('disabled', 'true');
  butsave.innerText = "Commit";
  butsave.addEventListener('click', function(r) { storeCredentials(); });
  divmain.appendChild(butsave);

  document.getElementById("doc-wrapper").appendChild(div);
}

function checkSaveActivation() {
  let gitCredentialSaveButton = document.querySelector('#git-credential-save');
  gitCredentialSaveButton.disabled = (gitUsernameInput.value.length === 0 && gitPasswordInput.value.length === 0);
}

function storeCredentials() {
  cancelDialog('git-dialog-full');
  setCookie('username', gitUsernameInput.value);
  setCookie('password', gitPasswordInput.value);
}

function cancelDialog(id) {
  let dialogElement = document.querySelector(`#${id}`);
  dialogElement.parentNode.removeChild(dialogElement);
}

function setCookie(key, value) {
  return document.cookie = `${key}=${(value || '')}; path=/`;
}

/* function adjusted from: https://stackoverflow.com/questions/5639346/what-is-the-shortest-function-for-reading-a-cookie-by-name-in-javascript */
function getCookieValue(key) {
  let value = document.cookie.match(`(^|[^;]+)\\s*${key}\\s*=\\s*([^;]+)`);
  return value ? value.pop() : '';
}

/** LOCK NOTIFICATIONS */
toastr.options.preventDuplicates = true;

let url = new URL(window.location.href);
let file = url.searchParams.get('aslfile') || url.searchParams.get('javafile');

if (file) {
  h.post(`/lock/${file}?username=${usernameCookie}`).then(function(response) {
    let jResponse = JSON.parse(response);
    if (jResponse[file] && jResponse[file].length > 1) {
      let otherEditors = jResponse[file].slice(0, -1);
      toastr.warning(
        `The following user(s) are already editing this file: ${otherEditors.join(', ')}.`,
        { timeOut: 10000 }
      );
    }
  });
}

window.onbeforeunload = _ => {
  navigator.sendBeacon(`/unlock/${file}?username=${usernameCookie}`);
};

/** GIT COMMIT/PUSH DIALOG */
function promptCommitDialog() {
  var div = document.createElement('div');
  div.setAttribute('id', 'commit-dialog-full');
  var divmain = document.createElement('div');
  divmain.setAttribute('id', 'git-dialog-main');
  div.appendChild(divmain);
  var divbody = document.createElement('div');
  divbody.setAttribute('id', 'git-dialog-body');
  divbody.innerHTML = "Enter your commit message here: <br>";
  divmain.appendChild(divbody);
  var message = document.createElement('input');
  message.setAttribute('id', 'git-commit-message');
  message.setAttribute('placeholder', 'Commit message');
  message.addEventListener('input', function(r) { checkCommitActivation(); } );
  divbody.appendChild(message);
  var divissues = document.createElement('div');
  divissues.setAttribute('id', 'commit-dialog-issues');
  divbody.appendChild(divissues);
  var butcancel = document.createElement('button');
  butcancel.setAttribute('class', 'git-button');
  butcancel.addEventListener('click', function(r) { cancelDialog('commit-dialog-full'); });
  butcancel.innerText = "Cancel";
  divmain.appendChild(butcancel);
  var butcommit = document.createElement('button');
  butcommit.setAttribute('class', 'git-button');
  butcommit.setAttribute('id', 'git-commit');
  butcommit.setAttribute('disabled', 'true');
  butcommit.innerText = "Commit";
  butcommit.addEventListener('click', function(r) { commitChanges(); });
  divmain.appendChild(butcommit);

  document.getElementById('doc-wrapper').appendChild(div);
  h.get('status').then(response => renderIssueOverview(response));
}

function renderIssueOverview(response) {
  jResponse = JSON.parse(response);
  let affectedFiles = jResponse.added.filter(filter).concat(jResponse.modified.filter(filter)).map(
    file => file.substring(file.lastIndexOf('/') + 1)
  );
  if (affectedFiles.length > 0) {
    document.querySelector('#commit-dialog-issues').innerHTML
      += '<h4 class="agent-check-heading">Agent Checks</h4>';
  }
  affectedFiles.forEach(
    file => h.get(`/agents/${file.split('.')[0]}/aslfile/${file}`).then(agentCode => {
      h.post(
        `/agents/${file.split('.')[0]}/parseAslfile/${file}`,
        `--blob\r\ncontent-disposition: form-data; name=aslfile\r\n\r\n${agentCode}\r\n--blob--`,
        `multipart/form-data; boundary=blob`
      ).then(result => {
        let div = `<div class="issue-check correct">Agent ${file.split('.')[0]}: ${result}</div>`;
        document.querySelector('#commit-dialog-issues').innerHTML += div;
      }).catch(errror => {
        if (errror.startsWith("Warning")) {
          let div = `<div class="issue-check warning">Warning: <strong>Agent ${file.split('.')[0]}</strong>: ${errror}</div>`;
          document.querySelector('#commit-dialog-issues').innerHTML += div;
        } else {
          let div = `<div class="issue-check error">Error: <strong>Agent ${file.split('.')[0]}</strong>: ${errror}</div>`;
          document.querySelector('#commit-dialog-issues').innerHTML += div;
        }
      });
    })
  );
  function filter(file) {
    return file.endsWith('.asl')
  }
}

function checkCommitActivation() {
  let commitButton = document.querySelector('#git-commit');
  let commitMessageInput = document.querySelector('#git-commit-message');
  commitButton.disabled = commitMessageInput.value.length === 0;
}

function commitChanges() {
  let message = document.querySelector('#git-commit-message').value;
  cancelDialog('commit-dialog-full');
  if (!message) {
    toastr.error('Operation cancelled.', { timeOut: 10000 });
  } else {
    h.post('/commit?email='+getCookieValue('username'), message).then(function(response) {
      toastr.info(`Commit result: ${response}`, { timeOut: 10000 });
    }).catch(function(error) {
      toastr.error(error, { timeOut: 10000 });
    });
  }
}

function pushChanges() {
  let usernameCookie = getCookieValue('username');
  let passwordCookie = getCookieValue('password');
  h.post('/push?username='+usernameCookie+'&password='+btoa(passwordCookie))
  .then(function(response) {
    toastr.info(`Push result: ${response}`, { timeOut: 10000 });
  }).catch(function(error) {
    toastr.error(error, { timeOut: 10000 });
  });
}

/**
 * EXPORTS
 */

module.exports = {
  promptCommitDialog,
  pushChanges,
  cancelDialog,
  commitChanges
}

/**
 * END OF FILE
 */
