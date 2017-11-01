package tech.kjpc.monitor;

/**
 * Created by kyle on 10/31/17.
 */

public class MonitCheckerResult {
    protected MonitConnectionView connection_view;
    protected String result_text;

    public MonitCheckerResult(MonitConnectionView connection_view, String result_text) {
        this.connection_view = connection_view;
        this.result_text = result_text;
    }
}
