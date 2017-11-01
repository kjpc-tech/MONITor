package tech.kjpc.monitor;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

/**
 * Created by kyle on 10/31/17.
 */

public class MonitConnectionView extends LinearLayout {
    private MonitConnection connection;

    public MonitConnectionView(Context context, MonitConnection connection) {
        super(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.monit_display, this);

        this.connection = connection;
    }

    public MonitConnection get_connection() {
        return this.connection;
    }
}
