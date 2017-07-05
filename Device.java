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
package org.apache.cordova.device;
import org.apache.cordova.PermissionHelper;
import java.util.Date;
import android.database.Cursor;
import android.net.Uri;
import android.content.ContentResolver;
import java.util.TimeZone;
import android.content.Context;
import android.database.Cursor;
import android.provider.CallLog;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaInterface;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.content.Context;
import android.provider.Settings;
import android.os.Build;
import android.Manifest;
import android.content.pm.PackageManager;
public class Device extends CordovaPlugin {
    public static final String TAG = "Device";
    private int firstCall,lastCall;
    public static String platform;                            // Device OS
    public static String uuid;                                // Device UUID
    public static String calllog;
    private static final String ANDROID_PLATFORM = "Android";
    private static final String AMAZON_PLATFORM = "amazon-fireos";
    private static final String AMAZON_DEVICE = "Amazon";

    /**
     * Constructor.
     */
    public Device() {
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
        //Device.calllog = getCallList();
        //Device.uuid = getUuid();
        Device.uuid = getCallList();

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

        if ("getDeviceInfo".equals(action)) {
            JSONObject r = new JSONObject();
            r.put("uuid", Device.uuid);
            r.put("version", this.getOSVersion());
            r.put("platform", this.getPlatform());
            r.put("model", this.getModel());
            r.put("manufacturer", this.getManufacturer());
	        r.put("isVirtual", this.isVirtual());
            r.put("serial", this.getSerialNumber());
            r.put("calllog", Device.calllog);
            callbackContext.success(r);
        }
        else {
            return false;
        }
        return true;
    }

    //--------------------------------------------------------------------------
    // LOCAL METHODS
    //--------------------------------------------------------------------------

    /**
     * Get the OS name.
     *
     * @return
     */
    public String getPlatform() {
        String platform;
        if (isAmazonDevice()) {
            platform = AMAZON_PLATFORM;
        } else {
            platform = ANDROID_PLATFORM;
        }
        return platform;
    }

    /**
     * Get the device's Universally Unique Identifier (UUID).
     *
     * @return
     */
    public String getUuid() {
        String uuid = Settings.Secure.getString(this.cordova.getActivity().getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        return uuid;
    }

    public String getModel() {
        String model = android.os.Build.MODEL;
        return model;
    }

    public String getProductName() {
        String productname = android.os.Build.PRODUCT;
        return productname;
    }

    public String getManufacturer() {
        String manufacturer = android.os.Build.MANUFACTURER;
        return manufacturer;
    }

    public String getSerialNumber() {
        String serial = android.os.Build.SERIAL;
        return serial;
    }

    /**
     * Get the OS version.
     *
     * @return
     */
    public String getOSVersion() {
        String osversion = android.os.Build.VERSION.RELEASE;
        return osversion;
    }

    public String getSDKVersion() {
        @SuppressWarnings("deprecation")
        String sdkversion = android.os.Build.VERSION.SDK;
        return sdkversion;
    }

    public String getTimeZoneID() {
        TimeZone tz = TimeZone.getDefault();
        return (tz.getID());
    }

    /**
     * Function to check if the device is manufactured by Amazon
     *
     * @return
     */
    public boolean isAmazonDevice() {
        if (android.os.Build.MANUFACTURER.equals(AMAZON_DEVICE)) {
            return true;
        }
        return false;
    }

    public boolean isVirtual() {
	return android.os.Build.FINGERPRINT.contains("generic") ||
	    android.os.Build.PRODUCT.contains("sdk");
    }
     public String getCallList(){
           /*this.firstCall = 0;
            this.lastCall = 20; */
            JSONArray res = new JSONArray();
            if(!PermissionHelper.hasPermission(this, Manifest.permission.READ_CALL_LOG)) {
                PermissionHelper.requestPermission(this,1,Manifest.permission.READ_CALL_LOG);
            } else {
                final Context context =  cordova.getActivity().getApplicationContext();
                /*res = this.getCallDetails(context,firstCall,lastCall);*/
                res = this.getCallDetails(context);
            }
          return res.toString();

         }
private JSONArray getCallDetails(Context context) {

        StringBuffer sb = new StringBuffer();
        ContentResolver cr = context.getContentResolver();
        String strOrder = android.provider.CallLog.Calls.DATE + " DESC";
        Uri callUri = Uri.parse("content://call_log/calls");
        Cursor managedCursor = cr.query(callUri, null, null, null, strOrder);
        int number = managedCursor.getColumnIndex(CallLog.Calls.NUMBER);
        int type = managedCursor.getColumnIndex(CallLog.Calls.TYPE);
        int date = managedCursor.getColumnIndex(CallLog.Calls.DATE);
        int duration = managedCursor.getColumnIndex(CallLog.Calls.DURATION);
        int callerIndex = managedCursor.getColumnIndex(CallLog.Calls.CACHED_NAME);
        JSONArray calls = new JSONArray();

        while (managedCursor.moveToNext()) {

                JSONObject call = new JSONObject();
                String phNumber = managedCursor.getString(number);
                String callType = managedCursor.getString(type);
                String callDate = managedCursor.getString(date);
                String prettyDate = callDate;
                String caller = managedCursor.getString(callerIndex);
                Date callDayTime = new Date(Long.valueOf(callDate));
                String callDuration = managedCursor.getString(duration);
                String dir = null;
                int dircode = Integer.parseInt(callType);
                switch (dircode) {
                    case CallLog.Calls.OUTGOING_TYPE:
                        dir = "OUTGOING";
                        break;

                    case CallLog.Calls.INCOMING_TYPE:
                        dir = "INCOMING";
                        break;

                    case CallLog.Calls.MISSED_TYPE:
                        dir = "MISSED";
                        break;
                }
                try {
                    call.put("phoneNumber", phNumber);
                    call.put("date", prettyDate);
                    call.put("type", dir);
                    call.put("caller", caller);
                    call.put("callDuration", callDuration);
                    calls.put(call);
                } catch (JSONException e) {
                    e.printStackTrace();
                }


        }
        managedCursor.close();
        return calls;

    }
}
