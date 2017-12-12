/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
var app = {
    // Application Constructor
    initialize: function() {
        document.addEventListener('deviceready', this.onDeviceReady.bind(this), false);
    },

    // deviceready Event Handler
    //
    // Bind any cordova events here. Common events are:
    // 'pause', 'resume', etc.
    onDeviceReady: function() {
        this.receivedEvent('deviceready');
    },

    // Update DOM on a Received Event
    receivedEvent: function(id) {
        console.log('Received Event: ' + id);

        window.bitalino.onDeviceFound(deviceFound, function(err) { console.log("onDeviceFound err: " + err); });
        window.bitalino.onConnectionStateChanged(onConnectionStateChanged, function(err) { console.log("onConnectionStateChanged err: " + err); });
        window.bitalino.onDataAvailable(onDataAvailable, function(err) { console.log("onDataAvailable err: " + err); });

        //ask for permission to scan for BTH devices
        window.bitalino.askForPermission(function(result) { console.log("askForPermission: " + result); }, function(err) { console.log("askForPermission: " + err); navigator.app.exitApp();});
    }
};

app.initialize();

function enableScan(enable, timeInMs, sampleRate) {
    window.bitalino.enableScan(function(result) { console.log("enableScan: " + result);}, function(err) { console.log("enableScan: " + err)}, enable, timeInMs)
}

//callbacks
var address = "B0:B4:48:F0:C8:60"
function deviceFound(result){
    var identifier = result.address
    var name = result.name
    var type = result.communication
    var rssi = result.rssi

    console.log("onDeviceFound: " + JSON.stringify(result))

    //BTH
    // if(result === "20:16:12:21:98:47"){
    //     document.getElementById("address").innerHTML = "20:16:12:21:98:47"
    //     connect(result);
    // }

    //BLE
    if(identifier === "B0:B4:48:F0:C8:60"){
        document.getElementById("address").innerHTML = "B0:B4:48:F0:C8:60"
        address = "B0:B4:48:F0:C8:60"
        //connect(result);
    }
}

function onConnectionStateChanged(stateObject){
    var identifier = stateObject.address
    var state = stateObject.state

    console.log(identifier + " -> state: " + state)
    console.log(identifier + " -> state: " + JSON.stringify(stateObject))

    var stateName = "DISCONNECTED";

    switch (state) {
        case 0: //NO_CONNECTION
            stateName = "NO_CONNECTION"
            break;
        case 1: //LISTEN
            stateName = "LISTEN"
            break;
        case 2: //CONNECTING
            stateName = "CONNECTING"
            break;
        case 3: //CONNECTED
            stateName = "CONNECTED"
            break;
        case 4: //ACQUISITION_TRYING
            stateName = "ACQUISITION_TRYING"
            break;
        case 5: //ACQUISITION_OK
            stateName = "ACQUISITION_OK"
            break;
        case 6: //ACQUISITION_STOPPING
            stateName = "ACQUISITION_STOPPING"
            break;
        case 7: //DISCONNECTED
            stateName = "DISCONNECTED"
            break;
        case 8: //ENDED
            stateName = "ENDED"
            break;
        default:
            stateName = "unknown state"
    }

    console.log(stateName)

    document.getElementById("state").innerHTML = stateName
}

function onDataAvailable(frame){
    var identifier = frame.address
    var seqNumber = frame.sequence
    var digitalChannels = frame.digitalChannels
    var analogChannels = frame.analogChannels

    console.log("frame: " + JSON.stringify(frame))
}


//UI methods
document.getElementById("scan").onclick = function(){
    enableScan(true, 15000)
}

document.getElementById("scanForDeviceButton").onclick = function(){
    var address = "20:16:12:21:98:47" //BTH
    //var address = "24:71:89:45:D0:3F" //BLE
    //var address = "B0:B4:48:F0:C8:60" //BLE
    var sampleRate = 1 //1 Hz
    scanForDevice(address, 15000, sampleRate)
}

