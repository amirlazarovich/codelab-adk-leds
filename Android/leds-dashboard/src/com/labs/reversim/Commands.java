package com.labs.reversim;

/**
 * @author Amir Lazarovich
 */
public class Commands {
    // adk-commands
    public static final byte COMMAND_LEDS = 1;

    // adk-actions
    public static final byte ACTION_LED_RED = 1;
    public static final byte ACTION_LED_GREEN = 2;
    public static final byte ACTION_LED_YELLOW = 3;

    // adk-data
    public static final byte[] LED_ON = {1};
    public static final byte[] LED_OFF = {0};
}
