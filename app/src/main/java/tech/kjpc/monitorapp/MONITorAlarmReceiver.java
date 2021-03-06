package tech.kjpc.monitorapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by kyle on 11/2/17.
 */

public class MONITorAlarmReceiver extends BroadcastReceiver {
    public static final int REQUEST_CODE = 6664867;  // MONITor in numbers

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(MONITorMainActivity.LOG_TAG, "MONITorAlarmReceiver: Alarm went off!");
        Intent alarm_intent = new Intent(context, MONITorCheckerService.class);
        MONITorCheckerService.enqueueWork(context, MONITorCheckerService.class, MONITorAlarmReceiver.REQUEST_CODE, alarm_intent);
    }
}