document.getElementById("connectButton").onclick = function(){
    //var address = "20:16:12:21:98:47" //BTH
    //var address = "B0:B4:48:F0:C8:60" //BLE
    connect(address)
}

document.getElementById("disconnectButton").onclick = function(){
    disconnect()
}

document.getElementById("startButton").onclick = function(){
    start([0,1,2,3,4,5], 100);
}

document.getElementById("stopButton").onclick = function(){
    stop();
}

var digital1Checked = false
document.getElementById("digital1").onclick = function () {
    if(digital1Checked) {
        document.getElementById("digital1").checked = false
        digital1Checked = false;
    }
    else{
        document.getElementById("digital1").checked = true
        digital1Checked = true;
    }}

var digital2Checked = false
document.getElementById("digital2").onclick = function () {
    if(digital2Checked) {
        document.getElementById("digital2").checked = false
        digital2Checked = false;
    }
    else{
        document.getElementById("digital2").checked = true
        digital2Checked = true;
    }

}

document.getElementById("triggerButton").onclick = function(){
    var digital1 = document.getElementById("digital1").checked ? 1 : 0
    var digital2 = document.getElementById("digital2").checked ? 1 : 0

    console.log("triggerButton: " + [digital1, digital2])

    trigger([digital1, digital2])
}

document.getElementById("stateButton").onclick = function(){
    console.log("stateButton")

    getState()
}

document.getElementById("descriptionButton").onclick = function(){
    console.log("descriptionButton")

    getVersion()
}

document.getElementById("batteryThresholdButton").onclick = function(){
    var value = document.getElementById("batteryThresholdSeekBar").value

    setBatteryThreshold(value)
}

document.getElementById("pwmButton").onclick = function(){
    var value = document.getElementById("pwmSeekBar").value

    setPwm(value)
}

//BITalino methods
function scanForDevice(address, timeInMs, sampleRate) {
    window.bitalino.scanForDevice(function(result) { console.log("scanForDevice: " + result);}, function(err) { console.log("scanForDevice: " + err)}, address, timeInMs, sampleRate)
}

function connect(address) {
    window.bitalino.connect(function(result) { console.log("connect: " + result);}, function(err) { console.log("connect: " + err)}, address)
}

function disconnect() {
    window.bitalino.disconnect(function(result) { console.log("disconnect: " + result);}, function(err) { console.log("v: " + err)})
}

function start(analogChannels, sampleRate) {
    window.bitalino.start(function(result) { console.log("start: " + result);}, function(err) { console.log("start: " + err)}, analogChannels, sampleRate)
}

function stop() {
    window.bitalino.stop(function(result) { console.log("stop: " + result);}, function(err) { console.log("stop: " + err)})
}

function getVersion() {
    window.bitalino.getVersion(
        function(result) {
            console.log("getVersion: " + JSON.stringify(result));

            document.getElementById("results").innerHTML = "Description: " + JSON.stringify(result)
        },
        function(err) {
            console.log("getVersion: " + err)
        }
    )
}

function setBatteryThreshold(value){
    window.bitalino.setBatteryThreshold(function(result) { console.log("setBatteryThreshold: " + result);}, function(err) { console.log("setBatteryThreshold: " + err)}, value)
}

function trigger(digitalChannels){
    window.bitalino.trigger(function(result) { console.log("trigger: " + result);}, function(err) { console.log("trigger: " + err)}, digitalChannels)
}

function getState(){
    window.bitalino.getState(
        function(result) {
            console.log("getState: " + JSON.stringify(result));

            document.getElementById("results").innerHTML = "State: " + JSON.stringify(result)
        },
        function(err) {
            console.log("getState: " + err)
        }
    )
}

function setPwm(pwmOutput){
    window.bitalino.setPwm(function(result) { console.log("setPwm: " + result);}, function(err) { console.log("setPwm: " + err)}, pwmOutput)
}
