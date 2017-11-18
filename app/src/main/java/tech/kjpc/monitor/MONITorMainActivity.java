package tech.kjpc.monitor;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class MONITorMainActivity extends AppCompatActivity {
    public static final int NOTIFICATION_REQUEST = 6664867;  // MONITor in numbers

    protected static boolean app_is_running = false;

    public static final String LOG_TAG = "MONITor";
    protected static final String CONNECTION_PARCELABLE_KEY = "connection";
    private static final String DIALOG_MONITOR_CONNECTION_ADD_TAG = "dialog_monitor_connection_add";
    private static final String DIALOG_MONITOR_CONNECTION_EDIT_TAG = "dialog_monitor_connection_edit";

    private MONITorDatabase monitor_database;
    private ArrayList<MONITorConnection> connections;
    private ArrayList<MONITorConnectionView> connection_views;

    private SimpleDateFormat timestamp_format = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss");

    private View.OnClickListener connection_goto_webview_click_listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            MONITorConnection connection = (MONITorConnection) view.getTag();
            Intent intent = new Intent(MONITorMainActivity.this, MONITorWebViewActivity.class);
            intent.putExtra(MONITorMainActivity.CONNECTION_PARCELABLE_KEY, connection);
            startActivity(intent);
        }
    };

    private View.OnLongClickListener connection_long_click_listener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View view) {
            MONITorConnectionView connection_view = (MONITorConnectionView) view;
            dialog_edit_connection(connection_view.get_connection());
            return true;
        }
    };

    private View.OnClickListener connection_reload_click_listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            MONITorConnection connection = (MONITorConnection) view.getTag();
            connection.set_status(MONITorConnection.STATUS_CHECKING);
            update_connection_views();
            Intent intent = new Intent(getApplicationContext(), MONITorCheckerService.class);
            Bundle bundle = new Bundle();
            bundle.putParcelable(MONITorMainActivity.CONNECTION_PARCELABLE_KEY, connection);
            intent.putExtras(bundle);
            startService(intent);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor_main);

        // connect to database
        this.monitor_database = new MONITorDatabase(getApplicationContext());

        BroadcastReceiver checker_result_receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                refresh_connections();
            }
        };
        LocalBroadcastManager.getInstance(getApplicationContext())
                .registerReceiver(checker_result_receiver, new IntentFilter(MONITorCheckerService.BROADCAST_ID));

        reload_connections();

        schedule_alarm();
    }

    @Override
    protected void onPause() {
        app_is_running = false;

        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        app_is_running = true;
    }

    private void schedule_alarm() {
        long interval = 10;
        Intent intent = new Intent(getApplicationContext(), MONITorAlarmReciever.class);
        final PendingIntent pending_intent = PendingIntent.getBroadcast(this, MONITorAlarmReciever.REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm_manager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarm_manager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), interval, pending_intent);
        Log.d(MONITorMainActivity.LOG_TAG, "Set alarm.");
    }

    // load connections from database
    private void load_connections() {
        this.connections = monitor_database.get_connections();
    }

    // add button, status for each connection
    private void setup_layout() {
        this.connection_views = new ArrayList<MONITorConnectionView>();
        LinearLayout main_layout = (LinearLayout) findViewById(R.id.main_connection_holder);
        main_layout.removeAllViews();
        for (MONITorConnection connection : this.connections) {
            MONITorConnectionView connection_view = new MONITorConnectionView(this, connection);
            Button button = (Button) connection_view.findViewById(R.id.view_connection_link);
            button.setText(connection.get_name());
            button.setTag(connection);
            button.setOnClickListener(this.connection_goto_webview_click_listener);
            FloatingActionButton floatingactionbutton = (FloatingActionButton) connection_view.findViewById(R.id.view_connection_button_refresh);
            floatingactionbutton.setTag(connection);
            floatingactionbutton.setOnClickListener(this.connection_reload_click_listener);
            connection_view.setOnLongClickListener(this.connection_long_click_listener);
            main_layout.addView(connection_view);
            this.connection_views.add(connection_view);
        }
    }

    protected void refresh_connections() {
        load_connections();
        setup_layout();
        update_connection_views();
    }

    // load connection from db, build layout, and update view texts
    protected void reload_connections() {
        refresh_connections();
        check_connections();
    }

    // update view texts
    private void update_connection_views() {
        for (MONITorConnectionView connection_view : this.connection_views) {
            MONITorConnection connection = connection_view.get_connection();
            TextView status_view = (TextView) connection_view.findViewById(R.id.view_connection_status);
            status_view.setText(connection.get_status());
            TextView timestamp_view = (TextView) connection_view.findViewById(R.id.view_connection_timestamp);
            timestamp_view.setText(timestamp_format.format(connection.get_timestamp()));
            // update connection status textview color
            if (connection.has_good_status()) {
                status_view.setTextColor(getResources().getColor(R.color.color_text_good));
            } else if (connection.has_error_status()) {
                status_view.setTextColor(getResources().getColor(R.color.color_text_error));
            } else {
                // get default text color from timestamp textview
                status_view.setTextColor(timestamp_view.getTextColors());
            }
        }
    }

    // call service to check connections
    private void check_connections() {
        for (MONITorConnectionView connection_view : this.connection_views) {
            connection_view.get_connection().set_status(MONITorConnection.STATUS_CHECKING);
        }
        update_connection_views();
        Intent intent = new Intent(getApplicationContext(), MONITorCheckerService.class);
        startService(intent);
    }

    public void button_add_connection_listener(View view) {
        dialog_add_connection();
    }

    private void dialog_add_connection() {
        MONITorAddConnectionDialog dialog = new MONITorAddConnectionDialog();
        dialog.show(getSupportFragmentManager(), DIALOG_MONITOR_CONNECTION_ADD_TAG);
    }

    private void dialog_edit_connection(MONITorConnection connection) {
        MONITorEditConnectionDialog dialog = new MONITorEditConnectionDialog();
        Bundle bundle = new Bundle();
        bundle.putParcelable(MONITorMainActivity.CONNECTION_PARCELABLE_KEY, connection);
        dialog.setArguments(bundle);
        dialog.show(getSupportFragmentManager(), DIALOG_MONITOR_CONNECTION_EDIT_TAG);
    }
}
