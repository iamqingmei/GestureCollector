package ageha.gesturecollector.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import ageha.gesturecollector.TagManager;
import ageha.gesturecollector.SensorManager;
import ageha.gesturecollector.data.Sensor;
import ageha.gesturecollector.data.TagData;
import ageha.gesturecollector.R;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import io.realm.Realm;

public class ExportActivity extends AppCompatActivity {
    private ProgressBar dataProgressbar;
    private ProgressBar tagProgressbar;
    private final String TAG = "ExportActivity";
    private TextView export_text;
    private FileOutputStream fos;
    private final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 99;
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "OnCreate()");

        setContentView(R.layout.activity_export);

        dataProgressbar = findViewById(R.id.export_progress);
        tagProgressbar = findViewById(R.id.export_progress_tag);

        setSupportActionBar((Toolbar) findViewById(R.id.my_awesome_toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setTitle("Export Data");
        findViewById(R.id.exportDataButton).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        Runnable r = new Runnable() {
                            @Override
                            public void run() {
                                exportDataExternalFile();
                            }
                        };

                        Thread t = new Thread(r);
                        t.start();
                    }
                }

        );

        findViewById(R.id.deleteTagButton).setOnClickListener(
                new View.OnClickListener(){
                    @Override
                    public void onClick(final View v) {
                        Log.i(TAG, "deleteTagButton");
                        TagManager.getInstance(ExportActivity.this).DeleteTags();
                        export_text.setText(getDataInfo());

                    }
                }
        );

        findViewById(R.id.exportTagsToFileButton).setOnClickListener(
                new View.OnClickListener(){
                    @Override
                    public void onClick(final View v) {
                        Runnable r = new Runnable() {
                            @Override
                            public void run() {
                                exportTagsExternalFile();
                            }
                        };

                        Thread t = new Thread(r);
                        t.start();
                    }
                }
        );


//        findViewById(R.id.exportTagsButton).setOnClickListener(
//                new View.OnClickListener() {
//                    @Override
//                    public void onClick(final View v) {
//                        Runnable r = new Runnable() {
//                            @Override
//                            public void run() {
//                                exportTagsFile();
//                            }
//                        };
//
//                        Thread t = new Thread(r);
//                        t.start();
//                    }
//                }
//
//        );


        findViewById(R.id.deleteDataButton).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        Log.i(TAG, "deleteDataButton");
                        SensorManager.getInstance(ExportActivity.this).DeleteAllSensors();
                        export_text.setText(getDataInfo());
                    }
                }
        );

        export_text = findViewById(R.id.data_summary_text);

        export_text.setText(getDataInfo());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private String getDataInfo(){
        ArrayList<Sensor> sensors = SensorManager.getInstance(ExportActivity.this).getSensors();
        int DataPointSize = 0;
        for (Sensor sensor: sensors){
            DataPointSize += sensor.getDataPoints().size();
        }
        String data_size = String.valueOf(DataPointSize);
        String tag_size = String.valueOf(TagManager.getInstance(ExportActivity.this).getTags().size());

        return "Data Points: " + data_size + " Tags: " + tag_size;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void exportDataExternalFile() {
        SensorManager mSensorManager = SensorManager.getInstance(this);

        String res = mSensorManager.getSensorDataString();

        final int total_row = res.split("\r\n|\r|\n").length;
        Log.i("GestureCollector", "total_row = " + total_row);

        final String date = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault()).format(new Date());
        String filename = String.format("%s_%s.txt", "SensorData", date);

        String file_path = getDocStorageDir(getBaseContext(),"SENSORDATA").getAbsolutePath()+"/"+filename;
        Log.i(TAG, "file path" + file_path);


        try {

            fos = new FileOutputStream(new File(file_path));

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dataProgressbar.setMax(total_row);
                    dataProgressbar.setVisibility(View.VISIBLE);
                    dataProgressbar.setProgress(0);
                }
            });

            // Write the string to the file
            fos.write(res.getBytes());
            if (fos!=null){
                fos.close();
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            dataProgressbar.setVisibility(View.GONE);
                        }
                    }, 1000);

                }
            });


            Log.i("GestureCollector", "export sensor data to external file finished!");
        } catch (IOException ioe) {
            Log.e("GestureCollector", "export sensor data IOException while writing Logfile");
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        export_text.setText(getDataInfo());
                    }
                }, 1000);

            }
        });

    }


