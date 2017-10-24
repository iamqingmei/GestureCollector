package ageha.gesturecollector;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.SparseLongArray;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import ageha.shared.DataMapKeys;


class DeviceClient {
    private static final String TAG = "DeviceClient";
    private static final int CLIENT_CONNECTION_TIMEOUT = 15000;
    private SparseLongArray lastSensorData;
    private long lastSendTime;

    private static DeviceClient instance;
    private int filterId;
    private String sensorDataBuffer = "";
    private static ArrayList<Integer> usefulSensor = new ArrayList<>(Arrays.asList(1, 2, 4, 11, 3, 26, 17, 9, 10));
    public static DeviceClient getInstance(Context context) {
        if (instance == null) {
            instance = new DeviceClient(context.getApplicationContext());
        }

        return instance;
    }

    private GoogleApiClient googleApiClient;
    private ExecutorService executorService;

    private DeviceClient(Context context) {

        googleApiClient = new GoogleApiClient.Builder(context).addApi(Wearable.API).build();

        executorService = Executors.newCachedThreadPool();

        lastSensorData = new SparseLongArray();
    }

//    public void setSensorFilter(int filterId) {
//        Log.d(TAG, "Now filtering by sensor: " + filterId);
//
//        this.filterId = filterId;
//    }

    void sendSensorData(final String sensorName, final int sensorType, final int accuracy, final long timestamp, final float[] values) {
        long t = System.currentTimeMillis();

        long lastTimestamp = lastSensorData.get(sensorType);
        long timeAgo = t - lastTimestamp;

        if (timeAgo < 100){
                return;
        }

        lastSensorData.put(sensorType, t);

        executorService.submit(new Runnable() {
            @Override
            public void run() {
                sendSensorDataInBackground(sensorName, sensorType, accuracy, timestamp, values);
            }
        });
    }
//    void sendSensorDataPack(final String sensorDataPack){
//        long timeAgo = System.currentTimeMillis() - lastSendTime;
//        sensorDataBuffer += sensorDataPack;
//        if (timeAgo < 1000){
//            return;
//        }
//        Log.i(TAG, "sendSensorDataPack");
//        lastSendTime = System.currentTimeMillis();
//        executorService.submit(new Runnable() {
//            @Override
//            public void run() {
//                sendPackedSensorDataInBackground(sensorDataBuffer);
//            }
//        });
//        sensorDataBuffer = "";
//    }
//    private void sendPackedSensorDataInBackground(String sensorDataPack){
//        PutDataMapRequest dataMap = PutDataMapRequest.create("/sensors/sensorPack");
//        dataMap.getDataMap().putString(DataMapKeys.SENSORPACK, sensorDataPack);
//        PutDataRequest putDataRequest = dataMap.asPutDataRequest();
//        send(putDataRequest);
//    }

    private void sendSensorDataInBackground(String sensorName, int sensorType, int accuracy, long timestamp, float[] values) {
//        if (sensorType == filterId) {
//            Log.i(TAG, "Sensor " + sensorType + " = " + Arrays.toString(values));
//        } else {
//            Log.d(TAG, "Sensor " + sensorType + " = " + Arrays.toString(values));
//        }

        PutDataMapRequest dataMap = PutDataMapRequest.create("/sensors/" + sensorType);
        dataMap.getDataMap().putString(DataMapKeys.SENSORNAME, sensorName);
        dataMap.getDataMap().putInt(DataMapKeys.ACCURACY, accuracy);
        dataMap.getDataMap().putLong(DataMapKeys.TIMESTAMP, timestamp);
        dataMap.getDataMap().putFloatArray(DataMapKeys.VALUES, values);
        PutDataRequest putDataRequest = dataMap.asPutDataRequest();
        send(putDataRequest);
    }

    private boolean validateConnection() {
        if (googleApiClient.isConnected()) {
            return true;
        }

        ConnectionResult result = googleApiClient.blockingConnect(CLIENT_CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS);

        return result.isSuccess();
    }

    private void send(PutDataRequest putDataRequest) {
//        Log.i(TAG, "send()");
        if (validateConnection()) {
            Wearable.DataApi.putDataItem(googleApiClient, putDataRequest).setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                @Override
                public void onResult(@NonNull DataApi.DataItemResult dataItemResult) {
//                    Log.v(TAG, "Sending sensor data: " + dataItemResult.getStatus().isSuccess());
                }
            });
        }
    }
}
