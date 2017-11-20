package tech.kjpc.monitorapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

/**
 * Created by kyle on 11/2/17.
 */

public class MONITorBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent receiver_intent) {
        long interval = 10;
        Intent intent = new Intent(context, MONITorAlarmReciever.class);
        final PendingIntent pending_intent = PendingIntent.getBroadcast(context, MONITorAlarmReciever.REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm_manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarm_manager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), interval, pending_intent);
    }
}
