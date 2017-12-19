package ageha.gesturecollector;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.sql.Timestamp;
import java.util.ArrayList;

import ageha.gesturecollector.event.BusProvider;
import ageha.gesturecollector.ui.*;
import ageha.shared.DataMapKeys;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, DataApi.DataListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, AmbientMode.AmbientCallbackProvider {


    static final private int count_down_time = 4;
    static final private int action_time = 5;
    static final private String TAG = "MainActivity";

    private Toolbar mToolbar;

    private SensorManager mSensorManager;
    private TagManager mTagManager;
    private TextView status;
    private TextView empty_state;
    private EditText tester_name;
    private EditText test_age;
    private EditText test_height;
    private RadioGroup test_genger;
    private RadioGroup test_hand;
    private EditText test_weight;
    private TextView connection_state;
    private TextView data_state;
    private TimeStart timer;
    private ConnectionState connection_checker;
//    private MakeBeepSound beep;

    GoogleApiClient mGoogleApiClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupUI(findViewById(ageha.gesturecollector.R.id.main_activity));
        Log.i("MainActivity", "OnCreate");
        System.out.print("MainActivity onCreate");
        this.mToolbar = findViewById(ageha.gesturecollector.R.id.my_awesome_toolbar);
        this.empty_state = findViewById(R.id.empty_state);
        this.tester_name = findViewById(R.id.text_input_name);
        this.test_age = findViewById(R.id.text_input_age);
        this.test_height = findViewById(R.id.text_input_height);
        this.test_genger = findViewById(R.id.radioGrpGender);
        this.test_hand = findViewById(R.id.radioGrpHand);
        this.test_weight = findViewById(R.id.text_input_weight);
        this.connection_state = findViewById(R.id.connection_state);
        this.data_state = findViewById(R.id.data_state);
        this.mTagManager = TagManager.getInstance(MainActivity.this);
        this.mSensorManager = SensorManager.getInstance(this);
        NavigationView mNavigationView = findViewById(R.id.navView);
        mNavigationView.setNavigationItemSelectedListener(this);
        timer = new TimeStart();
        connection_checker = new ConnectionState();
//        beep = new MakeBeepSound(100, 150);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        initToolbar();

        findViewById(R.id.button_refresh).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                connection_checker.run();
                data_state.setText(getDataInfo());
            }
        });

        ArrayList<Integer> tagButtonIDs = new ArrayList<>();
        tagButtonIDs.add(ageha.gesturecollector.R.id.tag0_button);
        tagButtonIDs.add(ageha.gesturecollector.R.id.tag1_button);
        tagButtonIDs.add(ageha.gesturecollector.R.id.tag2_button);
        tagButtonIDs.add(ageha.gesturecollector.R.id.tag3_button);
        tagButtonIDs.add(ageha.gesturecollector.R.id.tag4_button);
        tagButtonIDs.add(ageha.gesturecollector.R.id.tag5_button);
        tagButtonIDs.add(ageha.gesturecollector.R.id.tag6_button);
        tagButtonIDs.add(ageha.gesturecollector.R.id.tag7_button);
        tagButtonIDs.add(ageha.gesturecollector.R.id.tag8_button);
        tagButtonIDs.add(ageha.gesturecollector.R.id.tag9_button);
        tagButtonIDs.add(ageha.gesturecollector.R.id.tag_start2_button);
        tagButtonIDs.add(ageha.gesturecollector.R.id.tag_start1_button);

        for (Integer btnId : tagButtonIDs){
            final Button cur_btn = findViewById(btnId);
            cur_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean check_res = check_tester_info_input(getApplicationContext());
                    if (!check_res){
                        util.warning_msg(getApplicationContext());
                    }
//                    else if (!mTagManager.if_recording()){
//                        util.warning_msg(getApplicationContext(), "Watch has not started recording!");
//                    }
                    else{
                        tagging(cur_btn.getText().toString());
                        String tex = "Action: \n" + cur_btn.getText().toString();
                        empty_state.setText(tex);
                        data_state.setText(getDataInfo());
                        timer.run();
                    }

                }
            });
        }

        findViewById(R.id.btn_action_finish).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean check_res = check_tester_info_input(getApplicationContext());
                if (!check_res){
                    util.warning_msg(getApplicationContext());
                }
                else{
                    tagging("ACTION_FINISH");
                }
            }
        });

        findViewById(R.id.btn_time_calibration).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean check_res = check_tester_info_input(getApplicationContext());
                if (!check_res){
                    util.warning_msg(getApplicationContext());
                }
                else{
                    tagging("TIME_CALIBRATION");
                }
            }
        });

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

    }


    @Override
    protected void onResume(){
        super.onResume();
        mGoogleApiClient.connect();
        Wearable.DataApi.addListener(mGoogleApiClient, this);
        BusProvider.getInstance().register(this);
        int sensor_number =  mSensorManager.getSensorNumber();
        util.warning_msg(getApplicationContext(), "Number of Sensors: " + sensor_number);

        if (sensor_number > 0){
            mSensorManager.startMeasurement();
        }
        connection_checker.run();
        data_state.setText(getDataInfo());
    }


    @Override
    protected void onPause() {
        super.onPause();
        Wearable.DataApi.removeListener(mGoogleApiClient, this);
        mGoogleApiClient.disconnect();
        BusProvider.getInstance().unregister(this);

        mSensorManager.stopMeasurement();
    }

    private void initToolbar() {
        setSupportActionBar(mToolbar);

        final ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(false);
            ab.setTitle(R.string.app_name);
            mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.action_about:
                            startActivity(new Intent(MainActivity.this, AboutActivity.class));
                            return true;
                        case R.id.action_export:
                            startActivity(new Intent(MainActivity.this, ExportActivity.class));
                            return true;
                    }

                    return true;
                }
            });
        }
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.v("MainActivity","onConnectionSuspended called");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.v("MainActivity","onConnectionFailed called");
    }

    @Override
    public void onConnected(Bundle bundle) {
        Wearable.DataApi.addListener(mGoogleApiClient, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(final MenuItem pMenuItem) {
        Toast.makeText(this, "Device: " + pMenuItem.getTitle(), Toast.LENGTH_SHORT).show();
        return false;
    }

    private class TimeStart extends Thread{
        public void run(){
            Log.i("main_activity", "TimeStart");
            status = findViewById(ageha.gesturecollector.R.id.timer_state);
            CountDownTimer countDownTimer = new CountDownTimer((count_down_time + action_time) * 1000, 1000){
                @Override
                public void onTick(long millisUntilFinish){
                    String text;
                    if ((millisUntilFinish / 1000) == action_time){
                        text = "Start!";
//                        beep.run();
                    }
                    else if ((millisUntilFinish / 1000) >= action_time){
                        text = "Counting Down: " + String.valueOf((millisUntilFinish - action_time * 1000)/1000);
                    }else{
                        text = "Recording Data: " + String.valueOf(millisUntilFinish /1000);
//                        beep.run();

                    }
                    status.setText(text);
                }
                @Override
                public void onFinish(){
                    status.setText(ageha.gesturecollector.R.string.done_msg);
                }
            };
            countDownTimer.start();
        }
    }


    private boolean check_tester_info_input(Context context){
        Log.i("main_activity", "check_tester_info_input function");
        if (tester_name.getText().toString().isEmpty()) {
            util.warning_msg(context, "No tester name");
            return false;
        }
        else if (test_age.getText().toString().isEmpty()){
            util.warning_msg(context, "No tester age");
            return false;
        }
        else if (test_height.getText().toString().isEmpty()){
            util.warning_msg(context, "No tester height");
            return false;
        }
        else if (test_weight.getText().toString().isEmpty()){
            util.warning_msg(context, "No tester weight");
            return false;
        }
        else{
            Integer tester_age_int;
            Integer tester_height_int;
            Integer tester_weight_int;
            try{
                tester_age_int = Integer.parseInt(test_age.getText().toString());
                tester_height_int = Integer.parseInt(test_height.getText().toString());
                tester_weight_int = Integer.parseInt(test_weight.getText().toString());
            } catch (NumberFormatException e){
                Log.w("main_activity", "NumberFormatException: " + e.getMessage());
                util.warning_msg(context, "Please input the correct age or height!");
                return false;
            }

            if ((tester_age_int > 120)||(tester_age_int<10)){
                util.warning_msg(context, "Please input the correct age!");
                return false;
            }
            if ((tester_height_int > 240)||(tester_height_int<100)){
                util.warning_msg(context, "Please input the correct height!");
                return false;
            }

            if ((tester_weight_int > 100)||(tester_weight_int<20)){
                util.warning_msg(context, "Please input the correct weight!");
                return false;
            }
            return true;
        }
    }

    private void setupUI(View view) {
        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                @SuppressLint("ClickableViewAccessibility")
                public boolean onTouch(View v, MotionEvent event) {
                    util.hideSoftKeyboard(MainActivity.this);
                    return false;
                }
            });
        }

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupUI(innerView);
            }
        }
    }

    private void tagging(String tag){
        if (tag.startsWith("beep")){
            util.warning_msg(getApplicationContext(), "Beep");
            return;
        }

        if (tag.equals("wear_start")){
            findViewById(R.id.linearLayout_bottom_tags).setVisibility(View.VISIBLE);
            findViewById(R.id.linearLayout_top_tags).setVisibility(View.VISIBLE);
            findViewById(R.id.linearLayout_time).setVisibility(View.GONE);
        }
        if (tag.equals("wear_stop")){
            findViewById(R.id.linearLayout_bottom_tags).setVisibility(View.GONE);
            findViewById(R.id.linearLayout_top_tags).setVisibility(View.GONE);
            findViewById(R.id.linearLayout_time).setVisibility(View.VISIBLE);
        }

        String gender;
        char hand;
        if (test_genger.getCheckedRadioButtonId() == R.id.radioF){
            gender = "F";
        }else{
            gender = "M";
        }
        if (test_hand.getCheckedRadioButtonId() == R.id.radioLeft){
            hand = 'l';
        }else{
            hand = 'r';
        }

        mTagManager.
                addTag(tag,
                        tester_name.getText().toString(),
                        Integer.parseInt(test_age.getText().toString()),
                        Integer.parseInt(test_height.getText().toString()),
                        gender,
                        Integer.parseInt(test_weight.getText().toString()),
                        hand);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.v("MainActivity", "onStart called");
        connection_checker.run();
    }

