package com.eaglesakura.android.sensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.eaglesakura.math.Vector3;
import com.eaglesakura.util.LogUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * コンパスを取得する
 */
public class Compass {

    private final Context context;

    private final SensorManager sensorManager;

    private static final int MATRIX_SIZE = 16;

    private int compassCacheNum = 20;

    private Set<CompassUpdateListener> listeners = new HashSet<CompassUpdateListener>();

    private List<Double> orientationCaches = new ArrayList<Double>();


    public Compass(Context context) {
        this.context = context;
        this.sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
    }

    /**
     * コンパスに接続する
     */
    public void connect() {
        sensorManager.registerListener(sensorListener, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(sensorListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
    }

    /**
     * コンパスから切断する
     */
    public void disconnect() {
        sensorManager.unregisterListener(sensorListener);
    }

    public void addListener(CompassUpdateListener listener) {
        listeners.add(listener);
    }

    public void removeListener(CompassUpdateListener listener) {
        listeners.remove(listener);
    }

    private final SensorEventListener sensorListener = new SensorEventListener() {
        float[] magneticValues;
        float[] accelerometerValues;

        @Override
        public void onSensorChanged(SensorEvent event) {

            if (event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE)
                return;

            switch (event.sensor.getType()) {
                case Sensor.TYPE_MAGNETIC_FIELD:
                    // 地磁気センサ
                    magneticValues = Arrays.copyOf(event.values, event.values.length);
                    break;
                case Sensor.TYPE_ACCELEROMETER:
                    // 加速度センサ
                    accelerometerValues = Arrays.copyOf(event.values, event.values.length);
                    double len = new Vector3(accelerometerValues[0], accelerometerValues[1], accelerometerValues[2]).length();

                    if (len > 0) {
                        for (int i = 0; i < accelerometerValues.length; ++i) {
                            accelerometerValues[i] /= len;
                        }
                    }
                    break;
            }

            if (magneticValues != null && accelerometerValues != null) {

                float[] orientationValues = new float[3];
                float[] rotationMatrix = new float[MATRIX_SIZE];
                float[] inclinationMatrix = new float[MATRIX_SIZE];
                float[] remapedMatrix = new float[MATRIX_SIZE];

                // 加速度センサと地磁気センサから回転行列を取得
                SensorManager.getRotationMatrix(rotationMatrix, inclinationMatrix, accelerometerValues, magneticValues);
                SensorManager.remapCoordinateSystem(rotationMatrix, SensorManager.AXIS_X, SensorManager.AXIS_Z, remapedMatrix);
                SensorManager.getOrientation(remapedMatrix, orientationValues);

                // 0.0〜1.0の方位として扱う
                double value = orientationValues[0];

                {

                    value = (float) (value >= 0 ? Math.toDegrees(value) : 360 + Math
                            .toDegrees(value));    // (13)

                    value /= 360.0;
                }
                // 方位の正規化
                {
                    while (value < 0) {
                        value += 1.0;
                    }
                    while (value > 1.0) {
                        value -= 1.0;
                    }
                }

                orientationCaches.add(Double.valueOf(value));
                if (orientationCaches.size() > compassCacheNum) {
                    orientationCaches.remove(0);
                }

                // 平均値
                double avg = 0;
                for (Double d : orientationCaches) {
                    avg += d;
                }
                avg /= orientationCaches.size();

//                LogUtil.log(String.format("orient :: %.2f", value));

                for (CompassUpdateListener listener : listeners) {
                    listener.onCompassUpdated(Compass.this, avg);
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };


    /**
     * 向いている方向が更新されたら呼び出される
     */
    public interface CompassUpdateListener {

        /**
         * 方角が更新された
         *
         * @param compass     コンパスオブジェクト
         * @param orientation 方角が北0.0, 西 0.25, 南 0.5, 東 0.75として取得できる
         */
        void onCompassUpdated(Compass compass, double orientation);
    }
}
