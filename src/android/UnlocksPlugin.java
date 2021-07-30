package com.dwinc.cordova.plugin;

// The native Toast API
import android.widget.Toast;
// Cordova-required packages
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.app.usage.UsageEvents;
import android.app.usage.UsageEvents.Event;
import android.content.Context;

public class UnlocksPlugin extends CordovaPlugin {

    private static final String DURATION_LONG = "long";
    public Context mContext;

    @Override
    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) {
        
        if (action.equals("unlocks")) {

            int unlockcount=0;

            Long start_time;
            Long end_time;
            try {
                JSONObject options = args.getJSONObject(0);
                start_time = options.getLong("start");
                end_time = options.getLong("duration");
            } catch (JSONException e) {
                callbackContext.error("Error encountered: " + e.getMessage());
                return false;
            }

            UsageStatsManager mUsageStatsManager = (UsageStatsManager)
                    getApplicationContext().getApplicationContext().getSystemService(Context.USAGE_STATS_SERVICE);

            if (mUsageStatsManager != null) {

                UsageEvents usageEvents = mUsageStatsManager.queryEvents(start_time, end_time);

                    while (usageEvents.hasNextEvent()) {
                        Event currentEvent = new UsageEvents.Event();
                        usageEvents.getNextEvent(currentEvent);

                        if (currentEvent.getEventType() == UsageEvents.Event.KEYGUARD_HIDDEN)
                        {
                            ++unlockcount;
                        }
                    }

            } else {
                Toast.makeText(this.mContext.getApplicationContext(), "Sorry...", Toast.LENGTH_SHORT).show();
            }

            callbackContext.success(unlockcount);
            return true;
          
            } else {

                callbackContext.error("\"" + action + "\" is not a recognized action.");
                return false;
              
        }
    }
}            
