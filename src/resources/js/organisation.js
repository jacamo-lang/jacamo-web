/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
RETRIEVE DATA ABOUT ONE WORKSPACE
* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

function getDetails() {
  var itemDetails = "";
  const selectedItem = new URL(location.href).searchParams.get('organisation');

  const Http = new XMLHttpRequest();
  Http.open("GET", "./oe/" + selectedItem + "/os/");
  Http.send();
  Http.onreadystatechange = function() {
    if (this.readyState == 4 && this.status == 200) {
      itemDetails = JSON.parse(Http.responseText);
      updateTable(selectedItem, itemDetails);
    }
  };
};

function addTwoCellsInARow(table, p, v) {
  var tr, cellProperty, cellDetail;
  tr = table.insertRow(-1);
  cellProperty = tr.insertCell(-1);
  cellDetail = tr.insertCell(-1);
  cellProperty.innerHTML = p;
  cellDetail.innerHTML = v;
}

let createTable = (section) => {
  t = document.createElement('table');
  t.setAttribute("class", 'striped');
  t.style.maxHeight = "100%";
  let s = document.getElementById(section);
  s.appendChild(t);
  return t;
};

function updateTable(selectedItem, item) {

  Object.keys(item.groups).forEach(function(g) {
    var table = createTable("groupssection");
    addTwoCellsInARow(table, "group", item.groups[g].group +
      "&#160;&#160;&#160;<a href='/oe/" + selectedItem + "/group/" + item.groups[g].group + "/" +
      item.groups[g].group + ".npl'>[specification]</a>&#160;<a href='/oe/" + selectedItem +
      "/group/" + item.groups[g].group + "/debug'>[instance]</a>"
    );
    addTwoCellsInARow(table, "well formed", item.groups[g].isWellFormed);
    var roles = "";
    Object.keys(item.groups[g].roles).forEach(function(r) {
      roles += item.groups[g].roles[r].role + " ( " +
        item.groups[g].roles[r].cardinality + " )";
      if (item.groups[g].roles[r].superRoles.length > 0) roles += " <- " +
        item.groups[g].roles[r].superRoles.join(', ');
      roles += " <br />"
    });
    addTwoCellsInARow(table, "roles", roles);
  });
  if (Object.keys(item.groups).length <= 0) {
    p = document.createElement('p');
    p.innerText = "nothing to show";
    let s = document.getElementById("groupssection");
    s.appendChild(p);
  }

  Object.keys(item.schemes).forEach(function(s) {
    var table = createTable("schemessection");
    addTwoCellsInARow(table, "scheme", item.schemes[s].scheme);
    addTwoCellsInARow(table, "well formed", item.schemes[s].isWellFormed);
    addTwoCellsInARow(table, "goals", item.schemes[s].goals.join('<br />'));
    var missions = "";
    Object.keys(item.schemes[s].missions).forEach(function(m) {
      missions += item.schemes[s].missions[m].mission + " ( " +
        item.schemes[s].missions[m].missionGoals.join(', ') + " ) <br />"
    });
    addTwoCellsInARow(table, "missions", missions);
    addTwoCellsInARow(table, "players", item.schemes[s].players.join('<br />'));
  });
  if (Object.keys(item.schemes).length <= 0) {
    p = document.createElement('p');
    p.innerText = "nothing to show";
    let s = document.getElementById("schemessection");
    s.appendChild(p);
  }


  if (Object.keys(item.norms).length <= 0) {
    p = document.createElement('p');
    p.innerText = "nothing to show";
    let s = document.getElementById("normssection");
    s.appendChild(p);
  } else {
    var table = createTable("normssection");
    Object.keys(item.norms).forEach(function(n) {
      addTwoCellsInARow(table, "norm", item.norms[n].norm + ": " +
        item.norms[n].role + " -> " + item.norms[n].type + " -> " + item.norms[n].mission);
    });
  }
}


/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
END OF FILE
* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
