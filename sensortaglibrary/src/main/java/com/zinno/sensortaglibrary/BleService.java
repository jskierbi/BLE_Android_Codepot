package com.zinno.sensortaglibrary;

import android.app.Service;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import com.zinno.sensortaglibrary.ble.BleManager;
import com.zinno.sensortaglibrary.ble.BleServiceListener;
import com.zinno.sensortaglibrary.sensor.TiSensor;

import java.util.List;

/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */
public class BleService extends Service implements BleServiceListener {
    @SuppressWarnings("UnusedDeclaration")
    private final static String TAG = BleService.class.getSimpleName();

    public class LocalBinder extends Binder {
        public BleService getService() {
            return BleService.this;
        }
    }

    private final IBinder binder = new LocalBinder();

    private final Handler uiThreadHandler = new Handler(Looper.getMainLooper());
    private final BleManager bleManager = new BleManager();
    private BleServiceListener serviceListener;

    @Override
    public void onCreate() {
        super.onCreate();

        bleManager.setServiceListener(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        bleManager.close();
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        bleManager.disconnect();
        bleManager.close();
    }

    public void updateSensor(String deviceAddress, TiSensor<?> sensor) {
        bleManager.updateSensor(deviceAddress, sensor);
    }

    public BleManager getBleManager() {
        return bleManager;
    }

    public void setServiceListener(BleServiceListener listener) {
        serviceListener = listener;
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param sensor  sensor to be enabled/disabled
     * @param enabled If true, enable notification.  False otherwise.
     */
    public void enableSensor(String address, TiSensor<?> sensor, boolean enabled) {
        bleManager.enableSensor(address, sensor, enabled);
    }

    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices(String address) {
        return bleManager.getSupportedGattServices(address);
    }

    @Override
    public void onConnected(final String deviceAddress) {
        uiThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                if (serviceListener != null) {
                    serviceListener.onConnected(deviceAddress);
                }
            }
        });
    }

    @Override
    public void onDisconnected(final String deviceAddress) {
        uiThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                if (serviceListener != null) {
                    serviceListener.onDisconnected(deviceAddress);
                }
            }
        });
    }

    @Override
    public void onServiceDiscovered(final String deviceAddress) {
        uiThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                if (serviceListener != null) {
                    serviceListener.onServiceDiscovered(deviceAddress);
                }
            }
        });
    }

    @Override
    public void onDataAvailable(final String deviceAddress, final String serviceUuid, final String characteristicUuid,
                                final String text, final Object data) {

        uiThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                if (serviceListener != null) {
                    serviceListener.onDataAvailable(deviceAddress, serviceUuid, characteristicUuid, text, data);
                }
            }
        });
    }

}
