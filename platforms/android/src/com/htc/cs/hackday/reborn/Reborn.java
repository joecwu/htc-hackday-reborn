/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
 */

package com.htc.cs.hackday.reborn;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.apache.cordova.Config;
import org.apache.cordova.CordovaActivity;

import android.annotation.SuppressLint;
import android.app.admin.DevicePolicyManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.koushikdutta.async.AsyncNetworkSocket;
import com.koushikdutta.async.AsyncServer;
import com.koushikdutta.async.AsyncServerSocket;
import com.koushikdutta.async.AsyncSocket;
import com.koushikdutta.async.ByteBufferList;
import com.koushikdutta.async.DataEmitter;
import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.callback.DataCallback;
import com.koushikdutta.async.callback.ListenCallback;

public class Reborn extends CordovaActivity {
    private static final String LOG_TAG = "Reborn";
    private static final String PARTIAL_WAKE_LOCK_TAG = "partialWakeLock";
    private static final int COMMAND_SERVER_PORT = 4225;
    private static final String CMD_TURN_ON_SCREEN = "screen_on";
    private static final String CMD_TURN_OFF_SCREEN = "screen_off";
    private static final String CMD_EXIT = "exit";
    private static final int REQUEST_CODE_ENABLE_BLUETOOTH = 10001;
    private static final UUID BLUETOOTH_UUID = UUID
            .fromString("7136EB1F-0520-4615-A94D-CF235C5A7702");
    private static final int BLUETOOTH_WRITE_BUFFER_SIZE = 990;
    private static final int BLUETOOTH_WRITE_PERIOD_MILLISECS = 500;
    private View mDecorView;
    private AsyncServer mCommandServer;
    private AsyncNetworkSocket mCommandClient;
    private WakeLock mPartialWakeLock;
    private BluetoothAdapter mBluetoothAdapter;

    @SuppressLint("InlinedApi")
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        super.init();
        // getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        // Set by <content src="index.html" /> in config.xml
        mDecorView = getWindow().getDecorView();
        mDecorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        super.loadUrl(Config.getStartUrl());
        // super.loadUrl("file:///android_asset/www/index.html");

