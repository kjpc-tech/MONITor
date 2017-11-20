package tech.kjpc.monitorapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

/**
 * Created by kyle on 10/31/17.
 */

public class MONITorConnectionView extends LinearLayout {
    private MONITorConnection connection;

    public MONITorConnectionView(Context context, MONITorConnection connection) {
        super(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.view_monitor_connection, this);

        this.connection = connection;
    }

    public MONITorConnection get_connection() {
        return this.connection;
    }

    public void update_background(int color) {
        this.findViewById(R.id.view_connection_layout).setBackgroundColor(color);
    }
}
