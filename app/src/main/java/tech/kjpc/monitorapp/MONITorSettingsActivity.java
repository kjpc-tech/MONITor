package tech.kjpc.monitorapp;

import android.app.AlarmManager;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Switch;

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
    public static final String SETTING_VIBRATION = "SETTING_VIBRATION";
    public static final String SETTING_SOUND = "SETTING_SOUND";
    public static final String SETTING_CHECK_SSL = "SETTING_CHECK_SSL";

    private Spinner settings_ping;
    private ArrayAdapter<CharSequence> settings_ping_options;
    private Switch settings_vibration;
    private Switch settings_sound;
    private Switch settings_check_ssl;

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
        this.settings_vibration = (Switch) findViewById(R.id.monitor_settings_vibration);
        this.settings_sound = (Switch) findViewById(R.id.monitor_settings_sound);
        this.settings_check_ssl = (Switch) findViewById(R.id.monitor_settings_check_ssl);

        // load settings from file
        JSONObject settings = load_settings(getApplicationContext());

        // put settings into layout
        try {
            this.settings_ping.setSelection(this.settings_ping_options.getPosition(settings.getString(SETTING_PING_TIME)));
        } catch (JSONException e) {
            Log.e(MONITorMainActivity.LOG_TAG, e.getMessage());
        }
        try {
            this.settings_vibration.setChecked(settings.getBoolean(SETTING_VIBRATION));
        } catch (JSONException e) {
            Log.e(MONITorMainActivity.LOG_TAG, e.getMessage());
        }
        try {
            this.settings_sound.setChecked(settings.getBoolean(SETTING_SOUND));
        } catch (JSONException e) {
            Log.e(MONITorMainActivity.LOG_TAG, e.getMessage());
        }
        Boolean settings_check_ssl_value = true;  // default true
        try {
            settings_check_ssl_value = settings.getBoolean(SETTING_CHECK_SSL);
        } catch (JSONException e) {
            Log.e(MONITorMainActivity.LOG_TAG, e.getMessage());
        }
        this.settings_check_ssl.setChecked(settings_check_ssl_value);
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
            default_settings.put(SETTING_VIBRATION, false);
            default_settings.put(SETTING_SOUND, false);
            default_settings.put(SETTING_CHECK_SSL, true);
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
        Boolean settings_vibration_value = this.settings_vibration.isChecked();
        Boolean settings_sound_value = this.settings_sound.isChecked();
        Boolean settings_check_ssl_value = this.settings_check_ssl.isChecked();

        // update setting values
        try {
            settings.put(SETTING_PING_TIME, settings_ping_value);
        } catch (JSONException e) {
            Log.e(MONITorMainActivity.LOG_TAG, e.getMessage());
        }
        try {
            settings.put(SETTING_VIBRATION, settings_vibration_value);
        } catch (JSONException e) {
            Log.e(MONITorMainActivity.LOG_TAG, e.getMessage());
        }
        try {
            settings.put(SETTING_SOUND, settings_sound_value);
        } catch (JSONException e) {
            Log.e(MONITorMainActivity.LOG_TAG, e.getMessage());
        }
        try {
            settings.put(SETTING_CHECK_SSL, settings_check_ssl_value);
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

    protected static boolean get_use_vibration(Context context) {
        JSONObject settings = load_settings(context);
        if (settings.has(SETTING_VIBRATION)) {
            try {
                return settings.getBoolean(SETTING_VIBRATION);
            } catch (JSONException e) {
                Log.e(MONITorMainActivity.LOG_TAG, e.getMessage());
            }
        }
        return false;  // no vibration by default
    }

    protected static boolean get_use_sound(Context context) {
        JSONObject settings = load_settings(context);
        if (settings.has(SETTING_SOUND)) {
            try {
                return settings.getBoolean(SETTING_SOUND);
            } catch (JSONException e) {
                Log.e(MONITorMainActivity.LOG_TAG, e.getMessage());
            }
        }
        return false;  // no sound by default
    }

    protected static boolean get_check_ssl(Context context) {
        JSONObject settings = load_settings(context);
        if (settings.has(SETTING_CHECK_SSL)) {
            try {
                return settings.getBoolean(SETTING_CHECK_SSL);
            } catch (JSONException e) {
                Log.e(MONITorMainActivity.LOG_TAG, e.getMessage());
            }
        }
        return true;  // check SSL by default
    }
}
