package tech.kjpc.monitorapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

/**
 * Created by kyle on 11/2/17.
 */

public class MONITorBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent receiver_intent) {
        if (receiver_intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            long interval = MONITorSettingsActivity.get_ping_time(context);
            Intent intent = new Intent(context, MONITorAlarmReceiver.class);
            final PendingIntent pending_intent = PendingIntent.getBroadcast(context, MONITorAlarmReceiver.REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager alarm_manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarm_manager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + interval, interval, pending_intent);
            Log.d(MONITorMainActivity.LOG_TAG, "MONITorBroadcastReceiver: Set alarm for every " + String.valueOf(interval / 1000 / 60) + " minute(s).");
        }
    }
}
