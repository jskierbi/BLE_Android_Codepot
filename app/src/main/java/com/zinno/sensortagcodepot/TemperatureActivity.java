package com.zinno.sensortagcodepot;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.zinno.sensortaglibrary.BleService;
import com.zinno.sensortaglibrary.BleServiceBindingActivity;
import com.zinno.sensortaglibrary.sensor.TiSensor;
import com.zinno.sensortaglibrary.sensor.TiSensors;
import com.zinno.sensortaglibrary.sensor.TiTemperatureSensor;

import java.util.Arrays;

public class TemperatureActivity extends BleServiceBindingActivity {
    private static final String TAG = TemperatureActivity.class.getSimpleName();

    private static final double THRESHOLD = 5;
    private static final int valuesRange = 6;

    private float ambientTemp = 0;

    private float values[] = new float[valuesRange];
    private int valuesIdx = 0;
    private long valuesCount = 0;

    private TextView sensorValuesTextView;
    private TextView additionalTextView;

    private TiSensor<?> tempSensor;
    private boolean sensorEnabled = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_values);

        tempSensor = TiSensors.getSensor(TiTemperatureSensor.UUID_SERVICE);

        sensorValuesTextView = (TextView) findViewById(R.id.tv_sensor_values);
        additionalTextView = (TextView) findViewById(R.id.tv_additional);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_humidity, menu);
        return true;
    }

    private void estimateValues() {
        float avg = 0;

        for (int idx = 0; idx < valuesRange; ++idx) {
            avg += values[idx];
        }

        avg = avg / valuesRange;

//        Log.d(TAG, "avg=" + avg + ", ambient=" + ambientTemp + ", valuesIdx=" + valuesIdx);

        if (avg - ambientTemp >= THRESHOLD) {
            additionalTextView.setText("Too close");
        } else {
            additionalTextView.setText("Too far");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        BleService bleService = getBleService();
        if (bleService != null && sensorEnabled) {
            bleService.enableSensor(getDeviceAddress(), tempSensor, false);
        }

        super.onPause();
    }

    @Override
    public void onServiceDiscovered(String deviceAddress) {
        sensorEnabled = true;

        getBleService().enableSensor(getDeviceAddress(), tempSensor, true);
    }

    @Override
    public void onDataAvailable(String deviceAddress, String serviceUuid, String characteristicUUid, String text, Object data) {
        //        Log.d(TAG, String.format("DeviceAddress: %s,ServiceUUID: %s, CharacteristicUUIS: %s", deviceAddress, serviceUuid, characteristicUUid));
//        Log.d(TAG, String.format("Data: %s", text));

        TiSensor<?> tiSensor = TiSensors.getSensor(serviceUuid);
        final TiTemperatureSensor temperatureSensor = (TiTemperatureSensor) tiSensor;

        float temp[] = temperatureSensor.getData();
        if (temp.length != 2) {
            return;
        }

        ambientTemp = temp[0];

        values[valuesIdx] = temp[1];

        sensorValuesTextView.setText(Arrays.toString(temp));
        valuesIdx = (valuesIdx + 1) % valuesRange;

        if (valuesCount > valuesRange)
            estimateValues();

        valuesCount++;
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
