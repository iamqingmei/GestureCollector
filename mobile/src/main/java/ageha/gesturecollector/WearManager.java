package ageha.gesturecollector;

import android.content.Context;
import android.util.SparseArray;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Ageha on 9/10/17.
 */
import ageha.gesturecollector.R;

public class WearManager {
    private static WearManager instance;
    private ExecutorService executorService;
    private GoogleApiClient googleApiClient;
    private String Wear_name = null;

    public static synchronized WearManager getInstance(Context context) {
        if (instance == null) {
            instance = new WearManager(context.getApplicationContext());
        }
        return instance;
    }

    //private constructor.
    private WearManager(Context context) {
        this.googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(Wearable.API)
                .build();

        this.executorService = Executors.newCachedThreadPool();
    }

    public void sensorConnected(String str){
        this.Wear_name = str;
    }

    public String getWearName(){
        return Wear_name;
    }
}
