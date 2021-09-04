package com.dwinc.cordova.plugin;

import android.app.Activity;
import android.app.AppOpsManager;
import android.app.usage.EventStats;
import android.app.usage.UsageEvents;
import android.content.Intent;
import android.provider.Settings;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

import android.app.usage.UsageStatsManager;
import android.app.usage.UsageEvents.Event;
import android.content.Context;
import android.os.Process;
import org.json.JSONObject;

import java.util.List;

public class UnlocksPlugin extends CordovaPlugin {
    private static final int REQUEST_CODE_SETTINGS = 1;
    private CallbackContext activityResultCallback;

    @Override
    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) {
        switch (action) {
            case "unlockEvents":
                cordova.getThreadPool().execute(() -> getUnlockEvents(args, callbackContext));
                return true;
            case "unlockStats":
                cordova.getThreadPool().execute(() -> getUnlockStats(args, callbackContext));
                return true;
            case "hasPermission":
                this.hasPermission(callbackContext);
                return true;
            case "requestPermission":
                this.requestPermission(callbackContext);
                return true;
            default:
                return false;
        }
    }

    private void getUnlockEvents(JSONArray args, final CallbackContext callbackContext) {
        try {
            long timeBegin = args.getLong(0);
            long timeEnd = args.getLong(1);

            JSONArray results = new JSONArray();
            UsageEvents usageEvents = getUsageStatsManager().queryEvents(timeBegin, timeEnd);
            Event event = new Event();

            while (usageEvents.getNextEvent(event)) {
                if (event.getEventType() == Event.KEYGUARD_HIDDEN) {
                    results.put(event.getTimeStamp());
                }
            }

            callbackContext.success(results);
        } catch (JSONException e) {
            callbackContext.error("Error parsing arguments: " + e.getMessage());
        }
    }

    private void getUnlockStats(JSONArray args, final CallbackContext callbackContext) {
        try {
            long timeBegin = args.getLong(0);
            long timeEnd = args.getLong(1);
            int intervalType = args.optInt(2, UsageStatsManager.INTERVAL_YEARLY);

            JSONArray results = new JSONArray();
            List<EventStats> eventStatsList = getUsageStatsManager().queryEventStats(intervalType, timeBegin, timeEnd);

            for (EventStats eventStats : eventStatsList) {
                if (eventStats.getEventType() == Event.KEYGUARD_HIDDEN) {
                    JSONObject obj = new JSONObject();
                    obj.put("firstTimeStamp", eventStats.getFirstTimeStamp());
                    obj.put("lastTimeStamp", eventStats.getLastTimeStamp());
                    obj.put("lastEventTime", eventStats.getLastEventTime());
                    obj.put("count", eventStats.getCount());
                    results.put(obj);
                }
            }

            callbackContext.success(results);
        } catch (JSONException e) {
            callbackContext.error("Error parsing arguments: " + e.getMessage());
        }
    }

    private void hasPermission(final CallbackContext callbackContext) {
        Context context = this.cordova.getContext();
        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        String packageName = context.getPackageName();
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), packageName);
        boolean granted = mode == AppOpsManager.MODE_ALLOWED;
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, granted));
    }

    private void requestPermission(final CallbackContext callbackContext) {
        activityResultCallback = callbackContext;
        cordova.setActivityResultCallback(this);

        Activity activity = this.cordova.getActivity();
        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        activity.startActivityForResult(intent, REQUEST_CODE_SETTINGS);
    }

    private UsageStatsManager getUsageStatsManager() {
        Context context = this.cordova.getActivity();
        return (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == REQUEST_CODE_SETTINGS) {
            hasPermission(activityResultCallback);
        }
    }
}
