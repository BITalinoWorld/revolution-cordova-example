/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
*/
package info.plux.cordova.bitalino;

import java.util.TimeZone;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.util.Log;

import android.os.*;

import android.bluetooth.BluetoothDevice;

import info.plux.pluxapi.BTHDeviceScan;
import info.plux.pluxapi.Communication;
import info.plux.pluxapi.Constants;
import info.plux.pluxapi.bitalino.*;
import info.plux.pluxapi.bitalino.bth.OnBITalinoDataAvailable;

import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.apache.cordova.CordovaInterface;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

import android.provider.Settings;
import static info.plux.pluxapi.Constants.*;


public class BITalino extends CordovaPlugin implements OnBITalinoDataAvailable{
    public static final String TAG = "BITalino";

    //Constants
    private static final String ACTION_ASK_FOR_PERMISSION           = "askForPermission";
    private static final String ACTION_ENABLE_SCAN        		    = "enableScan";
    private static final String ACTION_SCAN_FOR_DEVICE     		    = "scanForDevice";

    private static final String ACTION_ON_DEVICE_FOUND  		    = "onDeviceFound";
    private static final String ACTION_ON_CONNECTION_STATE_CHANGED  = "onConnectionStateChanged";
    private static final String ACTION_ON_DATA_AVAILABLE            = "onDataAvailable";
    private static final String ACTION_ON_REPLY_AVAILABLE           = "onReplyAvailable";

    private static final String ACTION_CONNECT  			        = "connect";
    private static final String ACTION_DISCONNECT		            = "disconnect";
    private static final String ACTION_START		                = "start";
    private static final String ACTION_STOP                         = "stop";
    private static final String ACTION_GET_VERSION                  = "getVersion";
    private static final String ACTION_SET_BATTERY_THRESHOLD        = "setBatteryThreshold";
    private static final String ACTION_TRIGGER                      = "trigger";
    private static final String ACTION_GET_STATE                    = "getState";
    private static final String ACTION_SET_PWM                      = "setPwm";

    private final String NEUTRAL_IDENTIFIER = "00:00:00:00:00:00";

    private BluetoothDevice bluetoothDevice;
    private int rssi = Integer.MIN_VALUE;
    private BITalinoCommunication bitalino;
    private String deviceIdentifier = NEUTRAL_IDENTIFIER;
    private final int sampleRate = 100;
    private int[] selectedChannels = new int[]{0,1,2,3,4,5};

    private BTHDeviceScan bthDeviceScan;
    private List<BluetoothDevice> deviceList = new ArrayList<BluetoothDevice>();
    
    private CallbackContext askForPermissionCallback;

    private CallbackContext onDeviceFoundCallback;
    private CallbackContext onConnectionStateChangedCallback;
    private CallbackContext onDataAvailableCallback;
    private CallbackContext onReplyAvailableCallback;

    private boolean isConnectWorkflowEnabled = false;

    /**
     * Constructor.
     */
    public BITalino() {
        Log.d(TAG, "BITalino()");
    }

