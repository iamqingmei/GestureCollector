package ageha.gesturecollector;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import ageha.gesturecollector.ui.*;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{


    static final private int count_down_time = 4;
    static final private int action_time = 5;

    private Menu mNavigationViewMenu;
    private Toolbar mToolbar;

    private TextView status;
    private TextView empty_state;
    private EditText tester_name;
    private EditText test_age;
    private EditText test_height;
    private RadioGroup test_genger;

    private Handler mHandler;

    private WearManager wearManager;
    private TimeStart timer;
    private MakeBeepSound beep;


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
        NavigationView mNavigationView = findViewById(R.id.navView);
        mNavigationView.setNavigationItemSelectedListener(this);
        this.mNavigationViewMenu = mNavigationView.getMenu();
        timer = new TimeStart();
        beep = new MakeBeepSound(100, 150);

        if(null == mGoogleApiClient) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Wearable.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
            Log.v("MainActivity", "GoogleApiClient created");
        }

        if(!mGoogleApiClient.isConnected()){
            mGoogleApiClient.connect();
            Log.v("MainActivity", "Connecting to GoogleApiClient..");
        }
//        final Handler handler = new Handler();
//        final Runnable refresh;

        initToolbar();

        wearManager = WearManager.getInstance(this);
        findViewById(ageha.gesturecollector.R.id.tag0_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean check_red = check_tester_info_input(getApplicationContext());
                if (!check_red){
                    util.warning_msg(getApplicationContext());
                }
                else{
                    tagging("0");
                    timer.run();
                }

            }
        });

        findViewById(ageha.gesturecollector.R.id.tag1_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean check_red = check_tester_info_input(getApplicationContext());
                if (!check_red){
                    util.warning_msg(getApplicationContext());
                }
                else{
                    tagging("1");

                }

            }
        });

        findViewById(ageha.gesturecollector.R.id.tag2_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean check_red = check_tester_info_input(getApplicationContext());
                if (!check_red){
                    util.warning_msg(getApplicationContext());
                }
                else{
                    tagging("2");
                    timer.run();
                }

            }
        });

        findViewById(ageha.gesturecollector.R.id.tag3_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean check_red = check_tester_info_input(getApplicationContext());
                if (!check_red){
                    util.warning_msg(getApplicationContext());
                }
                else{
                    tagging("3");
                    timer.run();
                }

            }
        });

        findViewById(ageha.gesturecollector.R.id.tag4_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean check_red = check_tester_info_input(getApplicationContext());
                if (!check_red){
                    util.warning_msg(getApplicationContext());
                }
                else{
                    tagging("4");
                    timer.run();
                }

            }
        });

        findViewById(ageha.gesturecollector.R.id.tag5_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean check_red = check_tester_info_input(getApplicationContext());
                if (!check_red){
                    util.warning_msg(getApplicationContext());
                }
                else{
                    tagging("5");
                    timer.run();
                }

            }
        });

        findViewById(ageha.gesturecollector.R.id.tag6_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean check_red = check_tester_info_input(getApplicationContext());
                if (!check_red){
                    util.warning_msg(getApplicationContext());
                }
                else{
                    tagging("6");
                    timer.run();
                }

            }
        });

        findViewById(ageha.gesturecollector.R.id.tag7_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean check_red = check_tester_info_input(getApplicationContext());
                if (!check_red){
                    util.warning_msg(getApplicationContext());
                }
                else{
                    tagging("7");
                    timer.run();
                }

            }
        });

        findViewById(ageha.gesturecollector.R.id.tag8_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean check_red = check_tester_info_input(getApplicationContext());
                if (!check_red){
                    util.warning_msg(getApplicationContext());
                }
                else{
                    tagging("8");
                    timer.run();
                }

            }
        });


        findViewById(ageha.gesturecollector.R.id.tag9_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean check_red = check_tester_info_input(getApplicationContext());
                if (!check_red){
                    util.warning_msg(getApplicationContext());
                }
                else{
                    tagging("9");
                    timer.run();
                }

            }
        });

        findViewById(ageha.gesturecollector.R.id.tag_start2_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean check_red = check_tester_info_input(getApplicationContext());
                if (!check_red){
                    util.warning_msg(getApplicationContext());
                }
                else{
                    tagging("start1");
                    timer.run();
                }

            }
        });

        findViewById(ageha.gesturecollector.R.id.tag_start1_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean check_red = check_tester_info_input(getApplicationContext());
                if (!check_red){
                    util.warning_msg(getApplicationContext());
                }
                else{
                    tagging("start2");
                    timer.run();
                }

            }
        });

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

