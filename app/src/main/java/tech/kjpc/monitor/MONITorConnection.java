package tech.kjpc.monitor;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Base64;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

/**
 * Created by kyle on 10/31/17.
 */

public class MONITorConnection implements Parcelable {
    private static final String STATUS_DEFAULT = "N/A";
    protected static final String STATUS_CHECKING = "Checking..";
    protected static final String STATUS_GOOD = "All is well.";
    protected static final String STATUS_ERROR_NO_MATCH = "Error: no match.";
    protected static final String STATUS_ERROR_NO_RESULT = "Error: no result.";
    protected static final String STATUS_NO_NETWORK = "Error: no network.";

    private static final Date TIMESTAMP_DEFAULT = new Date(2000, 0, 0, 0, 0);

    private String name;
    private URL url;
    private String username;
    private String password;
    private String status;
    private Date timestamp;

    public MONITorConnection(String name, URL url, String username, String password) {
        this.name = name;
        this.url = url;
        this.username = username;
        this.password = password;
        this.status = STATUS_DEFAULT;
        this.timestamp = TIMESTAMP_DEFAULT;
    }

    public MONITorConnection(String name, URL url, String username, String password, String status, Date timestamp) {
        this(name, url, username, password);

        this.status = status;
        this.timestamp = timestamp;
    }

    private MONITorConnection(Parcel parcel) {
        String[] data = parcel.createStringArray();
        if (data.length == 4) {
            this.name = data[0];
            try {
                this.url = new URL(data[1]);
            } catch (MalformedURLException e) {
                Log.e(MONITorMainActivity.LOG_TAG, e.getMessage());
            }
            this.username = data[2];
            this.password = data[3];
        }
    }

    protected String get_name() {
        return this.name;
    }

    protected URL get_url() {
        return this.url;
    }

    protected String get_username() {
        return this.username;
    }

    protected String get_password() {
        return this.password;
    }

    protected String get_authorization() {
        String authorization = null;
        String username_and_password = this.username + ":" + this.password;
        try {
            authorization = "Basic " + Base64.encodeToString(username_and_password.getBytes("UTF-8"), Base64.NO_WRAP);
        } catch (UnsupportedEncodingException e) {
            Log.e(MONITorMainActivity.LOG_TAG, e.getMessage());
        }
        return authorization;
    }

    protected void set_status(String status) {
        this.status = status;
    }

    protected String get_status() {
        return this.status;
    }

    protected void set_timestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    protected Date get_timestamp() {
        return this.timestamp;
    }

    protected boolean has_good_status() {
        if (this.status.equals(STATUS_GOOD)) {
            return true;
        } else {
            return false;
        }
    }

    protected boolean has_error_status() {
        switch (this.status) {
            case STATUS_ERROR_NO_MATCH:
                return true;
            case STATUS_ERROR_NO_RESULT:
                return true;
            case STATUS_NO_NETWORK:
                return true;
            default:
                return false;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeStringArray(new String[]{this.name, this.url.toString(), this.username, this.password});
    }

    public static final Parcelable.Creator<MONITorConnection> CREATOR = new Parcelable.Creator<MONITorConnection>() {
        @Override
        public MONITorConnection createFromParcel(Parcel parcel) {
            return new MONITorConnection(parcel);
        }

        @Override
        public MONITorConnection[] newArray(int i) {
            return new MONITorConnection[0];
        }
    };
}
