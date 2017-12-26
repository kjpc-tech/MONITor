package tech.kjpc.monitorapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.webkit.HttpAuthHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.net.MalformedURLException;
import java.net.URL;


public class MONITorWebViewActivity extends AppCompatActivity {
    private WebView webview;
    private MONITorConnection connection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor_webview);

        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (NullPointerException e) {
            Log.e(MONITorMainActivity.LOG_TAG, e.getMessage());
        }

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            connection = extras.getParcelable(MONITorMainActivity.CONNECTION_PARCELABLE_KEY);
        }
        if (connection == null) {
            Log.e(MONITorMainActivity.LOG_TAG, "Error: no connection.");
        }

        this.webview = (WebView) findViewById(R.id.webview_webview);
        webview.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView webView, String url) {
                boolean webview_handles;
                try {
                    // only stay in the webview if still on connection host
                    webview_handles = new URL(url).getHost().equals(connection.get_url().getHost());
                } catch (MalformedURLException e) {
                    Log.e(MONITorMainActivity.LOG_TAG, e.getMessage());
                    webview_handles = false;
                }

                if (!webview_handles) {  // handle external host link
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                }

                return !webview_handles;
            }

            @Override
            public void onReceivedHttpAuthRequest(WebView webView, HttpAuthHandler authHandler, String host, String realm) {
                // only send auth credentials if on the connection host
                if (host.equals(connection.get_url().getHost())) {
                    authHandler.proceed(connection.get_username(), connection.get_password());
                }
            }
        });
        webview.loadUrl(connection.get_url().toString());
    }

    @Override
    public void onBackPressed() {
        // if webview can go back do that before going back to main activity
        if (this.webview.canGoBack()) {
            this.webview.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // if webview can go back do that before going back to main activity
                if (this.webview.canGoBack()) {
                    this.webview.goBack();
                    return true;
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
