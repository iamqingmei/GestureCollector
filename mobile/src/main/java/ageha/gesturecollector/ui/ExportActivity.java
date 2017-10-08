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
import android.widget.Button;
import android.widget.ProgressBar;

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

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_export);

        dataProgressbar = findViewById(R.id.export_progress);
        tagProgressbar = findViewById(R.id.export_progress_tag);

        setSupportActionBar((Toolbar) findViewById(R.id.my_awesome_toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setTitle("Export Data");
        Button exportDataButton = (Button) findViewById(R.id.exportDataButton);
        exportDataButton.setOnClickListener(
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


        Button deleteButton = (Button) findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(
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

        Button allInOneButton = (Button) findViewById(R.id.exportDeleteAllInOneButton);
        deleteButton.setOnClickListener(
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

    private void deleteAllData() {
        mRealm = Realm.getInstance(this);
        mRealm.beginTransaction();

        RealmResults<DataEntry> result = mRealm.where(DataEntry.class).findAll();
        Log.e("DataCollector", "rows after delete = " + result.size());
        TagManager.getInstance(ExportActivity.this).DeleteTags();

        // Delete all matches
        result.clear();

        mRealm.commitTransaction();

        result = mRealm.where(DataEntry.class).findAll();
        Log.e("DataCollector", "rows after delete = " + result.size());
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

                StringBuilder sb = new StringBuilder(result.get(i).getAndroidDevice());
                sb.append(" ,");
                sb.append(String.valueOf(
                        (new Date()).getTime() +
                                (result.get(i).getTimestamp() - System.nanoTime()) / 1000000L));
                sb.append(" ,");
                sb.append(String.valueOf(result.get(i).getX()));
                sb.append(" ,");
                sb.append(String.valueOf(result.get(i).getY()));
                sb.append(" ,");
                sb.append(String.valueOf(result.get(i).getZ()));
                sb.append(" ,");
                sb.append(String.valueOf(result.get(i).getAccuracy()));
                sb.append(" ,");
                sb.append(String.valueOf(result.get(i).getDatasource()));
                sb.append(" ,");
                sb.append(String.valueOf(result.get(i).getDatatype()));
                sb.append("\n");
                bw.write(sb.toString());
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
    }


    private void exportTagsFile() {
        mRealm = Realm.getInstance(this);
        String date = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault()).format(new Date());
        String filename = String.format("%s_%s.txt", "Tags", date);

        final String directory = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DataCollector";

        final File logfile = new File(directory, filename);
        final File logPath = logfile.getParentFile();

        if (!logPath.isDirectory() && !logPath.mkdirs()) {
            Log.e("SensorDashbaord", "Could not create directory for log files");
        }


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
    }
}
