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
      /*do something*/
    }
    console.log("received: " + event.data);
  }
  myWebSocket.onopen = function(evt) {
    console.log("onopen.");
  }
  myWebSocket.onclose = function(evt) {
    console.log("onclose.");
  }
  myWebSocket.onerror = function(evt) {
    console.log("Error!");
  }
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
