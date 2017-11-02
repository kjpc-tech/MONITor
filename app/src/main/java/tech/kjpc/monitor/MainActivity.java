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
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    private DatabaseHelper database_helper;
    private ArrayList<MonitConnection> connections;
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

    private View.OnLongClickListener connection_long_click_listener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View view) {
            MonitConnectionView connection_view = (MonitConnectionView) view;
            edit_connection(connection_view.get_connection());
            return true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // connect to database
        this.database_helper = new DatabaseHelper(getApplicationContext());

        reload_connections();

        check_connections();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    // load connections from database
    private void load_connections() {
        this.connections = database_helper.get_connections();
    }

    // add button, status for each connection
    private void setup_layout() {
        this.connection_views = new ArrayList<MonitConnectionView>();
        LinearLayout main_layout = (LinearLayout) findViewById(R.id.main_layout);
        main_layout.removeAllViews();
        for (MonitConnection connection : this.connections) {
            MonitConnectionView connection_view = new MonitConnectionView(this, connection);
            Button button = (Button) connection_view.findViewById(R.id.button_connection);
            button.setText(connection.get_name());
            button.setTag(connection);
            button.setOnClickListener(this.button_click_listener);
            connection.set_status("Initializing...");
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
        for (MonitConnectionView connection_view : this.connection_views) {
            MonitConnection connection = connection_view.get_connection();
            TextView textview = (TextView) connection_view.findViewById(R.id.textview_status_text);
            textview.setText(connection.get_status());
        }
    }

    private void check_connections() {
        for (MonitConnectionView connection_view : this.connection_views) {
            connection_view.get_connection().set_status("Checking...");
            new CheckMonit().execute(connection_view);
        }
        update_connection_views();
    }

    public void button_add_connection_listener(View view) {
        AddMonitConnectionDialog dialog = new AddMonitConnectionDialog();
        dialog.show(getSupportFragmentManager(), "add_monit_connection");
    }

    private void edit_connection(MonitConnection connection) {
        EditMonitConnectionDialog dialog = new EditMonitConnectionDialog();
        Bundle bundle = new Bundle();
        bundle.putParcelable("connection", connection);
        dialog.setArguments(bundle);
        dialog.show(getSupportFragmentManager(), "edit_monit_connection");
    }

    private class CheckMonit extends AsyncTask<MonitConnectionView, Void, MonitCheckerResult> {
        protected MonitCheckerResult doInBackground(MonitConnectionView... connection_views) {
            String response = "";
            for (int v = 0; v < connection_views.length; v++) {
                MonitConnectionView connection_view = connection_views[v];
                try {
                    HttpURLConnection urlConnection = (HttpURLConnection) connection_view.get_connection().get_url().openConnection();
                    urlConnection.setUseCaches(false);
                    urlConnection.setRequestProperty("Authorization", connection_view.get_connection().get_authorization());
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
                    result.connection_view.get_connection().set_status("Error: no result.");
                } else {
                    Pattern pattern = Pattern.compile(".*Monit Service Manager.*");
                    Matcher matcher = pattern.matcher(result.result_text);
                    if (matcher.matches()) {
                        result.connection_view.get_connection().set_status("All is well.");
                    } else {
                        result.connection_view.get_connection().set_status("Error: no match.");
                    }
                }
                update_connection_views();
            }
        }
    }
}
