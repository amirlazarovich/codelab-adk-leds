package com.labs.reversim;

import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;
import com.labs.ui.AdkActivity;

/**
 * Simple leds dashboard activity for Reversim Summit 2013 code lab
 *
 * @author Amir Lazarovich
 */
public class MainActivity extends AdkActivity implements CompoundButton.OnCheckedChangeListener {
    ///////////////////////////////////////////////
    // Members
    ///////////////////////////////////////////////
    private ToggleButton mToggleRed;
    private ToggleButton mToggleGreen;
    private ToggleButton mToggleYellow;

    private ProgressBar mProgress;
    private TextView mTxtAck;

    ///////////////////////////////////////////////
    // Activity Flow
    ///////////////////////////////////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        init();
    }

    private void init() {
        mProgress = (ProgressBar) findViewById(R.id.progress);
        mTxtAck = (TextView) findViewById(R.id.ack);
        mToggleRed = (ToggleButton) findViewById(R.id.toggle_red);
        mToggleGreen = (ToggleButton) findViewById(R.id.toggle_green);
        mToggleYellow = (ToggleButton) findViewById(R.id.toggle_yellow);

        // listeners
        mToggleRed.setOnCheckedChangeListener(this);
        mToggleGreen.setOnCheckedChangeListener(this);
        mToggleYellow.setOnCheckedChangeListener(this);
    }

    ///////////////////////////////////////////////
    // Overrides & Implementations
    ///////////////////////////////////////////////

    @Override
    protected void onSendCommand() {
        mProgress.setVisibility(View.VISIBLE);
        mTxtAck.setVisibility(View.INVISIBLE);
        enableButtons(false);
    }

    @Override
    public void onAckReceived(boolean ack) {
        enableButtons(ack);
        mTxtAck.setTextColor(ack ?
                getResources().getColor(R.color.holo_green_light) :
                getResources().getColor(R.color.holo_red_light));
        mTxtAck.setText(String.format("Ack received: %b", ack));
        mProgress.setVisibility(View.INVISIBLE);
        mTxtAck.setVisibility(View.VISIBLE);
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
        switch (compoundButton.getId()) {
            case R.id.toggle_red:
                sendCommand(Commands.COMMAND_LEDS, Commands.ACTION_LED_RED, checked ? Commands.LED_ON : Commands.LED_OFF);
                break;

            case R.id.toggle_green:
                sendCommand(Commands.COMMAND_LEDS, Commands.ACTION_LED_GREEN, checked ? Commands.LED_ON : Commands.LED_OFF);
                break;

            case R.id.toggle_yellow:
                sendCommand(Commands.COMMAND_LEDS, Commands.ACTION_LED_YELLOW, checked ? Commands.LED_ON : Commands.LED_OFF);
                break;
        }
    }

    ///////////////////////////////////////////////
    // Private
    ///////////////////////////////////////////////
    private void enableButtons(boolean enabled) {
        mToggleRed.setEnabled(enabled);
        mToggleGreen.setEnabled(enabled);
        mToggleYellow.setEnabled(enabled);
    }
}
