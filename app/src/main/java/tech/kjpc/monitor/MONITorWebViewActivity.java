package tech.kjpc.monitor;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.HttpAuthHandler;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;


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
            public boolean shouldOverrideUrlLoading(WebView webView, WebResourceRequest request) {
                // TODO make sure request is within monit
                return false;
            }

            @Override
            public void onReceivedHttpAuthRequest(WebView webView, HttpAuthHandler authHandler, String host, String realm) {
                // TODO make sure host is correct
                authHandler.proceed(connection.get_username(), connection.get_password());
            }
        });
        webview.loadUrl(connection.get_url().toString());
    }

    @Override
    public void onBackPressed() {
        if (this.webview.canGoBack()) {
            this.webview.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
