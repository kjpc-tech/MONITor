package tech.kjpc.monitorapp;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;

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

    private EditText settings_ping;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor_settings);

        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (NullPointerException e) {
            Log.e(MONITorMainActivity.LOG_TAG, e.getMessage());
        }

        this.settings_ping = (EditText) findViewById(R.id.monitor_settings_ping);

        // load settings from file
        JSONObject settings = load_settings(getApplicationContext());

        // put settings into layout
        try {
            this.settings_ping.setText(String.valueOf(settings.getLong(SETTING_PING_TIME)));
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
            default_settings.put(SETTING_PING_TIME, 10);
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
        long settings_ping_value = Long.parseLong(this.settings_ping.getText().toString());

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
        long ping_time = 10; // 10 is default value
        JSONObject settings = load_settings(context);
        if (settings.has(SETTING_PING_TIME)) {
            try {
                ping_time = settings.getLong(SETTING_PING_TIME);
            } catch (JSONException e) {
                Log.e(MONITorMainActivity.LOG_TAG, e.getMessage());
            }
        }
        return ping_time;
    }
}
