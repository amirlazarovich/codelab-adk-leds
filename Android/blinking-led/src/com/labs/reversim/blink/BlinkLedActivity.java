package com.labs.reversim.blink;

import android.os.Bundle;
import android.util.Log;
import com.labs.ui.AdkActivity;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Send commands to an ADK device in a fixed interval to turn a led on and off
 *
 * @author Amir Lazarovich
 */
public class BlinkLedActivity extends AdkActivity {
    //////////////////////////////////////////
    // Constants
    //////////////////////////////////////////
    private static final String TAG = "BlinkLedActivity";

    //////////////////////////////////////////
    // Members
    //////////////////////////////////////////
    private int count;
    private Timer timer;

    //////////////////////////////////////////
    // Overrides & Implementations
    //////////////////////////////////////////
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }

    @Override
    public void onConnected() {
        super.onConnected();
        Log.d(TAG, "onConnected");
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (isConnected()) {
                    sendCommand(Commands.COMMAND_LEDS, Commands.ACTION_LED_13, (count++ % 2 == 0) ? Commands.LED_ON : Commands.LED_OFF);
                }
            }
        }, 0, 500);
    }

    @Override
    public void onDisconnected() {
        super.onDisconnected();
        Log.d(TAG, "onDisconnected");
        timer.cancel();
    }

    @Override
    protected void onSendCommand() {
        super.onSendCommand();
        Log.d(TAG, "onSendCommand");
    }

    @Override
    public void onAckReceived(boolean ack) {
        super.onAckReceived(ack);
        Log.d(TAG, "onAckReceived: " + ack);
    }
}