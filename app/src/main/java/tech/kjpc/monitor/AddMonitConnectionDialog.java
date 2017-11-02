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

public class AddMonitConnectionDialog extends AppCompatDialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.AppTheme_NoActionBar));
        LayoutInflater inflater = getActivity().getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.add_connection_dialog, null));
        builder.setTitle("Add Connection");
        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                AlertDialog dialog = (AlertDialog) dialogInterface;
                String name = ((EditText) dialog.findViewById(R.id.dialog_add_name)).getText().toString();
                String url = ((EditText) dialog.findViewById(R.id.dialog_add_url)).getText().toString();
                String username = ((EditText) dialog.findViewById(R.id.dialog_add_username)).getText().toString();
                String password = ((EditText) dialog.findViewById(R.id.dialog_add_password)).getText().toString();

                // TODO validate this

                DatabaseHelper database = new DatabaseHelper(getActivity());
                try {
                    database.add_connection(new MonitConnection(name, new URL(url), username, password));
                } catch (MalformedURLException e) {
                    Log.e("MONITor", e.getMessage());
                }

                // reload connections
                ((MainActivity) getActivity()).reload_connections();
            }
        });
        builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        return builder.create();
    }
}
