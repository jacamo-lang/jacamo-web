const toastr = require('toastr')

/**
 * WEBSOCKTES FUNCTIONS
 */
var myWebSocket;

function connectToWS() {
  var endpoint = "ws://" + window.location.hostname + ":8026/ws/messages";
  if (myWebSocket !== undefined) {
    console.log("closing...");
    myWebSocket.close();
  }
  myWebSocket = new WebSocket(endpoint);
  myWebSocket.onmessage = function(event) {
    if (event.data.startsWith(".send")) {
      let data = event.data;
      let args = data.substring(5, data.length).replace("<","").replace(">","").split(",");
      toastr.info(args[1] + " " + args[2] + " " + args[3] + " " + args[4], { timeOut: 1000 });
      localStorage.setItem("agentArrow", args[2] + "->" + args[3] + "[label =\"" + args[4] + "\"]");
    }
  };
  myWebSocket.onopen = function(evt) {
    console.log("onopen.");
  };
  myWebSocket.onclose = function(evt) {
    console.log("onclose.");
  };
  myWebSocket.onerror = function(evt) {
    console.log("Error!");
  };
}

function sendMsg() {
  myWebSocket.send("message");
}

function closeConn() {
  myWebSocket.close();
}

connectToWS();

/**
 * EXPORTS
 */

window.connectToWS = connectToWS;

/**
 * END OF FILE
 */
