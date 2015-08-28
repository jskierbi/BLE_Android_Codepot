package com.zinno.sensortaglibrary.sensor;

import java.util.HashMap;

/**
 * Created by steven on 9/4/13.
 */
public class TiSensors {

    private static HashMap<String, TiSensor<?>> SENSORS = new HashMap<String, TiSensor<?>>();

    static {
        final TiAccelerometerSensor accelerometerSensor = new TiAccelerometerSensor();
        final TiGyroscopeSensor gyroscopeSensor = new TiGyroscopeSensor();
        final TiHumiditySensor humiditySensor = new TiHumiditySensor();
        final TiTemperatureSensor temperatureSensor = new TiTemperatureSensor();
        final TiKeysSensor keysSensor = new TiKeysSensor();

        SENSORS.put(accelerometerSensor.getServiceUUID(), accelerometerSensor);
        SENSORS.put(gyroscopeSensor.getServiceUUID(), gyroscopeSensor);
        SENSORS.put(humiditySensor.getServiceUUID(), humiditySensor);
        SENSORS.put(temperatureSensor.getServiceUUID(), temperatureSensor);
        SENSORS.put(keysSensor.getServiceUUID(), keysSensor);
    }

    public static TiSensor<?> getSensor(String uuid) {
        return SENSORS.get(uuid);
    }
}