    /**
     * Sets the context of the Command. This can then be used to do things like
     * get file paths associated with the Activity.
     *
     * @param cordova The context of the main Activity.
     * @param webView The CordovaWebView Cordova is running in.
     */
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);

        Log.d(TAG, "initialize()");

        cordova.getActivity().registerReceiver(updateReceiver, makeUpdateReceiverIntentFilter());

        bthDeviceScan = new BTHDeviceScan(cordova.getActivity());
    }
    
    /**
     * Executes the request and returns PluginResult.
     *
     * @param action            The action to execute.
     * @param args              JSONArry of arguments for the plugin.
     * @param callbackContext   The callback id used when calling back into JavaScript.
     * @return                  True if the action was valid, false if not.
     */
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {

        if (ACTION_ASK_FOR_PERMISSION.equals(action)){
            askForPermission(args, callbackContext);
        }
        else if (ACTION_ENABLE_SCAN.equals(action)){
            enableScan(args, callbackContext);
        }
        else if (ACTION_SCAN_FOR_DEVICE.equals(action)){
            scanForDevice(args, callbackContext);
        }
        else if (ACTION_ON_DEVICE_FOUND.equals(action)){
            onDeviceFound(args, callbackContext);
        }
        else if (ACTION_ON_CONNECTION_STATE_CHANGED.equals(action)){
            onConnectionStateChanged(args, callbackContext);
        }
        else if (ACTION_ON_DATA_AVAILABLE.equals(action)){
            onDataAvailable(args, callbackContext);
        }
        else if (ACTION_ON_REPLY_AVAILABLE.equals(action)){
            onReplyAvailable(args, callbackContext);
        }
        else if (ACTION_CONNECT.equals(action)){
            connect(args, callbackContext);
        }
        else if(ACTION_DISCONNECT.equals(action)) {
            disconnect(args, callbackContext);
        }
        else if(ACTION_START.equals(action)) {
            start(args, callbackContext);
        }
        else if(ACTION_STOP.equals(action)) {
            stop(args, callbackContext);
        }
        else if(ACTION_GET_VERSION.equals(action)) {
            getVersion(args, callbackContext);
        }
        else if(ACTION_SET_BATTERY_THRESHOLD.equals(action)) {
            setBatteryThreshold(args, callbackContext);
        }
        else if(ACTION_TRIGGER.equals(action)) {
            trigger(args, callbackContext);
        }
        else if(ACTION_GET_STATE.equals(action)) {
            getState(args, callbackContext);
        }
        else if(ACTION_SET_PWM.equals(action)) {
            setPWM(args, callbackContext);
        }
        else
        {
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.INVALID_ACTION));
            return false;
        }
        return true;
    }

    
    /**
     * Called when the activity is becoming visible to the user.
     */
    @Override
    public void onStart() {
        super.onStart();

        Log.d(TAG, "onStart()");
    }
    
    
    /**
     * The final call you receive before your activity is destroyed.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d(TAG, "onDestroy()");
        
        cordova.getActivity().unregisterReceiver(updateReceiver);

        if(bthDeviceScan != null){
            bthDeviceScan.closeScanReceiver();
        }
    }

    @Override
    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) throws JSONException {

        if(cordova.hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION) || cordova.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)){
            askForPermissionCallback.sendPluginResult(new PluginResult(PluginResult.Status.OK));
        }
        else {
            Log.d(TAG, "All permissions should be granted");
            askForPermissionCallback.sendPluginResult(new PluginResult(PluginResult.Status.ERROR));
        }
    }

    @Override
    public void onBITalinoDataAvailable(BITalinoFrame frame) {

        JSONObject data = null;
        try {
            data = frameToJSONObject(frame);
        }
        catch (JSONException e){
            PluginResult result = new PluginResult(PluginResult.Status.ERROR, e.getMessage());
            result.setKeepCallback(true);
            onDataAvailableCallback.sendPluginResult(result);
            return;
        }

        PluginResult result = new PluginResult(PluginResult.Status.OK, data);
        result.setKeepCallback(true);
        onDataAvailableCallback.sendPluginResult(result);
    }
    
    private final BroadcastReceiver updateReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if(ACTION_STATE_CHANGED.equals(action)){
                String identifier = intent.getStringExtra(IDENTIFIER);
                Constants.States state = Constants.States.getStates(intent.getIntExtra(EXTRA_STATE_CHANGED, 0));

                Log.i(TAG, identifier + " -> " + state.name());

                PluginResult result = new PluginResult(PluginResult.Status.OK, state.ordinal());
                result.setKeepCallback(true);
                onConnectionStateChangedCallback.sendPluginResult(result);
            }
            else if(ACTION_DATA_AVAILABLE.equals(action)){
                if(intent.hasExtra(EXTRA_DATA)){
                    Parcelable parcelable = intent.getParcelableExtra(EXTRA_DATA);
                    if(parcelable.getClass().equals(BITalinoFrame.class)){ //BITalino
                        BITalinoFrame frame = (BITalinoFrame) parcelable;

                        JSONObject data = null;
                        try {
                            data = frameToJSONObject(frame);
                        }
                        catch (JSONException e){
                            PluginResult result = new PluginResult(PluginResult.Status.ERROR, e.getMessage());
                            result.setKeepCallback(true);
                            onDataAvailableCallback.sendPluginResult(result);
                            return;
                        }

                        PluginResult result = new PluginResult(PluginResult.Status.OK, data);
                        result.setKeepCallback(true);
                        onDataAvailableCallback.sendPluginResult(result);
                    }
                }
            }
            else if(ACTION_COMMAND_REPLY.equals(action)){
                String identifier = intent.getStringExtra(IDENTIFIER);

                if(intent.hasExtra(EXTRA_COMMAND_REPLY) && (intent.getParcelableExtra(EXTRA_COMMAND_REPLY) != null)){
                    Parcelable parcelable = intent.getParcelableExtra(EXTRA_COMMAND_REPLY);
                    if(parcelable.getClass().equals(BITalinoState.class)){ //BITalino
                        Log.d(TAG, identifier + " -> " + parcelable.toString());

                        JSONArray reply = new JSONArray();
                        reply.put(1);
                        reply.put(parcelable.toString());

                        PluginResult result = new PluginResult(PluginResult.Status.OK, reply);
                        result.setKeepCallback(true);
                        onReplyAvailableCallback.sendPluginResult(result);
                    }
                    else if(parcelable.getClass().equals(BITalinoDescription.class)){ //BITalino
                        boolean isBITalino2 = ((BITalinoDescription)parcelable).isBITalino2();
                        Log.d(TAG, identifier + " -> isBITalino2: " + isBITalino2 + "; FwVersion: " + String.valueOf(((BITalinoDescription)parcelable).getFwVersion()));

                        JSONArray reply = new JSONArray();
                        reply.put(0);
                        reply.put("isBITalino2: " + isBITalino2 + "; FwVersion: " + String.valueOf(((BITalinoDescription)parcelable).getFwVersion()));

                        PluginResult result = new PluginResult(PluginResult.Status.OK, reply);
                        result.setKeepCallback(true);
                        onReplyAvailableCallback.sendPluginResult(result);

                        if(isConnectWorkflowEnabled){
                            start(onReplyAvailableCallback);
                        }

                    }
                }
            }
            else if(action.equals(Constants.ACTION_MESSAGE_SCAN)){
                BluetoothDevice bluetoothDevice = intent.getParcelableExtra(Constants.EXTRA_DEVICE_SCAN);
                int rssi = intent.getIntExtra(Constants.EXTRA_DEVICE_RSSI, Integer.MIN_VALUE);

                deviceList.add(bluetoothDevice);

                if(!isConnectWorkflowEnabled) {
                    PluginResult result = new PluginResult(PluginResult.Status.OK, bluetoothDevice.getAddress());
                    result.setKeepCallback(true);
                    onDeviceFoundCallback.sendPluginResult(result);
                }

            }
        }
    };

    private IntentFilter makeUpdateReceiverIntentFilter(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_STATE_CHANGED);
        intentFilter.addAction(ACTION_DATA_AVAILABLE);
        intentFilter.addAction(ACTION_EVENT_AVAILABLE);
        intentFilter.addAction(ACTION_DEVICE_READY);
        intentFilter.addAction(ACTION_COMMAND_REPLY);
        intentFilter.addAction(ACTION_MESSAGE_SCAN);
        return  intentFilter;
    }



    //--------------------------------------------------------------------------
    // LOCAL METHODS
    //--------------------------------------------------------------------------

    public void askForPermission (JSONArray args, CallbackContext callbackCtx){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(!cordova.hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION) || !cordova.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)){
                cordova.requestPermissions(this, 1, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION});

                askForPermissionCallback = callbackCtx;

                PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
                result.setKeepCallback(true);
                callbackCtx.sendPluginResult(result);
            }
            else{
                PluginResult result = new PluginResult(PluginResult.Status.OK);
            }
        }
        else {
            PluginResult result = new PluginResult(PluginResult.Status.OK);
        }
    }

    private void enableScan(JSONArray args, CallbackContext callbackCtx) {
        try{
            isConnectWorkflowEnabled = false;

            final boolean enable = args.getBoolean(0);
            final long timeInMs = args.getLong(1);

            if (enable) {
                deviceList.clear();
                // Stops scanning after a pre-defined scan period.
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        bthDeviceScan.stopScan();
                    }
                }, timeInMs);

                bthDeviceScan.doDiscovery();
            } else {
                bthDeviceScan.stopScan();
            }

            callbackCtx.sendPluginResult(new PluginResult(PluginResult.Status.OK));
        }
        catch(Exception e) {
            Log.e(TAG, "Error on scanDevice: " + e.getMessage());
            callbackCtx.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, e.getMessage()));
        }
    }

    private void scanForDevice(JSONArray args, final CallbackContext callbackCtx) {
        try{
            isConnectWorkflowEnabled = true;

            final String identifier = args.getString(0);
            final long timeInMs = args.getLong(1);

            Log.d(TAG, "identifier: " + identifier);
            Log.d(TAG, "timeInMs: " + timeInMs);

            deviceList.clear();
            // Stops scanning after a pre-defined scan period.

            new Handler().postDelayed(new Runnable() {
                   @Override
                   public void run() {
                       bthDeviceScan.stopScan();

                       if(getBluetoothDevice(identifier) != null){
                           PluginResult result = new PluginResult(PluginResult.Status.OK, "The device " + identifier + " was found.");
                           result.setKeepCallback(true);
                           callbackCtx.sendPluginResult(result);

                           connect(identifier, callbackCtx);
                       }
                       else{
                           callbackCtx.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, "The device " + identifier + " was not found."));
                       }
                   }
            }, timeInMs);

            bthDeviceScan.doDiscovery();
        }
        catch(Exception e) {
            Log.e(TAG, "Error on scanDevice: " + e.getMessage());
            callbackCtx.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, e.getMessage()));
        }
    }

    private void connect(JSONArray args, CallbackContext callbackCtx) {
        try{
            final String identifier = args.getString(0);

            BluetoothDevice device = getBluetoothDevice(identifier);

            if(device == null){
                callbackCtx.sendPluginResult(new PluginResult(PluginResult.Status.ERROR));
                return;
            }

            try {
                if(bitalino == null) {
                    bitalino = new BITalinoCommunicationFactory().getCommunication(Communication.getById(device.getType()), cordova.getActivity(), this);
                }

                bitalino.connect(identifier);
            } catch (BITalinoException e) {
                e.printStackTrace();
                callbackCtx.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, e.getMessage()));
            }

            callbackCtx.sendPluginResult(new PluginResult(PluginResult.Status.OK));
        }
        catch(Exception e) {
            Log.e(TAG, "Error on connect: " + e.getMessage());
            callbackCtx.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, e.getMessage()));
        }
    }

    private void connect(String identifier, CallbackContext callbackCtx) {
        try{
            BluetoothDevice device = getBluetoothDevice(identifier);

            if(device == null){
                callbackCtx.sendPluginResult(new PluginResult(PluginResult.Status.ERROR));
                return;
            }

            try {
                bitalino = new BITalinoCommunicationFactory().getCommunication(Communication.getById(device.getType()), cordova.getActivity(), this);

                bitalino.connect(identifier);
            } catch (BITalinoException e) {
                e.printStackTrace();
                callbackCtx.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, e.getMessage()));
            }

            callbackCtx.sendPluginResult(new PluginResult(PluginResult.Status.OK));
        }
        catch(Exception e) {
            Log.e(TAG, "Error on connect: " + e.getMessage());
            callbackCtx.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, e.getMessage()));
        }
    }

    private void disconnect(JSONArray args, CallbackContext callbackCtx) {
        try{

            try {
                bitalino.closeReceivers();

                bitalino.disconnect();
            } catch (BITalinoException e) {
                e.printStackTrace();

                callbackCtx.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, e.getMessage()));
            }

            callbackCtx.sendPluginResult(new PluginResult(PluginResult.Status.OK));
        }
        catch(Exception e) {
            Log.e(TAG, "Error on connect: " + e.getMessage());
            callbackCtx.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, e.getMessage()));
        }
    }

    public void start(JSONArray args, CallbackContext callbackCtx) {
        try{

            JSONArray channelsArray = args.getJSONArray(0);

            final int[] analogChannels = new int[channelsArray.length()];
            for(int i = 0 ; i < channelsArray.length(); i++) {
                analogChannels[i] = channelsArray.getInt(i);
            }

            final int sampleRate = args.getInt(1);

            try {
                bitalino.start(analogChannels, sampleRate);
            } catch (BITalinoException e) {
                e.printStackTrace();
                callbackCtx.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, e.getMessage()));
            }

            callbackCtx.sendPluginResult(new PluginResult(PluginResult.Status.OK));
        }
        catch(Exception e) {
            Log.e(TAG, "Error on connect: " + e.getMessage());
            callbackCtx.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, e.getMessage()));
        }
    }

    public void start(CallbackContext callbackCtx) {
        try{

            final int sampleRate = 100;
            final int[] analogChannels = new int[]{0,1,2,3,4,5};

            try {
                bitalino.start(analogChannels, sampleRate);
            } catch (BITalinoException e) {
                PluginResult result = new PluginResult(PluginResult.Status.ERROR, e.getMessage());
                result.setKeepCallback(true);
                callbackCtx.sendPluginResult(result);
            }

            PluginResult result = new PluginResult(PluginResult.Status.OK);
            result.setKeepCallback(true);
            callbackCtx.sendPluginResult(result);
        }
        catch(Exception e) {
            Log.e(TAG, "Error on connect: " + e.getMessage());
            PluginResult result = new PluginResult(PluginResult.Status.ERROR, e.getMessage());
            result.setKeepCallback(true);
            callbackCtx.sendPluginResult(result);
        }
    }

    public void stop(JSONArray args, CallbackContext callbackCtx) {
        try{
            try {
                bitalino.stop();
            } catch (BITalinoException e) {
                e.printStackTrace();
                callbackCtx.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, e.getMessage()));
            }

            callbackCtx.sendPluginResult(new PluginResult(PluginResult.Status.OK));
        }
        catch(Exception e) {
            Log.e(TAG, "Error on connect: " + e.getMessage());
            callbackCtx.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, e.getMessage()));
        }
    }

    public void getVersion(JSONArray args, CallbackContext callbackCtx) {
        try{
            try {
                bitalino.getVersion();
            } catch (BITalinoException e) {
                e.printStackTrace();
                callbackCtx.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, e.getMessage()));
            }

            callbackCtx.sendPluginResult(new PluginResult(PluginResult.Status.OK));
        }
        catch(Exception e) {
            Log.e(TAG, "Error on connect: " + e.getMessage());
            callbackCtx.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, e.getMessage()));
        }
    }

    public void setBatteryThreshold(JSONArray args, CallbackContext callbackCtx) {
        try{
            final int batteryThreshold = args.getInt(0);

            try {
                bitalino.battery(batteryThreshold);
            } catch (BITalinoException e) {
                e.printStackTrace();
                callbackCtx.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, e.getMessage()));
            }

            callbackCtx.sendPluginResult(new PluginResult(PluginResult.Status.OK));
        }
        catch(Exception e) {
            Log.e(TAG, "Error on connect: " + e.getMessage());
            callbackCtx.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, e.getMessage()));
        }
    }

    public void trigger(JSONArray args, CallbackContext callbackCtx) {
        try{
            JSONArray channelsArray = args.getJSONArray(0);

            final int[] digitalChannels = new int[channelsArray.length()];
            for(int i = 0 ; i < channelsArray.length(); i++) {
                digitalChannels[i] = channelsArray.getInt(i);
            }


            try {
                bitalino.trigger(digitalChannels);
            } catch (BITalinoException e) {
                e.printStackTrace();
                callbackCtx.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, e.getMessage()));
            }

            callbackCtx.sendPluginResult(new PluginResult(PluginResult.Status.OK));
        }
        catch(Exception e) {
            Log.e(TAG, "Error on connect: " + e.getMessage());
            callbackCtx.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, e.getMessage()));
        }
    }

    public void getState(JSONArray args, CallbackContext callbackCtx) {
        try{
            try {
                bitalino.state();
            } catch (BITalinoException e) {
                e.printStackTrace();
                callbackCtx.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, e.getMessage()));
            }

            callbackCtx.sendPluginResult(new PluginResult(PluginResult.Status.OK));
        }
        catch(Exception e) {
            Log.e(TAG, "Error on connect: " + e.getMessage());
            callbackCtx.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, e.getMessage()));
        }
    }

    public void setPWM(JSONArray args, CallbackContext callbackCtx) {
        try{
            final int pwm = args.getInt(0);

            try {
                bitalino.pwm(pwm);
            } catch (BITalinoException e) {
                e.printStackTrace();
                callbackCtx.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, e.getMessage()));
            }

            callbackCtx.sendPluginResult(new PluginResult(PluginResult.Status.OK));
        }
        catch(Exception e) {
            Log.e(TAG, "Error on connect: " + e.getMessage());
            callbackCtx.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, e.getMessage()));
        }
    }

    /*
     * Callbacks
     */

    public void onDeviceFound(JSONArray args, CallbackContext callbackCtx) {
        onDeviceFoundCallback = callbackCtx;

        PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
        result.setKeepCallback(true);
        callbackCtx.sendPluginResult(result);
    }

    public void onConnectionStateChanged(JSONArray args, CallbackContext callbackCtx) {
        onConnectionStateChangedCallback = callbackCtx;

        PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
        result.setKeepCallback(true);
        callbackCtx.sendPluginResult(result);
    }

    public void onDataAvailable(JSONArray args, CallbackContext callbackCtx) {
        onDataAvailableCallback = callbackCtx;

        PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
        result.setKeepCallback(true);
        callbackCtx.sendPluginResult(result);
    }

    public void onReplyAvailable(JSONArray args, CallbackContext callbackCtx) {
        onReplyAvailableCallback = callbackCtx;

        PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
        result.setKeepCallback(true);
        callbackCtx.sendPluginResult(result);
    }

    /*
     * Auxiliary methods
     */
    private BluetoothDevice getBluetoothDevice(String identifier){
        for(BluetoothDevice device: deviceList){
            if(device.getAddress().equals(identifier)){
                return device;
            }
        }

        return null;
    }

    private JSONObject frameToJSONObject(BITalinoFrame frame) throws JSONException{
        JSONObject object = new JSONObject();
        object.put("address",frame.getIdentifier());
        object.put("sequence", frame.getSequence());

        JSONArray digitalJSONArray = new JSONArray();
        for(int j = 0; j < frame.getDigitalArray().length; j++){
            digitalJSONArray.put(frame.getDigitalArray()[j]);
        }
        object.put("digitalChannels", digitalJSONArray);

        JSONArray analogJSONArray = new JSONArray();
        for(int j = 0; j < frame.getAnalogArray().length; j++){
            analogJSONArray.put(frame.getAnalogArray()[j]);
        }
        object.put("analogChannels", analogJSONArray);

        return object;
    }

}
