
package com.htc.cs.hackday.reborn.client;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity {
    public static final String KEY_TRANSMISSION_RATE = "com.htc.cs.hackday.reborn.client.TransmissionRate";
    private static final String LOG_TAG = "RebornClient";
    private static final int REQUEST_CODE_ENABLE_BLUETOOTH = 10001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check bluetooth.
        BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();
        if (ba != null) {
            if (ba.isEnabled()) {
                startService(new Intent(this, BluetoothService.class));
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_CODE_ENABLE_BLUETOOTH);
            }
        }

        onNewIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.d(LOG_TAG, "onNewIntent");
        super.onNewIntent(intent);
        setIntent(intent);

        // Display transmission rate.
        long transmissionRate = intent.getLongExtra(KEY_TRANSMISSION_RATE, 0);
        TextView textTransmissionRate = (TextView) findViewById(R.id.text_transmission_rate);
        if (textTransmissionRate != null) {
            textTransmissionRate.setText(String.format(getString(R.string.transmission_rate),
                    transmissionRate));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == REQUEST_CODE_ENABLE_BLUETOOTH) {
            if (resultCode == RESULT_OK) {
                startService(new Intent(this, BluetoothService.class));
            } else {
                Log.d(LOG_TAG, "User chose not to enable bluetooth. Finish now.");
                finish();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
