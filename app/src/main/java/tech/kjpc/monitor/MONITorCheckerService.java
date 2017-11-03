package tech.kjpc.monitor;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by kyle on 11/2/17.
 */

public class MONITorCheckerService extends IntentService {
    public MONITorCheckerService() {
        super("MONITorCheckerService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.d("MONITor", "Service running.");
        Context context = getApplicationContext();

        DatabaseHelper database = new DatabaseHelper(context);
        ArrayList<MonitConnection> connections = database.get_connections();


    }
}
