package com.labs.adk;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import com.android.future.usb.UsbAccessory;
import com.android.future.usb.UsbManager;
import com.labs.commons.SLog;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Controls over communication with an ADK device. <br/>
 * Communication protocol: [command - 1 byte][action - 1 byte][data length - 1 byte][data - X bytes]
 *
 * @author Amir Lazarovich
 */
public class ADKManager implements Runnable {
    ///////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////
    private static final String TAG = "ADKManager";
    private static final String ACTION_USB_PERMISSION = "com.labs.adk.action.USB_PERMISSION";

    ///////////////////////////////////////////////
    // Members
    ///////////////////////////////////////////////
    private UsbManager mUsbManager;
    private UsbAccessory mAccessory;
    private ParcelFileDescriptor mFileDescriptor;
    private FileInputStream mInputStream;
    private FileOutputStream mOutputStream;

    private ExecutorService mPool;
    private Context mContext;
    private Handler mHandler;
    private Callback mCallback;
    private Thread mCommunicationThread;
    private final Object[] mLock;

    private boolean mConnected = false;
    private Timer mTimer;
    private BroadcastReceiver mUsbReceiver;

    ///////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////

    public ADKManager(Context context, Callback callback) {
        mContext = context;
        mHandler = new Handler();
        mCallback = callback;
        mPool = Executors.newCachedThreadPool();
        mLock = new Object[0];
    }


    ///////////////////////////////////////////////
    // Public
    ///////////////////////////////////////////////

    /**
     * Connect to the ADK
     */
    public void connect() {
        mTimer = new Timer();
        TimerTask reconnectTask = new TimerTask() {

            @Override
            public void run() {
                synchronized (mLock) {
                    if (!mConnected) {
                        SLog.d(TAG, "Connecting to ADK...");
                        disconnectInternal();
                        connectInternal();
                    } else {
                        mTimer.cancel();
                    }
                }
            }
        };

        try {
            mTimer.schedule(reconnectTask, 0, 10000);
        } catch (IllegalStateException e) {
            SLog.e(TAG, "Can't schedule a task on a canceled timer", e);
        }
    }

    /**
     * Disconnect from the ADK
     */
    public void disconnect() {
        SLog.d(TAG, "Disconnecting from the ADK device");
        disconnectInternal();
        mCallback.onDisconnected();
    }

    /**
     * Send command to the ADK
     *
     * @param command
     * @param action
     * @param data    May also be null if there's no data (if you read this, you rock!)
     */
    public void sendCommand(final byte command, final byte action, final byte[] data) {
        mPool.execute(new Runnable() {
            @Override
            public void run() {
                int dataLength = ((data != null) ? data.length : 0);

                ByteBuffer buffer = ByteBuffer.allocate(3 + dataLength);
                buffer.put(command);
                buffer.put(action);
                buffer.put(toUnsignedByte(dataLength));
                if (data != null) {
                    buffer.put(data);
                }

                if (mOutputStream != null) {
                    try {
                        SLog.d(TAG, "sendCommand: Sending data to ADK device: " + buffer);
                        mOutputStream.write(buffer.array());
                    } catch (IOException e) {
                        SLog.e(TAG, e, "sendCommand: Failed to send command to ADK device");
                        reconnect();
                    }
                } else {
                    SLog.d(TAG, "sendCommand: Send failed: mOutStream was null");
                    reconnect();
                }
            }
        });
    }

    /**
     * Convert <code>integer</code> to unsigned byte
     *
     * @param integer
     * @return
     */
    public static byte toUnsignedByte(int integer) {
        return (byte) (integer & 0xFF);
    }

    /**
     * Check if connected to the ADK device
     *
     * @return
     */
    public boolean isConnected() {
        return mConnected;
    }

    ///////////////////////////////////////////////
    // Overrides & Implementations
    ///////////////////////////////////////////////

