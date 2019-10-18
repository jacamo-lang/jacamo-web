/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
RETRIEVE DATA ABOUT ALL ORGANISATIONS
* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

/*Get Organisations */
function getOE() {
  var set = [];
  const Http = new XMLHttpRequest();
  Http.open("GET", "./oe");
  Http.send();
  Http.onreadystatechange = function() {
    if (this.readyState == 4 && this.status == 200) {
      set = JSON.parse(Http.responseText);
      updateMenu("nav-drawer", set);
      updateMenu("nav-drawer-frame", set);
    };
  };
};

function updateMenu(nav, set) {
  const selectedItem = new URL(location.href).searchParams.get('organisation');
  set.sort();
  set.forEach(function(n) {
    var lag = document.createElement('a');
    lag.setAttribute("href", "./organisation.html?organisation=" + n);
    if (selectedItem === n) {
      lag.innerHTML = "<h5><b>" + n + "</b></h5>";
      document.getElementById(nav).appendChild(lag);
    } else {
      lag.innerHTML = "<h5>" + n + "</h5>";
      document.getElementById(nav).appendChild(lag);
    }
  });
  var br = document.createElement("br");
  document.getElementById(nav).appendChild(br);
  document.getElementById(nav).appendChild(br);
  var lnew = document.createElement('a');
  lnew.setAttribute("href", "./oe_role_new.html");
  lnew.innerHTML = "create role";
  document.getElementById(nav).appendChild(lnew);
}

/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
NEW ORGANIZATIONAL ROLE
* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

/* create role: POST in "/{oename}/group/{groupname}" */
function newRole(org, gr) {
  /*
  TODO: The current method used window.open on the js, which is not showing the html to feedback the user after the post. Maybe a solution is changing it to a form using submit function
  var f = document.getElementById('TheForm');
  f.something.value = something;
  f.more.value = additional;
  f.other.value = misc;
  window.open('', 'TheWindow');
  f.submit();*/

  http = new XMLHttpRequest();
  var input = document.getElementById('orgGrRole').value;
  var lastDot = input.lastIndexOf('.');
  var firstPart = input.substring(0, lastDot);
  var role = input.substring(lastDot + 1);
  var firstDot = firstPart.indexOf('.');
  var org = firstPart.substring(0, firstDot);
  var group = firstPart.substring(firstDot + 1);

  http.open("POST", '/oe/' + org + '/group/' + group, false);
  http.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
  var data = "role=" + encodeURIComponent(role);
  http.send(data);
  window.location.assign('/oe.html');
}

/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
END OF FILE
* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
