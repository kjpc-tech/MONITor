package tech.kjpc.monitorapp;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.Toast;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by kyle on 11/1/17.
 */

public class MONITorAddConnectionDialog extends AppCompatDialogFragment implements DialogInterface.OnShowListener {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.AppTheme_NoActionBar));
        LayoutInflater inflater = getActivity().getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.dialog_monitor_connection, null));
        builder.setTitle(getResources().getString(R.string.dialog_monitor_connection_add_title));
        builder.setPositiveButton(getResources().getString(R.string.dialog_monitor_connection_add_positive), new DialogInterface.OnClickListener() {
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

                boolean connection_added = false;

                if (name != null && url != null && username != null && password != null) {
                    MONITorDatabase database = new MONITorDatabase(getActivity());
                    try {
                        database.add_connection(new MONITorConnection(name, url, username, password));
                        connection_added = true;
                    } catch (Exception e) {
                        Log.e(MONITorMainActivity.LOG_TAG, e.getMessage());
                    }
                }

                if (connection_added) {
                    // reload connections
                    ((MONITorMainActivity) getActivity()).reload_connections();
                } else {
                    Toast.makeText(getActivity(), getResources().getString(R.string.error_adding_connection), Toast.LENGTH_LONG).show();
                }
            }
        });
        builder.setNeutralButton(getResources().getString(R.string.dialog_monitor_connection_add_neutral), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(this);
        return dialog;
    }

    @Override
    public void onShow(DialogInterface dialog) {
        try {
            ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.color_accent_dark));
            ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(getResources().getColor(R.color.color_accent_dark));
            ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.color_accent_dark));
        } catch (Exception e) {
            Log.e(MONITorMainActivity.LOG_TAG, e.getMessage());
        }
    }
}
