package tech.kjpc.monitorapp;

import android.app.AlarmManager;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class MONITorSettingsActivity extends AppCompatActivity {
    private static final String SETTINGS_FILE = "monitor_settings";

    public static final String SETTING_PING_TIME = "SETTING_PING_TIME";

    private Spinner settings_ping;
    private ArrayAdapter<CharSequence> settings_ping_options;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor_settings);

        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (NullPointerException e) {
            Log.e(MONITorMainActivity.LOG_TAG, e.getMessage());
        }

        this.settings_ping = (Spinner) findViewById(R.id.monitor_settings_ping);
        this.settings_ping_options = ArrayAdapter.createFromResource(this, R.array.monitor_settings_ping_options, R.layout.spinner_item);
        this.settings_ping.setAdapter(this.settings_ping_options);


        // load settings from file
        JSONObject settings = load_settings(getApplicationContext());

        // put settings into layout
        try {
            this.settings_ping.setSelection(this.settings_ping_options.getPosition(settings.getString(SETTING_PING_TIME)));
        } catch (JSONException e) {
            Log.e(MONITorMainActivity.LOG_TAG, e.getMessage());
        }
    }

    @Override
    public void onBackPressed() {
        update_settings();

        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                update_settings();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private static JSONObject load_settings(Context context) {
        // first attempt: load settings from file
        try {
            FileInputStream inputStream = context.openFileInput(SETTINGS_FILE);
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            try {
                return new JSONObject(bufferedReader.readLine().toString());
            } catch (JSONException e) {
                Log.e(MONITorMainActivity.LOG_TAG, e.getMessage());
            }
        } catch (IOException e) {
            Log.e(MONITorMainActivity.LOG_TAG, e.getMessage());
        }

        // second attempt: create new settings and set default values
        try {
            JSONObject default_settings = new JSONObject();
            default_settings.put(SETTING_PING_TIME, "Fifteen Minutes");
            return default_settings;
        } catch (JSONException e) {
            Log.e(MONITorMainActivity.LOG_TAG, e.getMessage());
        }

        // third attempt: create empty settings object
        return new JSONObject();
    }

    private static void save_settings(Context context, JSONObject settings) {
        // write settings object to file
        try {
            FileOutputStream outputStream = context.openFileOutput(SETTINGS_FILE, context.MODE_PRIVATE);
            outputStream.write(settings.toString().getBytes());
            outputStream.close();
        } catch (IOException e) {
            Log.e(MONITorMainActivity.LOG_TAG, e.getMessage());
        }
    }

    private void update_settings() {
        // load settings from file
        JSONObject settings = load_settings(getApplicationContext());

        // get settings from layout
        String settings_ping_value = this.settings_ping.getSelectedItem().toString();

        // update setting values
        try {
            settings.put(SETTING_PING_TIME, settings_ping_value);
        } catch (JSONException e) {
            Log.e(MONITorMainActivity.LOG_TAG, e.getMessage());
        }

        // save settings to file
        save_settings(getApplicationContext(), settings);
    }

    protected static long get_ping_time(Context context) {
        String ping_time = "Fifteen Minutes";  // default value
        JSONObject settings = load_settings(context);
        if (settings.has(SETTING_PING_TIME)) {
            try {
                ping_time = settings.getString(SETTING_PING_TIME);
            } catch (JSONException e) {
                Log.e(MONITorMainActivity.LOG_TAG, e.getMessage());
            }
        }
        switch (ping_time) {
            case "Half Hour":
                return AlarmManager.INTERVAL_HALF_HOUR;
            case "Hour":
                return AlarmManager.INTERVAL_HOUR;
            case "Half Day":
                return AlarmManager.INTERVAL_HALF_DAY;
            case "Day":
                return AlarmManager.INTERVAL_DAY;
            default:
                // Fifteen Minutes
                return AlarmManager.INTERVAL_FIFTEEN_MINUTES;
        }
    }
}