//    private class MakeBeepSound extends Thread{
//        private ToneGenerator beep;
//        int dur;
//
//        MakeBeepSound(int duration, int volume){
//            this.dur = duration;
//            this.beep = new ToneGenerator(AudioManager.STREAM_MUSIC, volume); // beep's volume
//        }
//
//        public void run(){
//            this.beep.startTone(ToneGenerator.TONE_CDMA_PIP,this.dur);
//        }
//    }

//    private void registerTagUserInfo(String gender){
//        TagManager.getInstance(MainActivity.this).addUserInfo("name: " + tester_name.getText().toString() + " age: " + test_age.getText().toString() + " height: " + test_height.getText().toString() + "gender: " + gender + "\n");
//    }

    private String getDataInfo(){
        String data_size = String.valueOf(mSensorManager.getDataPointSize());
        String tag_size = String.valueOf(TagManager.getInstance(MainActivity.this).getTags().size());
        return "Data Points: " + data_size + " Tags: " + tag_size;
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.d(TAG, "onDataChanged()");

        for (DataEvent dataEvent : dataEvents) {

            if (dataEvent.getType() == DataEvent.TYPE_CHANGED) {
                DataItem dataItem = dataEvent.getDataItem();
                Uri uri = dataItem.getUri();
                String path = uri.getPath();
//                Log.i(TAG, uri.getPath());
                if (path.startsWith("/sensors/datapoint")) {
                    unpackSensorData(
                            Integer.parseInt(uri.getLastPathSegment()),
                            DataMapItem.fromDataItem(dataItem).getDataMap()
                    );
                }
                if (path.startsWith("/sensors/tag")){
                    Log.i(TAG, "received tags from wear");
                    DataMap dataMap = DataMapItem.fromDataItem(dataItem).getDataMap();
                    tagging(dataMap.getString(DataMapKeys.TAG));
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

        mSensorManager.addSensorData(sensorName, sensorType, accuracy, timestamp, values);
    }


//
    private class ConnectionState {
        public void run(){
            getNodes();
        }
        private void getNodes() {

            Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                @Override
                public void onResult(@NonNull NodeApi.GetConnectedNodesResult nodes) {
                    StringBuilder results = null;
                    for (Node node : nodes.getNodes()) {
                        if (results == null){
                            results = new StringBuilder();
                        }
                        results.append(node.getDisplayName()).append(" ");

                    }
                    String temp;
                    if (results == null){
                        temp =  "connected to : None" ;
                    }else{
                        temp =  "connected to : " + results.toString();
                    }

                    connection_state.setText(temp);
                }
            });
        }
    }

}
