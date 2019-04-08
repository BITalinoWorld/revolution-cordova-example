# BITalino Plugin for Apache Cordova

This plugin enables the communication between an Android device and BITalino. It supports Classic Bluetooth and Bluetooth Low Energy. 

## Supported Platforms

* Android

## Limitations

 * BITalino BLE does not allow a high sampling rate.

# Installing

Install with Cordova cli

    $ cordova plugin add cordova-plugin-bitalino

# Examples

There is a [sample project](https://bitalino.com) included with the plugin.

# API

## Methods

- [bitalino.enableScan](#enableScan)
- [bitalino.scanForDevice](#scanForDevice)
- [bitalino.connect](#connect)
- [bitalino.disconnect](#disconnect)
- [bitalino.start](#start)
- [bitalino.stop](#stop)
- [bitalino.getVersion](#getVersion)
- [bitalino.setBatteryThreshold](#setBatteryThreshold)
- [bitalino.trigger](#trigger)
- [bitalino.getState](#getState)
- [bitalino.setPwm](#setPwm)

## enableScan

Enables the scan to start or stop the discovery of BITalino devices.

    bitalino.enableScan(onSuccess, onError, enable, timeInMs);

### Description

Function `enableScan` provides the capability to start or stop the scan of Bluetooth devices. If the enable argument is true the scan will start, otherwise if a scanning is occurring it will be stopped. The scan’s duration will be the value passed through the timeInMs parameter. Success will be called when the function is called with success. Failure is called when some parameter is wrongly passed, or if the function could not be called. The available devices are returned via `onDeviceFound` callback.

### Parameters

-   __onSuccess__: Success callback function that is invoked when the scan starts successfully.
-   __onError__: Error callback function, invoked when error occurs.
-   __enable__: Enable flag.
-   __timeInMs__: Scan period (in milliseconds).

## scanForDevice

Enables the scan of a specific BITalino device.

    bitalino.scanForDevice(onSuccess, onError, address, timeOut);

### Description

Function `scanForDevice` enables the scan for a specific Bluetooth device for a certain period. Success will be called if the device was found. Failure is called otherwise.

### Parameters

- __onSuccess__: Success callback function that is invoked if the device was found.
- __onError__: Error callback function, invoked when the device was not found, or some error has occurred.
- __address__: Device’s identifier (MAC Address).
- __timeOut__: Duration of the scan (in milliseconds).


## connect

Connect to a BITalino Device.

    bitalino.connect(onSuccess, onError, address);

### Description
Function `connect` tries to establish a connection with the BITalino device with the given identifier. Success will be called if the connection process can be started, failure will be called otherwise. The changes in the device’s connection state will be given using the `onConnectionStateChanged` callback.

### Parameters
- __onSuccess__: Success callback function that is invoked with the connection command was called with success.
-  __onError__: Error callback function, invoked when errors occur.
-  __address__: Device’s identifier (MAC Address).

## disconnect

Disconnect from the BITalino Device.

    bitalino.disconnect(onSuccess, onError);

### Description
Function `disconnect` allows the disconnection from the BITalino device. Success will be called if the disconnection could be triggered. Failure is called if the device is not connected or if some error occurs.


### Parameters
-   __onSuccess__: Success callback function that is invoked with the device can be disconnected.
-   __onError__: Error callback function, invoked when some error occurs or if the device is not connected.

## start

Starts a real-time acquisition with the given parameters.

    bitalino.start(onSuccess, onError, analogChannels, sampleRate);

### Description

Function `start` will start an acquisition with the given parameters. Success will be called if the instruction can be given with success. Failure will be called with some error occurs.

### Parameters
  

- __onSuccess__: Success callback function that is invoked if the start command could be fired.
- __onError__: Error callback function invoked when some error occurs.
- __analogChannels__: An array with the active channels.
-  __sampleRate__: The acquisition’s sampling rate.

## stop

Stops the acquisition.

    bitalino.stop(onSuccess, onError);

### Description

Function `stop` stops the acquisition occurring in the device. Success will be called if the command is given with success. Failure will be called if there is no acquisition occurring or if the device is disconnected.

### Parameters

-  __onSuccess__: Success callback function that is invoked if the stop command could be given.
-  __onError__: Error callback function invoked when some error occurs.

## getVersion

Gets the BITalino’s firmware version.

    bitalino.getVersion(onSuccess, onError);

### Description

Function `getVersion` allows the user to retrieve the BITalino’s firmware version. Success will be called when the version information is received. Failure will be called if some error occurs.

### Parameters
-  __onSuccess__: Success callback function that is invoked, returning the device’s version.
-   __onError__: Error callback function invoked when some error occurs.

## setBatteryThreshold

Sets a new battery threshold for the low-battery LED.

    bitalino.setBatteryThreshold(onSuccess, onError, value);

### Description

Function `setBatteryThreshold` allows the user to set a new battery’s threshold value for the BITalino device to show the low-battery LED indicator. Success will be called if the command can be given, failure will be called otherwise.

### Parameters
 
-   __onSuccess__: Success callback function that is invoked if the command could be given.
-   __onError__: Error callback function invoked when some error occurs.
-   __value__: new battery threshold value

## trigger

Assigns the digital outputs states.

    bitalino.trigger(onSuccess, onError, digitalChannels);

### Description

Function `trigger`  sets a new state for the BITalino’s output digital channels. Success will be called if the command could be called, failure will be called otherwise.

### Parameters
  

-  __onSuccess__: Success callback function that is invoked if the command could be given.
-  __onError__: Error callback function invoked when some error occurs.
-  __digitalChannels__: an array with the digital channels states (if enabled it should be set as 1, otherwise it should be set as 0).

## getState

Gets the device’s current state.

    bitalino.getState(onSuccess, onError);

### Description

Function `getState` gets the current state of all BITalino’s featured components: digital channels, analog channels, battery, PWM (analog output) and battery threshold. Success will be called when the current state is returned. Failure will be called with some error occurs.

### Parameters

- __onSuccess__: Success callback function that is invoked when the device returns its current state, returning a JSONObject that is described below.
-  __onError__: Error callback function invoked when some error occurs.

### JSONObject State

    {“address”:“B0:B4:48:F0:C8:60",“digitalChannels”:[1,1,0,0],“analogChannels”:[499,621,522,0,516,512],“analogOutput”:0,“battery”:585,“batThreshold”:0}

__Elements__:
-  __address__: device’s MAC Address
-  __digitalChannels__: current state of the digital channels
-  __analogChannels__: current state of the analog channels
-  __analogOutput__: current PWM value
-  __battery__: current battery level
-  __batThreshold__: current battery threshold

## setPwm

Assigns the analog (PWM) output value.

    bitalino.setPwm(onSuccess, onError, pwmOut);

### Description
Function `setPWM` enables setting a new PWM output value. Success will be called if the command can be given. Failure will be called if some error occurs.

### Parameters
  

-   __onSuccess__: Success callback function that is invoked if the command could be given.
-   __onError__: Error callback function invoked when some error occurs.
-   __pwmOutput__: PWM output


## Android’s Auxiliary methods

- [bitalino.askForPermission](#askForPermission)

## askForPermission

Tests and asks for permission to use the Coarse Location functionality of the mobile device, needed to scan Bluetooth Devices.

    bitalino.askForPermission(onSuccess, onError);

### Description

Function `askForPermission` allows the user to ensure if he has granted the application the possibility to use the needed permissions. If the user has not granted the permission yet, the plugin will ask for it. Success will be called if the permission is granted. Failure will be called otherwise or if some error occurs.

### Parameters

-  __onSuccess__: Success callback function that is invoked when the asked permissions are granted.
-  __onError__: Error callback function invoked if the user did not grant the asked permission or when some error occurs.

## Callbacks

- [bitalino.onDeviceFound](#onDeviceFound)
- [bitalino.onConnectionStateChanged](#onConnectionStateChanged)
- [bitalino.onDataAvailable](#onDataAvailable)

## onDeviceFound

Callback that retrieves the Bluetooth Devices found during the scan.

    bitalino.onDeviceFound(device, onError);

### Description

Function `onDeviceFound` is a callback function that is set to be a channel of communication between the plugin and the application, sending the found devices during the scan. Success (device) will be called every time a new BITalino device is found by the mobile device, this data is structured in a JSON object as described below. Failure will be called if there was any error parsing the data.

### Parameters

-   __device__: JSONObject that contains the current connection state
-   __onError__: Error callback function invoked when some parsing error occurs.

### JSONObject data

    {“address”:“B0:B4:48:F0:C8:60",“name”:“BITalino BLE”,“communication”:“BLE”,“rssi”:-32}

__Elements__:

-  __address__: device’s MAC Address
-  __name__: device’s name
-  __communication__: device’s type of communication (BTH, BLE or DUAL)
-  __rssi__: signal strength indicator

## onConnectionStateChanged

Callback that retrieves the states of the connection from the plugin.

    bitalino.onConnectionStateChanged(state, onError);

### Description

Function `onDeviceFound` is a callback function that is set to be a channel of communication between the plugin and the application, sending the found devices during the scan. Success (device) will be called every time a new BITalino device is found by the mobile device, this data is structured in a JSON object as described below. Failure will be called if there was any error parsing the data.

Function `onConnectionStateChanged` is a callback function that is set to be a channel of communication between the plugin and the application, sending the current state of the connection every time it changes. Success (state) will be called every time the connection state changes, as this data is structured in a JSON object as described below. Failure will be called if there is any error parsing the data.

### Parameters

-  __state__: JSONObject that contains the current connection state
-  __onError__: Error callback function invoked when some parsing error occurs.

### JSONObject data

    {“address”:“B0:B4:48:F0:C8:60",“state”:3}

__Elements__:
-  __identifier__: device’s MAC Address
-  __state__: current connection state ordinal 
--  __0__: NO CONNECTION
-- __1__: LISTEN
-- __2__: CONNECTING
-- __3__: CONNECTED
-- __4__: ACQUISITION TRYING
-- __5__: ACQUISITION OK
-- __6__: ACQUISTIION STOPPING
-- __7__: DISCONNECTED
-- __8__: ENDED

## onDataAvailable

Callback that retrieves the data from the plugin.

    bitalino.onDataAvailable(data, onError);

### Description

Function `onDataAvailable` is a callback function that is set to be a channel of communication between the plugin and the application, sending the acquired data through it. Success (data) will be called every time new data is available, as this data is structured in a JSON object as described below. Failure will be called if there is any error parsing the data.

### Parameters
  
- __data__: JSONObject that contains the data frame
- __onError__: Error callback function invoked when some parsing error occurs.

### JSONObject data
    {“address”:“B0:B4:48:F0:C8:60",“sequence”:4,“digitalChannels”:[1,1,0,0],“analogChannels”:[495,514,0,521,39,0]}

__Elements__:
  

- __identifier__: device’s MAC Address
- __sequence__: frame’s sequence number (from 0 to 15)
- __digitalChannels__: digital channels current state
-  __analogChannels__: analog channels received values

## Feedback

Try the code. If you find an problem or missing feature  file an issue.

## Acknowledgments

![alt text](https://cordis.europa.eu/docs/results/h2020/690/690367_PS/polycare-summary-for-publication-pic2.png "Project Logo")

This project has received funding from the European Union’s Horizon 2020 research and innovation programme under grant agreement N°690367 (http://www.polycare-project.com/)