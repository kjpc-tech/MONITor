package tech.kjpc.monitorapp;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class MONITorAboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor_about);

        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (NullPointerException e) {
            Log.e(MONITorMainActivity.LOG_TAG, e.getMessage());
        }
    }

    public void view_source(View view) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.source_link)));
        startActivity(intent);
    }
}
