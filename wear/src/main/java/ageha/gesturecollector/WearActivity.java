package ageha.gesturecollector;


//import android.Manifest;
import android.content.Context;
//import android.content.pm.PackageManager;
//import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.TriggerEvent;
import android.hardware.TriggerEventListener;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
//import android.support.v4.app.ActivityCompat;
//import android.support.v4.content.ContextCompat;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
//import com.google.android.gms.wearable.Asset;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEventBuffer;
//import com.google.android.gms.wearable.PutDataMapRequest;
//import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.io.File;
//import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
//import java.util.Arrays;
//import java.util.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ageha.shared.DataMapKeys;


public class WearActivity extends WearableActivity implements SensorEventListener, DataApi.DataListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    private static final String TAG = "WearActivity";

//    public static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 99;
    private String FILENAME ="";
    private String file_path;
    private boolean WRITE_TO_FILE = false;
    private boolean isRecording = false;
    private boolean send_to_mobile = false;
    private Button btn_record;
    private CheckBox cb_write_to_file;

    private GoogleApiClient mGoogleApiClient;
    private FileOutputStream fos = null;
    private StringBuilder sb = new StringBuilder("");

    private int send_count = 0;
    private long sensorTimeReference = 0L;
    private long myTimeReference = 0L;
    private DeviceClient client;
    private static ArrayList<Integer> usefulSensor = new ArrayList<>(Arrays.asList(1, 2, 4, 11, 3, 26, 17, 9, 10));
    private int write_count = 0;
    private TextView textView;
//    private String sensorEvent
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
//        Let the app always on
        setAmbientEnabled();
        setContentView(R.layout.activity_main);
//        startService(new Intent(this, MessageReceiverService.class));
//        startService(new Intent(this, SensorService.class));
        int sampling_rate = SensorManager.SENSOR_DELAY_FASTEST;
        client = DeviceClient.getInstance(this);

        textView = findViewById(R.id.text);
        btn_record = findViewById(R.id.btn_recording);
        cb_write_to_file = findViewById(R.id.cb_write_to_file);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();


        findViewById(R.id.button_quit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                android.os.Process.killProcess(android.os.Process.myPid());
//                stopService(new Intent(getApplicationContext(), SensorService.class));
                System.exit(1);
            }
        });

        final Button btn_send = findViewById(R.id.btn_send);
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (!send_to_mobile){
                    send_to_mobile = true;
                    btn_send.setText("STOP SEND");
                }
                else{
                    send_to_mobile = false;
                    btn_send.setText("SEND");
                }
            }
        });

        btn_record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isRecording){
                    isRecording = false;
                    String start_tex = "START";
                    btn_record.setText(start_tex);
                    cb_write_to_file.setEnabled(true);
                    try {
                        if(fos!=null)
                            fos.close();

//                        PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/filename");
//                        putDataMapReq.getDataMap().putString("filename", FILENAME);
//                        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
//                        Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);
//                        Asset asset = createAssetFromFile(file_path);
//                        PutDataMapRequest dataMap = PutDataMapRequest.create("/file");
//                        dataMap.getDataMap().putAsset("fileasset", asset);
//                        PutDataRequest request = dataMap.asPutDataRequest();
//                        Wearable.DataApi.putDataItem(mGoogleApiClient, request);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }else{
                    isRecording = true;
                    String stop_tex = "STOP";
                    btn_record.setText(stop_tex);
                    WRITE_TO_FILE = cb_write_to_file.isChecked();
                    cb_write_to_file.setEnabled(false);
                    if(WRITE_TO_FILE) {
                        FILENAME = "SENSORDATA" + System.currentTimeMillis()+".txt";
                        file_path = getDocStorageDir(getBaseContext(),"DATA").getAbsolutePath()+"/"+FILENAME;
                        Log.i(TAG, "file path" + file_path);
                        try {
                            fos = new FileOutputStream(new File(file_path));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try{
                            fos.write(sb.toString().getBytes());
                            fos.write("TIMESTAMP, SENSORTYPE, VALUES1, VALUES2, VALUES3, VALUES4, VALUES5 \n".getBytes());
                        } catch (IOException e) {
                            Log.e(TAG, "here");
                            e.printStackTrace();
                        }
                    }
                }
            }
        });

//        Register Sensors
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        List<Sensor> list = sensorManager.getSensorList(Sensor.TYPE_ALL);
        if (list.size() < 1) {
            Log.e(TAG, "No sensors returned from getSensorList");
            String tex = "No sensors returned from getSensorList";
            textView.setText(tex);
        }
        else{
            Sensor[] sensorArray = list.toArray(new Sensor[list.size()]);
            for (int i = 0; i < sensorArray.length; i++) {
//                Log.i(TAG, "Found sensor " + i + " " + sensorArray[i].toString());
//                //set to fastest delay
                String tex = "Found sensor " + i + " " + sensorArray[i].toString() + '\n';
                sb.append(tex);
                sensorManager.registerListener(this, sensorArray[i], sampling_rate);

            }
            textView.setText("Found " + sensorArray.length + " sensors");
        }
    }
