package com.zinno.sensortagcodepot;

import android.bluetooth.*;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.zinno.sensortaglibrary.sensor.TiAccelerometerSensor;
import com.zinno.sensortaglibrary.sensor.TiSensor;
import rx.Observable;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.subjects.PublishSubject;
import rx.subscriptions.Subscriptions;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static rx.android.schedulers.AndroidSchedulers.mainThread;

/**
 * Created on 08/28/2015.
 */
public class SimpleReadActivity extends AppCompatActivity {

  private static final String TAG = SimpleReadActivity.class.getSimpleName();

  public enum ConnBle {CONNECT, DISCONNECT}


  private PublishSubject<ConnBle> mScanSubject = PublishSubject.create();
  private PublishSubject<String>  mUiSubject   = PublishSubject.create();

  private Subscription mConnectBleSubscription = Subscriptions.empty();
  private Subscription mUiSubscription         = Subscriptions.empty();
  private Subscription mConnDeviceSubscription = Subscriptions.empty();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.simple_activity);
    ButterKnife.inject(this);

    subscribeConnectEvents();
    subscribeUiSubject();
  }

  @Override
  protected void onDestroy() {
    mConnectBleSubscription.unsubscribe();
    mUiSubscription.unsubscribe();
    mConnDeviceSubscription.unsubscribe();
    super.onDestroy();
  }

  @OnClick(R.id.btn_read_adv)
  void readAdvClick() {
    subscribeConnect();
  }

  private void subscribeConnect() {
    mConnectBleSubscription.unsubscribe();
    mScanSubject.onNext(ConnBle.CONNECT);
    mConnectBleSubscription = Observable.just(ConnBle.DISCONNECT)
        .delay(3, TimeUnit.SECONDS)
        .subscribe(new Action1<ConnBle>() {
          @Override
          public void call(ConnBle connBle) {
            mScanSubject.distinct();
          }
        });
  }

  private void subscribeConnectEvents() {
    final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
    final BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();

    mScanSubject
        .subscribe(new Action1<ConnBle>() {
          boolean flgScanning;

          @Override
          public void call(ConnBle connBle) {
            switch (connBle) {
              case CONNECT:
                Log.d(TAG, "call CONNECT");
                if (!flgScanning) bluetoothAdapter.startLeScan(mLeScanCallback);
                flgScanning = true;
                break;
              case DISCONNECT:
                Log.d(TAG, "call DISCONNECT");
                if (flgScanning) bluetoothAdapter.stopLeScan(mLeScanCallback);
                flgScanning = false;
                break;
            }
          }
        });
  }

  private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
    @Override
    public void onLeScan(BluetoothDevice bluetoothDevice, int i, byte[] bytes) {
      Log.d(TAG, "onLeScan " + bluetoothDevice);
//      if (bluetoothDevice.getAddress().equals("5C:31:3E:87:B4:1A")) {
      if (bluetoothDevice.getAddress().toUpperCase().equals("B4:99:4C:34:DE:3F")) {
        mScanSubject.onNext(ConnBle.DISCONNECT);
        final BluetoothGatt bluetoothGatt = bluetoothDevice.connectGatt(SimpleReadActivity.this, false, mGattCallback);
        mConnDeviceSubscription.unsubscribe();
        mConnDeviceSubscription = Subscriptions.create(new Action0() {
          @Override
          public void call() {
            bluetoothGatt.disconnect();
          }
        });
      }
    }
  };

  private void subscribeUiSubject() {
    mUiSubscription.unsubscribe();
    mUiSubscription = mUiSubject
        .observeOn(mainThread())
        .subscribe(new Action1<String>() {
          @Override
          public void call(String s) {
            Log.d(TAG, "call " + s);
            Toast.makeText(SimpleReadActivity.this, s, Toast.LENGTH_SHORT).show();
          }
        });
  }

  private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
      mUiSubject.onNext("onServicesDiscovered: " + gatt + " status: " + status);
      super.onServicesDiscovered(gatt, status);

      if (status == BluetoothGatt.GATT_SUCCESS) {
        BluetoothGattService service = gatt.getService(UUID.fromString(TiAccelerometerSensor.UUID_SERVICE));
        BluetoothGattCharacteristic charData = service.getCharacteristic(UUID.fromString(TiAccelerometerSensor.UUID_DATA));
        gatt.setCharacteristicNotification(charData, true);

        BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(TiAccelerometerSensor.UUID_CONFIG));
        characteristic.setValue(new byte[]{(byte) 1});
        gatt.writeCharacteristic(characteristic);

      } else {
        Log.w(TAG, "onServicesDiscovered received: " + status);
      }
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
      mUiSubject.onNext("onConnectionStateChanged: " + gatt + " status: " + status + " newState: " + newState);

      if (newState == BluetoothProfile.STATE_CONNECTED) {
        boolean flgDiscoverServices = gatt.discoverServices();
        Log.i(TAG, "Connected to GATT server.");
        Log.i(TAG, "Attempting to start service discovery:" + flgDiscoverServices);

      } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
        mUiSubject.onNext("Disconnected");
      }
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
      Log.d(TAG, "onCharacteristicChanged " + characteristic);
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
      if (status == BluetoothGatt.GATT_SUCCESS) {
        mUiSubject.onNext("onCharacteristicRead: " + characteristic);
//        broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
      }
    }

    @Override
    public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
      Log.d(TAG, "onReliableWriteCompleted " + status);
      super.onReliableWriteCompleted(gatt, status);
    }

    @Override
    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
      Log.d(TAG, "onDescriptorWrite ");
      super.onDescriptorWrite(gatt, descriptor, status);
    }

    @Override
    public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
      super.onDescriptorRead(gatt, descriptor, status);
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
      Log.d(TAG, "onCharacteristicWrite ");

      BluetoothGattService service = gatt.getService(UUID.fromString(TiAccelerometerSensor.UUID_SERVICE));
      BluetoothGattCharacteristic charData = service.getCharacteristic(UUID.fromString(TiAccelerometerSensor.UUID_DATA));
      BluetoothGattDescriptor descriptor = charData.getDescriptor(UUID.fromString(TiSensor.CHARACTERISTIC_CONFIG));
      descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
      gatt.writeDescriptor(descriptor);
    }
  };
}
