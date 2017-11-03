package tech.kjpc.monitor;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by kyle on 10/31/17.
 */

public class MONITorDatabase extends SQLiteOpenHelper {
    // https://developer.android.com/training/basics/data-storage/databases.html

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "monitor.db";

    private static final String TABLE_CONNECTIONS = "CONNECTIONS";
    private static final String KEY_ID = "ID";
    private static final String KEY_NAME = "NAME";
    private static final String KEY_URL = "URL";
    private static final String KEY_USERNAME = "USERNAME";
    private static final String KEY_PASSWORD = "PASSWORD";
    private static final String KEY_STATUS = "STATUS";
    private static final String KEY_TIMESTAMP = "TIMESTAMP";

    public MONITorDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // create connections table
        String create_connections_table = "CREATE TABLE " + TABLE_CONNECTIONS + " ( "
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_NAME + " TEXT,"
                + KEY_URL + " TEXT,"
                + KEY_USERNAME + " TEXT,"
                + KEY_PASSWORD + " TEXT,"
                + KEY_STATUS + " TEXT,"
                + KEY_TIMESTAMP + " INTEGER"
                + " )";
        sqLiteDatabase.execSQL(create_connections_table);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // drop old table if it exists
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_CONNECTIONS);

        // recreate connections table
        onCreate(sqLiteDatabase);
    }

    @Override
    public void onDowngrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // do same thing as upgrade
        onUpgrade(sqLiteDatabase, oldVersion, newVersion);
    }

    protected void add_connection(MONITorConnection connection) {
        SQLiteDatabase database = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, connection.get_name());
        values.put(KEY_URL, connection.get_url().toString());
        values.put(KEY_USERNAME, connection.get_username());
        values.put(KEY_PASSWORD, connection.get_password());
        values.put(KEY_STATUS, connection.get_status());
        values.put(KEY_TIMESTAMP, connection.get_timestamp().getTime());

        database.insert(TABLE_CONNECTIONS, null, values);
        database.close();
    }

    protected ArrayList<MONITorConnection> get_connections() {
        ArrayList<MONITorConnection> connections = new ArrayList<MONITorConnection>();

        String query = "SELECT * FROM " + TABLE_CONNECTIONS;

        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                String name = cursor.getString(1);
                URL url = null;
                try {
                    url = new URL(cursor.getString(2));
                } catch (MalformedURLException e) {
                    Log.e(MONITorMainActivity.LOG_TAG, e.getMessage());
                }
                String username = cursor.getString(3);
                String password = cursor.getString(4);
                String status = cursor.getString(5);
                Long timestamp = cursor.getLong(6);
                connections.add(new MONITorConnection(name, url, username, password, status, new Date(timestamp)));
            } while (cursor.moveToNext());
        }

        return connections;
    }

    private String[] get_matching_connection_ids(MONITorConnection connection) {
        String query = "SELECT " + KEY_ID + " FROM " + TABLE_CONNECTIONS + " WHERE " + KEY_NAME + "=? AND " + KEY_URL + "=? AND " + KEY_USERNAME + "=? AND " + KEY_PASSWORD + "=?";

        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(query, new String[]{connection.get_name(), connection.get_url().toString(), connection.get_username(), connection.get_password()});

        ArrayList<String> update_ids = new ArrayList<String>();
        if (cursor.moveToFirst()) {
            do {
                String id = cursor.getString(0);
                update_ids.add(id);
            } while (cursor.moveToNext());
        }

        String[] update_ids_array = new String[update_ids.size()];
        update_ids.toArray(update_ids_array);
        return update_ids_array;
    }

    protected void edit_connection(MONITorConnection connection, String name, URL url, String username, String password, String status, Date timestamp) {
        SQLiteDatabase database = this.getWritableDatabase();

        String[] update_ids_array = get_matching_connection_ids(connection);

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, name);
        values.put(KEY_URL, url.toString());
        values.put(KEY_USERNAME, username);
        values.put(KEY_PASSWORD, password);
        values.put(KEY_STATUS, status);
        values.put(KEY_TIMESTAMP, timestamp.getTime());

        database.update(TABLE_CONNECTIONS, values, KEY_ID + " = ?", update_ids_array);
    }

    protected void edit_connection(MONITorConnection connection, String name, URL url, String username, String password) {
        edit_connection(connection, name, url, username, password, connection.get_status(), connection.get_timestamp());
    }

    protected void edit_connection(MONITorConnection connection, String status, Date timestamp) {
        edit_connection(connection, connection.get_name(), connection.get_url(), connection.get_username(), connection.get_password(), status, timestamp);
    }

    protected void delete_connection(MONITorConnection connection) {
        SQLiteDatabase database = this.getWritableDatabase();

        String[] delete_ids_array = get_matching_connection_ids(connection);

        database.delete(TABLE_CONNECTIONS, KEY_ID + " = ?", delete_ids_array);
    }
}