//
    public void onBeep(View view){
        Log.w("WearActivity", "onBeep");
    }

//    @Override
//    public void onSensorChanged(SensorEvent sensorEvent) {
//        return;
//    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        // calculate timestamp
        if(sensorTimeReference == 0L && myTimeReference == 0L) {
            sensorTimeReference = event.timestamp;
            myTimeReference = System.currentTimeMillis();
        }
        // set event timestamp to current time in milliseconds
        long time = myTimeReference +
                Math.round((event.timestamp - sensorTimeReference) / 1000000.0);

        if (!usefulSensor.contains(event.sensor.getType())){
            // if the sensor type is not useful, skip it
            return;
        }

        if(WRITE_TO_FILE && isRecording){
            WriteSensorEvent(time,event.sensor.getType(), event.values);
        }

//        sensorEventBuffer += sensorEventToString(event.sensor.getName(), time, event.sensor.getType(), event.values, event.accuracy);
//
//        if (System.currentTimeMillis() - lastSendTime < 1000){
//            return;
//        }
        if (send_to_mobile){
            client.sendSensorData(event.sensor.getName(), event.sensor.getType(), event.accuracy, time, event.values);
        }
//        lastSendTime = System.currentTimeMillis();
//        client.sendSensorDataPack(sensorEventBuffer);
//        sensorEventBuffer = "";
//         client.sendCount();
//        sendCount();
//        sendSensorData(event.sensor.getName(), event.sensor.getType(), event.accuracy, time, event.values);
    }


    public void WriteSensorEvent(long time, int type, float[] values){
        try {
            String temp = String.valueOf(time) + ", " + String.valueOf(type);
            for (int i = 0; i < 5; i++){
                if (i<values.length){
                    temp = temp + ", " + String.valueOf(values[i]);

                } else {
                    temp = temp + ", ";
                }

            }
            temp += '\n';
            fos.write(temp.getBytes());
            write_count += 1;
            if (write_count % 10000 == 0){
                Log.i(TAG, "write count: " + write_count);
                textView.setText("write count: " + write_count);
            }
        } catch (IOException e) {
            Log.e(TAG, "here");
            e.printStackTrace();
        }
    }

    public String sensorEventToString(String name, long time, int type, float[] values, int acc){
//        "SENSORNAME, SENSORID, TIMESTAMP, ACCURACY, VALUES1, VALUES2, VALUES3, VALUES4, VALUES5\n";
        String temp = name + ',' + String.valueOf(type) + ',' + String.valueOf(time) + ", " + String.valueOf(acc);
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

    public File getDocStorageDir(Context context, String albumName) {
        File file = new File(context.getExternalFilesDir(
                Environment.DIRECTORY_DOCUMENTS), albumName);
        if (!file.mkdirs()) {
            Log.e(TAG, "Directory not created");
        }
        return file;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Wearable.DataApi.addListener(mGoogleApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onResume(){
        super.onResume();
        mGoogleApiClient.connect();
//        startService(new Intent(this, SensorService.class));
    }

    @Override
    public void onPause(){
        super.onPause();
//        stopService(new Intent(this, SensorService.class));
        Wearable.DataApi.removeListener(mGoogleApiClient, this);
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
//        stopService(new Intent(this, SensorService.class));
    }


    private void sendCount(){
        PutDataMapRequest dataMap = PutDataMapRequest.create("/sensors/sensorcount");
        Log.i(TAG, "run() " + send_count);
        dataMap.getDataMap().putInt(DataMapKeys.SENSORCOUNT, send_count);
        send_count += 1;
        PutDataRequest putDataRequest = dataMap.asPutDataRequest();
        Wearable.DataApi.putDataItem(mGoogleApiClient, putDataRequest);
    }

    private void sendSensorData(String sensorName, int sensorType, int accuracy, long timestamp, float[] values) {
        Log.i(TAG, "sendSensorData");
        PutDataMapRequest dataMap = PutDataMapRequest.create("/sensors/datapoint/" + sensorType);
        dataMap.getDataMap().putString(DataMapKeys.SENSORNAME, sensorName);
        dataMap.getDataMap().putInt(DataMapKeys.ACCURACY, accuracy);
        dataMap.getDataMap().putLong(DataMapKeys.TIMESTAMP, timestamp);
        dataMap.getDataMap().putFloatArray(DataMapKeys.VALUES, values);
        PutDataRequest putDataRequest = dataMap.asPutDataRequest();
        PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi.putDataItem(mGoogleApiClient, putDataRequest);
    }
}
