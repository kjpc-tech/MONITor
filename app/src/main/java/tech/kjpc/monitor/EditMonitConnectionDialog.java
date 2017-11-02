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


/**
 * Created by kyle on 11/1/17.
 */

public class EditMonitConnectionDialog extends AppCompatDialogFragment {
    private MonitConnection connection;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.connection = (MonitConnection) getArguments().getParcelable("connection");
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.AppTheme_NoActionBar));
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View layout = inflater.inflate(R.layout.add_connection_dialog, null);

        // set defaults
        EditText name = (EditText) layout.findViewById(R.id.dialog_add_name);
        EditText url = (EditText) layout.findViewById(R.id.dialog_add_url);
        EditText username = (EditText) layout.findViewById(R.id.dialog_add_username);
        EditText password = (EditText) layout.findViewById(R.id.dialog_add_password);
        if (this.connection != null) {
            try {
                name.setText(this.connection.get_name());
                url.setText(this.connection.get_url().toString());
                username.setText(this.connection.get_username());
                password.setText(this.connection.get_password());
            } catch (NullPointerException e) {
                Log.e("MONITor", e.getMessage());
            }
        }

        builder.setView(layout);
        builder.setTitle("Edit Connection");
        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                AlertDialog dialog = (AlertDialog) dialogInterface;
                String name = ((EditText) dialog.findViewById(R.id.dialog_add_name)).getText().toString();
                String url = ((EditText) dialog.findViewById(R.id.dialog_add_url)).getText().toString();
                String username = ((EditText) dialog.findViewById(R.id.dialog_add_username)).getText().toString();
                String password = ((EditText) dialog.findViewById(R.id.dialog_add_password)).getText().toString();
                
                // TODO validate this

                DatabaseHelper database = new DatabaseHelper(getActivity());
                database.edit_connection(connection, name, url, username, password);

                // reload connections
                ((MainActivity) getActivity()).reload_connections();
            }
        });
        builder.setNegativeButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                DatabaseHelper database = new DatabaseHelper(getActivity());
                database.delete_connection(connection);

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
