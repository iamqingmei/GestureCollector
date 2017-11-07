package ageha.gesturecollector;

import android.content.Context;
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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import ageha.shared.DataMapKeys;


class DeviceClient {
    private static final String TAG = "DeviceClient";
    private static final int CLIENT_CONNECTION_TIMEOUT = 15000;
    private SparseLongArray lastSensorData;
    private static DeviceClient instance;
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

    void sendTag(final String tagName){
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                sendTagInBackground(tagName);
            }
        });
    }

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

    private void sendTagInBackground(String tagName){
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/sensors/tag");
        putDataMapReq.getDataMap().putString(DataMapKeys.TAG, tagName);
        PutDataRequest putDataRequest = putDataMapReq.asPutDataRequest();
        send(putDataRequest);
    }
    private void sendSensorDataInBackground(String sensorName, int sensorType, int accuracy, long timestamp, float[] values) {

        PutDataMapRequest dataMap = PutDataMapRequest.create("/sensors/datapoint/" + sensorType);
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
        Log.i(TAG, "send()");
        if (validateConnection()) {
            Wearable.DataApi.putDataItem(googleApiClient, putDataRequest).setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                @Override
                public void onResult(@NonNull DataApi.DataItemResult dataItemResult) {
                    Log.v(TAG, "Sending sensor data: " + dataItemResult.getStatus().isSuccess());
                }
            });
        }
//        PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi.putDataItem(googleApiClient, putDataRequest);
    }
}
