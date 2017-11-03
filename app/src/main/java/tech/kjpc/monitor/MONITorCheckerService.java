package tech.kjpc.monitor;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by kyle on 11/2/17.
 */

public class MONITorCheckerService extends IntentService {
    public MONITorCheckerService() {
        super("MONITorCheckerService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.d("MONITor", "Service running.");
        Context context = getApplicationContext();

        MONITorDatabase database = new MONITorDatabase(context);
        ArrayList<MONITorConnection> connections = database.get_connections();

        for (MONITorConnection connection : connections) {
            check_connection(database, connection);
        }
    }

    private void check_connection(MONITorDatabase database, MONITorConnection connection) {
        String response = "";

        // make request and get response
        try {
            HttpURLConnection urlConnection = (HttpURLConnection) connection.get_url().openConnection();
            urlConnection.setUseCaches(false);
            urlConnection.setRequestProperty("Authorization", connection.get_authorization());
            InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                response += line;
            }
        } catch (IOException e) {
            e.getMessage();
        }

        // check response
        if (response != null && response.length() > 0) {
            Pattern pattern = Pattern.compile(".*Monit Service Manager.*");
            Matcher matcher = pattern.matcher(response);
            if (matcher.matches()) {
                connection.set_status("All is well.");
            } else {
                connection.set_status("Error: no match.");
            }
        } else {
            connection.set_status("Error: no result.");
        }

        connection.set_timestamp(new Date());

        // update connection in database
        database.edit_connection(connection, connection.get_status(), connection.get_timestamp());
    }
}
