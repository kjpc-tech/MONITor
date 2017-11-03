package tech.kjpc.monitor;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MONITorMainActivity extends AppCompatActivity {
    private MONITorDatabase database_helper;
    private ArrayList<MONITorConnection> connections;
    private ArrayList<MONITorConnectionView> connection_views;

    private SimpleDateFormat timestamp_format = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss");

    private View.OnClickListener button_click_listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            MONITorConnection connection = (MONITorConnection) view.getTag();
            Intent intent = new Intent(MONITorMainActivity.this, MONITorWebViewActivity.class);
            intent.putExtra("connection", connection);
            startActivity(intent);
        }
    };

    private View.OnLongClickListener connection_long_click_listener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View view) {
            MONITorConnectionView connection_view = (MONITorConnectionView) view;
            edit_connection(connection_view.get_connection());
            return true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor_main);

        // connect to database
        this.database_helper = new MONITorDatabase(getApplicationContext());

        reload_connections();

        check_connections();

        schedule_alarm();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void schedule_alarm() {
        long interval = 10;
        Intent intent = new Intent(getApplicationContext(), MONITorAlarmReciever.class);
        final PendingIntent pending_intent = PendingIntent.getBroadcast(this, MONITorAlarmReciever.REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm_manager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarm_manager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), interval, pending_intent);
        Log.d("MONITor", "Set alarm.");
    }

    // load connections from database
    private void load_connections() {
        this.connections = database_helper.get_connections();
    }

    // add button, status for each connection
    private void setup_layout() {
        this.connection_views = new ArrayList<MONITorConnectionView>();
        LinearLayout main_layout = (LinearLayout) findViewById(R.id.main_layout);
        main_layout.removeAllViews();
        for (MONITorConnection connection : this.connections) {
            MONITorConnectionView connection_view = new MONITorConnectionView(this, connection);
            Button button = (Button) connection_view.findViewById(R.id.button_connection);
            button.setText(connection.get_name());
            button.setTag(connection);
            button.setOnClickListener(this.button_click_listener);
            connection_view.setOnLongClickListener(this.connection_long_click_listener);
            main_layout.addView(connection_view);
            this.connection_views.add(connection_view);
        }
    }

    protected void reload_connections() {
        load_connections();
        setup_layout();
        update_connection_views();
    }

    private void update_connection_views() {
        for (MONITorConnectionView connection_view : this.connection_views) {
            MONITorConnection connection = connection_view.get_connection();
            TextView status_view = (TextView) connection_view.findViewById(R.id.textview_status_text);
            status_view.setText(connection.get_status());
            TextView timestamp_view = (TextView) connection_view.findViewById(R.id.textview_timestamp_text);
            timestamp_view.setText(timestamp_format.format(connection.get_timestamp()));
        }
    }

    private void check_connections() {
        for (MONITorConnectionView connection_view : this.connection_views) {
            connection_view.get_connection().set_status("Checking...");
            new CheckMonit().execute(connection_view);
        }
        update_connection_views();
    }

    public void button_add_connection_listener(View view) {
        MONITorAddConnectionDialog dialog = new MONITorAddConnectionDialog();
        dialog.show(getSupportFragmentManager(), "add_monit_connection");
    }

    private void edit_connection(MONITorConnection connection) {
        MONITorEditConnectionDialog dialog = new MONITorEditConnectionDialog();
        Bundle bundle = new Bundle();
        bundle.putParcelable("connection", connection);
        dialog.setArguments(bundle);
        dialog.show(getSupportFragmentManager(), "edit_monit_connection");
    }

    private class CheckMonit extends AsyncTask<MONITorConnectionView, Void, MONITorCheckerResult> {
        protected MONITorCheckerResult doInBackground(MONITorConnectionView... connection_views) {
            String response = "";
            for (int v = 0; v < connection_views.length; v++) {
                MONITorConnectionView connection_view = connection_views[v];
                try {
                    HttpURLConnection urlConnection = (HttpURLConnection) connection_view.get_connection().get_url().openConnection();
                    urlConnection.setUseCaches(false);
                    urlConnection.setRequestProperty("Authorization", connection_view.get_connection().get_authorization());
                    InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    String line = null;
                    while ((line = bufferedReader.readLine()) != null) {
                        response += line;
                    }
                    return new MONITorCheckerResult(connection_view, response);
                } catch (IOException e) {
                    e.getMessage();
                }
            }
            return null;
        }

        protected void onProgressUpdate(Void... voids) {

        }

        protected void onPostExecute(MONITorCheckerResult result) {
            if (result != null) {
                TextView textview = (TextView) result.connection_view.findViewById(R.id.textview_status_text);
                MONITorConnection connection = result.connection_view.get_connection();
                if (result.result_text == null) {
                    connection.set_status("Error: no result.");
                    connection.set_timestamp(new Date());
                } else {
                    Pattern pattern = Pattern.compile(".*Monit Service Manager.*");
                    Matcher matcher = pattern.matcher(result.result_text);
                    if (matcher.matches()) {
                        connection.set_status("All is well.");
                        connection.set_timestamp(new Date());
                    } else {
                        connection.set_status("Error: no match.");
                        connection.set_timestamp(new Date());
                    }
                }
                database_helper.edit_connection(connection, connection.get_status(), connection.get_timestamp());
                update_connection_views();
            }
        }
    }
}
