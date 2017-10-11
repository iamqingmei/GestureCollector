package ageha.gesturecollector.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import ageha.gesturecollector.TagManager;
import ageha.gesturecollector.data.TagData;
import ageha.gesturecollector.R;
import ageha.gesturecollector.database.DataEntry;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmResults;

public class ExportActivity extends AppCompatActivity {
    private Realm mRealm;
    private ProgressBar dataProgressbar;
    private ProgressBar tagProgressbar;
    private final String TAG = "ExportActivity";
    private TextView export_text;
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
                                exportDataFile();
                            }
                        };

                        Thread t = new Thread(r);
                        t.start();
                    }
                }

        );


        findViewById(R.id.exportTagsButton).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {

                        Runnable r = new Runnable() {
                            @Override
                            public void run() {
                                exportTagsFile();
                            }
                        };

                        Thread t = new Thread(r);
                        t.start();
                    }
                }

        );


        findViewById(R.id.deleteButton).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                deleteAllData();
                            }
                        }).start();

                    }
                }
        );

        findViewById(R.id.exportDeleteAllInOneButton).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                exportDataFile();
                                exportTagsFile();
                                deleteAllData();
                            }
                        }).start();

                    }
                }
        );

        export_text = (TextView) findViewById(R.id.data_summary_text);

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
        mRealm = Realm.getInstance(this);
        mRealm.beginTransaction();

        String data_size = String.valueOf(mRealm.where(DataEntry.class).findAll().size());
        String tag_size = String.valueOf(TagManager.getInstance(ExportActivity.this).getTags().size());

        return new String("Data Entry: " + data_size + " Tags: " + tag_size);
    }
    private void deleteAllData() {
        mRealm = Realm.getInstance(this);
        mRealm.beginTransaction();

        RealmResults<DataEntry> result = mRealm.where(DataEntry.class).findAll();
        Log.e("DataCollector", "rows before delete = " + result.size());
        TagManager.getInstance(ExportActivity.this).DeleteTags();

        // Delete all matches
        result.clear();

        mRealm.commitTransaction();

        result = mRealm.where(DataEntry.class).findAll();
        Log.e("DataCollector", "rows after delete = " + result.size());

        export_text.setText(getDataInfo());
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void exportDataFile() {
        mRealm = Realm.getInstance(this);

        RealmResults<DataEntry> result = mRealm.where(DataEntry.class).findAll();
        final int total_row = result.size();
//        final int total_col = 8;
        Log.i("DataCollector", "total_row = " + total_row);
        final String fileprefix = "SensorData";
        final String date = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault()).format(new Date());
        String filename = String.format("%s_%s.txt", fileprefix, date);

        final String directory = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DataCollector";

        final File logfile = new File(directory, filename);
        final File logPath = logfile.getParentFile();

        if (!logPath.isDirectory() && !logPath.mkdirs()) {
            Log.e("DataCollector", "Could not create directory for log files");
        }

        try {
            FileWriter filewriter = new FileWriter(logfile);
            BufferedWriter bw = new BufferedWriter(filewriter);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dataProgressbar.setMax(total_row);
                    dataProgressbar.setVisibility(View.VISIBLE);
                    dataProgressbar.setProgress(0);
                }
            });

            // Write the string to the file
            bw.write("DeviceID,TimeStamp,X,Y,Z,Accuracy,DataSource,SensorID\n");
            for (int i = 1; i < total_row; i++) {
                final int progress = i;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dataProgressbar.setProgress(progress);
                    }
                });

                String sb = result.get(i).getAndroidDevice() + " ," +
                        String.valueOf(
                                (new Date()).getTime() +
                                        (result.get(i).getTimestamp() - System.nanoTime()) / 1000000L) +
                        " ," +
                        String.valueOf(result.get(i).getX()) +
                        " ," +
                        String.valueOf(result.get(i).getY()) +
                        " ," +
                        String.valueOf(result.get(i).getZ()) +
                        " ," +
                        String.valueOf(result.get(i).getAccuracy()) +
                        " ," +
                        String.valueOf(result.get(i).getDatasource()) +
                        " ," +
                        String.valueOf(result.get(i).getDatatype()) +
                        "\n";
                bw.write(sb);
            }
            bw.flush();
            bw.close();

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


            Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
            emailIntent.setType("*/*");

            emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
                    "DataCollector data export");
            emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(logfile));
            startActivity(Intent.createChooser(emailIntent, "Send mail..."));


            Log.i("DataCollector", "export finished!");
        } catch (IOException ioe) {
            Log.e("DataCollector", "IOException while writing Logfile");
        }

        export_text.setText(getDataInfo());
    }


    private void exportTagsFile() {
        Log.i(TAG, "exportTagsFile1");
        Log.i(TAG, "exportTagsFile4");
        String date = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault()).format(new Date());
        String filename = String.format("%s_%s.txt", "Tags", date);

        final String directory = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DataCollector";
        Log.i(TAG, "exportTagsFile2");
        final File logfile = new File(directory, filename);
        final File logPath = logfile.getParentFile();
        Log.i(TAG, "exportTagsFile5");
        if (!logPath.isDirectory() && !logPath.mkdirs()) {
            Log.e(TAG, "Could not create directory for log files");
        }
        Log.i(TAG, "exportTagsFile3");

        try {
            FileWriter filewriter = new FileWriter(logfile);
            BufferedWriter bw = new BufferedWriter(filewriter);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tagProgressbar.setMax(TagManager.getInstance(ExportActivity.this).getTags().size());
                    tagProgressbar.setVisibility(View.VISIBLE);
                    tagProgressbar.setProgress(0);
                }
            });

            int i = 0;
            bw.write("TagName,TimeStamp,Tester_Name,Tester_Age,Tester_Height, Tester_Gender\n");
            for (TagData tag : TagManager.getInstance(this).getTags()) {
                final int progress = i;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tagProgressbar.setProgress(progress);
                    }
                });
                ++i;
                bw.write(tag.getTagName() + ", " +
                        tag.getTimestamp() + ", " +
                        tag.getName()+ ", " +
                        String.valueOf(tag.getAge())+ ", " +
                        String.valueOf(tag.getHeight())+ ", " +
                        tag.getGender() + "\n");
                Log.i(TAG, tag.getTagName() + ", " +
                        tag.getTimestamp() + ", " +
                        tag.getName()+ ", " +
                        String.valueOf(tag.getAge())+ ", " +
                        String.valueOf(tag.getHeight())+ ", " +
                        tag.getGender() + "\n");
            }
            bw.flush();
            bw.close();

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

            Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
            emailIntent.setType("*/*");

            emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
                    "DataCollector data export");
            emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(logfile));
            startActivity(Intent.createChooser(emailIntent, "Send mail..."));


            Log.i("DataCollector", "export finished!");
        } catch (IOException ioe) {
            Log.e("DataCollector", "IOException while writing Logfile");
        }

        export_text.setText(getDataInfo());
    }
}
