package tech.kjpc.monitor;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;

import java.util.HashMap;

public class MonitWebViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monit_web_view);
        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (NullPointerException e) {
            Log.e("MONITor", e.getMessage());
        }

        WebView webview = (WebView) findViewById(R.id.webview);

        MonitConnection connection = null;
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                connection = extras.getParcelable("connection");
            }
        } else {
            // connection_index = savedInstanceState.getSerializable("connection");
        }
        if (connection != null) {
            HashMap<String, String> headers = new HashMap<String, String>();
            String authorization = connection.get_authorization();
            headers.put("Authorization", authorization);
            webview.loadUrl("https://monitoring.kjpc.tech/", headers);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}