        // Acquire CPU wake lock.
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        mPartialWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PARTIAL_WAKE_LOCK_TAG);

        // Start command server.
        Log.d(LOG_TAG, "Starting command server.");
        mCommandServer = new AsyncServer();
        mCommandServer.listen(null, COMMAND_SERVER_PORT, new RemoteControlListenCallback());

        // Check bluetooth.
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter != null) {
            if (mBluetoothAdapter.isEnabled()) {
                tryConnectBluetooth();
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_CODE_ENABLE_BLUETOOTH);
            }
        }

        // Start bluetooth receiver.
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        registerReceiver(new BluetoothConnectionReceiver(), filter);
    }

    /**
     * Try to connect to bluetooth devices.
     */
    private void tryConnectBluetooth() {
        Log.d(LOG_TAG, "tryConnectBluetooth");
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        // Turn on / off screen accordingly.
        if (pairedDevices.size() > 0) {

            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                Log.d(LOG_TAG, "Bluetooth device: " + device.getName()
                        + ": " + device.getAddress());

                // Try to connect each.
                AsyncTask.THREAD_POOL_EXECUTOR.execute(new ConnectBluetoothRunnable(device));
            }
        } else {
            turnScreenOff();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == REQUEST_CODE_ENABLE_BLUETOOTH) {
            if (resultCode == RESULT_OK && mBluetoothAdapter.isEnabled()) {
                tryConnectBluetooth();
            } else {
                Log.d(LOG_TAG, "User chose not to enable bluetooth. Finish now.");
                finish();
            }
        }
    }

    @SuppressLint("InlinedApi")
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            mDecorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    @Override
    protected void onStart() {
        Log.d(LOG_TAG, "onStart");
        super.onStart();

        // Turn on the screen.
        turnScreenOn();
    }

    @SuppressLint("Wakelock")
    @Override
    public void onDestroy() {
        super.onDestroy();

        // Release wake lock.
        if (mPartialWakeLock != null) {
            if (mPartialWakeLock.isHeld())
                mPartialWakeLock.release();
            mPartialWakeLock = null;
        }

        disconnectCommandClient();
        shutdownCommandServer();
    }

    /**
     * Disconnect the command client, if any.
     */
    private synchronized void disconnectCommandClient() {
        // Disconnect client.
        if (mCommandClient != null) {
            mCommandClient.close();
            mCommandClient = null;
        }
    }

    /**
     * Shutdown command server.
     */
    private synchronized void shutdownCommandServer() {
        // Shutdown server.
        if (mCommandServer != null) {
            mCommandServer.stop();
            mCommandServer = null;
        }
    }

    /**
     * Turn on device screen.
     */
    private void turnScreenOn() {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                Log.d(LOG_TAG, "Turn on screen.");

                // Unlock the device and make the screen as bright as possible.
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_FULLSCREEN
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                WindowManager.LayoutParams params = getWindow().getAttributes();
                params.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL;
                getWindow().setAttributes(params);
            }
        });

    }

    /**
     * Turn off the device screen.
     */
    private void turnScreenOff() {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                Log.d(LOG_TAG, "Turn off screen.");

                // Make the screen as dark as possible.
                WindowManager.LayoutParams params = getWindow().getAttributes();
                params.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_OFF;
                getWindow().setAttributes(params);

                // Lock the screen, if possible.
                DevicePolicyManager dpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
                if (dpm.isAdminActive(new ComponentName(Reborn.this,
                        RebornDeviceAdminReceiver.class))) {
                    dpm.lockNow();
                }
            }
        });
    }

    @SuppressWarnings("unused")
    private String windowFlagsToString(int flags) {
        return new StringBuilder("flags {").append("FLAG_FULLSCREEN=")
                .append((flags &= WindowManager.LayoutParams.FLAG_FULLSCREEN) != 0)
                .append(", FLAG_TURN_SCREEN_ON=")
                .append((flags &= WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON) != 0)
                .append(", FLAG_KEEP_SCREEN_ON=")
                .append((flags &= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) != 0)
                .append(", FLAG_SHOW_WHEN_LOCKED=")
                .append((flags &= WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED) != 0)
                .append(", FLAG_DISMISS_KEYGUARD=")
                .append((flags &= WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD) != 0)
                .append("}").toString();
    }

    /**
     * {@link ListenCallback} implementation for the server socket.
     * 
     * @author samael_wang@htc.com
     */
    private class RemoteControlListenCallback implements ListenCallback {

        @Override
        public void onAccepted(AsyncSocket socket) {
            if (mCommandClient != null) {
                Log.d(LOG_TAG, "Closing existing client.");
                mCommandClient.close();
            }

            mCommandClient = (AsyncNetworkSocket) socket;
            mCommandClient.setDataCallback(new ClientDataCallback());
            mCommandClient.setClosedCallback(new ClientCompletedCallback());
        }

        @Override
        public void onListening(AsyncServerSocket socket) {
            Log.d(LOG_TAG, "Server listening on port " + socket.getLocalPort());
        }

        @Override
        public void onCompleted(Exception ex) {
            if (ex != null) {
                Log.e(LOG_TAG, "Socket closed unexpectedly: ", ex);
            } else {
                Log.d(LOG_TAG, "Socket closed.");
            }
        }
    }

    /**
     * {@link CompletedCallback} implementation for the accepted client.
     * 
     * @author samael_wang@htc.com
     */
    private class ClientCompletedCallback implements CompletedCallback {
        @Override
        public void onCompleted(Exception ex) {
            if (ex != null) {
                Log.e(LOG_TAG, "Client disconnected unexpectedly: ", ex);
            } else {
                Log.d(LOG_TAG, "Client disconnected.");
            }

            mCommandClient = null;
        }
    }

    /**
     * {@link DataCallback} implementation for the accepted client.
     * 
     * @author samael_wang@htc.com
     */
    private class ClientDataCallback implements DataCallback {

        @Override
        public void onDataAvailable(DataEmitter emitter, ByteBufferList bb) {
            String cmd = bb.readString().trim();
            Log.d(LOG_TAG, "Command received: " + cmd);

            if (CMD_TURN_ON_SCREEN.equals(cmd)) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        recreate();
                    }
                });
            } else if (CMD_TURN_OFF_SCREEN.equals(cmd)) {
                turnScreenOff();
            } else if (CMD_EXIT.equals(cmd)) {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        // Disconnect client.
                        if (mCommandClient != null) {
                            mCommandClient.close();
                            mCommandClient = null;
                        }
                    }
                });

            } else {
                Log.e(LOG_TAG, "Unknown command: " + cmd);
            }
        }
    }

    /**
     * Receiver to trigger when a bluetooth device is connected.
     * 
     * @author samael_wang@htc.com
     */
    private class BluetoothConnectionReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(LOG_TAG, "action=" + intent.getAction());
            tryConnectBluetooth();
        }

    }

    /**
     * Runnable to connect to a bluetooth device and keep sending useless data.
     * 
     * @author samael_wang@htc.com
     */
    private class ConnectBluetoothRunnable implements Runnable {
        private BluetoothDevice mmBluetoothDevice;

        public ConnectBluetoothRunnable(BluetoothDevice device) {
            mmBluetoothDevice = device;
        }

        @Override
        public void run() {
            // Cancel discovery because it will slow down the connection
            mBluetoothAdapter.cancelDiscovery();

            BluetoothSocket socket;
            try {
                socket = mmBluetoothDevice
                        .createRfcommSocketToServiceRecord(BLUETOOTH_UUID);
                turnScreenOn();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error creating bluetooth socket: ", e);
                turnScreenOff();
                return;
            }

            try {
                /*
                 * Connect the device through the socket. This will block until
                 * it succeeds or throws an exception.
                 */
                socket.connect();
                flooding(socket);
            } catch (IOException connectException) {
                Log.e(LOG_TAG, "Error connecting bluetooth: ", connectException);
                turnScreenOff();
            } finally {
                try {
                    socket.close();
                } catch (IOException closeException) {
                    Log.e(LOG_TAG, "Error closing bluetooth socket: ", closeException);
                }
            }
        }

        private void flooding(BluetoothSocket socket) throws IOException {
            byte[] buffer = new byte[BLUETOOTH_WRITE_BUFFER_SIZE];
            OutputStream output = socket.getOutputStream();
            while (true) {
                // Generate random buffer content.
                new Random().nextBytes(buffer);

                // Write buffer until exceeds write period.
                long ts = System.currentTimeMillis();
                long count = 0;
                long tw;
                do {
                    output.write(buffer);
                    tw = System.currentTimeMillis() - ts;
                    count++;
                } while (tw < BLUETOOTH_WRITE_PERIOD_MILLISECS);

                // Calculate the (rough) transmission rate.
                long bytes = BLUETOOTH_WRITE_BUFFER_SIZE * count;
                long kbps = bytes / tw;
                Log.d(LOG_TAG, bytes + " bytes sent in " + tw + " ms. "
                        + "speed=" + kbps + " KB/s");
            }
        }

    }

}
