package com.labs.adk;

/**
* @author Amir Lazarovich
*/
public interface Callback {
    /**
     * Callback invoked after sending the ADK a command in order to determine whether it received the command
     *
     * @param ack
     */
    void onAckReceived(boolean ack);

    /**
     * Callback invoked when the ADK device is connected
     */
    void onConnected();

    /**
     * Callback invoked when the ADK device is disconnected
     */
    void onDisconnected();
}
