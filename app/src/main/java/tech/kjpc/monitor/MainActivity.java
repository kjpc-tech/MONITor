package tech.kjpc.monitor;

import android.content.Intent;
import android.os.AsyncTask;
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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    protected ArrayList<MonitConnection> connections;
    private ArrayList<MonitConnectionView> connection_views;

    private View.OnClickListener button_click_listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            MonitConnection connection = (MonitConnection) view.getTag();
            Intent intent = new Intent(MainActivity.this, MonitWebViewActivity.class);
            intent.putExtra("connection", connection);
            startActivity(intent);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.connections = new ArrayList<MonitConnection>();
        this.connection_views = new ArrayList<MonitConnectionView>();

        // set temporary connections
        try {
            //this.connections.add(new MonitConnection("TITLE", new URL("https://example.com/"), "user", "password"));
        } catch (MalformedURLException e) {
            Log.e("MONITor", e.getMessage());
        }

        // add button, status for each connection
        this.setup_layout();

        // check each connection
        this.check_connections();
    }

    private void setup_layout() {
        LinearLayout main_layout = (LinearLayout) findViewById(R.id.main_layout);
        for (MonitConnection connection : this.connections) {
            MonitConnectionView connection_view = new MonitConnectionView(this, connection);
            Button button = (Button) connection_view.findViewById(R.id.button_connection);
            TextView textview = (TextView) connection_view.findViewById(R.id.textview_status_text);
            button.setText(connection.get_name());
            button.setTag(connection);
            button.setOnClickListener(this.button_click_listener);
            textview.setText("Checking...");
            main_layout.addView(connection_view);
            this.connection_views.add(connection_view);
        }
    }

    private void check_connections() {
        for (MonitConnectionView connection_view : this.connection_views) {
            new CheckMonit().execute(connection_view);
        }
    }

    private class CheckMonit extends AsyncTask<MonitConnectionView, Void, MonitCheckerResult> {
        protected MonitCheckerResult doInBackground(MonitConnectionView... connection_views) {
            String response = "";
            for (int v = 0; v < connection_views.length; v++) {
                MonitConnectionView connection_view = connection_views[v];
                try {
                    HttpURLConnection urlConnection = (HttpURLConnection) connection_view.get_connection().get_url().openConnection();
                    //urlConnection.setUseCaches(false);
                    urlConnection.setRequestProperty("Authorization", connection_view.get_connection().get_authorization());
                    //urlConnection.connect();
                    InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    String line = null;
                    while ((line = bufferedReader.readLine()) != null) {
                        response += line;
                    }
                    return new MonitCheckerResult(connection_view, response);
                } catch (IOException e) {
                    e.getMessage();
                }
            }
            return null;
        }

        protected void onProgressUpdate(Void... voids) {

        }

        protected void onPostExecute(MonitCheckerResult result) {
            if (result != null) {
                TextView textview = (TextView) result.connection_view.findViewById(R.id.textview_status_text);
                if (result.result_text == null) {
                    textview.setText("Error: no result.");
                } else {
                    Pattern pattern = Pattern.compile(".*Monit Service Manager.*");
                    Matcher matcher = pattern.matcher(result.result_text);
                    if (matcher.matches()) {
                        textview.setText("All is well.");
                    } else {
                        textview.setText("Error: no match.");
                    }
                }
            }
        }
    }
}
