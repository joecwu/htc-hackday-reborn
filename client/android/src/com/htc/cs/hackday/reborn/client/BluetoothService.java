
package com.htc.cs.hackday.reborn.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import android.app.IntentService;
import android.app.Notification;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.util.Log;

/**
 * Service to keep listening and operating bluetooth connections.
 * 
 * @author samael_wang@htc.com
 */
public class BluetoothService extends IntentService {
    private static final String LOG_TAG = "RebornClient";
    private static final int NOTIFICATION_ID = 10001;
    private static final UUID BLUETOOTH_UUID = UUID
            .fromString("7136EB1F-0520-4615-A94D-CF235C5A7702");
    private static final String BLUETOOTH_SERVICE_NAME = "Reborn";
    private static final int BLUETOOTH_READ_BUFFER_SIZE = 990;
    private static final int BLUETOOTH_READ_PERIOD_MILLISECS = 500;
    private BluetoothAdapter mBluetoothAdapter;

    public BluetoothService() {
        super(BluetoothService.class.getSimpleName());
    }

    @Override
    public void onCreate() {
        Log.d(LOG_TAG, "onCreate");
        super.onCreate();

        // Start as a foreground service.
        startForeground(NOTIFICATION_ID, new Notification());

        // Check bluetooth.
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter != null) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                enableBtIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(enableBtIntent);
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(LOG_TAG, "onHandleIntent");

        BluetoothServerSocket serverSocket;
        try {
            serverSocket = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(
                    BLUETOOTH_SERVICE_NAME, BLUETOOTH_UUID);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Failed to listen rfcomm: ", e);
            return;
        }

        BluetoothSocket socket;
        try {
            while ((socket = serverSocket.accept()) != null) {
                readSocket(socket);
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error accepting incoming socket connection: ", e);
        }
    }

    private void readSocket(BluetoothSocket socket) {
        try {
            InputStream input = socket.getInputStream();
            byte[] buffer = new byte[BLUETOOTH_READ_BUFFER_SIZE];

            while (true) {
                // Read buffer until exceeds write period.
                long ts = System.currentTimeMillis();
                long bytes = 0;
                long tr;
                do {
                    int count = input.read(buffer);
                    tr = System.currentTimeMillis() - ts;
                    bytes += count;
                } while (tr < BLUETOOTH_READ_PERIOD_MILLISECS);

                // Calculate the (rough) transmission rate.
                long kbps = bytes / tr;
                Log.d(LOG_TAG, bytes + " bytes received in " + tr + " ms. "
                        + "speed=" + kbps + " KB/s");

                // Pass the result to the activity.
                Intent mainActivityIntent = new Intent(this, MainActivity.class);
                mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mainActivityIntent.putExtra(MainActivity.KEY_TRANSMISSION_RATE, kbps);
                startActivity(mainActivityIntent);
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error reading socket: ", e);
        }
    }
}
