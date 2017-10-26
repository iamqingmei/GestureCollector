package ageha.gesturecollector;

import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import ageha.shared.DataMapKeys;


public class SensorReceiverService extends WearableListenerService{
    private static final String TAG = "SensorReceiverService";

    private SensorManager sensorManager;

    public SensorReceiverService(){

    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.w(TAG, "onCreate");

        sensorManager = SensorManager.getInstance(this);

    }

    @Override
    public void onPeerConnected(Node peer) {
        super.onPeerConnected(peer);
//        wearManager.sensorConnected("Connected: " + peer.getDisplayName() + " (" + peer.getId() + ")");
        Log.i(TAG, "Connected: " + peer.getDisplayName() + " (" + peer.getId() + ")");
    }

    @Override
    public void onPeerDisconnected(Node peer) {
        super.onPeerDisconnected(peer);

        Log.i(TAG, "Disconnected: " + peer.getDisplayName() + " (" + peer.getId() + ")");
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.d(TAG, "onDataChanged()");

        for (DataEvent dataEvent : dataEvents) {

            if (dataEvent.getType() == DataEvent.TYPE_CHANGED) {
                DataItem dataItem = dataEvent.getDataItem();
                Uri uri = dataItem.getUri();
                String path = uri.getPath();
                Log.i(TAG, uri.getPath());
                if (path.startsWith("/sensors/datapoint")) {
                    unpackSensorData(
                            Integer.parseInt(uri.getLastPathSegment()),
                            DataMapItem.fromDataItem(dataItem).getDataMap()
                    );
                }

                if (path.startsWith("/sensors/sensorpacks")){
                    Log.i(TAG, "sensor pack received");
                    unpackSensorPackData(DataMapItem.fromDataItem(dataItem).getDataMap());
                }

                if (path.startsWith("/sensors/sensorcount")){
                    Log.i(TAG, "sensor count received");
                    Log.i(TAG, "count" + DataMapItem.fromDataItem(dataItem).getDataMap().getInt(DataMapKeys.SENSORCOUNT));
                }



            }
        }
    }

    private void unpackSensorData(int sensorType, DataMap dataMap) {
//        Log.w("TEST", dataMap.keySet().toString()); [accuracy, timestamp, values]
        String sensorName = dataMap.getString(DataMapKeys.SENSORNAME);
        int accuracy = dataMap.getInt(DataMapKeys.ACCURACY);
        Timestamp timestamp = new Timestamp(dataMap.getLong(DataMapKeys.TIMESTAMP));
        float[] values = dataMap.getFloatArray(DataMapKeys.VALUES);

//        Log.d(TAG, "Received sensor data " + sensorType + " = " + Arrays.toString(values));

        sensorManager.addSensorData(sensorName, sensorType, accuracy, timestamp, values);
    }
//
    private void unpackSensorPackData(DataMap dataMap) {
        sensorManager.addSensorDataPack(dataMap.getString(DataMapKeys.SENSORPACK));
    }

}
