package com.zinno.sensortagcodepot;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.zinno.sensortaglibrary.BleService;
import com.zinno.sensortaglibrary.BleServiceBindingActivity;
import com.zinno.sensortaglibrary.sensor.TiAccelerometerSensor;
import com.zinno.sensortaglibrary.sensor.TiPeriodicalSensor;
import com.zinno.sensortaglibrary.sensor.TiSensor;
import com.zinno.sensortaglibrary.sensor.TiSensors;

public class AccelerometerActivity extends BleServiceBindingActivity {
    public static final String TAG = AccelerometerActivity.class.getSimpleName();

    private TiSensor<?> accelerationSensor;
    private TextView accelerationTextView;

    private boolean sensorEnabled = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accelerometer);

        accelerationTextView = (TextView) findViewById(R.id.tv_acceleration);

        accelerationSensor = TiSensors.getSensor(TiAccelerometerSensor.UUID_SERVICE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        BleService bleService = getBleService();
        if (bleService != null && sensorEnabled) {
            getBleService().enableSensor(getDeviceAddress(), accelerationSensor, false);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_accelerometer, menu);
        return true;
    }

    @Override
    public void onServiceDiscovered(String deviceAddress) {
        sensorEnabled = true;

        getBleService().enableSensor(getDeviceAddress(), accelerationSensor, true);
        if (accelerationSensor instanceof TiPeriodicalSensor) {
            TiPeriodicalSensor periodicalSensor = (TiPeriodicalSensor) accelerationSensor;
            periodicalSensor.setPeriod(periodicalSensor.getMinPeriod());

            getBleService().getBleManager().updateSensor(deviceAddress, accelerationSensor);
        }
    }

    @Override
    public void onDataAvailable(String deviceAddress, String serviceUuid, String characteristicUUid, String text, Object data) {
        Log.d(TAG, String.format("ServiceUUID: %s, CharacteristicUUIS: %s", serviceUuid, characteristicUUid));
        //Data as string
        Log.d(TAG, String.format("Data: %s", text));

        TiSensor<?> tiSensor = TiSensors.getSensor(serviceUuid);
        //First way to get data
        final TiAccelerometerSensor tiAccelerometerSensor = (TiAccelerometerSensor) tiSensor;
        float[] accelerationValues = tiAccelerometerSensor.getData();

        //Second way to get data
        float[] accelerationValues1 = (float[]) data;

        accelerationTextView.setText(text);

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
