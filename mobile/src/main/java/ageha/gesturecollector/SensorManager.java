package ageha.gesturecollector;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.SparseArray;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import ageha.shared.*;
import ageha.gesturecollector.data.Sensor;
import ageha.gesturecollector.data.SensorDataPoint;
import ageha.gesturecollector.event.BusProvider;
import ageha.gesturecollector.event.SensorUpdatedEvent;
import ageha.gesturecollector.event.NewSensorEvent;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;



public class SensorManager {
    private static final String TAG = "SensorManager";
    private static final int CLIENT_CONNECTION_TIMEOUT = 15000;

    private static SensorManager instance;

    private ExecutorService executorService;
    private GoogleApiClient googleApiClient;
    private boolean sensorDataLock = false;
    private SparseArray<Sensor> sensorMapping;
    private ArrayList<Sensor> sensors;
//    private SensorNames sensorNames;

    public static synchronized SensorManager getInstance(Context context) {
        if (instance == null) {
            instance = new SensorManager(context.getApplicationContext());
        }

        return instance;
    }

    //private constructor.
    private SensorManager(Context context) {
        this.sensorMapping = new SparseArray<>();
        this.sensors = new ArrayList<>();
        this.googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(Wearable.API)
                .build();

        this.executorService = Executors.newCachedThreadPool();
    }

    public ArrayList<Sensor> getSensors() {
        return (ArrayList<Sensor>) sensors.clone();
    }

//    Sensor getSensor(String id) {
//        return sensorMapping.get(id);
//    }

    private Sensor createSensor(int id, String name) {
        Sensor sensor = new Sensor(id, name);

        sensors.add(sensor);
        sensorMapping.append(id, sensor);

        BusProvider.postOnMainThread(new NewSensorEvent(sensor));

        return sensor;
    }

    private Sensor getOrCreateSensor(int sensorType, String sensorName) {

        Sensor sensor = sensorMapping.get(sensorType);

        if (sensor == null) {
            sensor = createSensor(sensorType, sensorName);
        }

        return sensor;
    }

    synchronized void addSensorData(String sensorName, int sensorType, int accuracy, Timestamp timestamp, float[] values) {
        if (sensorDataLock){
            return;
        }
        Sensor sensor = getOrCreateSensor(sensorType, sensorName);

        // TODO: We probably want to pull sensor data point objects from a pool here
        SensorDataPoint dataPoint = new SensorDataPoint(timestamp, accuracy, values);

        sensor.addDataPoint(dataPoint);

        BusProvider.postOnMainThread(new SensorUpdatedEvent(sensor, dataPoint));
    }


    private boolean validateConnection() {
        if (googleApiClient.isConnected()) {
            return true;
        }

        ConnectionResult result = googleApiClient.blockingConnect(CLIENT_CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS);

        return result.isSuccess();
    }

//    void filterBySensorId(final int sensorId) {
//        executorService.execute(new Runnable() {
//            @Override
//            public void run() {
//                filterBySensorIdInBackground(sensorId);
//            }
//        });
//    }
//
//    ;
//
//    private void filterBySensorIdInBackground(final int sensorId) {
//        Log.d(TAG, "filterBySensorId(" + sensorId + ")");
//
//        if (validateConnection()) {
//            PutDataMapRequest dataMap = PutDataMapRequest.create("/filter");
//
//            dataMap.getDataMap().putInt(DataMapKeys.FILTER, sensorId);
//            dataMap.getDataMap().putLong(DataMapKeys.TIMESTAMP, System.currentTimeMillis());
//
//            PutDataRequest putDataRequest = dataMap.asPutDataRequest();
//            Wearable.DataApi.putDataItem(googleApiClient, putDataRequest).setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
//                @Override
//                public void onResult(@NonNull DataApi.DataItemResult dataItemResult) {
//                    Log.d(TAG, "Filter by sensor " + sensorId + ": " + dataItemResult.getStatus().isSuccess());
//                }
//            });
//        }
//    }

    void startMeasurement() {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                controlMeasurementInBackground(ClientPaths.START_MEASUREMENT);
            }
        });
    }

    void stopMeasurement() {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                controlMeasurementInBackground(ClientPaths.STOP_MEASUREMENT);
            }
        });
    }

//    void getNodes(ResultCallback<NodeApi.GetConnectedNodesResult> pCallback) {
//        Wearable.NodeApi.getConnectedNodes(googleApiClient).setResultCallback(pCallback);
//    }

    private void controlMeasurementInBackground(final String path) {
        if (validateConnection()) {
            List<Node> nodes = Wearable.NodeApi.getConnectedNodes(googleApiClient).await().getNodes();

            Log.d(TAG, "Sending to nodes: " + nodes.size());

            for (Node node : nodes) {
                Log.i(TAG, "add node " + node.getDisplayName());
                Wearable.MessageApi.sendMessage(
                        googleApiClient, node.getId(), path, null
                ).setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                    @Override
                    public void onResult(@NonNull MessageApi.SendMessageResult sendMessageResult) {
                        Log.d(TAG, "controlMeasurementInBackground(" + path + "): " + sendMessageResult.getStatus().isSuccess());
                    }
                });
            }
        } else {
            Log.w(TAG, "No connection possible");
        }
    }

    public void DeleteAllSensors(){
        sensors = new ArrayList<>();

    }

    public String getSensorDataString(){
        String res = "SENSORNAME, SENSORID, SENSORACCURACY, TIMESTAMP, ACCURACY, VALUES\n";
        sensorDataLock = true;
        for (Sensor s:this.sensors){
            res += s.toString();
        }
        sensorDataLock = false;
        return res;
    }


    boolean getConnectionState(){
        return sensors.size() != 0;
    }
}
