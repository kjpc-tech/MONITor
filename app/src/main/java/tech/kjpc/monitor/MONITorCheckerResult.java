package tech.kjpc.monitor;

/**
 * Created by kyle on 10/31/17.
 */

public class MONITorCheckerResult {
    protected MONITorConnectionView connection_view;
    protected String result_text;

    public MONITorCheckerResult(MONITorConnectionView connection_view, String result_text) {
        this.connection_view = connection_view;
        this.result_text = result_text;
    }
}