//    private void exportTagsFile() {
//        String date = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault()).format(new Date());
//        String filename = String.format("%s_%s.txt", "Tags", date);
//
//        final String directory = Environment.getExternalStorageDirectory().getAbsolutePath() + "/GestureCollector";
//        final File logfile = new File(directory, filename);
//        final File logPath = logfile.getParentFile();
//        if (!logPath.isDirectory() && !logPath.mkdirs()) {
//            Log.e(TAG, "Could not create directory for log files");
//        }
//
//        try {
//            FileWriter filewriter = new FileWriter(logfile);
//            BufferedWriter bw = new BufferedWriter(filewriter);
//
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    tagProgressbar.setMax(TagManager.getInstance(ExportActivity.this).getTags().size());
//                    tagProgressbar.setVisibility(View.VISIBLE);
//                    tagProgressbar.setProgress(0);
//                }
//            });
//
//            int i = 0;
////            bw.write(TagManager.getInstance(this).getUserInfo());
//            bw.write("TagName,TimeStamp,Tester_Name,Tester_Age,Tester_Height, Tester_Gender\n");
//            for (TagData tag : TagManager.getInstance(this).getTags()) {
//                final int progress = i;
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        tagProgressbar.setProgress(progress);
//                    }
//                });
//                ++i;
//                bw.write(tag.getTagName() + ", " +
//                        tag.getTimestamp() + ", " +
//                        tag.getName()+ ", " +
//                        String.valueOf(tag.getAge())+ ", " +
//                        String.valueOf(tag.getHeight())+ ", " +
//                        tag.getGender() + "\n");
//                Log.i(TAG, tag.getTagName() + ", " +
//                        tag.getTimestamp() + ", " +
//                        tag.getName()+ ", " +
//                        String.valueOf(tag.getAge())+ ", " +
//                        String.valueOf(tag.getHeight())+ ", " +
//                        tag.getGender() + "\n");
//            }
//            bw.flush();
//            bw.close();
//
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//
//                    new Handler().postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            tagProgressbar.setVisibility(View.GONE);
//                        }
//                    }, 1000);
//
//                }
//            });
//
//            Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
//            emailIntent.setType("*/*");
//
//            emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
//                    "GestureCollector data export");
//            emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(logfile));
//            startActivity(Intent.createChooser(emailIntent, "Send mail..."));
//
//
//            Log.i("GestureCollector", "export finished!");
//        } catch (IOException ioe) {
//            Log.e("GestureCollector", "IOException while writing Logfile");
//        }
//
//        export_text.setText(getDataInfo());
//    }

    private void exportTagsExternalFile(){
        String date = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault()).format(new Date());
        String filename = String.format("%s_%s.txt", "Tags", date);

        String file_path = getDocStorageDir(getBaseContext(),"TAGDATA").getAbsolutePath()+"/"+filename;
        Log.i(TAG, "file path" + file_path);

        try {
            fos = new FileOutputStream(new File(file_path));

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tagProgressbar.setMax(TagManager.getInstance(ExportActivity.this).getTags().size());
                    tagProgressbar.setVisibility(View.VISIBLE);
                    tagProgressbar.setProgress(0);
                }
            });

            int i = 0;
//            fos.write(TagManager.getInstance(this).getUserInfo().getBytes());
            fos.write(("TagName,TimeStamp,Tester_Name,Tester_Age,Tester_Height, Tester_Gender, Tester_Weight, Tester_hand\n").getBytes());
            for (TagData tag : TagManager.getInstance(this).getTags()) {
                final int progress = i;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tagProgressbar.setProgress(progress);
                    }
                });
                ++i;
                fos.write((tag.getTagName() + ", " +
                        tag.getTimestamp() + ", " +
                        tag.getName()+ ", " +
                        String.valueOf(tag.getAge())+ ", " +
                        String.valueOf(tag.getHeight())+ ", " +
                        tag.getGender() + ", " +
                        String.valueOf(tag.getWeight()) + "," +
                        String.valueOf(tag.getLeftright()) + "\n").getBytes());
            }
            // Close
            if(fos!=null)
                fos.close();



            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            tagProgressbar.setVisibility(View.GONE);
                        }
                    }, 1000);

                }
            });

            Log.i("GestureCollector", "export finished!");
        } catch (IOException ioe) {
            Log.e("GestureCollector", "IOException while writing Logfile");
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        export_text.setText(getDataInfo());
                    }
                }, 1000);

            }
        });
    }

    private File getDocStorageDir(Context context, String albumName) {
        if (!isExternalStorageWritable()){
            super.onStop();
            Log.e(TAG, "cannot excess to external storage!");
            return null;
        }

        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    MY_PERMISSIONS_REQUEST_READ_CONTACTS);
        }
        File file = new File(context.getExternalFilesDir(
                Environment.DIRECTORY_DOCUMENTS), albumName);
        if (!file.mkdirs()) {
            Log.e(TAG, "Directory not created");
        }
        return file;
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        Log.i(TAG, "isExternalStorageWritable()");
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }


}
