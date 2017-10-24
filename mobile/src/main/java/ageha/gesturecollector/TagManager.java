package ageha.gesturecollector;

import android.content.Context;

import java.sql.Timestamp;
import java.util.LinkedList;

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
    }

    public LinkedList<TagData> getTags() {
        return (LinkedList<TagData>) tags.clone();
    }

    public void DeleteTags(){
        this.tags = new LinkedList<>();
    }

    synchronized void addTag(String pTagName, String name, int age, int height, String gender, int weight) {
        TagData tag = new TagData(pTagName, new Timestamp(System.currentTimeMillis()), name, age, height, gender, weight);
        this.tags.add(tag);

        BusProvider.postOnMainThread(new TagAddedEvent(tag));
    }

}

