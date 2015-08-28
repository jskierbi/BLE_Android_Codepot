package com.zinno.sensortagcodepot;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.zinno.sensortaglibrary.BleService;
import com.zinno.sensortaglibrary.BleServiceBindingActivity;
import com.zinno.sensortaglibrary.sensor.TiHumiditySensor;
import com.zinno.sensortaglibrary.sensor.TiSensor;
import com.zinno.sensortaglibrary.sensor.TiSensors;

public class HumidityActivity extends BleServiceBindingActivity {
    private static final String TAG = HumidityActivity.class.getSimpleName();

    private static final double THRESHOLD = 85;


    private TiSensor<?> humiditySensor;

    private TextView sensorValuesTextView;
    private TextView additionalTextView;

    private boolean sensorEnabled;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_values);

        humiditySensor = TiSensors.getSensor(TiHumiditySensor.UUID_SERVICE);

        sensorValuesTextView = (TextView) findViewById(R.id.tv_sensor_values);
        additionalTextView = (TextView) findViewById(R.id.tv_additional);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_humidity, menu);
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();

        BleService bleService = getBleService();
        if (bleService != null && sensorEnabled) {
            bleService.enableSensor(getDeviceAddress(), humiditySensor, false);
        }

        super.onPause();
    }

    @Override
    public void onServiceDiscovered(String deviceAddress) {
        sensorEnabled = true;

        getBleService().enableSensor(getDeviceAddress(), humiditySensor, true);
    }

    @Override
    public void onDataAvailable(String deviceAddress, String serviceUuid, String characteristicUUid, String text, Object data) {
        Log.d(TAG, String.format("DeviceAddress: %s,ServiceUUID: %s, CharacteristicUUIS: %s", deviceAddress, serviceUuid, characteristicUUid));
        Log.d(TAG, String.format("Data: %s", text));

        TiSensor<?> tiSensor = TiSensors.getSensor(serviceUuid);
        final TiHumiditySensor tiHumiditySensor = (TiHumiditySensor) tiSensor;

        sensorValuesTextView.setText(String.valueOf(tiHumiditySensor.getData()));
        if (tiHumiditySensor.getData() > THRESHOLD) {
            additionalTextView.setText("Stop blowing");
        } else {
            additionalTextView.setText("Start blowing");
        }

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