//        this.mHandler = new Handler();
//        this.mHandler.postDelayed(m_Runnable, 2000);
        Log.i("MainActivity", "111");
        startService(new Intent(this, SensorReceiverService.class));
        Log.i("MainActivity", "222");


    }


//    private final Runnable m_Runnable = new Runnable() {
//        @Override
//        public void run() {
//            if (wearManager.getWearName() != null){
//                TextView empty_state = (TextView) findViewById(R.id.empty_state);
//                empty_state.setText(wearManager.getWearName());
//            }
//            mHandler.postDelayed(m_Runnable, 2000);
//        }
//    };

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
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.v("MainActivity","onConnectionFailed called");
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.v("MainActivity","onConnected called");
    }

    public void sendNotification(View view) {
        Log.i("MainActivity", "sendNotification");
        TextView editText = (TextView) findViewById(R.id.editText);
        if (editText.length() > 0) {
            editText.setText(null);
        }
        String toSend = editText.getText().toString();
        if(toSend.isEmpty())
            toSend = "You sent an empty notification";

        Notification notification = new NotificationCompat.Builder(getApplication())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Gesture Collector")
                .setContentText(toSend)
                .extend(new NotificationCompat.WearableExtender().setHintShowBackgroundOnly(true))
                .build();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplication());
        int notificationId = 1;
        notificationManager.notify(notificationId, notification);

//        new SendActivityPhoneMessage("/testing_path", "hii").run();
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
                    }
                    else if ((millisUntilFinish / 1000) >= action_time){
                        text = "Counting Down: " + String.valueOf((millisUntilFinish - action_time * 1000)/1000);
                        beep.run();
                    }else{
                        text = "Recording Data: " + String.valueOf(millisUntilFinish /1000);
                        beep.run();

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
        else{
            Integer tester_age_int;
            Integer tester_height_int;
            try{
                tester_age_int = Integer.parseInt(test_age.getText().toString());
                tester_height_int = Integer.parseInt(test_height.getText().toString());
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
            return true;
        }
    }

    private void setupUI(View view) {
        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
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
        String gender;
        if (test_genger.getCheckedRadioButtonId() == ageha.gesturecollector.R.id.radioF){
            gender = "F";
        }else{
            gender = "M";
        }
        TagManager.getInstance(MainActivity.this).
                addTag(tag,
                        tester_name.getText().toString(),
                        Integer.parseInt(test_age.getText().toString()),
                        Integer.parseInt(test_height.getText().toString()),
                        gender);
        String tex = "Action: \n" + tag;
        empty_state.setText(tex);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.v("MainActivity", "onStart called");
    }

//    private class SendActivityPhoneMessage extends Thread {
//        String path;
//        String message;
//
//        // Constructor to send a message to the data layer
//        SendActivityPhoneMessage(String p, String msg) {
//            Log.i("SendActivityPhoneMsg", "inited");
//            path = p;
//            message = msg;
//        }
//
//        public void run() {
//            NodeApi.GetLocalNodeResult nodes = Wearable.NodeApi.getLocalNode(mGoogleApiClient).await();
//            Node node = nodes.getNode();
//            Log.v("SendActivityPhoneMsg", "Activity Node is : "+node.getId()+ " - " + node.getDisplayName());
//            MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(), path, message.getBytes()).await();
//            if (result.getStatus().isSuccess()) {
//                Log.v("SendActivityPhoneMsg", "Activity Message: {" + message + "} sent to: " + node.getDisplayName());
//            }
//            else {
//                // Log an error
//                Log.v("SendActivityPhoneMsg", "ERROR: failed to send Activity Message");
//            }
//        }
//    }

    private class MakeBeepSound extends Thread{
        private ToneGenerator beep;
        int dur;

        public MakeBeepSound(int duration, int volume){
            this.dur = duration;
            this.beep = new ToneGenerator(AudioManager.STREAM_MUSIC, volume); // beep's volume
        }

        public void run(){
            this.beep.startTone(ToneGenerator.TONE_CDMA_PIP,this.dur);
        }
    }
}
