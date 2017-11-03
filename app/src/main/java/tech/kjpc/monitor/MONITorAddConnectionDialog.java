package tech.kjpc.monitor;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.widget.EditText;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by kyle on 11/1/17.
 */

public class MONITorAddConnectionDialog extends AppCompatDialogFragment {
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
                String url = ((EditText) dialog.findViewById(R.id.dialog_connection_url)).getText().toString();
                String username = ((EditText) dialog.findViewById(R.id.dialog_connection_username)).getText().toString();
                String password = ((EditText) dialog.findViewById(R.id.dialog_connection_password)).getText().toString();

                // TODO validate this

                MONITorDatabase database = new MONITorDatabase(getActivity());
                try {
                    database.add_connection(new MONITorConnection(name, new URL(url), username, password));
                } catch (MalformedURLException e) {
                    Log.e(MONITorMainActivity.LOG_TAG, e.getMessage());
                }

                // reload connections
                ((MONITorMainActivity) getActivity()).reload_connections();
            }
        });
        builder.setNeutralButton(getResources().getString(R.string.dialog_monitor_connection_add_neutral), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        return builder.create();
    }
}
