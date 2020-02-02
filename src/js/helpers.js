/**
 * HELPFUL FUNCTIONS
 */

/* GET A GIVEN URL CONTENT */
function get(url) {
  return new Promise(function(resolve, reject) {
    var req = new XMLHttpRequest();
    req.open('GET', url);
    req.onload = function() {
      if (req.status == 200) {
        resolve(req.response);
      } else {
        reject(Error(req.statusText));
      }
    };
    req.onerror = function() {
      reject(Error("Network Error"));
    };
    req.send();
  });
}

/* POST ON A GIVEN URL, RETURN SERIALISED CONTENT */
function post(url, data, type) {
  return new Promise(function(resolve, reject) {
    var req = new XMLHttpRequest();
    req.open('POST', url);
    if (type != undefined) req.setRequestHeader("Content-type", type);
    req.onload = function() {
      if (req.status == 200) {
        resolve(req.response);
      } else {
        reject(req.statusText);
      }
    };
    req.onerror = function() {
      reject(Error("Network Error"));
    };
    req.send(data);
  });
}

/* SEND A DELETE COMMAND TO A GIVEN RESOURCE */
let deleteResource = (url) => {
  return new Promise(function(resolve, reject) {
    var req = new XMLHttpRequest();
    req.open('DELETE', url);
    req.onload = function() {
      if (req.status == 200) {
        resolve(req.response);
      } else {
        reject(Error(req.statusText));
      }
    };
    req.onerror = function() {
      reject(Error("Network Error"));
    };
    req.send();
  });
};

/* TABLE FUNCTIONS: CREATE TABLE IN A SECTION, ADD A ROW IN A TABLE */
let createTable = (section) => {
  t = document.createElement('table');
  t.setAttribute("class", 'striped');
  t.style.maxHeight = "100%";
  let s = document.getElementById(section);
  s.appendChild(t);
  return t;
};

function addTwoCellsInARow(table, p, v) {
  var tr, cellProperty, cellDetail;
  tr = table.insertRow(-1);
  cellProperty = tr.insertCell(-1);
  cellDetail = tr.insertCell(-1);
  cellProperty.innerHTML = p;
  cellDetail.innerHTML = v;
}

/* INSTANT MESSAGE - HTML must have a top-alert DIV with a top-alert-message LABEL */
const instantMessage = (msg) => {
  if (msg != null) {
    $('#top-alert-message').text(msg);
    $('#top-alert').fadeTo(2000+(msg.length*10), 500).slideUp(500, function() {
      $('#top-alert').slideUp(500);
    });
  }
};

let createDefaultHR = () => {
  var hr = document.createElement('hr');
  hr.style.display = 'line';
  hr.style.background = '#f8f8f8';
  hr.style.borderBottom = '1px solid #ddd';
  return hr;
};

/**
 * EXPORTS
 */

module.exports = {
  get,
  post,
  deleteResource,
  createTable,
  addTwoCellsInARow,
  instantMessage,
  createDefaultHR
}

/**
 * END OF FILE
 */
