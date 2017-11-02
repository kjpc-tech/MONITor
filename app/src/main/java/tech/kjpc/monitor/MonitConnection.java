package tech.kjpc.monitor;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Base64;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by kyle on 10/31/17.
 */

public class MonitConnection implements Parcelable {
    private String name;
    private URL url;
    private String username;
    private String password;
    private String status;

    public MonitConnection(String name, URL url, String username, String password) {
        this.name = name;
        this.url = url;
        this.username = username;
        this.password = password;
        this.status = "N/A";
    }

    private MonitConnection(Parcel parcel) {
        String[] data = parcel.createStringArray();
        if (data.length == 4) {
            this.name = data[0];
            try {
                this.url = new URL(data[1]);
            } catch (MalformedURLException e) {
                Log.e("MONITor", e.getMessage());
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
            Log.e("MONITor", e.getMessage());
        }
        return authorization;
    }

    protected void set_status(String status) {
        this.status = status;
    }

    protected String get_status() {
        return this.status;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeStringArray(new String[]{this.name, this.url.toString(), this.username, this.password});
    }

    public static final Parcelable.Creator<MonitConnection> CREATOR = new Parcelable.Creator<MonitConnection>() {
        @Override
        public MonitConnection createFromParcel(Parcel parcel) {
            return new MonitConnection(parcel);
        }

        @Override
        public MonitConnection[] newArray(int i) {
            return new MonitConnection[0];
        }
    };
}
