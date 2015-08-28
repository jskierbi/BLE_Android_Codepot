package com.zinno.sensortagcodepot;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.zinno.sensortaglibrary.BleServiceBindingActivity;

public class AccelerometerActivity extends BleServiceBindingActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accelerometer);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_accelerometer, menu);
        return true;
    }

    @Override
    public void onServiceDiscovered(String deviceAddress) {
        super.onServiceDiscovered(deviceAddress);

        //TODO enable accelerometer sensor on BleService
        //TODO update reading frequency
    }

    @Override
    public void onDataAvailable(String deviceAddress, String serviceUuid, String characteristicUUid, String text, Object data) {
        super.onDataAvailable(deviceAddress, serviceUuid, characteristicUUid, text, data);

        //TODO Parse and present acceleratin data. Requires proper implementation of parse method in TiAccelerometerSensor
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
