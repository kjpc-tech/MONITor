package tech.kjpc.monitor;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.net.MalformedURLException;
import java.net.URL;


/**
 * Created by kyle on 11/1/17.
 */

public class MONITorEditConnectionDialog extends AppCompatDialogFragment {
    private MONITorConnection connection;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.connection = (MONITorConnection) getArguments().getParcelable(MONITorMainActivity.CONNECTION_PARCELABLE_KEY);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.AppTheme_NoActionBar));
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_monitor_connection, null);

        // set defaults
        EditText name = (EditText) layout.findViewById(R.id.dialog_connection_name);
        EditText url = (EditText) layout.findViewById(R.id.dialog_connection_url);
        EditText username = (EditText) layout.findViewById(R.id.dialog_connection_username);
        EditText password = (EditText) layout.findViewById(R.id.dialog_connection_password);
        if (this.connection != null) {
            try {
                name.setText(this.connection.get_name());
                url.setText(this.connection.get_url().toString());
                username.setText(this.connection.get_username());
                password.setText(this.connection.get_password());
            } catch (NullPointerException e) {
                Log.e(MONITorMainActivity.LOG_TAG, e.getMessage());
            }
        }

        builder.setView(layout);
        builder.setTitle(getResources().getString(R.string.dialog_monitor_connection_edit_title));
        builder.setPositiveButton(getResources().getString(R.string.dialog_monitor_connection_edit_positive), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                AlertDialog dialog = (AlertDialog) dialogInterface;
                String name = ((EditText) dialog.findViewById(R.id.dialog_connection_name)).getText().toString();
                URL url = null;
                try {
                    url = new URL(((EditText) dialog.findViewById(R.id.dialog_connection_url)).getText().toString());
                } catch (MalformedURLException e) {
                    Log.e(MONITorMainActivity.LOG_TAG, e.getMessage());
                    Toast.makeText(getActivity(), getResources().getString(R.string.error_url_invalid), Toast.LENGTH_SHORT).show();
                }
                String username = ((EditText) dialog.findViewById(R.id.dialog_connection_username)).getText().toString();
                String password = ((EditText) dialog.findViewById(R.id.dialog_connection_password)).getText().toString();
                
                // TODO validate this

                boolean connection_edited = false;

                if (name != null && url != null && username != null && password != null) {
                    MONITorDatabase database = new MONITorDatabase(getActivity());
                    try {
                        database.edit_connection(connection, name, url, username, password);
                        connection_edited = true;
                    } catch (Exception e) {
                        Log.e(MONITorMainActivity.LOG_TAG, e.getMessage());
                    }
                }

                if (connection_edited) {
                    // reload connections
                    ((MONITorMainActivity) getActivity()).reload_connections();
                } else {
                    Toast.makeText(getActivity(), getResources().getString(R.string.error_editing_connection), Toast.LENGTH_LONG).show();
                }
            }
        });
        builder.setNegativeButton(getResources().getString(R.string.dialog_monitor_connection_edit_negative), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                MONITorDatabase database = new MONITorDatabase(getActivity());
                database.delete_connection(connection);

                // reload connections
                ((MONITorMainActivity) getActivity()).reload_connections();
            }
        });
        builder.setNeutralButton(getResources().getString(R.string.dialog_monitor_connection_edit_neutral), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        return builder.create();
    }
}
