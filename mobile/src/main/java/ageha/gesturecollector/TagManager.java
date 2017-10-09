package ageha.gesturecollector;

import android.content.Context;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;

import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ageha.gesturecollector.data.TagData;
import ageha.gesturecollector.event.*;

public class TagManager {
    private static final String TAG = "TagManager";
//    private static final int CLIENT_CONNECTION_TIMEOUT = 15000;

    private static TagManager instance;

    private LinkedList<TagData> tags = new LinkedList<>();



    public static synchronized TagManager getInstance(Context context) {
        if (instance == null) {
            instance = new TagManager(context.getApplicationContext());
        }

        return instance;
    }

    //private constructor.
    private TagManager(Context context) {
        ExecutorService executorService;
        GoogleApiClient googleApiClient;

        googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(Wearable.API)
                .build();

        executorService = Executors.newCachedThreadPool();
    }

    public LinkedList<TagData> getTags() {
        return (LinkedList<TagData>) tags.clone();
    }

    public void DeleteTags(){
        this.tags = new LinkedList<>();
    }

    synchronized void addTag(String pTagName, String name, int age, int height, String gender) {
        TagData tag = new TagData(pTagName, new Timestamp(System.currentTimeMillis()), name, age, height, gender);
        this.tags.add(tag);

        BusProvider.postOnMainThread(new TagAddedEvent(tag));
    }
}

