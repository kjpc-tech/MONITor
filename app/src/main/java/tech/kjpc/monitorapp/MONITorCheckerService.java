package tech.kjpc.monitorapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by kyle on 11/2/17.
 */

public class MONITorCheckerService extends JobIntentService {
    public static final String NOTIFICATION_CHANNEL = "MONITor_NOTIFICATION";
    public static final int NOTIFICATION_ID = 6664867;  // MONITor in numbers

    public static  final String BROADCAST_CHECKER_ID = "MONITor_CHECKER_BROADCAST";

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
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
                // if check SSL setting is false and the connection is https
                //  then trust all certificates
                if (!MONITorSettingsActivity.get_check_ssl(getApplicationContext()) && urlConnection instanceof HttpsURLConnection) {
                    trust_all_ssl((HttpsURLConnection) urlConnection);
                }
                urlConnection.setUseCaches(false);
                urlConnection.setRequestProperty("Authorization", connection.get_authorization());
                InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line = null;
                while ((line = bufferedReader.readLine()) != null) {
                    response += line;
                }
            } catch (IOException e) {
                Log.e(MONITorMainActivity.LOG_TAG, e.getMessage());
            }

            // check response
            if (response != null && response.length() > 0) {
                Pattern pattern = Pattern.compile(".*Monit Service Manager.*Monit is.*running.*on.*and monitoring:.*");
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

    private void trust_all_ssl(HttpsURLConnection https_url_connection) {
        // trust all SSL certificates
        // not recommended but works for self-signed certificates
        // derived from https://stackoverflow.com/q/2642777/5286674
        try {
            SSLContext ssl_context = SSLContext.getInstance("SSL");
            ssl_context.init(null, new TrustManager[] {
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {}

                        @Override
                        public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {}

                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }
                    }
            }, new SecureRandom());
            https_url_connection.setSSLSocketFactory(ssl_context.getSocketFactory());
            https_url_connection.setHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String s, SSLSession sslSession) {
                    return true;
                }
            });
        } catch (Exception e) {
            Log.e(MONITorMainActivity.LOG_TAG, e.getMessage());
        }
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // make notification channel
                NotificationChannel notification_channel = new NotificationChannel(NOTIFICATION_CHANNEL, getString(R.string.notification_monitor_channel_name), NotificationManager.IMPORTANCE_DEFAULT);
                // toggle vibration to the notification channel
                notification_channel.enableVibration(MONITorSettingsActivity.get_use_vibration(getApplicationContext()));
                notification_manager.createNotificationChannel(notification_channel);

            }
            notification_manager.notify(NOTIFICATION_ID, notification_builder.build());
        }
    }
}