    /**
     * The running thread. It takes care of the communication between the Android and the ADK
     */
    @Override
    public void run() {
        int ret;
        byte[] buffer = new byte[16384];

        // Keeps reading messages forever.
        // There are probably a lot of messages in the buffer, each message 4 bytes.
        while (true) {
            try {
                ret = mInputStream.read(buffer);
                if (ret > 0) {
                    final boolean ack = buffer[0] == 1;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mCallback.onAckReceived(ack);
                        }
                    });
                }
            } catch (Exception e) {
                break;
            }

        }
    }

    ///////////////////////////////////////////////
    // Private
    ///////////////////////////////////////////////
    /**
     * Connect to the ADK device
     */
    void connectInternal() {
        synchronized (mLock) {
            mUsbManager = UsbManager.getInstance(mContext);
            PendingIntent permissionIntent = PendingIntent.getBroadcast(mContext, 0, new Intent(ACTION_USB_PERMISSION), 0);

            // register receiver
            IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
            filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
            filter.addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED);
            mUsbReceiver = new UsbReceiver();
            mContext.registerReceiver(mUsbReceiver, filter);

            // assume the only connected usb device is our ADK
            UsbAccessory[] accessories = mUsbManager.getAccessoryList();
            UsbAccessory accessory = (accessories == null) ? null : accessories[0];

            if (accessory != null) {
                if (mUsbManager.hasPermission(accessory)) {
                    openAccessory(accessory);
                } else {
                    mUsbManager.requestPermission(accessory, permissionIntent);
                }
            }
        }
    }

    /**
     * Disconnect from the ADK device
     */
    private void disconnectInternal() {
        synchronized (mLock) {
            mConnected = false;
            if (mTimer != null) {
                mTimer.cancel();
            }

            if (mUsbReceiver != null) {
                try {
                    mContext.unregisterReceiver(mUsbReceiver);
                } catch (Exception e) {
                    SLog.e(TAG, e, "Couldn't unregister receiver");
                } finally {
                    mUsbReceiver = null;
                }

            }

            if (mFileDescriptor != null) {
                try {
                    mFileDescriptor.close();
                } catch (IOException e) {
                    SLog.e(TAG, e, "Couldn't close file descriptor");
                } finally {
                    mFileDescriptor = null;
                }
            }

            if (mInputStream != null) {
                try {
                    mInputStream.close();
                } catch (IOException e) {
                    SLog.e(TAG, e, "Couldn't close input stream");
                } finally {
                    mInputStream = null;
                }
            }

            if (mOutputStream != null) {
                try {
                    mOutputStream.close();
                } catch (IOException e) {
                    SLog.e(TAG, e, "Couldn't close output stream");
                } finally {
                    mOutputStream = null;
                }
            }

            mAccessory = null;
        }
    }

    /**
     * Try to reconnected
     */
    void reconnect() {
        SLog.i(TAG, "attempting to reconnect to ADK device");
        disconnect();
        connect();
    }

    /**
     * Open read and write to and from the ADK device
     *
     * @param accessory
     */
    void openAccessory(UsbAccessory accessory) {
        synchronized (mLock) {
            SLog.d(TAG, "Trying to attach ADK device");
            mFileDescriptor = mUsbManager.openAccessory(accessory);
            if (mFileDescriptor != null) {
                mAccessory = accessory;
                FileDescriptor fd = mFileDescriptor.getFileDescriptor();
                mInputStream = new FileInputStream(fd);
                mOutputStream = new FileOutputStream(fd);

                if (mCommunicationThread != null) {
                    mCommunicationThread.interrupt();
                }

                mCommunicationThread = new Thread(null, this, TAG);
                mCommunicationThread.start();
                mConnected = true;
                mCallback.onConnected();
                SLog.d(TAG, "Attached");
            } else {
                SLog.d(TAG, "openAccessory: accessory open failed");
            }
        }
    }

    /**
     * Run on UI thread
     *
     * @param runnable
     */
    private void runOnUiThread(Runnable runnable) {
        mHandler.post(runnable);
    }

    ///////////////////////////////////////////////
    // Inner classes
    ///////////////////////////////////////////////

    /**
     * Listens for the following events:
     * {@link #ACTION_USB_PERMISSION}, {@link com.android.future.usb.UsbManager#ACTION_USB_ACCESSORY_ATTACHED}, {@link com.android.future.usb.UsbManager#ACTION_USB_ACCESSORY_DETACHED}
     */
    private class UsbReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            SLog.d(TAG, "Got USB intent ", action);

            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (mLock) {
                    UsbAccessory accessory = UsbManager.getAccessory(intent);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        openAccessory(accessory);
                    } else {
                        SLog.d(TAG, "USB permission denied");
                    }
                }
            } else if (UsbManager.ACTION_USB_ACCESSORY_ATTACHED.equals(action)) {
                SLog.d(TAG, "BroadcastReceiver:: USB Attached");
                connect();
            } else if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) {
                UsbAccessory accessory = UsbManager.getAccessory(intent);
                if (accessory != null && accessory.equals(mAccessory)) {
                    SLog.d(TAG, "BroadcastReceiver:: USB Detached");
                    disconnect();
                }
            }
        }
    }
}