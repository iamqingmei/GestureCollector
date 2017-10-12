package ageha.gesturecollector;

import android.net.Uri;
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


public class SensorReceiverService extends WearableListenerService implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{
    private static final String TAG = "SensorReceiverService";

    public static final String CONFIG_START = "config/start";
    public static final String CONFIG_STOP = "config/stop";

    private static final String START_ACTIVITY_PATH = "/start-activity";
    private static final String DATA_ITEM_RECEIVED_PATH = "/data-item-received";

    private SensorManager sensorManager;
    private WearManager wearManager;
    GoogleApiClient mGoogleApiClient;

    public SensorReceiverService(){

    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.w(TAG, "onCreate");

        if(null == mGoogleApiClient) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Wearable.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
            Log.v(TAG, "GoogleApiClient created");
        }

        if(!mGoogleApiClient.isConnected()){
            mGoogleApiClient.connect();
            Log.v(TAG, "Connecting to GoogleApiClient..");
        }

        sensorManager = SensorManager.getInstance(this);
        wearManager = WearManager.getInstance(this);
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
//
        for (DataEvent dataEvent : dataEvents) {
            if (dataEvent.getType() == DataEvent.TYPE_CHANGED) {
                DataItem dataItem = dataEvent.getDataItem();
                Uri uri = dataItem.getUri();
                String path = uri.getPath();

                if (path.startsWith("/sensors/")) {
                    unpackSensorData(
                            Integer.parseInt(uri.getLastPathSegment()),
                            DataMapItem.fromDataItem(dataItem).getDataMap()
                    );
                }
            }
        }

//        if (Log.isLoggable(TAG, Log.DEBUG)) {
//            Log.d(TAG, "onDataChanged: " + dataEvents);
//        }
//
//        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(this)
//                .addApi(Wearable.API)
//                .build();
//
//        ConnectionResult connectionResult =
//                googleApiClient.blockingConnect(30, TimeUnit.SECONDS);
//
//        if (!connectionResult.isSuccess()) {
//            Log.e(TAG, "Failed to connect to GoogleApiClient.");
//            return;
//        }
//
//        // Loop through the events and send a message
//        // to the node that created the data item.
//        for (DataEvent event : dataEvents) {
//            Uri uri = event.getDataItem().getUri();
//
//            // Get the node id from the host value of the URI
//            String nodeId = uri.getHost();
//            // Set the data of the message to be the bytes of the URI
//            byte[] payload = uri.toString().getBytes();
//
//            // Send the RPC
//            Wearable.MessageApi.sendMessage(googleApiClient, nodeId,
//                    DATA_ITEM_RECEIVED_PATH, payload);
//        }
    }

    private void unpackSensorData(int sensorType, DataMap dataMap) {
//        Log.w("TEST", dataMap.keySet().toString()); [accuracy, timestamp, values]

        int accuracy = dataMap.getInt(DataMapKeys.ACCURACY);
        Timestamp timestamp = new Timestamp(dataMap.getLong(DataMapKeys.TIMESTAMP));
        float[] values = dataMap.getFloatArray(DataMapKeys.VALUES);

        Log.d(TAG, "Received sensor data " + sensorType + " = " + Arrays.toString(values));

        sensorManager.addSensorData(sensorType, accuracy, timestamp, values);
    }
//
    @Override
    public void onDestroy() {

        Log.v(TAG, "Destroyed");

        if(null != mGoogleApiClient){
            if(mGoogleApiClient.isConnected()){
                mGoogleApiClient.disconnect();
                Log.v(TAG, "GoogleApiClient disconnected");
            }
        }

        super.onDestroy();
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);
        if(messageEvent.getPath().equals(CONFIG_START)){
            Log.i(TAG, "start " + Arrays.toString(messageEvent.getData()));
        }else if(messageEvent.getPath().equals(CONFIG_STOP)){
            Log.i(TAG, "stop " + Arrays.toString(messageEvent.getData()));
        }
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.v(TAG,"onConnectionSuspended called");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.v(TAG,"onConnectionFailed called");
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.v(TAG,"onConnected called");

    }
}
