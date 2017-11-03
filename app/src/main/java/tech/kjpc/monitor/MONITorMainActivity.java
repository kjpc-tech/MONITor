package tech.kjpc.monitor;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
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
    private MONITorDatabase monitor_database;
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
        this.monitor_database = new MONITorDatabase(getApplicationContext());

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
        this.connections = monitor_database.get_connections();
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
            Intent intent = new Intent(getApplicationContext(), MONITorCheckerService.class);
            startService(intent);
        }
        update_connection_views();
    }

    public void button_add_connection_listener(View view) {
        MONITorAddConnectionDialog dialog = new MONITorAddConnectionDialog();
        dialog.show(getSupportFragmentManager(), "add_monit_connection");
    }

    public void button_refresh_listner(View view) {
        reload_connections();
    }

    private void edit_connection(MONITorConnection connection) {
        MONITorEditConnectionDialog dialog = new MONITorEditConnectionDialog();
        Bundle bundle = new Bundle();
        bundle.putParcelable("connection", connection);
        dialog.setArguments(bundle);
        dialog.show(getSupportFragmentManager(), "edit_monit_connection");
    }
}
