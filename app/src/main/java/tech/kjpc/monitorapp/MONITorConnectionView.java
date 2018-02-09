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

    public void set_background_normal() {
        this.findViewById(R.id.view_connection_layout).setBackground(getResources().getDrawable(R.drawable.bordered_block));
    }

    public void set_background_active() {
        this.findViewById(R.id.view_connection_layout).setBackground(getResources().getDrawable(R.drawable.bordered_block_active));
    }
}
