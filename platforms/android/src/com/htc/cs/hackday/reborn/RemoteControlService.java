
package com.htc.cs.hackday.reborn;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.koushikdutta.async.AsyncNetworkSocket;
import com.koushikdutta.async.AsyncServer;
import com.koushikdutta.async.AsyncServerSocket;
import com.koushikdutta.async.AsyncSocket;
import com.koushikdutta.async.ByteBufferList;
import com.koushikdutta.async.DataEmitter;
import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.callback.DataCallback;
import com.koushikdutta.async.callback.ListenCallback;

/**
 * Service to accept remote commands through network.
 * 
 * @author samael_wang@htc.com
 */
public class RemoteControlService extends Service {
    private static final String TAG = RemoteControlService.class.getName();
    private static final int SERVER_PORT = 4225;
    private AsyncServer mAsyncServer;
    private AsyncNetworkSocket mAsyncClient;

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate() {
        super.onCreate();

        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "Creating service.");
        }

        // Start as a foreground service.
        startForeground(0, new Notification.Builder(this)
                .setSmallIcon(R.drawable.icon).getNotification());

        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "Starting server socket.");
        }
        mAsyncServer = new AsyncServer();
        mAsyncServer.listen(null, SERVER_PORT, new RemoteControlListenCallback());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "Starting service.");
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
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
                if (Log.isLoggable(TAG, Log.DEBUG)) {
                    Log.d(TAG, "Closing existing client.");
                }
                mAsyncClient.close();
            }

            mAsyncClient = (AsyncNetworkSocket) socket;
            mAsyncClient.setDataCallback(new ClientDataCallback());
            mAsyncClient.setClosedCallback(new ClientCompletedCallback());
        }

        @Override
        public void onListening(AsyncServerSocket socket) {
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "Server listening on port " + socket.getLocalPort());
            }
        }

        @Override
        public void onCompleted(Exception ex) {
            if (ex != null) {
                if (Log.isLoggable(TAG, Log.ERROR)) {
                    Log.e(TAG, "Socket closed unexpectedly: ", ex);
                }
            } else {
                if (Log.isLoggable(TAG, Log.DEBUG)) {
                    Log.d(TAG, "Socket closed.");
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
                if (Log.isLoggable(TAG, Log.ERROR)) {
                    Log.e(TAG, "Client disconnected unexpectedly: ", ex);
                }
            } else {
                if (Log.isLoggable(TAG, Log.DEBUG)) {
                    Log.d(TAG, "Client disconnected.");
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
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "Data received: " + bb.readString());
                
                // TODO parse client commands.
            }
        }

    }
}
