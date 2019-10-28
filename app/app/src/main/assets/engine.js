log('Funziona!');
log('Funziona davvero!');

function log(message) {
    var log = document.getElementById("log");
    log.innerHTML = log.innerHTML + message + '\n';
}

var port;
var count = 0;

window.onmessage = function (e) {
    if (port == null) {
        port = e.ports[0];
        port.onmessage = processMessage;
        console.log("Received WebMessagePort from host: sending PING");
        port.postMessage("PING");
    }
}


function processMessage(msg) {
    var data = msg.data;

    log("Received message from host: " + data);

    var [command, payload] = data.split(' ');

    switch (command) {
        case 'PONG':
            console.log('Received PONG: sending widget tree');
            sendTree();
            break;

        case "CLICK":
            if (payload == 'button1') {
                count += 1;
                port.postMessage("TEXT-UPDATE text1/Il bottone è stato 'clickato' " + count + " volte");
            }
    }
}

function sendTree() {
    port.postMessage("CLEAR");
    port.postMessage("LINEAR-LAYOUT VERTICAL");
    port.postMessage("TEXT Viva il LinuxDay forevah!");
    port.postMessage("BUTTON Toucha, Toucha, Touch Me!/button1");
    port.postMessage("TEXT Il bottone è stato 'clickato' " + count + " volte/text1");
}
