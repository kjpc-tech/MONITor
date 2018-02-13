package tech.kjpc.monitorapp;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by kyle on 11/2/17.
 */

public class MONITorCheckerService extends IntentService {
    public static final String NOTIFICATION_CHANNEL = "MONITor_NOTIFICATION";
    public static final int NOTIFICATION_ID = 6664867;  // MONITor in numbers

    public static  final String BROADCAST_CHECKER_ID = "MONITor_CHECKER_BROADCAST";

    private static final String SERVICE_NAME = "MONITorCheckerService";

    public MONITorCheckerService() {
        super(SERVICE_NAME);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Context context = getApplicationContext();
        MONITorDatabase database = new MONITorDatabase(context);
        MONITorConnection single_connection = null;

        Log.d(MONITorMainActivity.LOG_TAG, "MONITorCheckerService: running.");

        if (intent != null && intent.hasExtra(MONITorMainActivity.CONNECTION_PARCELABLE_KEY)) {
            try {
                single_connection = (MONITorConnection) intent.getExtras().getParcelable(MONITorMainActivity.CONNECTION_PARCELABLE_KEY);
            } catch (Exception e) {
                Log.e(MONITorMainActivity.LOG_TAG, e.getMessage());
            }
        }

        if (single_connection != null) {
            // just check single connection from intent
            check_connection(database, single_connection);
        } else {
            // check all connections
            ArrayList<MONITorConnection> connections = database.get_connections();

            for (MONITorConnection connection : connections) {
                check_connection(database, connection);
            }
        }
    }

    private void check_connection(MONITorDatabase database, MONITorConnection connection) {
        String response = "";

        if (is_online()) {
            // make request and get response
            try {
                HttpURLConnection urlConnection = (HttpURLConnection) connection.get_url().openConnection();
                urlConnection.setUseCaches(false);
                urlConnection.setRequestProperty("Authorization", connection.get_authorization());
                InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line = null;
                while ((line = bufferedReader.readLine()) != null) {
                    response += line;
                }
            } catch (IOException e) {
                e.getMessage();
            }

            // check response
            if (response != null && response.length() > 0) {
                Pattern pattern = Pattern.compile(".*Monit Service Manager.*");
                Matcher matcher = pattern.matcher(response);
                if (matcher.matches()) {
                    connection.set_status(MONITorConnection.STATUS_GOOD);
                } else {
                    connection.set_status(MONITorConnection.STATUS_ERROR_NO_MATCH);
                    notify_status(connection);
                }
            } else {
                connection.set_status(MONITorConnection.STATUS_ERROR_NO_RESULT);
                notify_status(connection);
            }
        } else {
            connection.set_status(MONITorConnection.STATUS_NO_NETWORK);
            // don't notify when no network at the moment
            //  this gets annoying when switching networks (ie. walking between buildings, etc)
        }

        connection.set_timestamp(new Date());

        // update connection in database
        database.edit_connection(connection, connection.get_status(), connection.get_timestamp());

        // set main activity know it should update
        broadcast_completion();
    }

    private boolean is_online() {
        ConnectivityManager connectivity_manager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo active_network = connectivity_manager.getActiveNetworkInfo();
        return active_network != null && active_network.isConnectedOrConnecting();
    }

    private void broadcast_completion() {
        Intent intent = new Intent(BROADCAST_CHECKER_ID);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    private void notify_status(MONITorConnection connection) {
        // only notify if the app is running in the background
        if (!MONITorMainActivity.app_is_running) {
            Intent notification_intent = new Intent(getApplicationContext(), MONITorMainActivity.class);
            PendingIntent notification_pending_intent = PendingIntent.getActivity(getApplicationContext(), MONITorMainActivity.NOTIFICATION_REQUEST, notification_intent, PendingIntent.FLAG_UPDATE_CURRENT);
            // make notification to alert user of error
            NotificationCompat.Builder notification_builder = new NotificationCompat.Builder(getApplicationContext(), NOTIFICATION_CHANNEL);
            notification_builder.setSmallIcon(R.drawable.ic_notification);
            notification_builder.setContentTitle(getResources().getString(R.string.notification_monitor_status_title));
            notification_builder.setContentText(getResources().getString(R.string.notification_monitor_status_text, connection.get_name(), connection.get_status()));
            notification_builder.setAutoCancel(true);
            notification_builder.setContentIntent(notification_pending_intent);
            // add vibration to the notification
            if (MONITorSettingsActivity.get_use_vibration(getApplicationContext())) {
                notification_builder.setVibrate(new long[]{50, 150, 50, 200, 50, 150});
            }
            // add sound to the notification
            if (MONITorSettingsActivity.get_use_sound(getApplicationContext())) {
                notification_builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
            }
            NotificationManager notification_manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notification_manager.notify(NOTIFICATION_ID, notification_builder.build());
        }
    }
}
