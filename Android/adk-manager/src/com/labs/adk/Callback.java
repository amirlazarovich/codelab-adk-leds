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
}
