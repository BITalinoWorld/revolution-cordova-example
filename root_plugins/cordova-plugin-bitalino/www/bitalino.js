/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
*/

var exec = require('cordova/exec'),
    cordova = require('cordova');

/**
 * This represents the mobile device, and provides properties for inspecting the model, version, UUID of the
 * phone, etc.
 * @constructor
 */
function BITalino() {
    
}

BITalino.prototype.askForPermission = function(onSuccess, onError)
{
    exec(onSuccess, onError, "BITalino", "askForPermission", []);
}

BITalino.prototype.enableScan = function(onSuccess, onError, enable, timeInMs)
{
    exec(onSuccess, onError, "BITalino", "enableScan", [enable, timeInMs]);
}

BITalino.prototype.scanForDevice = function(onSuccess, onError, identifier, timeInMs, sampleRate)
{
    exec(onSuccess, onError, "BITalino", "scanForDevice", [identifier, timeInMs, sampleRate]);
}

/*
 * Callbacks
 */
BITalino.prototype.onDeviceFound = function(deviceFound, onError)
{
    exec(deviceFound, onError, "BITalino", "onDeviceFound", []);
}

BITalino.prototype.onConnectionStateChanged = function(onSuccess, onError)
{
    exec(onSuccess, onError, "BITalino", "onConnectionStateChanged", []);
}

BITalino.prototype.onDataAvailable = function(onData, onError)
{
    exec(onData, onError, "BITalino", "onDataAvailable", []);
}

/*
 * BITalino
 */
BITalino.prototype.connect = function(onSuccess, onError, identifier)
{
    exec(onSuccess, onError, "BITalino", "connect", [identifier]);
}

BITalino.prototype.disconnect = function(onSuccess, onError)
{
    exec(onSuccess, onError, "BITalino", "disconnect", []);
}

BITalino.prototype.start = function(onSuccess, onError, analogChannels, sampleRate)
{
    exec(onSuccess, onError, "BITalino", "start", [analogChannels, sampleRate]);
}

BITalino.prototype.stop = function(onSuccess, onError)
{
    exec(onSuccess, onError, "BITalino", "stop", []);
}

BITalino.prototype.getVersion = function(onSuccess, onError)
{
    exec(onSuccess, onError, "BITalino", "getVersion", []);
}

BITalino.prototype.setBatteryThreshold = function(onSuccess, onError, value)
{
    exec(onSuccess, onError, "BITalino", "setBatteryThreshold", [value]);
}

BITalino.prototype.trigger = function(onSuccess, onError, digitalChannels)
{
    exec(onSuccess, onError, "BITalino", "trigger", [digitalChannels]);
}

BITalino.prototype.getState = function(onSuccess, onError)
{
    exec(onSuccess, onError, "BITalino", "getState", []);
}

BITalino.prototype.setPwm = function(onSuccess, onError, pwmOutput)
{
    exec(onSuccess, onError, "BITalino", "setPwm", [pwmOutput]);
}


module.exports = new BITalino();
