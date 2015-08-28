package com.zinno.sensortagcodepot;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.zinno.sensortaglibrary.BleService;
import com.zinno.sensortaglibrary.BleServiceBindingActivity;
import com.zinno.sensortaglibrary.sensor.TiGyroscopeSensor;
import com.zinno.sensortaglibrary.sensor.TiPeriodicalSensor;
import com.zinno.sensortaglibrary.sensor.TiSensor;
import com.zinno.sensortaglibrary.sensor.TiSensors;

public class GyroscopeActivity extends BleServiceBindingActivity {

    public static final String TAG = BleServiceBindingActivity.class.getSimpleName();
    private TiSensor<?> sensor;
    private boolean sensorEnabled;
    private Dimens calibration;
    private long lastTime;
    private float rotationZ;
    private TextView gyroTextView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gyroscope);

        gyroTextView = (TextView) findViewById(R.id.tv_gyroscope);

        sensor = TiSensors.getSensor(TiGyroscopeSensor.UUID_SERVICE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_gyroscope, menu);
        return true;
    }

    @Override
    public void onServiceDiscovered(String deviceAddress) {
        sensorEnabled = true;
        getBleService().enableSensor(getDeviceAddress(), sensor, true);

        if (sensor instanceof TiPeriodicalSensor) {
            TiPeriodicalSensor periodicalSensor = (TiPeriodicalSensor) sensor;
            periodicalSensor.setPeriod(periodicalSensor.getMinPeriod());
            getBleService().getBleManager().updateSensor(deviceAddress, sensor);
        }
    }

    @Override
    protected void onPause() {
        BleService bleService = getBleService();
        if (bleService != null && sensorEnabled) {
            bleService.enableSensor(getDeviceAddress(), sensor, false);
        }

        super.onPause();
    }

    @Override
    public void onDataAvailable(String deviceAddress, String serviceUuid, String characteristicUUid, String text, Object data) {
        Dimens dimens = Dimens.fromString(text);
        if (dimens == null) {
            Log.e(TAG, "onDataAvailable: cannot create Dimens from '" + text + "'");
            return;
        }


        long currentTime = System.currentTimeMillis();

        if (calibration == null) {
            lastTime = currentTime;
            calibration = dimens;
            return;
        }

        dimens.adjust(calibration);

        Log.d(TAG, "dimens=" + dimens + " , diff " + (currentTime - lastTime));

        // in millis
        float deltaTime = currentTime - lastTime;
//        Log.d(TAG, "deltaTime=" + deltaTime);
//        Log.d(TAG, "z-lastZ=" + (z - firstZ));

        float deltaAngle = (dimens.z - calibration.z) * (deltaTime / 1000f);
//        Log.d(TAG, "deltaAngle=" + deltaAngle);

        rotationZ += deltaAngle;

        long rotation = (long) (Math.abs(rotationZ) % 360);
        gyroTextView.setText("Rotation Z" + String.valueOf(rotation));

        lastTime = currentTime;
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

    public static class Dimens {
        float x, y, z;

        public Dimens(float values[]) {
            this.x = values[0];
            this.y = values[1];
            this.z = values[2];
        }

        public void adjust(Dimens calibration) {
            this.x -= calibration.x;
            this.y -= calibration.y;
            this.z -= calibration.z;
        }

        @Override
        public String toString() {
            return "Dimens{" +
                    "x=" + x +
                    ", y=" + y +
                    ", z=" + z +
                    '}';
        }

        public static Dimens fromString(String data) {
            String split[] = data.split("\n");
            if (split.length != 3) {
                Log.e(TAG, "Dimens::fromString text split != 3");
                return null;
            }

            float values[] = new float[3];

            for (int i = 0; i < split.length; i++) {
                String valSplit[] = split[i].split("=");
                if (valSplit.length != 2) {
                    Log.e(TAG, "Dimens::fromString val split != 2: " + split[i]);
                    return null;
                }

                values[i] = Float.valueOf(valSplit[1]);
            }

            return new Dimens(values);
        }
    }
}
