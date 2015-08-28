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

        SENSORS.put(accelerometerSensor.getServiceUUID(), accelerometerSensor);
        SENSORS.put(gyroscopeSensor.getServiceUUID(), gyroscopeSensor);
    }

    public static TiSensor<?> getSensor(String uuid) {
        return SENSORS.get(uuid);
    }
}
