package com.labs.ui;

import android.app.Activity;
import android.os.Bundle;
import com.labs.adk.ADKManager;
import com.labs.adk.Callback;

/**
 * Abstract base class for screens that communicate with an ADK device
 *
 * @author Amir Lazarovich
 */
public abstract class AdkActivity extends Activity implements Callback {
    ///////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////
    private static final String TAG = "AdkActivity";

    ///////////////////////////////////////////////
    // Members
    ///////////////////////////////////////////////
    private ADKManager mADKManager;

    ///////////////////////////////////////////////
    // Activity Flow
    ///////////////////////////////////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mADKManager = new ADKManager(this, this);
    }

    @Override
    public void onResume() {
        super.onResume();
        mADKManager.connect();
    }

    @Override
    public void onPause() {
        super.onPause();
        mADKManager.disconnect();
    }

    ///////////////////////////////////////////////
    // Protected
    ///////////////////////////////////////////////
    /**
     * Send command to the ADK
     *
     * @param command
     * @param action
     * @param data
     */
    protected final void sendCommand(final byte command, final byte action, final byte[] data) {
        onSendCommand();
        mADKManager.sendCommand(command, action, data);
    }

    /**
     * Fired before sending a command to the ADK.<br/>
     * Override this method in order to handle all requests to communicate with the ADK device in a centralized place.
     * For example, you could use this method to show a progress bar on screen until received an Ack from the device
     * through the callback method {@link #onAckReceived(boolean)}
     */
    protected void onSendCommand() {

    }

    ///////////////////////////////////////////////
    // Protected
    ///////////////////////////////////////////////
    @Override
    public void onAckReceived(boolean ack) {

    }
}
