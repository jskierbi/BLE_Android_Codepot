package com.zinno.sensortaglibrary;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.zinno.sensortaglibrary.ble.BleServiceListener;

import java.util.ArrayList;
import java.util.List;

public abstract class BleServiceBindingActivity extends AppCompatActivity
        implements BleServiceListener,
        ServiceConnection {
    private final static String TAG = BleServiceBindingActivity.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    public static final String EXTRAS_DEVICE_NAMES = "DEVICE_NAMES";
    public static final String EXTRAS_DEVICE_ADDRESSES = "DEVICE_ADDRESSES";

    private String deviceName;
    protected String deviceAddress;
    private BleService bleService;

    private ArrayList<String> deviceNames;
    protected List<String> deviceAddresses;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Intent intent = getIntent();
        deviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        deviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        deviceNames = intent.getStringArrayListExtra(EXTRAS_DEVICE_NAMES);
        deviceAddresses = intent.getStringArrayListExtra(EXTRAS_DEVICE_ADDRESSES);
    }

    @Override
    protected void onStart() {
        super.onStart();

        final Intent gattServiceIntent = new Intent(this, BleService.class);
        bindService(gattServiceIntent, this, BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (bleService != null) {
            bleService.getBleManager().disconnect();
        }

        unbindService(this);
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getDeviceAddress() {
        return deviceAddress;
    }

    public ArrayList<String> getDeviceNames() {
        return deviceNames;
    }

    public List<String> getDeviceAddresses() {
        return deviceAddresses;
    }

    public BleService getBleService() {
        return bleService;
    }

    @Override
    public void onConnected(String deviceAddress) {
    }

    @Override
    public void onDisconnected(String deviceAddress) {
    }

    @Override
    public void onServiceDiscovered(String deviceAddress) {
    }

    @Override
    public void onDataAvailable(String deviceAddress, String serviceUuid, String characteristicUUid, String text, Object data) {
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        bleService = ((BleService.LocalBinder) service).getService();
        bleService.setServiceListener(this);

        if (!bleService.getBleManager().initialize(getBaseContext())) {
            Log.e(TAG, "Unable to initialize Bluetooth");
            finish();
            return;
        }

        // Automatically connects to the device upon successful start-up initialization.
        if (deviceAddresses != null) {
            for (String address : deviceAddresses) {
                bleService.getBleManager().connect(getBaseContext(), address);
            }
        } else {
            bleService.getBleManager().connect(getBaseContext(), deviceAddress);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        bleService = null;
        //TODO: show toast
    }
}
