
package com.htc.cs.hackday.reborn.client;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Receiver to start {@link BluetoothService} when bluetooth is available.
 * 
 * @author samael_wang@htc.com
 */
public class BluetoothStateReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(intent.getAction())
                && intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF) == BluetoothAdapter.STATE_ON) {
            context.startService(new Intent(context, BluetoothService.class));
        }
    }

}
