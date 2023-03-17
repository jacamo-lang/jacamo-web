/**
 * HELPFUL FUNCTIONS
 */

const $ = require('jquery')

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

/* PUT ON A GIVEN URL, RETURN SERIALISED CONTENT */
function put(url, data, type) {
  return new Promise(function(resolve, reject) {
    var req = new XMLHttpRequest();
    req.open('PUT', url);
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

let createDefaultHR = () => {
  var hr = document.createElement('hr');
  hr.style.display = 'line';
  hr.style.background = '#f8f8f8';
  hr.style.borderBottom = '1px solid #ddd';
  return hr;
};

function orderByNameAsc(a,b) {
  if (a.name.toLowerCase() < b.name.toLowerCase()) {
      return -1;
  }
  if (a.name.toLowerCase() > b.name.toLowerCase()) {
      return 1;
  }
  return 0;
}

/**
 * EXPORTS
 */

module.exports = {
  get,
  post,
  deleteResource,
  createTable,
  addTwoCellsInARow,
  createDefaultHR,
  orderByNameAsc
}

/**
 * END OF FILE
 */
