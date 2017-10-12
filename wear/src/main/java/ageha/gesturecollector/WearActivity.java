package ageha.gesturecollector;


//import android.Manifest;
import android.content.Context;
//import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEventBuffer;
//import com.google.android.gms.wearable.PutDataMapRequest;
//import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.io.File;
//import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
//import java.util.Arrays;
import java.util.Date;
import java.util.List;


public class WearActivity extends WearableActivity implements SensorEventListener, DataApi.DataListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    private static final String TAG = "WearActivity";

//    public static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 99;
    private String FILENAME ="";
    private String file_path;
    private boolean WRITE_TO_FILE = false;
    private boolean isRecording = false;
    private Button btn_record;
    private CheckBox cb_write_to_file;

    private GoogleApiClient mGoogleApiClient;
    private FileOutputStream fos = null;
    private StringBuilder sb = new StringBuilder("");
    private long dateBase;
    private long timeStampBase;

    private long sensorTimeReference = 0l;
    private long myTimeReference = 0l;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
//        Let the app always on
        setAmbientEnabled();
        setContentView(R.layout.activity_main);

        int sampling_rate = SensorManager.SENSOR_DELAY_FASTEST;

        dateBase = (new Date()).getTime();
        timeStampBase = System.nanoTime();
        TextView textView = findViewById(R.id.text);
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
                System.exit(1);
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
                        FILENAME = System.currentTimeMillis()+"_b";
                        file_path = getDocStorageDir(getBaseContext(),"DATA").getAbsolutePath()+"/"+FILENAME;
                        Log.i(TAG, "file path" + file_path);
                        try {
                            fos = new FileOutputStream(new File(file_path));
                            // calibration purpose
                            // WriteSensorEvent(1234,1234,new float[]{1.2f,2.3f,3.4f},new Quaternion(0.1,new Vec3(1.2,2.3,3.4)));
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
                Log.i(TAG, "Found sensor " + i + " " + sensorArray[i].toString());
                //set to fastest delay
                String tex = "Found sensor " + i + " " + sensorArray[i].toString() + '\n';
                sb.append(tex);
                sensorManager.registerListener(this, sensorArray[i], sampling_rate);

            }
            textView.setText("Found " + sensorArray.length + " sensors");
        }
    }

    public void onBeep(View view){
        Log.w("WearActivity", "onBeep");
    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(WRITE_TO_FILE && isRecording){
//            long time = dateBase + (sensorEvent.timestamp - timeStampBase) / 1000000L;
            // set reference times
            if(sensorTimeReference == 0l && myTimeReference == 0l) {
                sensorTimeReference = sensorEvent.timestamp;
                myTimeReference = System.currentTimeMillis();
            }
            // set event timestamp to current time in milliseconds
            long time = myTimeReference +
                    Math.round((sensorEvent.timestamp - sensorTimeReference) / 1000000.0);

            WriteSensorEvent(time,sensorEvent.sensor.getType(), sensorEvent.values);
        }
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
        } catch (IOException e) {
            Log.e(TAG, "here");
            e.printStackTrace();
        }
//        try {
//            fos.write(long2bytes(time));
//            fos.write(int2bytes(type));
//            for (int i=0; i< values.length; i++){
//                fos.write(float2bytes(values[i]));
//            }
////            fos.write(float2bytes(values[0]));
////            fos.write(float2bytes(values[1]));
////            fos.write(float2bytes(values[2]));
//            fos.write(sensorInfo.getBytes());
//        } catch (IOException e) {
//            Log.e(TAG, "here");
//            e.printStackTrace();
//        }

    }


//    public synchronized byte[] int2bytes(int value){
//        byte[] result = new byte[4];
//        for(int i = 3; i>=0; i--){
//            result[i] = (byte)(value & 0xFF);
//            value >>= 8;
//        }
//        return result;
//    }
//
//    public synchronized byte[] long2bytes(long value){
//        byte[] result = new byte[8];
//        for (int i = 7; i >= 0; i--) {
//            result[i] = (byte)(value & 0xFF);
//            value >>= 8;
//        }
//        return result;
//    }

//    public synchronized byte[] float2bytes(float value){
//        return int2bytes(Float.floatToIntBits(value));
//    }

//    public synchronized byte[] double2bytes(double value){
//        return long2bytes(Double.doubleToLongBits(value));
//    }

    public File getDocStorageDir(Context context, String albumName) {
//        if ((!isExternalStorageReadable()) | (!isExternalStorageWritable())){
//            super.onStop();
//            Log.e(TAG, "cannot excess to external storage!");
//            return null;
//        }
//
//        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
//        if (permissionCheck != PackageManager.PERMISSION_GRANTED){
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.READ_CONTACTS},
//                    MY_PERMISSIONS_REQUEST_READ_CONTACTS);
//        }
        File file = new File(context.getExternalFilesDir(
                Environment.DIRECTORY_DOCUMENTS), albumName);
        if (!file.mkdirs()) {
            Log.e(TAG, "Directory not created");
        }
        return file;
    }

    /* Checks if external storage is available for read and write */
//    public boolean isExternalStorageWritable() {
//        Log.i(TAG, "isExternalStorageWritable()");
//        String state = Environment.getExternalStorageState();
//        return Environment.MEDIA_MOUNTED.equals(state);
//    }
//
//    /* Checks if external storage is available to at least read */
//    public boolean isExternalStorageReadable() {
//        Log.i(TAG, "isExternalStorageReadable()");
//        String state = Environment.getExternalStorageState();
//        return Environment.MEDIA_MOUNTED.equals(state) ||
//                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
//    }


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

//    private static Asset createAssetFromFile(String filepath) {
//        FileInputStream fileInputStream;
//        File file = new File(filepath);
//        byte[] bFile = new byte[(int) file.length()];
//
//        try {
//            //convert file into array of bytes
//            fileInputStream = new FileInputStream(file);
//            fileInputStream.read(bFile);
//            fileInputStream.close();
//        }catch(Exception e){
//            e.printStackTrace();
//        }
//        return Asset.createFromBytes(bFile);
//    }
}
