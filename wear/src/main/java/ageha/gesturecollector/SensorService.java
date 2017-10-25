package ageha.gesturecollector;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;
import android.util.SparseLongArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
//import java.util.concurrent.ScheduledExecutorService;


public class SensorService extends Service implements SensorEventListener {
    private static final String TAG = "SensorService";
    private SparseLongArray lastSensorData;
    private long sensorTimeReference = 0L;
    private long myTimeReference = 0L;
    SensorManager mSensorManager;
    ArrayList<String> sensorEventBuffer;
//    private final int sensorEventBufferMaxiSize = 1000;
    private static ArrayList<Integer> usefulSensor = new ArrayList<>(Arrays.asList(1, 2, 4, 11, 3, 26, 17, 9, 10));

    private DeviceClient client;
//    private ScheduledExecutorService mScheduler;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate");
        client = DeviceClient.getInstance(this);

//        Notification.Builder builder = new Notification.Builder(this);
//        builder.setContentTitle("Sensor Dashboard");
//        builder.setContentText("Collecting sensor data..");
//        builder.setSmallIcon(ageha.gesturecollector.R.mipmap.ic_launcher);
        System.out.print("TESTING~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
//        startForeground(1, builder.build());
        startMeasurement();

        sensorEventBuffer = new ArrayList<String> ();
        lastSensorData = new SparseLongArray();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        stopMeasurement();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    protected void startMeasurement() {
        mSensorManager = ((SensorManager) getSystemService(SENSOR_SERVICE));

        if (BuildConfig.DEBUG) {
            logAvailableSensors();
        }

        List<Sensor> list = mSensorManager.getSensorList(Sensor.TYPE_ALL);
        Sensor[] sensorArray = list.toArray(new Sensor[list.size()]);
        Log.i(TAG, "Register the listener");
        for (Sensor aSensorArray : sensorArray) {
            mSensorManager.registerListener(this, aSensorArray, SensorManager.SENSOR_DELAY_FASTEST);
        }

//
    }

    private void stopMeasurement() {
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(this);
        }
//        if (mScheduler != null && !mScheduler.isTerminated()) {
//            mScheduler.shutdown();
//        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (!usefulSensor.contains(event.sensor.getType())){
            // if the sensor type is not useful, skip it
            return;
        }

        // calculate timestamp
        if(sensorTimeReference == 0L && myTimeReference == 0L) {
            sensorTimeReference = event.timestamp;
            myTimeReference = System.currentTimeMillis();
        }
        // set event timestamp to current time in milliseconds
        long time = myTimeReference +
                Math.round((event.timestamp - sensorTimeReference) / 1000000.0);

//        sensorEventBuffer.add(sensorEventToString(time,event.sensor.getType(), event.values));
//
//            if (sensorEventBuffer.size() > sensorEventBufferMaxiSize){
//                client.sendSensorDataPack(sensorEventBuffer.toString());
//                sensorEventBuffer = new ArrayList<String>();
//            }
        client.sendSensorData(event.sensor.getName(), event.sensor.getType(), event.accuracy, time, event.values);
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    /**
     * Log all available sensors to logcat
     */
    private void logAvailableSensors() {
        final List<Sensor> sensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
        Log.d(TAG, "=== LIST AVAILABLE SENSORS ===");
        Log.d(TAG, String.format(Locale.getDefault(), "|%-35s|%-38s|%-6s|", "SensorName", "StringType", "Type"));
        for (Sensor sensor : sensors) {
            Log.v(TAG, String.format(Locale.getDefault(), "|%-35s|%-38s|%-6s|", sensor.getName(), sensor.getStringType(), sensor.getType()));
        }

        Log.d(TAG, "=== LIST AVAILABLE SENSORS ===");
    }

    public String sensorEventToString(long time, int type, float[] values){
        String temp = String.valueOf(time) + ", " + String.valueOf(type);
        for (int i = 0; i < 5; i++){
            if (i<values.length){
                temp = temp + ", " + String.valueOf(values[i]);

            } else {
                temp = temp + ", ";
            }

        }
        temp += '\n';
        return temp;
    }
}
