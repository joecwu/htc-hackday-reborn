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

import org.apache.cordova.Config;
import org.apache.cordova.CordovaActivity;

import android.annotation.SuppressLint;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
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
    private static final String LOG_TAG = Reborn.class.getSimpleName();
    private static final String PARTIAL_WAKE_LOCK_TAG = "partialWakeLock";
    private static final int SERVER_PORT = 4225;
    private static final String CMD_TURN_ON_SCREEN = "screen_on";
    private static final String CMD_TURN_OFF_SCREEN = "screen_off";
    private static final String CMD_EXIT = "exit";
    private AsyncServer mAsyncServer;
    private AsyncNetworkSocket mAsyncClient;
    private WakeLock mPartialWakeLock;
    private View mDecorView;

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

        // Start socket server.
        if (Log.isLoggable(LOG_TAG, Log.DEBUG)) {
            Log.d(LOG_TAG, "Starting server socket.");
        }
        mAsyncServer = new AsyncServer();
        mAsyncServer.listen(null, SERVER_PORT, new RemoteControlListenCallback());
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
        super.onStart();

        if (Log.isLoggable(LOG_TAG, Log.DEBUG)) {
            Log.d(LOG_TAG, "onStart");
        }

        // Turn on the screen.
        screenOn();
    }

    @SuppressLint("Wakelock")
    @Override
    public void onDestroy() {
        super.onDestroy();

        // Disconnect client.
        if (mAsyncClient != null) {
            mAsyncClient.close();
            mAsyncClient = null;
        }

        // Release wake lock.
        if (mPartialWakeLock != null) {
            if (mPartialWakeLock.isHeld())
                mPartialWakeLock.release();
            mPartialWakeLock = null;
        }

        // Shutdown server.
        if (mAsyncServer != null) {
            mAsyncServer.stop();
            mAsyncServer = null;
        }
    }

    /**
     * {@link ListenCallback} implementation for the server socket.
     * 
     * @author samael_wang@htc.com
     */
    private class RemoteControlListenCallback implements ListenCallback {

        @Override
        public void onAccepted(AsyncSocket socket) {
            if (mAsyncClient != null) {
                if (Log.isLoggable(LOG_TAG, Log.DEBUG)) {
                    Log.d(LOG_TAG, "Closing existing client.");
                }
                mAsyncClient.close();
            }

            mAsyncClient = (AsyncNetworkSocket) socket;
            mAsyncClient.setDataCallback(new ClientDataCallback());
            mAsyncClient.setClosedCallback(new ClientCompletedCallback());
        }

        @Override
        public void onListening(AsyncServerSocket socket) {
            if (Log.isLoggable(LOG_TAG, Log.DEBUG)) {
                Log.d(LOG_TAG, "Server listening on port " + socket.getLocalPort());
            }
        }

        @Override
        public void onCompleted(Exception ex) {
            if (ex != null) {
                if (Log.isLoggable(LOG_TAG, Log.ERROR)) {
                    Log.e(LOG_TAG, "Socket closed unexpectedly: ", ex);
                }
            } else {
                if (Log.isLoggable(LOG_TAG, Log.DEBUG)) {
                    Log.d(LOG_TAG, "Socket closed.");
                }
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
                if (Log.isLoggable(LOG_TAG, Log.ERROR)) {
                    Log.e(LOG_TAG, "Client disconnected unexpectedly: ", ex);
                }
            } else {
                if (Log.isLoggable(LOG_TAG, Log.DEBUG)) {
                    Log.d(LOG_TAG, "Client disconnected.");
                }
            }

            mAsyncClient = null;
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
            if (Log.isLoggable(LOG_TAG, Log.DEBUG)) {
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
                    screenOff();
                } else if (CMD_EXIT.equals(cmd)) {
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            // Disconnect client.
                            if (mAsyncClient != null) {
                                mAsyncClient.close();
                                mAsyncClient = null;
                            }
                        }
                    });

                } else {
                    if (Log.isLoggable(LOG_TAG, Log.ERROR)) {
                        Log.e(LOG_TAG, "Unknown command: " + cmd);
                    }
                }
            }
        }
    }

    private void screenOn() {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (Log.isLoggable(LOG_TAG, Log.DEBUG)) {
                    Log.d(LOG_TAG, "Turn on screen.");
                }

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

    private void screenOff() {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (Log.isLoggable(LOG_TAG, Log.DEBUG)) {
                    Log.d(LOG_TAG, "Turn off screen.");
                }

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
}
